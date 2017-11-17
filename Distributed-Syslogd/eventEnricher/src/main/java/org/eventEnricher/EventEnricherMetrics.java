package org.eventEnricher;

import java.util.Date;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ee")
public class EventEnricherMetrics {

	@RequestMapping("/counter")
	public MetricsBean getMetrics() {
		
		MetricsBean metricsBean = new MetricsBean();
		metricsBean.setCounter(EventEnricherService.count);
		metricsBean.setEndTime(EventEnricherService.lastTimestamp + "");
		metricsBean.setStartTime(EventEnricherService.firstTimestamp + "");
		
		long diff = Math.abs(EventEnricherService.lastTimestamp.getTime() - EventEnricherService.firstTimestamp.getTime())/1000;
		metricsBean.setTotalExecutionTime(diff);
		if (diff == 0) diff = 1;
		metricsBean.setRate(EventEnricherService.count/diff);
		return metricsBean;
	}
	
	@RequestMapping("/resetCounter")
	public String resetCounter() {
		EventEnricherService.count = 0;
		
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
					if (EventEnricherService.count > 1) {
						EventEnricherService.firstTimestamp = new Date();
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
