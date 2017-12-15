package org.opennms.netmgt.eventd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.EventLabelComparator;
import org.opennms.netmgt.xml.eventconf.EventMatchers;
import org.opennms.netmgt.xml.eventconf.EventOrdering;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.netmgt.xml.eventconf.Events.EventCallback;
import org.opennms.netmgt.xml.eventconf.Events.EventCriteria;
import org.opennms.netmgt.xml.eventconf.Field;
import org.opennms.netmgt.xml.eventconf.Partition;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;

@Service
public class DefaultEventConfDao implements EventConfDao, InitializingBean {
	private static final String DEFAULT_PROGRAMMATIC_STORE_RELATIVE_PATH = "events/programmatic.events.xml";

    /**
     * Relative URL for the programmatic store configuration, relative to the
     * root configuration resource (which must be resolvable to a URL).
     */
	
    private String m_programmaticStoreRelativePath = DEFAULT_PROGRAMMATIC_STORE_RELATIVE_PATH;

	private Events m_events;

	private Resource m_configResource;

	private Partition m_partition;

    /**
     * Used to keep track of the last modified time for the loaded event files.
     * See the reloadConfig() for details.
     */
    private Map<String, Long> m_lastModifiedEventFiles = new LinkedHashMap<String, Long>();

	public String getProgrammaticStoreRelativeUrl() {
		return m_programmaticStoreRelativePath;
	}

	public void setProgrammaticStoreRelativeUrl(String programmaticStoreRelativeUrl) {
		m_programmaticStoreRelativePath = programmaticStoreRelativeUrl;
	}

	@Override
	public void reload() throws DataAccessException {
		try {
		    reloadConfig();
		} catch (Exception e) {
			throw new DataRetrievalFailureException("Unabled to load " + m_configResource, e);
		}
	}

	@Override
	public List<Event> getEvents(final String uei) {
		List<Event> events = m_events.forEachEvent(new ArrayList<Event>(), new EventCallback<List<Event>>() {

			@Override
			public List<Event> process(List<Event> accum, Event event) {
				if (uei.equals(event.getUei())) {
					accum.add(event);
				}
				return accum;
			}
		});
		
		return events.isEmpty() ? null : events;
	}

	@Override
	public List<String> getEventUEIs() {
		return m_events.forEachEvent(new ArrayList<String>(), new EventCallback<List<String>>() {

			@Override
			public List<String> process(List<String> ueis, Event event) {
				ueis.add(event.getUei());
				return ueis;
			}
		});
		
	}

	@Override
	public Map<String, String> getEventLabels() {
		return m_events.forEachEvent(new TreeMap<String, String>(), new EventCallback<Map<String, String>>() {

			@Override
			public Map<String, String> process(Map<String, String> ueiToLabelMap, Event event) {
				ueiToLabelMap.put(event.getUei(), event.getEventLabel());
				return ueiToLabelMap;
			}

		});
	}

	@Override
	public String getEventLabel(final String uei) {
		Event event = findByUei(uei);
		return event == null ? null : event.getEventLabel();

	}

	@Override
	public void saveCurrent() {
		m_events.save(m_configResource);
	}
	
	
	
	public List<Event> getAllEvents() {
		return m_events.forEachEvent(new ArrayList<Event>(), new EventCallback<List<Event>>() {

			@Override
			public List<Event> process(List<Event> accum, Event event) {
				accum.add(event);
				return accum;
			}
		});
	}

	@Override
	public List<Event> getEventsByLabel() {
		SortedSet<Event> events = m_events.forEachEvent(new TreeSet<Event>(new EventLabelComparator()), new EventCallback<SortedSet<Event>>() {
		
			@Override
			public SortedSet<Event> process(SortedSet<Event> accum, Event event) {
				accum.add(event);
				return accum;
			}
		});
		return new ArrayList<Event>(events);
	}

	@Override
	public void addEvent(Event event) {
		m_events.addEvent(event);
		m_events.initialize(m_partition, new EventOrdering());
	}

	@Override
	public void addEventToProgrammaticStore(Event event) {
		Events programmaticEvents = m_events.getLoadEventsByFile(m_programmaticStoreRelativePath);
		if (programmaticEvents == null) {
			programmaticEvents = new Events();
			m_events.addLoadedEventFile(m_programmaticStoreRelativePath, programmaticEvents);
		}

		programmaticEvents.addEvent(event);
		m_events.initialize(m_partition, new EventOrdering());

	}

