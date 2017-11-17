package com.cerner.event.broadcaster;


import java.util.Date;

import org.opennms.netmgt.xml.event.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;

@SpringBootApplication
@EnableBinding(Processor.class)
public class EventBroadCasterService {
	
	public static int count;
	public static Date firstTimestamp;
	public static Date lastTimestamp;
	
	public static void main(String[] args) {
		SpringApplication.run(EventBroadCasterService.class, args);
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
						firstTimestamp = new Date();
						System.out.println("Captured the first timeStamp"+firstTimestamp);
						break;
					}
				}
			}
		}
		
		new Thread(new SimpleCounter()).start();
	}
	
	
	@StreamListener(Processor.INPUT)
	public void receiveLogEvents(Log log) {
	count++;
	lastTimestamp = new Date();
		//System.out.println("Broad Cast Count:"+count);
	}
	
}

