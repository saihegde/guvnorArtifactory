package gov.utah.dts.erep.guvnorartifactory.utils.impl;

import gov.utah.dts.erep.guvnorartifactory.GuvnorArtifactoryConfig;
import gov.utah.dts.erep.guvnorartifactory.exceptions.GuvnorArtifactoryException;
import gov.utah.dts.erep.guvnorartifactory.utils.EncryptionUtils;
import gov.utah.dts.erep.guvnorartifactory.utils.GuvnorUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GuvnorUtilsImpl implements GuvnorUtils{

	private static final Logger logger = Logger.getLogger(Thread.currentThread().getStackTrace()[0].getClassName());
	@Autowired
	GuvnorArtifactoryConfig guvnorArtifactoryConfig;
	@Autowired
	EncryptionUtils encryptionUtils; 
	
	/**
	 * @return the client
	 */
	public WebClient client() {
	    WebClient client = WebClient.create("http://" + guvnorArtifactoryConfig.getHost() + ":"  + guvnorArtifactoryConfig.getPort());
		String auth = guvnorArtifactoryConfig.getUsername() + ":" + encryptionUtils.decrypt(guvnorArtifactoryConfig.getPassword());
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
	
	
    /* (non-Javadoc)
     * @see gov.utah.dts.erep.guvnorartifactory.utils.GuvnorUtils#getHttpClient()
     */
    @Override
    public HttpClient getHttpClient() {
        HttpClient client = new HttpClient();
        client.getState().setCredentials(guvnorArtifactoryConfig.getAuthScope(), guvnorArtifactoryConfig.getCredentials());
        return client;
    }


    @Override
    public List<File> listDirectories(File folder) {
        if (!folder.isDirectory()){
            logger.error(folder.getPath() + " is not a vaild directory.");
            throw new GuvnorArtifactoryException(folder.getPath() + " is not a vaild directory.");
        }
        List<File> directories = new ArrayList<File>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                directories.add(fileEntry);
            }
        }
        return directories;
    }
	
	/**
	 * Apply auth.
	 * 
	 * @param connection
	 *            the connection
	 */
	/*
	public static void applyAuth(HttpURLConnection connection) {
		String auth = properties.getProperty("guvnor.usr", "admin") + ":" + properties.getProperty("guvnor.pwd", "admin");
		connection.setRequestProperty("Authorization", "Basic " + Base64.encodeBase64String(auth.getBytes()));
	}*/

	

}
