package org.prathipati.tools.internal;

import java.util.Calendar;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.prathipati.tools.internal.communicator.SerialCommunicator;
import org.prathipati.tools.internal.monitor.HudsonMonitor;

/**
 * @author bprathipati
 *
 */
public class BuildMonitor {
	private static final Log LOGGER = LogFactory.getLog(BuildMonitor.class);
	boolean tvOn = false;
	boolean sleepMode = false;
	SerialCommunicator sch = new SerialCommunicator();
	
	public static void main(final String[] args) {
		BuildMonitor monitor = new BuildMonitor();
		monitor.run();
	}
	
	private void run() {
		sch.initialize();
		try {
			HudsonMonitor hm = new HudsonMonitor();
			while(true) {
				if(isAwake()) {
					if(!tvOn) {
						sch.writeData('Z');
						Thread.sleep(5000);
						LOGGER.info("powering on now");
						sch.writeData('A');
						tvOn = true;
					} else {
						hm.check();
						sch.writeData(hm.getSerialOutput());
					}
					/*if (hm.getSerialOutput() == 'A') {
						sch.writeData('Z');
						Thread.sleep(5000);
						LOGGER.info("powering on now");
						sch.writeData('A');
					}*/
					Thread.sleep(10000);
				} else {
					sleepMode = true;
					Thread.sleep(300000);
				}
			}
		} catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		} 
	}
	
	private boolean isAwake() {
		int hour = Calendar.getInstance(TimeZone.getTimeZone("GMT-7:00")).get(Calendar.HOUR_OF_DAY);
		return (hour > 18 && hour < 7) ? false : true;
	}
}