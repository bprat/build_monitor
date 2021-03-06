package org.prathipati.tools.internal.communicator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

public abstract class BaseCommunicator {
	private final Logger LOGGER = Logger.getLogger(BaseCommunicator.class);
	protected InputStream input;
	protected OutputStream output;
	/** Milliseconds to block while waiting for port open */
	protected static final int TIME_OUT = 2000;

	public abstract boolean initialize() throws ConfigurationException;

	public synchronized void close() {
	}
	
	public synchronized void writeData(byte[] data) {
		try {
			//LOGGER.info("writing data: " + data);
			output.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void writeData(boolean noChange, int data) {
		if(!noChange) {
			LOGGER.info("writing data: " + data);
		}
		writeData(data);
	}

	public synchronized void writeData(int data) {
		try {
			output.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}