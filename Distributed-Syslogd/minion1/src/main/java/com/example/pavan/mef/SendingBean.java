/**
 * 
 */
package com.example.pavan.mef;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * @author pk015603
 *
 */
public class SendingBean {

	
    public String name;
    
    public SendingBean(String name)
    {
    	this.name = name;
    }
    
	public String getName() {
		return name;
	}
	
}

//@Component
//public class SendingBean {
//
//    private Barista source;
//    
//    public SendingBean() {
//    }
//
//    @Autowired
//    public SendingBean(Barista source) {
//        this.source = source;
//    }
//
//    @Bean
//	@InboundChannelAdapter(value = Barista.SAMPLE, poller = @Poller(fixedDelay = "10000", maxMessagesPerPoll = "1"))
//    public void sayHello(String name) {
//         source.coldDrinks().send(MessageBuilder.withPayload(name).build());
//    }
//}
