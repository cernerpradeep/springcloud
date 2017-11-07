/**
 * 
 */
package com.example.pavan.mef;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author pk015603
 *
 */
public interface Minion1 {

	String SAMPLE = "minion1";

	@Output(Minion1.SAMPLE)
	MessageChannel minion1();

}