	@Override
	public boolean removeEventFromProgrammaticStore(Event event) {
		Events programmaticEvents = m_events.getLoadEventsByFile(m_programmaticStoreRelativePath);
		if (programmaticEvents == null) return false;

		programmaticEvents.removeEvent(event);
		if (programmaticEvents.getEvents().size() <= 0) {
			m_events.removeLoadedEventFile(m_programmaticStoreRelativePath);
		} 

		m_events.initialize(m_partition, new EventOrdering());

		return true;

	}

	@Override
	public boolean isSecureTag(String tag) {
		return m_events.isSecureTag(tag);
	}

	@Override
	public Event findByUei(final String uei) {
	    if (uei == null) {
	        return null;
	    }
	    return m_events.findFirstMatchingEvent(new EventCriteria() {
	        @Override public boolean matches(final Event e) {
	            return uei.equals(e.getUei());
	        }
	    });
	}

	@Override
	public Event findByEvent(final org.opennms.netmgt.xml.event.Event matchingEvent) {
		return m_events.findFirstMatchingEvent(matchingEvent);
	}

	@Override
	public Events getRootEvents() {
		return m_events;
	}

	public void setConfigResource(Resource configResource) throws IOException {
		m_configResource = configResource;
	}
	
	@Override
	public void afterPropertiesSet() throws DataAccessException {
		loadConfig();
	}

	private static class EnterpriseIdPartition implements Partition {

		private Field m_field = EventMatchers.field("id");

		@Override
		public List<String> group(Event eventConf) {
			List<String> keys = eventConf.getMaskElementValues("id");
			if (keys == null) return null;
			for(String key : keys) {
			    // if this issue is a wildcard issue we need to test against
			    // all events so return null here so it isn't pigeon-holed into
			    // a particular partition
			    if (key.endsWith("%")) return null;
			    if (key.startsWith("~")) return null;
			}
			return keys;
		}

		@Override
		public String group(org.opennms.netmgt.xml.event.Event matchingEvent) {
			return m_field.get(matchingEvent);
		}
		
	}

    private synchronized void reloadConfig() throws DataAccessException {
        try {
            // Load the root event file
        	    //ApplicationContext ctx= new AnnotationConfigApplicationContext(DefaultEventConfDao.class);
        	   // m_configResource = ctx.getResource("eventconf.xml");
        		System.out.println("Before loading event conf");
        	    m_configResource = new ClassPathResource("classpath:eventconf.xml");
        	    System.setProperty("opennms.home", "/opt/opennms");
            File syslogConfigFile = ConfigFileConstants.getConfigFileByName("eventconf.xml");
            Events events = JaxbUtils.unmarshal(Events.class, syslogConfigFile);

            // Hash the list of event files for efficient lookup
            Set<String> eventFiles = new HashSet<String>();
            eventFiles.addAll(events.getEventFiles());

            // Copy the loaded event files from the current root to the new root
            // if and only if they exist in the new root
            for (String eventFile : m_events.getEventFiles()) {
                if (!eventFiles.contains(eventFile)) {
                    m_lastModifiedEventFiles.remove(eventFile);
                    continue;
                }
                events.addLoadedEventFile(eventFile, m_events.getLoadEventsByFile(eventFile));
            }

            // Load/reload the event files as necessary
            events.loadEventFilesIfModified(m_configResource, m_lastModifiedEventFiles);

            // Order the events for efficient searching
            events.initialize(m_partition, new EventOrdering());

            m_events = events;
        } catch (Exception e) {
            throw new DataRetrievalFailureException("Unabled to load " + m_configResource, e);
        }
    }

	private synchronized void loadConfig() throws DataAccessException {
		try {
			m_configResource = new ClassPathResource("classpath:eventconf.xml");
			System.out.println("Setting the classPathResource....");
      	    System.setProperty("opennms.home", "/opt/opennms");
            File syslogConfigFile = ConfigFileConstants.getConfigFileByName("eventconf.xml");
            Events events = JaxbUtils.unmarshal(Events.class, syslogConfigFile);
            
			m_lastModifiedEventFiles = events.loadEventFiles(m_configResource);

			m_partition = new EnterpriseIdPartition();
			events.initialize(m_partition, new EventOrdering());

			m_events = events;
		} catch (Exception e) {
			throw new DataRetrievalFailureException("Unabled to load " + m_configResource, e);
		}
	}
}


