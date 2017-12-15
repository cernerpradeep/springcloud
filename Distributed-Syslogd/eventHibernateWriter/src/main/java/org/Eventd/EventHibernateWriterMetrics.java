package org.Eventd;

import java.util.Date;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ehw")
public class EventHibernateWriterMetrics {

	@RequestMapping("/counter")
	public MetricsBean getMetrics() {
		
		MetricsBean metricsBean = new MetricsBean();
		metricsBean.setCounter(EventHibernateWriterService.count);
		metricsBean.setEndTime(EventHibernateWriterService.lastTimestamp + "");
		metricsBean.setStartTime(EventHibernateWriterService.firstTimestamp + "");
		
		long diff = Math.abs(EventHibernateWriterService.lastTimestamp.getTime() - EventHibernateWriterService.firstTimestamp.getTime())/1000;
		metricsBean.setTotalExecutionTime(diff);
		if (diff == 0) diff = 1;
		metricsBean.setRate(EventHibernateWriterService.count/diff);
		return metricsBean;
	}
	
	@RequestMapping("/resetCounter")
	public String resetCounter() {
		EventHibernateWriterService.count = 0;
		
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
					if (EventHibernateWriterService.count > 1) {
						EventHibernateWriterService.firstTimestamp = new Date();
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
