package gov.utah.dts.erep.guvnorartifactory.services.impl;

import gov.utah.dts.erep.guvnorartifactory.GuvnorArtifactoryConfig;
import gov.utah.dts.erep.guvnorartifactory.exceptions.GuvnorArtifactoryException;
import gov.utah.dts.erep.guvnorartifactory.services.AssetManager;
import gov.utah.dts.erep.guvnorartifactory.services.PackageManager;
import gov.utah.dts.erep.guvnorartifactory.utils.GuvnorUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;

/**
 * @author Sai Hegde
 * @since October 14, 2013
 * */

@Service
public class AssetManagerImpl implements AssetManager {
    
    @Autowired
    PackageManager packageManager;
    @Autowired
    GuvnorUtils guvnorUtils;
    @Autowired
    GuvnorArtifactoryConfig guvnorArtifactoryConfig;
    
    
    private static final int RETRY_LIMIT = 3;
    private static final Logger logger = Logger.getLogger(Thread.currentThread().getStackTrace()[0].getClassName());

    /*
     * (non-Javadoc)
     * 
     * @see gov.utah.dts.erep.guvnorartifactory.services.AssetManager#listAll()
     */
    @Override
    public List<String> listAll() {
        List<String> assets = new ArrayList<String>();
        for(String packageName : packageManager.list()){
            assets.addAll(list(packageName));
        }
        return assets;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.utah.dts.erep.guvnorartifactory.services.AssetManager#list(java.lang.String)
     */
    @Override
    public List<String> list(String packageName) {
        List<String> assets = new ArrayList<String>();
        try {
            String content = guvnorUtils.client().path("drools-guvnor/rest/packages/" + packageName + "/assets").accept("application/atom+xml").get(String.class);
            SAXBuilder builder = new SAXBuilder();
            Document document = (Document) builder.build(new InputSource(new StringReader(content)));
            Element rootNode = document.getRootElement();
            @SuppressWarnings("unchecked")
            List<Element> list = rootNode.getChildren("entry", rootNode.getNamespace());
            for (int i = 0; i < list.size(); i++) {
                Element node = (Element) list.get(i);
                assets.add(node.getChildText("title", rootNode.getNamespace()) + "." + node.getChild("metadata").getChild("format").getChildText("value"));
            }
        } catch (JDOMException e) {
            throw new GuvnorArtifactoryException(e);
        } catch (IOException e) {
            throw new GuvnorArtifactoryException(e);
        }
        logger.fine("######################################################");
        logger.fine("Listing assets for " + packageName + ":<br/>");
        logger.fine(Arrays.toString(assets.toArray()));
        logger.fine("######################################################");
        return assets;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.utah.dts.erep.guvnorartifactory.services.AssetManager#exits(java.lang.String, java.lang.String)
     */
    @Override
    public boolean exists(String packageName, String assetName) {
        for (String asset : list(packageName)) {
            if (asset.equalsIgnoreCase(assetName)) return true;
        }
        return false;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see gov.utah.dts.erep.guvnorartifactory.services.AssetManager#save(java.lang.String, java.io.File)
     */
    @Override
    public void save(String packageName, File asset) {
        if (isExcludedType(asset)) {
            if (exists(packageName, asset.getName())) {
                update(packageName, asset, buildHeaders(asset));
            } else {
                create(packageName, asset, buildHeaders(asset));
            }
        }
        /*TODO 
         * Retry logic to be added.
         */
    }
    
    private synchronized void create(String packageName, File asset, MultivaluedMap<String, String> headers) {
        WebClient client = guvnorUtils.client();
        try {
            client.headers(headers);
            logger.info("\nCreating asset " + asset + " Package :" + packageName);
            client.path("drools-guvnor/rest/packages/" + packageName + "/assets").accept("application/atom+xml").post(asset);
        } finally {
            if (client != null) {
                client.reset();
            }
        }
    }

    private synchronized void update(String packageName, File asset, MultivaluedMap<String, String> headers) {
        WebClient client = guvnorUtils.client();
        try {
            client.headers(headers);
            logger.info("\nUpdating asset " + asset + " Package :" + packageName);
            client.path(guvnorArtifactoryConfig.getContextRoot() + "/rest/packages/" + packageName + "/assets/" + FilenameUtils.removeExtension(asset.getName()) + "/binary").accept("application/octet-stream").put(asset);
        } finally {
            if (client != null) {
                client.reset();
            }
        }
    }  
    
    /*
     * Checking for checkedOut assets should not be done since no one is supposed to be making changes to shared Guvnor instances.
     * Checking for this puts us at risk of incomplete Guvnor updates
     * Thoughts??? 
     * - SAI
     *  */
    
    /* if (isCheckOut(asset)) {
        failedAssets.add(packageName + "/" + asset.getName());
        logger.info(asset.getName() + " is not updated because it is writable and it could be checked out for on going work.");
    } else {
        client = getClient();
        List<String> slugHeader = new ArrayList<String>();
        slugHeader.add(asset.getName());
        MultivaluedMap<String, String> map = client.getHeaders();
        map.put("Slug", slugHeader);
        client.headers(map);
        logger.info("\nUpdating asset " + asset + " Package :" + packageName);
        client.path("drools-guvnor/rest/packages/" + packageName + "/assets/" + FilenameUtils.removeExtension(asset.getName()) + "/binary").accept("application/octet-stream").put(asset);
    }*/
    
    /*if (e instanceof ClientWebApplicationException && !(e.getCause() instanceof IllegalStateException)) {
        while (retries <= RETRY_LIMIT) {
            createAsset(packageName, asset);
            if (retries == RETRY_LIMIT) {
                failedAssets.add(packageName + "/" + asset.getName());
            }
            retries++;
        }
        retries = 0;
    }*/

    /*
     * (non-Javadoc)
     * 
     * @see gov.utah.dts.erep.guvnorartifactory.services.AssetManager#delete(java.lang.String, java.lang.String)
     */
    @Override
    public void delete(String packageName, String assetName) {
        WebClient client = guvnorUtils.client();
        client.headers(buildHeaders(null));
        client.path("drools-guvnor/rest/packages/" + packageName + "/assets/" + assetName.substring(0, assetName.indexOf("."))).delete();
    }
    
    /**
     * Utility method that returns true if a given file has to be excluded from a guvnor update.
    */
    protected static boolean isExcludedType(File asset){
        return FilenameUtils.isExtension(asset.getName(), Arrays.asList("zip", "keep", "jar", "contrib"));
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see gov.utah.dts.erep.guvnorartifactory.services.AssetManager#isGuvnorArtifact(java.io.File)
     */
    @Override
    public boolean isGuvnorArtifact(File asset){
        return FilenameUtils.isExtension(asset.getName(), Arrays.asList("bpmn2", "brl", "gdst", "enum"));
    }
    
    /**
     * Utility method to build headers for a Guvnor artifact.
    */
    private MultivaluedMap<String, String> buildHeaders(File asset){
        WebClient client = guvnorUtils.client();
        List<String> slugHeader = new ArrayList<String>();
        if(asset != null){
            slugHeader.add(asset.getName());
        }
        MultivaluedMap<String, String> headers = client.getHeaders();
        headers.put("Slug", slugHeader);
        return headers;
    }

}
