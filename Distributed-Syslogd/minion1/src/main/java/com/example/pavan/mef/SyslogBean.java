package com.example.pavan.mef;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.syslogd.api.SyslogConnection;
import org.opennms.netmgt.syslogd.api.SyslogMessageDTO;
import org.opennms.netmgt.syslogd.api.SyslogMessageLogDTO;

public class SyslogBean {

	public static SyslogMessageLogDTO getSyslogMsg() throws UnknownHostException {
		
		byte[] bytes = "<31>main: 2017-10-03 localhost foo%d: load testpavan %d on tty1".getBytes();
		DatagramPacket pkt = new DatagramPacket(bytes, bytes.length, InetAddress.getLocalHost(), 5140);
		SyslogConnection con = new SyslogConnection(pkt, false);
		final SyslogMessageLogDTO messageLog = new SyslogMessageLogDTO("LOC1", "SYSID1", con.getSource());
		SyslogMessageDTO syslogMsgDto = new SyslogMessageDTO();
		ByteBuffer message = ByteBuffer.wrap(bytes);
		syslogMsgDto.setBytes(message);
		List<SyslogMessageDTO> messageDTOs = new ArrayList<SyslogMessageDTO>();
		messageDTOs.add(syslogMsgDto);
		messageLog.setMessages(messageDTOs);
		return messageLog;
	}
	
	public static String getSyslogString() {
		String syslogMessage = "<syslog-message-log source-address=\"127.0.0.1\" source-port=\"59506\" system-id=\"e7e55cde-da74-11e7-9b40-0050569a0c79\" location=\"MINION\">\n"

		+ "   <messages timestamp=\"" + iso8601OffsetString(new Date(0), ZoneId.systemDefault(), ChronoUnit.SECONDS)

		+ "\">PDM0PiAyMDE3LTAzLTE2IDcuNDMuMi4xMyBmb28xMDAwMDogbG9hZCB0ZXN0IDEwMDAwIG9uIHByYWRlZXA=</messages>\n"

		+ "</syslog-message-log>";
		return syslogMessage;
	}
	
	public static String iso8601OffsetString(Date d, ZoneId zone, ChronoUnit truncateTo) {

		ZonedDateTime zdt = ((d).toInstant()).atZone(zone);

		if (truncateTo != null) {

		zdt = zdt.truncatedTo(truncateTo);

		}

		return zdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

		}
}
