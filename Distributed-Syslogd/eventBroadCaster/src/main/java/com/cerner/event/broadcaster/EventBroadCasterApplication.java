package com.cerner.event.broadcaster;


import org.opennms.netmgt.xml.event.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;

@SpringBootApplication
@EnableBinding(Processor.class)
public class EventBroadCasterApplication {
	
	private static int count;
	
	public static void main(String[] args) {
		SpringApplication.run(EventBroadCasterApplication.class, args);
	}
	
	
	@StreamListener(Processor.INPUT)
	public void receiveLogEvents(Log log) {
		count++;
		System.out.println("Broad Cast Count:"+count);
	}
	
}

