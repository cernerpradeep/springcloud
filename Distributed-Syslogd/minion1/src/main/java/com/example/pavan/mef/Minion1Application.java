/**
 * 
 */
package com.example.pavan.mef;

import java.net.UnknownHostException;
import java.util.Date;

import org.opennms.netmgt.syslogd.api.SyslogMessageLogDTO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.support.MessageBuilder;

/**
 * @author pk015603
 *
 */
@SpringBootApplication
@EnableBinding(Minion1.class)
public class Minion1Application {
	
	private static int count;
	
	private static int perSecond = 1;
	
	private static int batches = 2;
	
	private int batchCount = 1;
	
	private static boolean started=false;
	
	private static Date firstTimestamp;
	
	private static Date lastTimestamp;
	
	public static void main(String[] args) {
		SpringApplication.run(Minion1Application.class, args);
	}
 
	@Bean
	@InboundChannelAdapter(value = Minion1.SAMPLE,poller = @Poller(fixedDelay = "1", maxMessagesPerPoll = "10000"))
	public MessageSource<String> timerMessageSource() throws UnknownHostException {
		return () -> {
			try {
				if(!started) {
					started =true;
					firstTimestamp = new Date();
				}
				
				
				if(count == perSecond) {
						if(batchCount == batches) {
							System.out.println("Completed Sending :"+(perSecond * batchCount));
							lastTimestamp = new Date();
							long timeTaken = Math.abs(lastTimestamp.getTime() - firstTimestamp.getTime())/1000;
							System.out.println("Total time taken to send all messages:"+timeTaken);
							System.exit(0);
						}
					
					int totalSyslogs = perSecond * batchCount;
					System.out.println("Syslogs sent so far:"+totalSyslogs);
					count = 0;
					batchCount++;
					Thread.sleep(1000);
				}
				count++;
				return MessageBuilder.withPayload(SyslogBean.getSyslogString()).build();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		};
	}

}