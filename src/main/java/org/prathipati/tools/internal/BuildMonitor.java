package org.prathipati.tools.internal;

import java.util.Calendar;
import java.util.TimeZone;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.math.NumberUtils;
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
	SerialCommunicator sch;
	int sleep_time_in_millis ;
	
	public static void main(final String[] args) {
		new BuildMonitor().run();
	}
	
	private void run() {
		
		HudsonMonitor hm = null;
		try {
			XMLConfiguration config = new XMLConfiguration("settings.xml");
			sleep_time_in_millis = NumberUtils.toInt( (String) config.getProperty("hudson_monitor.sleep_time_in_millis"));
			sch = new SerialCommunicator();
			sch.initialize();
			hm = new HudsonMonitor();
			while(true) {
				if(isAwake()) {
					if(!tvOn) {
						LOGGER.info("initial setup or waking up from sleep, reset TV");
						sch.writeData('Z');
						Thread.sleep(3000);
						LOGGER.info("powering on");
						sch.writeData('A');
						tvOn = true;
					} else {
						hm.check();
						sch.writeData(hm.getSerialOutput());
					}
					Thread.sleep(sleep_time_in_millis);
				} else {
					if(tvOn) {
						LOGGER.info("nap time..");
						tvOn = false;
					}
					sch.writeData('Z');
					Thread.sleep(300000);
				}
			}
		} catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			if (hm != null) {
				hm.destroy();
			}
		}
	}
	
	private boolean isAwake() {
		int hour = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.HOUR_OF_DAY);
		LOGGER.info("hour: " + hour);
		return (hour < 7 || hour > 17) ? false : true;
	}
}