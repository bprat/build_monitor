package org.prathipati.tools.internal;

import gnu.io.CommPortIdentifier;

import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ListAvailablePorts {
	private static final Log LOGGER = LogFactory.getLog(ListAvailablePorts.class);
	public void listPorts() {  
		Enumeration ports = CommPortIdentifier.getPortIdentifiers();  
		while(ports.hasMoreElements()) {
			LOGGER.info(((CommPortIdentifier)ports.nextElement()).getName());
		}  
	}  
}