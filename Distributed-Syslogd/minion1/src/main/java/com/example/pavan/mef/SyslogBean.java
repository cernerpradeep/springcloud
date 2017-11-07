package com.example.pavan.mef;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
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
}
