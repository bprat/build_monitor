package org.prathipati.tools.internal.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author bprathipati
 * 
 */
public abstract class BaseMonitor {
	private static final Log LOGGER = LogFactory.getLog(BaseMonitor.class);
	public BaseMonitor()  {}
	public boolean check() throws Exception {
		return false;
	}
	public void destroy() {}
}