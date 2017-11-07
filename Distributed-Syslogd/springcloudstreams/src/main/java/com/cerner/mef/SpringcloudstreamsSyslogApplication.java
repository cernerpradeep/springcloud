package com.cerner.mef;

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
public class SpringcloudstreamsSyslogApplication {
	
	private Processor processor;
	
	private ExecutorService service = Executors.newFixedThreadPool(100);
	
	@Bean
	public SyslogSinkConsumer SyslogSinkConsumer() {
		return new SyslogSinkConsumer();
	}
	
	public SpringcloudstreamsSyslogApplication(Processor processor) {
		this.processor = processor;
	}
	
	private static int count;
	
	@Autowired
	private SyslogSinkConsumer syslogSinkConsumer;

	public static void main(String[] args) {
		SpringApplication.run(SpringcloudstreamsSyslogApplication.class, args);
	}
	
	@StreamListener(Processor.INPUT)
	public void receiveSyslogs(SyslogMessageLogDTO syslogMessageLogDTO) {
		
		class SyslogConvertTOEvent implements Runnable {
			
			private SyslogMessageLogDTO syslogMessageLogDTO;
			
			public SyslogConvertTOEvent(SyslogMessageLogDTO syslogMessageLogDTO) {
				this.syslogMessageLogDTO = syslogMessageLogDTO;
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
		service.execute(new SyslogConvertTOEvent(syslogMessageLogDTO));
	}
	
	
	public void postLogsToStream(Log log) {
		processor.output().send(MessageBuilder.withPayload(log).build());
		count++;
		System.out.println("Syslog Count:"+count);
	}
	
}
