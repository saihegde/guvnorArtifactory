package gov.utah.dts.erep.guvnorartifactory.utils;

import java.io.File;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.cxf.jaxrs.client.WebClient;

public interface GuvnorUtils {
    
    WebClient client();
    
    HttpClient getHttpClient();
    
    List<File> listDirectories(File folder);
    
	/*private static String host = "localhost";
	private static int port = 8080;
	private static String contextRoot = "drools-guvnor";
	private static final String defaultPass = "tE+VIgh8jy15U0a7ugcNow==";
	private static final Logger logger = Logger.getLogger(Thread.currentThread().getStackTrace()[0].getClassName());
	private static final Properties properties = new Properties();

	static {
		try {
			String ereproot = (System.getProperty("ereproot") != null) ? System.getProperty("ereproot") : System.getenv("ereproot");
			if (ereproot != null && new File(ereproot + "/ruleengine/guvnor/guvnor.properties").exists()) {
				logger.info("Loading properties from the defined ereproot location");
				properties.load(new FileInputStream(ereproot + "/ruleengine/guvnor/guvnor.properties"));
			} else {
				logger.info("No ereproot defined... Loading properties from the classpath.");
				properties.load(GuvnorArtifactory.class.getResourceAsStream("/guvnor.properties"));
			}
			logger.info("Loading assets to : " + properties.getProperty("guvnor.host"));
		} catch (Throwable t) {
			// do nothing, defaults will be loaded
			logger.warning("Property file could not be loaded. Loading default values" + t);
		}
	}

	*//**
	 * @return the client
	 *//*
	public static WebClient getClient() {
		WebClient client = WebClient.create("http://" + properties.getProperty("guvnor.host", "localhost") + ":"
				+ properties.getProperty("guvnor.port", "8080"));
		String auth = properties.getProperty("guvnor.usr", "admin") + ":" + EncryptionUtils.decrypt(properties.getProperty("guvnor.pwd", defaultPass));
		MultivaluedMap<String, String> map = new MetadataMap<String, String>();
		List<String> authHeader = new ArrayList<String>();
		authHeader.add("Basic " + Base64.encodeBase64String(auth.getBytes()));
		List<String> contentTypeHeader = new ArrayList<String>();
		contentTypeHeader.add("application/octet-stream");
		map.put("Authorization", authHeader);
		map.put("Content-Type", contentTypeHeader);
		client.headers(map);
		return client;
	}

	*//**
	 * Apply auth.
	 * 
	 * @param connection
	 *            the connection
	 *//*
	public static void applyAuth(HttpURLConnection connection) {
		String auth = properties.getProperty("guvnor.usr", "admin") + ":" + properties.getProperty("guvnor.pwd", "admin");
		connection.setRequestProperty("Authorization", "Basic " + Base64.encodeBase64String(auth.getBytes()));
	}

	public static String getBaseURL() {
		if (properties.getProperty("guvnor.baseURL") != null) {
			return properties.getProperty("guvnor.baseURL");
		} else {
			return "http://" + properties.getProperty("guvnor.host", host) + ":" + properties.getProperty("guvnor.port", String.valueOf(port)) + "/"
					+ properties.getProperty("guvnor.contextRoot", contextRoot);
		}
	}

	*//**
	 * @return the host
	 *//*
	public static String getHost() {
		return properties.getProperty("guvnor.host", "localhost");
	}

	*//**
	 * @param host
	 *            the host to set
	 *//*
	public static void setHost(String host) {
		GuvnorUtils.host = host;
	}

	*//**
	 * @return the port
	 *//*
	public static int getPort() {
		return Integer.valueOf(properties.getProperty("guvnor.port", "8080"));
	}

	*//**
	 * @param port
	 *            the port to set
	 *//*
	public static void setPort(int port) {
		GuvnorUtils.port = port;
	}

	*//**
	 * @return the contextRoot
	 *//*
	public static String getContextRoot() {
		return properties.getProperty("guvnor.contextRoot", "drools-guvnor");
	}

	*//**
	 * @param contextRoot
	 *            the contextRoot to set
	 *//*
	public static void setContextRoot(String contextRoot) {
		GuvnorUtils.contextRoot = contextRoot;
	}

	*//**
	 * @return the credentials
	 *//*
	public static Credentials getCredentials() {
		return new UsernamePasswordCredentials(properties.getProperty("guvnor.usr", "admin"), "");
	}
	
	*//**
	 * @return the username
	 *//*
	public static String getUserName() {
		return properties.getProperty("guvnor.usr", "admin");
	}*/

}
