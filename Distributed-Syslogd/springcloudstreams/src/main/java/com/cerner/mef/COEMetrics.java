package com.cerner.mef;

import java.util.Date;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coe")
public class COEMetrics {
	
	@RequestMapping("/counter")
	public MetricsBean getMetrics() {
		
		MetricsBean metrics = new MetricsBean();
		metrics.setCounter(SyslogConvertToSyslogMessageService.count);
		metrics.setEndTime(SyslogConvertToSyslogMessageService.lastTimeStamp + "");
		metrics.setStartTime(SyslogConvertToSyslogMessageService.firstTimeStamp + "");
		long diff = Math.abs(SyslogConvertToSyslogMessageService.lastTimeStamp.getTime() - SyslogConvertToSyslogMessageService.firstTimeStamp.getTime())/1000;
		metrics.setTotalExecutionTime(diff);
		if (diff == 0) diff = 1;
		metrics.setRate(SyslogConvertToSyslogMessageService.count/diff);
		return metrics;
	}
	
	@RequestMapping("/resetCounter")
	public String resetCounter() {
		SyslogConvertToSyslogMessageService.count = 0;
		
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
					if (SyslogConvertToSyslogMessageService.count > 1) {
						SyslogConvertToSyslogMessageService.firstTimeStamp = new Date();
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
