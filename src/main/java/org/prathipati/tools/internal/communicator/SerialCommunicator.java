package org.prathipati.tools.internal.communicator;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.util.Enumeration;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

public class SerialCommunicator extends BaseCommunicator implements SerialPortEventListener {
	private final Logger LOGGER = Logger.getLogger(SerialCommunicator.class);
	SerialPort serialPort;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;
	
	@Override
	public boolean initialize() throws ConfigurationException {
		CommPortIdentifier portId = null;
		XMLConfiguration config = new XMLConfiguration("settings.xml");
		@SuppressWarnings("rawtypes")
		Enumeration portEnum = CommPortIdentifier. getPortIdentifiers();
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			if (currPortId.getName().equals(config.getProperty("serial.port_name"))) {
				portId = currPortId;
				break;
			}
		}

		if (portId == null) {
			LOGGER.error("Could not find the defined COM port");
			return false;
		}

		try {
			// open serial port
			serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();

			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			LOGGER.error(e.toString(), e);
			return false;
		}
		return true;
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on some platforms (Linux?).
	 */
	@Override
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent event) {
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				int available = input.available();
				byte chunk[] = new byte[available];
				input.read(chunk, 0, available);
				LOGGER.info("data inbound: " + new String(chunk));
			} catch (Exception e) {
				LOGGER.error(e.toString(), e);
			}
		}// other eventTypes can be ignored, just logging them to see what's going on
		else {
			LOGGER.warn("unhandled event: " + event.toString());
		}
	}

	public static void main(String[] args) throws Exception {
		SerialCommunicator comm = new SerialCommunicator();
		if(comm.initialize()) {
			Thread.sleep(2000);
			comm.writeData(65);
		}
	}
}