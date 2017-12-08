package com.cerner.mef;

import java.util.Date;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/m2e")
public class MessageToEventMetrics {
	
	@RequestMapping("/counter")
	public MetricsBean getMetrics() {
		
		MetricsBean metrics = new MetricsBean();
		metrics.setCounter(SyslogConvertToEventService.count);
		metrics.setEndTime(SyslogConvertToEventService.lastTimeStamp + "");
		metrics.setStartTime(SyslogConvertToEventService.firstTimeStamp + "");
		long diff = Math.abs(SyslogConvertToEventService.lastTimeStamp.getTime() - SyslogConvertToEventService.firstTimeStamp.getTime())/1000;
		metrics.setTotalExecutionTime(diff);
		if (diff == 0) diff = 1;
		metrics.setRate(SyslogConvertToEventService.count/diff);
		return metrics;
	}
	
	@RequestMapping("/resetCounter")
	public String resetCounter() {
		SyslogConvertToEventService.count = 0;
		
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
					if (SyslogConvertToEventService.count > 1) {
						SyslogConvertToEventService.firstTimeStamp = new Date();
						//System.out.println("Captured the first timeStamp"+firstTimeStamp);
						break;
					}
				}
			}
		}
		
		new Thread(new SimpleCounter()).start();
		
		return "Reset Complete";
		
	}
	
}
