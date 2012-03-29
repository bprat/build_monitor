package org.prathipati.tools.internal.helpers;

import java.io.Console;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;

/**
 * @author bprathipati
 * 
 */
public class AuthHelper {
	private static final Log LOGGER = LogFactory.getLog(AuthHelper.class);
	XMLConfiguration config ;
	final String scheme ;
	final int port ;
	final int sleep_time_in_millis ;
	HttpHost targetHost;
	ClientConnectionManager cm;
	HttpParams params = new BasicHttpParams();
	DefaultHttpClient httpclient = new DefaultHttpClient();
	BasicHttpContext localcontext = new BasicHttpContext();
	
	public AuthHelper() throws ConfigurationException {
		config = new XMLConfiguration("settings.xml");
		scheme = (String) config.getProperty("hudson_monitor.scheme");
		port = NumberUtils.toInt( (String) config.getProperty("hudson_monitor.port"));
		sleep_time_in_millis = NumberUtils.toInt( (String) config.getProperty("hudson_monitor.sleep_time_in_millis"));
	}
	
	@SuppressWarnings("deprecation")
	public boolean initialize() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext ctx = SSLContext.getInstance("SSL");
		ctx.init(new KeyManager[0], new TrustManager[] { new DefaultTrustManager() }, new SecureRandom());
		SSLContext.setDefault(ctx);

		SSLSocketFactory sf = new SSLSocketFactory(ctx);
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		Scheme httpsScheme = new Scheme(scheme, sf, port);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(httpsScheme);
		
		cm = new SingleClientConnManager(params, schemeRegistry);
		targetHost = new HttpHost((String)config.getProperty("hudson_monitor.hostname"), port, scheme);
		httpclient = new DefaultHttpClient(cm, params);
		Console console = System.console();
		char[] passwdArr = new char[0];
		if (console != null) {
			passwdArr = console.readPassword("Enter password: ");
		} else {
			LOGGER.error("Console not available .. exiting");
		}
		httpclient.getCredentialsProvider().setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()),
				new UsernamePasswordCredentials((String) config.getProperty("hudson_monitor.login_username"), new String(passwdArr)));
		Arrays.fill(passwdArr, ' ');
		httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);

		BasicAuthCache authCache = new BasicAuthCache();
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(targetHost, basicAuth);

		// Add AuthCache to the execution context
		localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
		return true;
	}
	
/*	public static class DefaultTrustManager implements TrustManager, X509TrustManager {
		public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
		}

		public boolean isServerTrusted(final X509Certificate[] certs) {
		return true;
		}

		public boolean isClientTrusted(final X509Certificate[] certs) {
		return true;
		}

		//@Override
		public void checkServerTrusted(final X509Certificate[] certs, final String authType)
				throws java.security.cert.CertificateException {}
		public void checkClientTrusted(final X509Certificate[] certs, final String authType)
				throws java.security.cert.CertificateException {}
	}*/

	/**
	 * Create a trust manager to by-pass validating certificate chains
	 * a li'l trouble with certificates.. download the certificate file .crt/.cer and run this command
	 * ...\jre1.5.0_17\lib\security>keytool -keystore cacerts -import -file hudson.crt -alias hudson
	 */
	private static void trustAllHttpsCertificates() {
		final TrustManager tman = new DefaultTrustManager();
		final TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[]{tman};
		SSLContext sslc = null;
		try {
			sslc = SSLContext.getInstance("SSL");
			sslc.init(null, trustAllCerts, null);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sslc.getSocketFactory());
	}

	public static class DefaultTrustManager implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return new java.security.cert.X509Certificate[0];
		}
		public boolean isServerTrusted(final java.security.cert.X509Certificate[] certs) { return true; }
		public boolean isClientTrusted(final java.security.cert.X509Certificate[] certs) { return true; }
		public void checkServerTrusted(final java.security.cert.X509Certificate[] certs, final String authType) 
				throws java.security.cert.CertificateException { // empty 
		}
		public void checkClientTrusted(final java.security.cert.X509Certificate[] certs, final String authType)
				throws java.security.cert.CertificateException { // Intentionally left empty 
		}
	}
	public HttpHost getTargetHost() {
		return targetHost;
	}

	public void setTargetHost(HttpHost targetHost) {
		this.targetHost = targetHost;
	}

	public DefaultHttpClient getHttpclient() {
		return httpclient;
	}

	public void setHttpclient(DefaultHttpClient httpclient) {
		this.httpclient = httpclient;
	}

	public BasicHttpContext getLocalcontext() {
		return localcontext;
	}

	public void setLocalcontext(BasicHttpContext localcontext) {
		this.localcontext = localcontext;
	}
}