package com.cerner.mef;


import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.opennms.netmgt.syslogd.api.SyslogMessageLogDTO;
import org.springframework.stereotype.Component;

@Component
public class UtiliMarshlerUnmarshaler {
	
	Class clazz = SyslogMessageLogDTO.class;
	JAXBContext jc;
	Unmarshaller unmarshaller;
	

	public UtiliMarshlerUnmarshaler() throws JAXBException {
		
		jc = JAXBContext.newInstance(clazz);
		unmarshaller = jc.createUnmarshaller();

	}

	public synchronized Object unmarshal(String xml) {
		
		 StreamSource streamSource = new StreamSource(new StringReader(xml));
		 
		 JAXBElement<SyslogMessageLogDTO> je = null;
		 
		   try {
			je = unmarshaller.unmarshal(streamSource,clazz);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SyslogMessageLogDTO syslogMessageLogDTO = (SyslogMessageLogDTO) je.getValue();
		return syslogMessageLogDTO;

	}

}
