package org.prathipati.tools.internal.helpers;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;

/**
 * @author bprathipati
 * 
 */
public class ConfigurationHelper {
	private static final Log LOGGER = LogFactory.getLog(ConfigurationHelper.class);
	XMLConfiguration config ;
	final String nameAttribute ;
	final String buildStatusAttribute ;
	final String success_status_string ;
	final String failure_status_string ;
	final String project_tag_name ;
	final String scheme ;
	final int port ;
	final int sleep_time_in_millis ;
	HttpHost targetHost;
	ClientConnectionManager cm;
	HttpParams params = new BasicHttpParams();
	DefaultHttpClient httpclient = new DefaultHttpClient();
	BasicHttpContext localcontext = new BasicHttpContext();
	
	@SuppressWarnings("unchecked")
	public ConfigurationHelper() throws ConfigurationException {
		config = new XMLConfiguration("settings.xml");
		
		nameAttribute = (String) config.getProperty("hudson_monitor.cc_xml.project_name_attribute");
		buildStatusAttribute = (String) config.getProperty("hudson_monitor.cc_xml.build_status_attribute");
		success_status_string = (String) config.getProperty("hudson_monitor.cc_xml.status_code.success");
		failure_status_string = (String) config.getProperty("hudson_monitor.cc_xml.status_code.failure");
		project_tag_name = (String) config.getProperty("hudson_monitor.cc_xml.project_tag_name");
		scheme = (String) config.getProperty("hudson_monitor.scheme");
		port = NumberUtils.toInt( (String) config.getProperty("hudson_monitor.port"));
		sleep_time_in_millis = NumberUtils.toInt( (String) config.getProperty("hudson_monitor.sleep_time_in_millis"));
	}

	public XMLConfiguration getConfig() {
		return config;
	}

	public void setConfig(XMLConfiguration config) {
		this.config = config;
	}
}