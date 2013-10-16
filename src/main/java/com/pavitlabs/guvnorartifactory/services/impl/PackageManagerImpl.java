package gov.utah.dts.erep.guvnorartifactory.services.impl;

import gov.utah.dts.erep.guvnorartifactory.GuvnorArtifactoryConfig;
import gov.utah.dts.erep.guvnorartifactory.exceptions.GuvnorArtifactoryException;
import gov.utah.dts.erep.guvnorartifactory.services.AssetManager;
import gov.utah.dts.erep.guvnorartifactory.services.PackageManager;
import gov.utah.dts.erep.guvnorartifactory.utils.GuvnorUtils;
import gov.utah.dts.erep.guvnorartifactory.utils.impl.DiagnosisReportFormatter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.entity.ContentType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author Sai Hegde
 * @since October 14, 2013
 * */
@Service
public class PackageManagerImpl implements PackageManager {

    @Autowired
    GuvnorUtils guvnorUtils;
    @Autowired
    GuvnorArtifactoryConfig guvnorArtifactoryConfig;
    @Autowired
    AssetManager assetManager;

    private static final Logger logger = Logger.getLogger(Thread.currentThread().getStackTrace()[0].getClassName());
    
    private List <String> failedAssets = new ArrayList<String>();;
    
    /*
     * (non-Javadoc)
     * 
     * @see gov.utah.dts.erep.guvnorartifactory.services.PackageManager#listPackages()
     */
    @Override
    public List<String> list() throws GuvnorArtifactoryException {
        List<String> packages = new ArrayList<String>();
        try {
            String content = guvnorUtils.client().path(guvnorArtifactoryConfig.getContextRoot() + "/rest/packages").accept("application/atom+xml").get(String.class);
            SAXBuilder builder = new SAXBuilder();
            Document document = (Document) builder.build(new InputSource(new StringReader(content)));
            Element rootNode = document.getRootElement();
            @SuppressWarnings("unchecked")
            List<Element> list = rootNode.getChildren("entry", rootNode.getNamespace());
            for (int i = 0; i < list.size(); i++) {
                Element node = (Element) list.get(i);
                packages.add(node.getChildText("title", rootNode.getNamespace()));
            }
        } catch (JDOMException e) {
            throw new GuvnorArtifactoryException(e.getMessage(), e);
        } catch (IOException e) {
            throw new GuvnorArtifactoryException(e.getMessage(), e);
        }
        return packages;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * gov.utah.dts.erep.guvnorartifactory.services.PackageManager#packageExists(java.lang.String)
     */
    @Override
    public boolean contains(String packageName) throws GuvnorArtifactoryException {
        for (String guvnorPackage : list()) {
            if (packageName.equalsIgnoreCase(guvnorPackage)) return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.utah.dts.erep.guvnorartifactory.services.PackageManager#create(java.lang.String)
     */
    @Override
    public void create(String packageName) throws GuvnorArtifactoryException {
        if (!contains(packageName)) {
            WebClient client = guvnorUtils.client();
            List<String> slugHeader = new ArrayList<String>();
            slugHeader.add(packageName + ".pkg");
            MultivaluedMap<String, String> map = client.getHeaders();
            map.put("Slug", slugHeader);
            client.headers(map);
            File file = null;
            try {
                String content = "package " + packageName;
                file = File.createTempFile(packageName, ".drl");
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(content);
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new GuvnorArtifactoryException(e);
            }
            client.path(guvnorArtifactoryConfig.getContextRoot() + "/rest/packages/").post(file);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.utah.dts.erep.guvnorartifactory.services.PackageManager#delete(java.lang.String)
     */
    @Override
    public void delete(String packageName) throws GuvnorArtifactoryException {
        if (contains(packageName)) {
            WebClient client = guvnorUtils.client();
            List<String> slugHeader = new ArrayList<String>();
            MultivaluedMap<String, String> map = client.getHeaders();
            map.put("Slug", slugHeader);
            client.headers(map);
            client.path("drools-guvnor/rest/packages/" + packageName).delete();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * gov.utah.dts.erep.guvnorartifactory.services.PackageManager#uploadModel(java.lang.String,
     * java.io.File)
     */
    @Override
    public void uploadModel(String packageName, File modeljar) throws GuvnorArtifactoryException {
        PostMethod postMethod = null;
        try {
            String modelName = packageName + "Model";
            HttpClient client = new HttpClient();
            String url = guvnorArtifactoryConfig.getBaseURL() + "/org.drools.guvnor.Guvnor/api/packages/" + packageName + "/" + modelName + ".jar";

            client.getState().setCredentials(new AuthScope(guvnorArtifactoryConfig.getHost(), Integer.valueOf(guvnorArtifactoryConfig.getPort()), AuthScope.ANY_REALM), guvnorArtifactoryConfig.getCredentials());

            // delete old model
            DeleteMethod deleteMethod = new DeleteMethod(url);
            client.executeMethod(deleteMethod);
            logger.fine("Delete statusLine -->>>" + deleteMethod.getStatusLine());
            logger.info("Model file for " + packageName + " successfully deleted.");
            deleteMethod.releaseConnection();

            // post new
            postMethod = new PostMethod(url);
            client.getHttpConnectionManager().getParams().setConnectionTimeout(8000);
            postMethod.setRequestEntity(new FileRequestEntity(modeljar, "application/zip"));
            client.executeMethod(postMethod);

            logger.fine("Post statusLine -->>>" + postMethod.getStatusLine());
            logger.info("Model file for " + packageName + " successfully created.");

            logger.info("Model File Uploaded... Validating the model...");
            hookModel(packageName, modelName, modeljar);

        } catch (Exception ex) {
            logger.severe(ex.getLocalizedMessage());
            throw new GuvnorArtifactoryException(ex);
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.utah.dts.erep.guvnorartifactory.services.PackageManager#update(java.io.File, java.lang.String[])
     */
    @Override
    public void update(File resourcesFolder, String... packageNames) throws GuvnorArtifactoryException {
        packageNames = getPackageNamesToUpdate(resourcesFolder, packageNames);
        for (String packageName : packageNames){
            update(new File(resourcesFolder.getPath() + "/"  + packageName), packageName);
        }
    }
    
    protected void update(File resourcesFolder, String packageName) {
        validate(resourcesFolder);
        delete(packageName);
        create(packageName);
        for (final File fileEntry : resourcesFolder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                assetManager.save(packageName, fileEntry);
            }
        }
    }

    /* (non-Javadoc)
     * @see gov.utah.dts.erep.guvnorartifactory.services.PackageManager#diagnoseForCorruptedAssets(java.io.File, java.lang.String[])
     */
    @Override
    public void diagnoseForCorruptedAssets(File resourcesFolder, String... packageNames) {
        packageNames = getPackageNamesToUpdate(resourcesFolder, packageNames);
        for (String packageName : packageNames){
            update(new File(resourcesFolder.getPath() + "/"  + packageName), packageName);
        }
        logDiagnosisReport();
    }
    
    public void diagnoseForCorruptedAssets(File resourcesFolder, String packageName) {
        validate(resourcesFolder);
        delete(packageName);
        create(packageName);
        for (final File fileEntry : resourcesFolder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                assetManager.save(packageName, fileEntry);
                if(assetManager.isGuvnorArtifact(fileEntry)){
                    try{
                        build(packageName);
                    } catch(GuvnorArtifactoryException gae){
                        failedAssets.add(packageName + " : " +fileEntry.getName());
                        assetManager.delete(packageName, fileEntry.getName());
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.utah.dts.erep.guvnorartifactory.services.PackageManager#build(java.lang.String[])
     */
    @Override
    public void build(String... packageNames) throws GuvnorArtifactoryException {
        for(String packageName : packageNames){
            build(packageName);
        }
    }
    
    public void build(String packageName) throws GuvnorArtifactoryException {
        HttpClient client = guvnorUtils.getHttpClient();
        final String buildBinaryUrl = guvnorArtifactoryConfig.getBaseURL() + "/rest/packages/" + packageName + "/binary";
        GetMethod get = new GetMethod(buildBinaryUrl);
        try {
            int status = client.executeMethod(get);
            logger.info("Building " + packageName + " responded with HTTP status : " + status);
            if (!ok(status)) {
                throw new GuvnorArtifactoryException("Build failed for :" + packageName + " status=" + status);
            }

            byte[] content = get.getResponseBody();
            String errorMsg = "Unable to build package";
            String info1 = new String(Arrays.copyOfRange(content, 0, 24));

            // TODO refine error condition, find a more dependable way to detecting errors
            if (info1.contains(errorMsg)) {
                String info = new String(content);
                logger.info(info);
                throw new GuvnorArtifactoryException(info);
            }
            logger.info("Succeessfully built and validated package: " + packageName);
        } catch (Exception ex) {
            logger.severe(ex.getLocalizedMessage());
            throw new GuvnorArtifactoryException(ex);
        } finally {
            get.releaseConnection();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * gov.utah.dts.erep.guvnorartifactory.services.PackageManager#buildSnapshot(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void buildSnapshot(String packageName, String snapshotName) throws GuvnorArtifactoryException {
        // This will be removed later to facilitate continuous integration for guvnor.
        build(packageName);
        
        HttpClient client = guvnorUtils.getHttpClient();
        final String deploySnapshotUrl = guvnorArtifactoryConfig.getBaseURL() + "/rest/packages/" + packageName + "/snapshot/" + snapshotName;
        PostMethod post = new PostMethod(deploySnapshotUrl);
        try {
            int status = client.executeMethod(post);
            logger.info("Deploying snapshot " + snapshotName + " http status = " + status);
            if (!ok(status)) {
                throw new GuvnorArtifactoryException("Deploy snapshot failed for :" + packageName + " status=" + status);
            }
        } catch (Exception ex) {
            logger.severe(ex.getLocalizedMessage());
            throw new GuvnorArtifactoryException(ex);
        } finally {
            post.releaseConnection();
        }
    }
    
    private void hookModel(String packageName, String modelName, File modelJar) throws Exception {
        HttpClient client = new HttpClient();
        String modelUuid = getUUID(packageName, modelName);
        String modelUrl = guvnorArtifactoryConfig.getBaseURL() + "/org.drools.guvnor.Guvnor/asset";
        PostMethod post = new PostMethod(modelUrl);
        try {
            String contentType = ContentType.APPLICATION_OCTET_STREAM.getMimeType().toString();
            logger.info(FilePart.DEFAULT_CHARSET + " file content type: " + contentType);
            Part[] parts = {new StringPart("attachmentUUID", modelUuid), new FilePart("filename", modelJar, contentType, FilePart.DEFAULT_CHARSET)};

            MultipartRequestEntity entity = new MultipartRequestEntity(parts, post.getParams());
            post.setRequestEntity(entity);

            int status = client.executeMethod(post);
            if (ok(status)) {
                logger.info("Successfully updated model file for " + packageName + "/" + modelName + " status=" + status);
            } else {
                throw new GuvnorArtifactoryException("unable to update " + packageName + "/" + modelName + " status=" + status);
            }
        } finally {
            post.releaseConnection();
        }
    }
    
    protected String getUUID(String packageName, String modelName) {
        HttpClient client = new HttpClient();
        client.getState().setCredentials(new AuthScope(guvnorArtifactoryConfig.getHost(), Integer.valueOf(guvnorArtifactoryConfig.getPort()), AuthScope.ANY_REALM), guvnorArtifactoryConfig.getCredentials());
        String url = guvnorArtifactoryConfig.getBaseURL() + "/rest/packages/" + packageName + "/assets/" + modelName;
        GetMethod get = new GetMethod(url);
        try {
            int status = client.executeMethod(get);
            if (!ok(status)) {
                throw new Exception("unable to update " + packageName + "/" + modelName + " status=" + status);
            }
            logger.info("get uuid for " + modelName + ": " + url + " = " + status);
            byte[] content = get.getResponseBody();
            String data = new String(content);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(new InputSource(new StringReader(data)));
            XPathFactory pathFact = XPathFactory.newInstance();
            XPath xpath = pathFact.newXPath();
            XPathExpression expr = xpath.compile("//uuid");
            NodeList result = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            return result.item(0).getTextContent();
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
            throw new GuvnorArtifactoryException(ex);
        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Utility method to return <code>true</code> for a success HTTP response. 
     * @param HTTP response status code 
     * */
    protected static boolean ok(int status) {
        return (status >= 200 && status < 300);
    }
    
    /**
     * Utility method to return the package folders in a given resources folder. 
     * @param resources folder
     * @param package names to update
     * */
    protected String[] getPackageNamesToUpdate(File resourcesFolder, String... packageNames) {
        if(packageNames != null && packageNames.length > 0){
            return packageNames;
        } 
        List<String> packages = new ArrayList<String>();
        for(File packageFolder : guvnorUtils.listDirectories(resourcesFolder)){
            packages.add(packageFolder.getName());
        }
        return packages.toArray(new String[packages.size()]);
    }

    private void validate(File resourcesFolder) {
        if (!resourcesFolder.isDirectory()) {
            logger.severe(resourcesFolder.getPath() + " is not a vaild directory.");
            throw new GuvnorArtifactoryException(resourcesFolder.getPath() + " is not a vaild directory.");
        }
    }
    
    static{
        
    }
    private void logDiagnosisReport() {
        Logger diagnosisReportlogger = Logger.getLogger("diagnosisReportlogger");
        for(Handler handler : diagnosisReportlogger.getParent().getHandlers()){
            logger.getParent().removeHandler(handler);
        }
        DiagnosisReportFormatter formatter = new DiagnosisReportFormatter();
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatter);
        diagnosisReportlogger.addHandler(consoleHandler);
        diagnosisReportlogger.severe("~~~~~~~~~~~~~~~~~~~~~~~ DIAGNOSIS REPORT ~~~~~~~~~~~~~~~~~~~~~~~");
        diagnosisReportlogger.severe("\n");
        diagnosisReportlogger.severe("\n SUMMARY: " + failedAssets.size() + " assets corrupt.");
        diagnosisReportlogger.severe("\n");
        int i = 0;
        for(String failedAsset : failedAssets){
            i++;
            diagnosisReportlogger.severe("\n" + i + ". "  + failedAsset);
        }
        diagnosisReportlogger.severe("\n");
        diagnosisReportlogger.severe("~~~~~~~~~~~~~~~~~~~~~~~ ~~~~~~~~~~~~~~~~ ~~~~~~~~~~~~~~~~~~~~~~~");
    }
    
}
