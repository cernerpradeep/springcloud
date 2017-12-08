package com.cerner.mef;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opennms.netmgt.syslogd.SyslogSinkConsumer;
import org.opennms.netmgt.syslogd.api.SyslogMessageLogDTO;
import org.opennms.netmgt.xml.event.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.MessageBuilder;

@SpringBootApplication
@EnableBinding(Processor.class)
public class SyslogConvertToEventService {
	
	private Processor processor;
	private ExecutorService service = Executors.newFixedThreadPool(2);
	
	public static int count;
	public static Date lastTimeStamp;
	public static Date firstTimeStamp;
	
	@Autowired
	private UtiliMarshlerUnmarshaler xmlHandler;
	
	@Bean
	public SyslogSinkConsumer SyslogSinkConsumer() {
		return new SyslogSinkConsumer();
	}
	
	public SyslogConvertToEventService(Processor processor) {
		this.processor = processor;
	}
	
	@Autowired
	private SyslogSinkConsumer syslogSinkConsumer;

	public static void main(String[] args) {
		SpringApplication.run(SyslogConvertToEventService.class, args);
		
		class SimpleCounter implements Runnable{

			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (count > 1) {
						firstTimeStamp = new Date();
						//System.out.println("Captured the first timeStamp"+firstTimeStamp);
						break;
					}
				}
			}
		}
		
		new Thread(new SimpleCounter()).start();
	}
	
	@StreamListener(Processor.INPUT)
	public void receiveSyslogs(String syslogMessage) {
		
		class SyslogConvertTOEvent implements Runnable {
			
			private SyslogMessageLogDTO syslogMessageLogDTO;
			
			public SyslogConvertTOEvent(String message) {
				syslogMessageLogDTO = (SyslogMessageLogDTO) xmlHandler.unmarshal(syslogMessage);
			}

			@Override
			public void run() {
				convertTOEvent();
			}
			
			public void convertTOEvent() {
				    
				Log logObject = syslogSinkConsumer.handleMessage(syslogMessageLogDTO);
				postLogsToStream(logObject);
			}
		}
		service.execute(new SyslogConvertTOEvent(syslogMessage));
	}
	
	public void postLogsToStream(Log log) {
		count++;
		processor.output().send(MessageBuilder.withPayload(log).build());
		lastTimeStamp = new Date();
		//System.out.println("Count:"+count);
	}
	
	
}
