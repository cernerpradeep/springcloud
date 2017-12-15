package org.Eventd;

import java.util.Date;
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
import com.codahale.metrics.MetricRegistry;

@SpringBootApplication(scanBasePackages="org.opennms.netmgt.eventd")
@EnableBinding(Processor.class)
public class EventHibernateWriterService {
	
	public static int count;
	public static Date firstTimestamp;
	public static Date lastTimestamp;
	
	private ExecutorService service = Executors.newCachedThreadPool();
	
	@Autowired
	private EventIpcManagerDefaultImpl eventIpcManagerDefaultImpl;
	
	public static void main(String[] args) {
		SpringApplication.run(EventHibernateWriterService.class, args);
		
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
	public void receiveLogEvents(Log log) {
		
		class HibernateEventWriterThread implements Runnable{
			
			private Log eventLog;
			
			public HibernateEventWriterThread(Log eventLog) {
				this.eventLog = eventLog;
			}

			@Override
			public void run() {
				insertRecords();
				
			}
			
			private void insertRecords() {
				eventIpcManagerDefaultImpl.sendNowSync(eventLog);
			}
			
		}
		service.execute(new HibernateEventWriterThread(log));
		
		count++;
		lastTimestamp = new Date();
		//System.out.println("Hibernate Count:"+count);
	}
	
}
