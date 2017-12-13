/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.syslogd;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.syslogd.BufferParser.BufferParserFactory;
import org.opennms.netmgt.syslogd.api.SyslogMessageDTO;
import org.opennms.netmgt.syslogd.api.SyslogMessageLogDTO;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

@Resource
public class SyslogSinkConsumer  {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogSinkConsumer.class);

    
    private SyslogdConfig syslogdConfig;

    //private DistPollerDao distPollerDao;

    private final String localAddr;
    private final Timer consumerTimer;
    private final Timer toEventTimer;
    private final static ExecutorService m_executor = Executors.newSingleThreadExecutor();
    
    private static List<String> grokPatternsList;
    
    public static List<String> getGrokPatternsList() {
        return grokPatternsList;
    }

    public void setGrokPatternsList(List<String> grokPatternsListValue) {
        grokPatternsList = grokPatternsListValue;
    }

    public SyslogSinkConsumer() {
      	MetricRegistry registry = new MetricRegistry();
        consumerTimer = registry.timer("consumer");
        toEventTimer = registry.timer("consumer.toevent");
        registry.timer("consumer.broadcast");
        localAddr = InetAddressUtils.getLocalHostName();
        try {
			syslogdConfig = new SyslogdConfigFactory();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public SyslogSinkConsumer(MetricRegistry registry) {
        consumerTimer = registry.timer("consumer");
        toEventTimer = registry.timer("consumer.toevent");
        registry.timer("consumer.broadcast");
        localAddr = InetAddressUtils.getLocalHostName();
    }
    
    /**
     * Static block to load grokPatterns during the start of SyslogSink class call.
     */
    static {
        try {
			loadGrokParserList();
        } catch (IOException e) {
        		e.printStackTrace();
            LOG.debug("Failed to load Grok pattern list."+e);
        }

    }

    public static void loadGrokParserList() throws IOException {
    		//System.out.println("Before loading grok ");
        grokPatternsList = new ArrayList<String>();
        System.setProperty("opennms.home", "/opt/opennms");
        File syslogConfigFile = ConfigFileConstants.getConfigFileByName("syslogd-configuration.properites");
       // System.out.println("Printing importat info"+syslogConfigFile.getAbsolutePath());
        readPropertiesInOrderFrom(syslogConfigFile);
    }
    
    public Log handleMessage(SyslogMessageLogDTO syslogDTO) {
       // try (Context consumerCtx = consumerTimer.time()) {
        //    try (MDCCloseable mdc = Logging.withPrefixCloseable(Syslogd.LOG4J_CATEGORY)) {
                // Convert the Syslog UDP messages to Events
                final Log eventLog;
          //      try (Context toEventCtx = toEventTimer.time()) {
                    eventLog = toEventLog(syslogDTO);
            //    }
                //count ++;
                // Broadcast the Events to the event bus
               /* try (Context broadCastCtx = broadcastTimer.time()) {
                    broadcast(eventLog);
                }*/
                //System.out.println("Received Syslogs:"+count);
                return eventLog;
            }
       // }
   // }

    public Log toEventLog(SyslogMessageLogDTO messageLog) {
        final Log elog = new Log();
        final Events events = new Events();
        elog.setEvents(events);
        for (SyslogMessageDTO message : messageLog.getMessages()) {
            try {
                LOG.debug("Converting syslog message into event.");
                ConvertToEvent re = new ConvertToEvent(
                        messageLog.getSystemId(),
                        messageLog.getLocation(),
                        messageLog.getSourceAddress(),
                        messageLog.getSourcePort(),
                        // Decode the packet content as ASCII
                        // TODO: Support more character encodings?
                        StandardCharsets.US_ASCII.decode(message.getBytes()).toString(),
                        syslogdConfig,
                        parse(message.getBytes())
                    );
                events.addEvent(re.getEvent());
            } catch (final UnsupportedEncodingException e) {
                LOG.info("Failure to convert package", e);
            } catch (final MessageDiscardedException e) {
                LOG.info("Message discarded, returning without enqueueing event.", e);
            } catch (final Throwable e) {
                LOG.error("Unexpected exception while processing SyslogConnection", e);
            }
        }
        return elog;
    }

  /*  private void broadcast(Log eventLog)  {
        if (LOG.isTraceEnabled())  {
            for (Event event : eventLog.getEvents().getEventCollection()) {
                LOG.trace("Processing a syslog to event dispatch", event.toString());
                String uuid = event.getUuid();
                LOG.trace("Event {");
                LOG.trace("  uuid  = {}", (uuid != null && uuid.length() > 0 ? uuid : "<not-set>"));
                LOG.trace("  uei   = {}", event.getUei());
                LOG.trace("  src   = {}", event.getSource());
                LOG.trace("  iface = {}", event.getInterface());
                LOG.trace("  time  = {}", event.getTime());
                LOG.trace("  Msg   = {}", event.getLogmsg().getContent());
                LOG.trace("  Dst   = {}", event.getLogmsg().getDest());
                List<Parm> parms = (event.getParmCollection() == null ? null : event.getParmCollection());
                if (parms != null) {
                    LOG.trace("  parms {");
                    for (Parm parm : parms) {
                        if ((parm.getParmName() != null)
                                && (parm.getValue().getContent() != null)) {
                            LOG.trace("    ({}, {})", parm.getParmName().trim(), parm.getValue().getContent().trim());
                        }
                    }
                    LOG.trace("  }");
                }
                LOG.trace("}");
            }
        }
       // eventForwarder.sendNowSync(eventLog);

        if (syslogdConfig.getNewSuspectOnMessage()) {
            eventLog.getEvents().getEventCollection().stream()
                .filter(e -> !e.hasNodeid())
                .forEach(e -> {
                    LOG.trace("Syslogd: Found a new suspect {}", e.getInterface());
                    sendNewSuspectEvent(localAddr, e.getInterface(), e.getDistPoller());
                });
        }
    }*/

   /* private void sendNewSuspectEvent(String localAddr, String trapInterface, String distPoller) {
        EventBuilder bldr = new EventBuilder(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "syslogd");
        bldr.setInterface(addr(trapInterface));
        bldr.setHost(localAddr);
        bldr.setDistPoller(distPoller);
       // eventForwarder.sendNow(bldr.getEvent());
    }*/

    public void afterPropertiesSet() throws Exception {
        // Automatically register the consumer on initialization
        //messageConsumerManager.registerConsumer(this);
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
       // this.eventForwarder = eventForwarder;
    }

    public void setSyslogdConfig(SyslogdConfig syslogdConfig) {
        this.syslogdConfig = syslogdConfig;
    }

    public void setDistPollerDao(DistPollerDao distPollerDao) {
       // this.distPollerDao = distPollerDao;
    }
    
    /**
     * This method will parse the message against the grok patterns
     * @param messageBytes 
     *  
     * @return
     *  Parameter list
     */
        public static Map<String, String> parse(ByteBuffer messageBytes) {
                String grokPattern;
                Map<String, String> paramsMap = new HashMap<String,String>();
                if (null == getGrokPatternsList() || getGrokPatternsList().isEmpty()) {
                        LOG.error("No Grok Pattern has been defined");
                        return null;
                }
                for (int i = 0; i < getGrokPatternsList().size(); i++) {
                        grokPattern = getGrokPatternsList().get(i);
                        BufferParserFactory grokFactory = GrokParserFactory
                                        .parseGrok(grokPattern);
                        ByteBuffer incoming = ByteBuffer.wrap(messageBytes.array());
                        try {
                        		 paramsMap = loadParamsMap(grokFactory
                                        .parse(incoming.asReadOnlyBuffer(), m_executor).get()
                                        .getParmCollection());
                        		LOG.debug("Grok Pattern "+grokPattern+" matches syslog message.");
                                return paramsMap;
                        } catch (InterruptedException | ExecutionException e) {
                                LOG.debug("Parse Exception occured !!!Grok Pattern "+grokPattern+" didn't match");
                                continue;
                        }
                }
                return null;

        }
        
		public static Map<String, String> loadParamsMap(List<Parm> paramsList) {
                return paramsList.stream().collect(
                                Collectors.toMap(Parm::getParmName, param -> param.getValue()
                                                .getContent(), (paramKey1, paramKey2) -> paramKey2));
        }
        
        
        public static List<String> readPropertiesInOrderFrom(File syslogdConfigdFile)
                throws IOException {
            InputStream propertiesFileInputStream = new FileInputStream(syslogdConfigdFile);
            Set<String> grookSet=new LinkedHashSet<String>();
            final Properties properties = new Properties(); 
            final BufferedReader reader = new BufferedReader(new InputStreamReader(propertiesFileInputStream));

            String bufferedReader = reader.readLine();
            
            while (bufferedReader != null) {
                final ByteArrayInputStream lineStream = new ByteArrayInputStream(bufferedReader.getBytes("ISO-8859-1"));
                properties.load(lineStream); 

                final Enumeration<?> propertyNames = properties.<String>propertyNames();

                if (propertyNames.hasMoreElements()) { 

                    final String paramKey = (String) propertyNames.nextElement();
                    final String paramsValue = properties.getProperty(paramKey);

                    grookSet.add(paramsValue);
                    properties.clear(); 
                }
                bufferedReader = reader.readLine();
            }
            grokPatternsList=new ArrayList<String>(grookSet);
            reader.close();
            return grokPatternsList;
        }
        
        
        public static List<String> readPropertiesInOrderFrom(String syslogdConfigdFilePath)
                throws IOException {
        	
        	    new ClassPathResource(syslogdConfigdFilePath).getFile();
            ClassPathResource resource = new ClassPathResource(syslogdConfigdFilePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            Set<String> grookSet=new LinkedHashSet<String>();
            final Properties properties = new Properties(); 
            
            String bufferedReader = reader.readLine();
            

            while (bufferedReader != null) {
                final ByteArrayInputStream lineStream = new ByteArrayInputStream(bufferedReader.getBytes("ISO-8859-1"));
                properties.load(lineStream); 

                final Enumeration<?> propertyNames = properties.<String>propertyNames();

                if (propertyNames.hasMoreElements()) { 

                    final String paramKey = (String) propertyNames.nextElement();
                    final String paramsValue = properties.getProperty(paramKey);

                    grookSet.add(paramsValue);
                    properties.clear(); 
                }
                bufferedReader = reader.readLine();
            }
            grokPatternsList=new ArrayList<String>(grookSet);
            reader.close();
            return grokPatternsList;
        } 
        
}
