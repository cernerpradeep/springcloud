package org.eventEnricher;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opennms.netmgt.dao.mock.EventWrapper;
import org.opennms.netmgt.eventd.EventIpcManagerDefaultImpl;
import org.opennms.netmgt.xml.event.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.MessageBuilder;
import com.codahale.metrics.MetricRegistry;

@SpringBootApplication(scanBasePackages= {"org.opennms.netmgt.eventd","org.eventEnricher"})
@EnableBinding(Processor.class)
public class EventEnricherService {
	
	public static int count;
	public static Date firstTimestamp;
	public static Date lastTimestamp;
	
	private Processor processor;
	
	private ExecutorService service = Executors.newFixedThreadPool(16);
	
	@Autowired
	private EventIpcManagerDefaultImpl eventIpcManagerDefaultImpl;
	
	@Autowired
	public EventEnricherService(Processor processor) {
		this.processor = processor;
	}
	
	
	public static void main(String[] args) {
		SpringApplication.run(EventEnricherService.class, args);
		
		class SimpleCounter implements Runnable{

			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (count > 1) {
						firstTimestamp = new Date();
						//System.out.println("Captured the first timeStamp"+firstTimestamp);
						break;
					}
				}
			}
		}
		
		new Thread(new SimpleCounter()).start();
	}
	
	@Bean
	public EventIpcManagerDefaultImpl EventIpcManagerDefaultImpl() {
		return new EventIpcManagerDefaultImpl(new MetricRegistry());
	}
	
	
	@StreamListener(Processor.INPUT)
	public void receiveAndEnrichLog(Log log) throws InterruptedException, ExecutionException {
		
		class EventEnricherAsynThread implements Runnable{
			
			private Log eventLog;

			public EventEnricherAsynThread(Log eventLog) {
				this.eventLog = eventLog;
			}

			@Override
			public void run() {
				forwardEventlog();
			}
			
			public void forwardEventlog() {
				try {
					Log enrichedLog = eventIpcManagerDefaultImpl.getEnrichedEvent(eventLog).get();
					sendLogObject(enrichedLog);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		service.execute(new EventEnricherAsynThread(log));
	}
	
	public void sendLogObject(Log log) {
		processor.output().send(MessageBuilder.withPayload((log)).build());
		System.out.println(new EventWrapper(log.getEvents().getEvent(0)));
		count++;
		lastTimestamp = new Date();
	}

}
