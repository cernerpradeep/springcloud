package org.eventEnricher;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

@SpringBootApplication(scanBasePackages="org.opennms.netmgt.eventd")
@EnableBinding(Processor.class)
public class SpringcloudstreamsEventEnricher {
	
	private static int count;
	
	private Processor processor;
	
	private ExecutorService service = Executors.newFixedThreadPool(100);
	
	@Autowired
	private EventIpcManagerDefaultImpl eventIpcManagerDefaultImpl;
	
	@Autowired
	public SpringcloudstreamsEventEnricher(Processor processor) {
		this.processor = processor;
	}
	
	
	public static void main(String[] args) {
		SpringApplication.run(SpringcloudstreamsEventEnricher.class, args);
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
		count++;
		System.out.println("EnrichmentCount Count:"+count);
	}

}
