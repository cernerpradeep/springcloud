package com.cerner.mef;

import java.util.Date;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dto")
public class DTOConvertorMetrics {
	
	@RequestMapping("/counter")
	public MetricsBean getMetrics() {
		
		MetricsBean metrics = new MetricsBean();
		metrics.setCounter(SyslogStringToDTOConverterService.count);
		metrics.setEndTime(SyslogStringToDTOConverterService.lastTimeStamp + "");
		metrics.setStartTime(SyslogStringToDTOConverterService.firstTimeStamp + "");
		long diff = Math.abs(SyslogStringToDTOConverterService.lastTimeStamp.getTime() - SyslogStringToDTOConverterService.firstTimeStamp.getTime())/1000;
		metrics.setTotalExecutionTime(diff);
		if (diff == 0) diff = 1;
		metrics.setRate(SyslogStringToDTOConverterService.count/diff);
		return metrics;
	}
	
	@RequestMapping("/resetCounter")
	public String resetCounter() {
		SyslogStringToDTOConverterService.count = 0;
		
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
					if (SyslogStringToDTOConverterService.count > 1) {
						SyslogStringToDTOConverterService.firstTimeStamp = new Date();
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
