/**
 * 
 */
package com.cerner.mef;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author PS044221
 *
 */
public interface OpenNMSIn {

	String SYSLOG = "syslog";
	
	String SYSLOGEVENT = "syslogEvent";

	@Input(OpenNMSIn.SYSLOG)
	SubscribableChannel receiveSyslog();
	
	@Output(OpenNMSIn.SYSLOGEVENT)
	MessageChannel sendSyslogEvents();

}
