package org.prathipati.tools.internal.monitor;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.prathipati.tools.internal.helpers.AuthHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author bprathipati
 * 
 */
public class HudsonMonitor extends BaseMonitor {
	private static final Log LOGGER = LogFactory.getLog(HudsonMonitor.class);
	private int failureCount = 0, prevFailureCount = 0;
	char serialOutput, prevOutput;
	private final AuthHelper authHelper;
	List<String> iProjectList ;
	List<String> pProjectList ;
	final String nameAttribute ;
	final String buildStatusAttribute ;
	final String success_status_string ;
	final String failure_status_string ;
	final String project_tag_name ;
	final String scheme ;
	final int port ;
	final int sleep_time_in_millis ;
	XMLConfiguration config ;

	
	@SuppressWarnings("unchecked")
	public HudsonMonitor() throws ConfigurationException, NoSuchAlgorithmException, KeyManagementException {
		authHelper = new AuthHelper();
		authHelper.initialize();
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

	@Override
	public void check() throws Exception {
		try {
			HttpGet httpget = new HttpGet((String) config.getProperty("hudson_monitor.url"));
			LOGGER.info("executing request: " + httpget.getRequestLine());
			int prevIngFailCount = 0, prevPmtFailCount = 0, ingFailCount = 0, pmtFailCount = 0;
			while (true) {
				prevFailureCount = failureCount;
				failureCount = 0;
				prevIngFailCount = ingFailCount;
				prevPmtFailCount = pmtFailCount;
				String status = "";
				HttpResponse response = authHelper.getHttpclient().execute(authHelper.getTargetHost(), httpget, authHelper.getLocalcontext());
				HttpEntity entity = response.getEntity();

				if (entity != null && response.getStatusLine().getStatusCode() == 200) {
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder dbuilder = dbf.newDocumentBuilder();
					Document doc = dbuilder.parse(entity.getContent());
					doc.getDocumentElement().normalize();
					NodeList nodeLst = doc.getElementsByTagName(project_tag_name);
					int ingesterStatus = 0, pmtStatus = 0;
					for (int s = 0; (s < nodeLst.getLength()); s++) {
						final Node fstNode = nodeLst.item(s);
						String projectName = getNode(fstNode, nameAttribute).getNodeValue();
						if (iProjectList.contains(projectName)) {
							status = getNode(fstNode, buildStatusAttribute).getNodeValue();
							if (StringUtils.equalsIgnoreCase(status, failure_status_string)) {
								ingFailCount++;
								failureCount++;
								ingesterStatus = 1;
								LOGGER.info(status + ": " + projectName);
							}
						} else if (pProjectList.contains(projectName)) {
							status = getNode(fstNode, buildStatusAttribute).getNodeValue();
							if (StringUtils.equalsIgnoreCase(status, failure_status_string)) {
								pmtFailCount++;
								failureCount++;
								pmtStatus = 2;
								LOGGER.info(status + ": " + projectName);
							}
						}
					}
					int outInt = ingesterStatus + pmtStatus;
					serialOutput = (outInt == 0) ? 'N' : (outInt == 1) ? 'I' : (outInt == 2) ? 'P' : 'B';
					boolean newFailure = ((prevOutput != serialOutput && prevOutput != (serialOutput + 32)) && serialOutput != 'N') ? true : false;
					//if(this.failureCount > this.prevFailureCount) serialOutput+=32; 
					serialOutput += (failureCount > prevFailureCount ? 64 : (failureCount < prevFailureCount ? 32 : 0));
					prevOutput = serialOutput;
					LOGGER.info("Out: " + new Integer(serialOutput) + ", failureCount: " + failureCount + ", prevFailureCount: " + prevFailureCount);
					if (ingFailCount > prevIngFailCount || pmtFailCount > prevPmtFailCount) {
						buzz();
					}
				} else {
					LOGGER.error(response.getStatusLine());
					break;
				}
				EntityUtils.consume(entity);
				Thread.sleep(sleep_time_in_millis);
			}
		} finally {
			authHelper.getHttpclient().getConnectionManager().shutdown();
		}
	}

	public static void main(String[] args) throws Exception {
		HudsonMonitor hm = new HudsonMonitor();
		hm.check();
	}

	protected Node getNode (Node fstNode, final String input) {
		return fstNode.getAttributes().getNamedItem(input);
	}
	
	private static void buzz() {
	}

	public int getFailureCount() {
		return failureCount;
	}

	public void setFailureCount(int failureCount) {
		this.failureCount = failureCount;
	}

	public int getPrevFailureCount() {
		return prevFailureCount;
	}

	public void setPrevFailureCount(int prevFailureCount) {
		this.prevFailureCount = prevFailureCount;
	}

	public char getSerialOutput() {
		return serialOutput;
	}

	public void setSerialOutput(char serialOutput) {
		this.serialOutput = serialOutput;
	}

	public char getPrevOutput() {
		return prevOutput;
	}

	public void setPrevOutput(char prevOutput) {
		this.prevOutput = prevOutput;
	}
}