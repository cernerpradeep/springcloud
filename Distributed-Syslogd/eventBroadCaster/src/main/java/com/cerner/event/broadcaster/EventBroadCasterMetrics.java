package com.cerner.event.broadcaster;

import java.util.Date;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ebc")
public class EventBroadCasterMetrics {
	
	@RequestMapping("/counter")
	public MetricsBean eventBroadCasterMetrics() {
		
		MetricsBean metricsBean = new MetricsBean();
		
		metricsBean.setCounter(EventBroadCasterService.count);
		metricsBean.setEndTime(EventBroadCasterService.lastTimestamp + "");
		metricsBean.setStartTime(EventBroadCasterService.firstTimestamp + "");
		
		long diff = Math.abs(EventBroadCasterService.lastTimestamp.getTime() - EventBroadCasterService.firstTimestamp.getTime())/1000;
		metricsBean.setTotalExecutionTime(diff);
		
		if (diff == 0) diff = 1;
		
		metricsBean.setTotalExecutionTime(diff);
		metricsBean.setRate(EventBroadCasterService.count/diff);
		
		return metricsBean;
	}
	
	@RequestMapping("/resetCounter")
	public String resetCounter() {
		EventBroadCasterService.count = 0;
		
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
					if (EventBroadCasterService.count > 1) {
						EventBroadCasterService.firstTimestamp = new Date();
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
