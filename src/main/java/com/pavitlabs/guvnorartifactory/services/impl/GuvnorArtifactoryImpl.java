package gov.utah.dts.erep.guvnorartifactory.services.impl;


public class GuvnorArtifactoryImpl{

    /*private int retries = 0;
    private static final int RETRY_LIMIT = 3;
    private static List<String> failedAssets = new ArrayList<String>();
    private static final Logger logger = Logger.getLogger(Thread.currentThread().getStackTrace()[0].getClassName());
    private static final boolean overrideCheckedOutFile = Boolean.parseBoolean(System.getProperty("overrideCheckOut"));

    static {
        try {
            logger.addHandler(new FileHandler(System.getProperty("user.dir") + "/updateGuvnor.log", true));
        } catch (Exception ex) {}
    }


    private void errorReport() {
        if (failedAssets.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n********************************************************" + "\n");
            sb.append("****************** Summary REPORT **********************" + "\n");
            sb.append("********************************************************" + "\n");
            sb.append("There were " + failedAssets.size() + " assets not updated" + "\n");
            for (String asset : failedAssets) {
                sb.append(asset + "\n");
            }
            sb.append("End Time: " + new Date() + "\n");
            sb.append("****************** End REPORT **********************" + "\n");
            logger.info(sb.toString());
        } else {
            logger.info("All Artifacts were updated successfully");
        }
    }

    *//**
     * We don't want to accidentally over write files in the guvnor that user is currently working
     * on
     * 
     * @param assest
     * @return
     *//*
    private boolean isCheckOut(File assest) {

        if (overrideCheckedOutFile || !assest.canWrite()) {
            return false;
        } else {
            return true;
        }
    }



    public List<String> listPackages() {
        List<String> packages = new ArrayList<String>();
        try {
            String content = getClient().path("drools-guvnor/rest/packages").accept("application/atom+xml").get(String.class);
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return packages;
    }

    public List<String> listAssets(String packageName) {
        List<String> assets = new ArrayList<String>();
        try {
            String content = getClient().path("drools-guvnor/rest/packages/" + packageName + "/assets").accept("application/atom+xml").get(String.class);
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(assets);
        return assets;
    }

    public List<File> listAssets(File packageFolder) {
        List<File> assets = new ArrayList<File>();
        for (final File fileEntry : packageFolder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                assets.add(fileEntry);
            }
        }
        return assets;
    }

    public boolean packageExists(String packageName) {
        for (String guvnorPackage : listPackages()) {
            if (packageName.equalsIgnoreCase(guvnorPackage)) return true;
        }
        return false;
    }

    public boolean assetExists(String packageName, String assetName) {
        for (String asset : listAssets(packageName)) {
            if (asset.equalsIgnoreCase(assetName)) return true;
        }
        return false;
    }

    public synchronized void createAsset(String packageName, File asset) {
        WebClient client = null;
        try {
            if (!FilenameUtils.isExtension(asset.getName(), Arrays.asList("zip", "keep", "jar"))) {
                client = getClient();
                List<String> slugHeader = new ArrayList<String>();
                slugHeader.add(asset.getName());
                MultivaluedMap<String, String> map = client.getHeaders();
                map.put("Slug", slugHeader);
                client.headers(map);
                System.out.println("\nCreating asset " + asset + " Package :" + packageName);
                client.path("drools-guvnor/rest/packages/" + packageName + "/assets").accept("application/atom+xml").post(asset);
            }
        } catch (Exception e) {
            if (e instanceof ClientWebApplicationException) {
                while (retries <= RETRY_LIMIT) {
                    createAsset(packageName, asset);
                    if (retries == RETRY_LIMIT) {
                        failedAssets.add(packageName + "/" + asset.getName());
                    }
                    retries++;
                }
                retries = 0;
            }
        } finally {
            if (client != null) {
                client.reset();
            }
        }
    }

    public synchronized void updateAsset(String packageName, File asset) {
        WebClient client = null;
        try {
            if (!FilenameUtils.isExtension(asset.getName(), Arrays.asList("zip", "keep", "jar"))) {
                if (isCheckOut(asset)) {
                    failedAssets.add(packageName + "/" + asset.getName());
                    logger.info(asset.getName() + " is not updated because it is writable and it could be checked out for on going work.");
                } else {
                    client = getClient();
                    List<String> slugHeader = new ArrayList<String>();
                    slugHeader.add(asset.getName());
                    MultivaluedMap<String, String> map = client.getHeaders();
                    map.put("Slug", slugHeader);
                    client.headers(map);
                    System.out.println("\nUpdating asset " + asset + " Package :" + packageName);
                    client.path("drools-guvnor/rest/packages/" + packageName + "/assets/" + FilenameUtils.removeExtension(asset.getName()) + "/binary").accept("application/octet-stream").put(asset);
                }
            }
        } catch (Exception e) {
            if (e instanceof ClientWebApplicationException && !(e.getCause() instanceof IllegalStateException)) {
                while (retries <= RETRY_LIMIT) {
                    createAsset(packageName, asset);
                    if (retries == RETRY_LIMIT) {
                        failedAssets.add(packageName + "/" + asset.getName());
                    }
                    retries++;
                }
                retries = 0;
            }
        } finally {
            if (client != null) {
                client.reset();
            }
        }
    }

    public synchronized void deletePackage(String packageName) {
        WebClient client = null;
        if (packageExists(packageName)) {
            client = getClient();
            List<String> slugHeader = new ArrayList<String>();
            MultivaluedMap<String, String> map = client.getHeaders();
            map.put("Slug", slugHeader);
            client.headers(map);
            client.path("drools-guvnor/rest/packages/" + packageName).delete();
        }
    }

    public synchronized void createPackage(String packageName) {
        WebClient client = null;
        if (!packageExists(packageName)) {
            client = getClient();
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
            }
            client.path("drools-guvnor/rest/packages/").post(file);
        }
    }

    public void cleanGuvnor() {
        for (String guvnorPackage : listPackages()) {
            deletePackage(guvnorPackage);
        }
    }

    public void createPackages(File resourcesFolder) {
        for (final File fileEntry : resourcesFolder.listFiles()) {
            if (fileEntry.isDirectory()) {
                if (!packageExists(fileEntry.getName())) {
                    System.out.println("\n*********************************************");
                    System.out.println("Creating package " + fileEntry.getName());
                    System.out.println("\n*********************************************");
                    createPackage(fileEntry.getName());
                    System.out.println("\n*********************************************");
                    System.out.println("Created package " + fileEntry.getName());
                    System.out.println("\n*********************************************");
                }
            }
        }
    }

    private void updatePackage(File resourcesFolder, String packageName) {
        if (!packageExists(packageName)) {
            createPackage(packageName);
        }
        if (validateResourcesFolderForPackage(resourcesFolder, packageName)) {
            if (validateResourcesFolderForPackage(resourcesFolder, packageName)) {
                for (final File fileEntry : resourcesFolder.listFiles()) {
                    if (!fileEntry.isDirectory()) {
                        if (assetExists(packageName, fileEntry.getName())) {
                            updateAsset(packageName, fileEntry);
                        } else {
                            createAsset(packageName, fileEntry);
                        }
                    }
                }

            }
        }
    }

    private void validateArtifacts(File resourcesFolder) {
        for (final File fileEntry : resourcesFolder.listFiles()) {
            if (fileEntry.isDirectory()) {
                validateArtifacts(fileEntry, fileEntry.getName());
            }
        }
    }

    private void validateArtifacts(File resourcesFolder, String packageName) {
        if (!packageExists(packageName)) {
            createPackage(packageName);
        }
        if (validateResourcesFolderForPackage(resourcesFolder, packageName)) {
            int i=1;
            List<String> assetSet = new ArrayList<String>();
            for (final File fileEntry : resourcesFolder.listFiles()) {
                if (!fileEntry.isDirectory()) {
                    i++;
                    assetSet.add(fileEntry.getName());
                    if (assetExists(packageName, fileEntry.getName())) {
                        updateAsset(packageName, fileEntry);
                    } else {
                        createAsset(packageName, fileEntry);
                    }
                    if(i%1 == 0){
                        try {
                            buildSnapshot(packageName, packageName + "_ACDV");
                            assetSet.clear();
                        } catch (Exception e) {
                            System.out.println(Arrays.toString(failedAssets.toArray()));
                            for(String failedAsset : assetSet){
                                if(isDroolsAsset(failedAsset)){
                                    failedAssets.add(failedAsset);
                                    System.out.println("Deleting Asset ::: " + failedAsset);
                                    deleteAsset(packageName, failedAsset);
                                }
                            }
                            assetSet.clear();
                            try {
                                buildSnapshot(packageName, packageName + "_ACDV");
                            } catch (Exception ie) {
                                throw new RuntimeException("Aborted for failed assets in repository");
                            }
                            //throw new RuntimeException("Aborted for failed assets in repository");
                            WebClient client = WebClient.create("http://localhost:8080/drools-guvnor/rest/packages/" + packageName + "/assets/" + fileEntry.getName(), "admin", "admin", null);
                            Response response = client.delete();
                        }
                    }
                } else {
                    validateArtifacts(new File(resourcesFolder.getAbsolutePath() + "/" + packageName), packageName);
                }
            }
        }
    }
    
    private static boolean isDroolsAsset(String assetName){
        return (assetName.endsWith(".gdst") || assetName.endsWith(".bpmn2") || assetName.endsWith(".brl"));
    }

    public void updatePackages(File resourcesFolder, String... packageNames) {
        for (String packageName : packageNames) {
            System.out.println("\n*********************************************");
            System.out.println("Updating package " + packageName);
            System.out.println("\n*********************************************");

            File packageFolder = new File(resourcesFolder.getPath() + "/" + packageName);
            updatePackage(packageFolder, packageName);

            if (failedAssets.size() > 0) {
                errorReport();
            }

            System.out.println("\n*********************************************");
            System.out.println("Updated package :" + packageName + " successfully.");
            System.out.println("\n*********************************************");
        }
    }

    private boolean validateResourcesFolderForPackage(File resourcesFolder, String packageName) {
        if (listPackageNamesFromResourcesDirectory(resourcesFolder).contains(packageName)) {
            return true;
        }
        System.out.println("Resources folder cannot be validated. Please make sure that the guvnor assets are in directories corresponding to their package names.");
        return false;
    }

    public List<String> listPackageNamesFromResourcesDirectory(final File folder) {
        List<String> packageNames = new ArrayList<String>();
        for (File packageDirectory : listPackageDirectories(folder)) {
            packageNames.add(packageDirectory.getName());
        }
        return packageNames;
    }

    public List<File> listPackageDirectories(final File folder) {
        List<File> packageDirectories = new ArrayList<File>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                packageDirectories.add(fileEntry);
            }
        }
        if (packageDirectories.size() == 0) packageDirectories.add(folder);
        return packageDirectories;
    }

    public void listFilesForFolder(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                System.out.println("Package Name:" + fileEntry.getParentFile().getName() + " Asset: " + fileEntry.getName());
            }
        }
    }

    public void loadPackages(File resourcesFolder) {
        createPackages(resourcesFolder);
        List<String> packages = listPackageNamesFromResourcesDirectory(resourcesFolder);
        if (packages.size() > 0) {
            updatePackages(resourcesFolder, packages.toArray(new String[packages.size()]));
        }
        System.out.println("\n********************************************************");
        System.out.println("Guvnor load successful.");
        System.out.println("********************************************************");
    }

    //@Override
    public void buildPackage(String packageName) {

    }

    //@Override
    public void updateModel(String packageName, File asset) {
        PostMethod postMethod = null;
        try {
            String modelName = packageName + "Model";
            HttpClient client = new HttpClient();
            String url = GuvnorUtils.getBaseURL() + "/org.drools.guvnor.Guvnor/api/packages/" + packageName + "/" + modelName + ".jar";

            client.getState().setCredentials(new AuthScope(GuvnorUtils.getHost(), GuvnorUtils.getPort(), AuthScope.ANY_REALM), GuvnorUtils.getCredentials());

            // delete old model
            DeleteMethod deleteMethod = new DeleteMethod(url);
            client.executeMethod(deleteMethod);
            logger.fine("Delete statusLine>>>" + deleteMethod.getStatusLine());
            logger.info("Model file for " + packageName + " successfully deleted.");
            deleteMethod.releaseConnection();

            // post new
            postMethod = new PostMethod(url);
            client.getHttpConnectionManager().getParams().setConnectionTimeout(8000);
            postMethod.setRequestEntity(new FileRequestEntity(asset, "application/zip"));
            client.executeMethod(postMethod);

            logger.fine("Post statusLine>>>" + postMethod.getStatusLine());
            logger.info("Model file for " + packageName + " successfully created.");

            logger.info("Making Jar file work");
            hookModel(packageName, modelName, asset);

        } catch (Exception ex) {
            logger.info(ex.getLocalizedMessage());
            throw new RuntimeException(ex);
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
    }

    private static String getUUID(String packageName, String modelName) {
        HttpClient client = new HttpClient();
        client.getState().setCredentials(new AuthScope(GuvnorUtils.getHost(), GuvnorUtils.getPort(), AuthScope.ANY_REALM), GuvnorUtils.getCredentials());
        String url = GuvnorUtils.getBaseURL() + "/rest/packages/" + packageName + "/assets/" + modelName;
        GetMethod get = new GetMethod(url);
        try {
            int status = client.executeMethod(get);
            if (!okStatus(status)) {
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
            logger.info(ex.getMessage());
            throw new RuntimeException(ex);
        } finally {
            get.releaseConnection();
        }
    }

    private static boolean okStatus(int status) {
        if (status >= 200 && status < 300) {
            return true;
        } else {
            return false;
        }
    }

    private void hookModel(String packageName, String modelName, File modelJar) throws Exception {
        HttpClient client = new HttpClient();
        String modelUuid = getUUID(packageName, modelName);
        String modelUrl = GuvnorUtils.getBaseURL() + "/org.drools.guvnor.Guvnor/asset";
        PostMethod post = new PostMethod(modelUrl);
        try {
            String contentType = ContentType.APPLICATION_OCTET_STREAM.getMimeType().toString();
            logger.info(FilePart.DEFAULT_CHARSET + " file content type: " + contentType);
            Part[] parts = {new StringPart("attachmentUUID", modelUuid), new FilePart("filename", modelJar, contentType, FilePart.DEFAULT_CHARSET)};

            MultipartRequestEntity entity = new MultipartRequestEntity(parts, post.getParams());
            post.setRequestEntity(entity);

            int status = client.executeMethod(post);
            if (okStatus(status)) {
                logger.info("updating " + packageName + "/" + modelName + " status=" + status);
            } else {
                throw new Exception("unable to update " + packageName + "/" + modelName + " status=" + status);
            }
        } finally {
            post.releaseConnection();
        }
    }


    //@Override
    public void buildSnapshot(String packageName, String snapshotName) {
        HttpClient client = new HttpClient();
        final String buildSnapShotUrl = GuvnorUtils.getBaseURL() + "/rest/packages/" + packageName + "/binary";
        client.getState().setCredentials(new AuthScope(GuvnorUtils.getHost(), GuvnorUtils.getPort(), AuthScope.ANY_REALM), GuvnorUtils.getCredentials());
        GetMethod get = new GetMethod(buildSnapShotUrl);
        final String deploySnapshotUrl = GuvnorUtils.getBaseURL() + "/rest/packages/" + packageName + "/snapshot/" + snapshotName;
        PostMethod post = new PostMethod(deploySnapshotUrl);
        try {
            int status = client.executeMethod(get);
            logger.info("creating snopshot http status = " + status);
            if (!okStatus(status)) {
                throw new Exception("build snapshot failed " + packageName + "/" + snapshotName + " status=" + status);
            }

            byte[] content = get.getResponseBody();
            String errorMsg = "Unable to build package";
            String info1 = new String(Arrays.copyOfRange(content, 0, 24));
            // TODO refine error condition, find a more dependable way to detecting errors

            if (info1.contains(errorMsg)) {
                String info = new String(content);
                logger.info(info);
                throw new Exception(info);
            }
            logger.info("Succeeded DONE build and validating package " + buildSnapShotUrl);
            get.releaseConnection();

            status = client.executeMethod(post);
            logger.info("deploying snapshot " + snapshotName + " http status = " + status);
            post.releaseConnection();
        } catch (Exception ex) {
            logger.warning(ex.getLocalizedMessage());
            throw new RuntimeException(ex);
        } finally {
            get.releaseConnection();
            post.releaseConnection();
        }
    }

    public static void main(String[] args) {
        try {
            String packageName = "Eligibility";
            GuvnorArtifactoryImpl guvnorArtifactory = new GuvnorArtifactoryImpl();
            //guvnorArtifactory.deletePackage("RiskEngine");
            //guvnorArtifactory.createPackage(packageName);
            //guvnorArtifactory.updateModel(packageName, new File("C:/Users/shegde/Desktop/files/ACDV/Build500/rulesmodel.jar"));
            guvnorArtifactory.updatePackages(new File("C:/Users/shegde/Desktop/files/ACDV/Build500/erroneousFiles"), packageName);
            guvnorArtifactory.buildSnapshot(packageName, packageName + "_ACDV");
            guvnorArtifactory.validateArtifacts(new File("C:/Users/shegde/Desktop/files/ACDV/Build500/erroneousFiles"), packageName);
            //System.out.println(Arrays.toString(failedAssets.toArray()));
        } finally {
            System.out.println("Failed assets ::: " + Arrays.toString(failedAssets.toArray()));
        }
        //guvnorArtifactory.deleteAsset("Eligibility", "Asset_Common_Annuity_Calculation");
    }

    
    public void deleteAsset(String packageName, String assetName) {
        WebClient client = null;
        client = getClient();
        List<String> slugHeader = new ArrayList<String>();
        MultivaluedMap<String, String> map = client.getHeaders();
        map.put("Slug", slugHeader);
        client.headers(map);
        client.path("drools-guvnor/rest/packages/" + packageName + "/assets/" + assetName.substring(0, assetName.indexOf("."))).delete();
    }*/

}
