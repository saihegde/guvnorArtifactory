import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.impl.MetadataMap;

public class TestingWebDav {

	private static String baseURL = "http://localhost:8080/drools-guvnor/org.drools.guvnor.Guvnor/webdav/packages/";
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		//listAllResources();
		addUsingREST("Sardine");
		//listAllResources("Eligibility");
	}

	/*public static void listAllResources() throws SardineException {
		listAllResources(null);
	}
	
	public static void listAllResources(String packageName) throws SardineException {
		Sardine sardine = SardineFactory.begin("admin", "admin");
		String resourceURL = packageName == null ? baseURL : baseURL + packageName;
		List<DavResource> resources = sardine.getResources(resourceURL);
		for (DavResource res : resources) {
			System.out.println(res); // calls the .toString() method.
		}
	}
	
	public static void add(String resourcePath) throws FileNotFoundException, IOException {
		Sardine sardine = SardineFactory.begin("admin", "admin");
		byte[] jar = IOUtils.readBytesFromStream(new FileInputStream("src/test/resources/rulesmodel-1.0-SNAPSHOT.jar"));
		sardine.put("http://localhost:8080/drools-guvnor/org.drools.guvnor.Guvnor/webdav/packages/Eligibility/EligibilityBusinessModel.jar", jar);
	}*/
	
	public static void addUsingREST(String resourcePath) throws FileNotFoundException, IOException {
		byte[] jar = IOUtils.readBytesFromStream(new FileInputStream("src/test/resources/rulesmodel-1.0-SNAPSHOT.jar"));
		WebClient client = WebClient.create("http://localhost:8080");
		MultivaluedMap<String, String> map = new MetadataMap<String, String>();
		List<String> slugHeader = new ArrayList<String>();
		slugHeader.add("rulesmodel-1.0-SNAPSHOT.jar");
		List<String> authHeader = new ArrayList<String>();
		authHeader.add("Basic " + new String(Base64.encodeBase64("admin:admin" .getBytes())));
		List<String> contentTypeHeader = new ArrayList<String>();
		contentTypeHeader.add("application/octet-stream");
		map.put("slug", slugHeader);
		map.put("Authorization", authHeader);
		map.put("Content-Type", contentTypeHeader);
		client.headers(map);
		client.path("/drools-guvnor/rest/packages/Eligibility/assets/EligibilityModel/source");
		client.post(jar);
		
		/*HttpClient client = new HttpClient();
		String url = "http://localhost:8080/drools-guvnor/rest/packages/Eligibility/assets/EligibilityModel.jar";

		Credentials defaultcreds = new UsernamePasswordCredentials("admin", "admin");
		client.getState().setCredentials(new AuthScope("localhost", 8080, AuthScope.ANY_REALM), defaultcreds);

		//delete old model
		DeleteMethod deleteMethod = new DeleteMethod(url);
		int statusCode1 = client.executeMethod(deleteMethod);
		System.out.println("statusLine>>>" + deleteMethod.getStatusLine());
		deleteMethod.releaseConnection();

		//post new
		PostMethod postMethod = new PostMethod(url);

		client.setConnectionTimeout(8000);

		// Send the model file as the body of the POST request
		File f = new File("src/test/resources/rulesmodel-1.0-SNAPSHOT.jar");
		System.out.println("File Length = " + f.length());

		postMethod.setRequestBody(new FileInputStream(f));
		postMethod.setRequestHeader("Content-type","text/xml; charset=ISO-8859-1");

		int statusCode2 = client.executeMethod(postMethod);

		System.out.println("statusLine>>>" + postMethod.getStatusLine());
		postMethod.releaseConnection();*/
		
		
		/*PackageResource packageResource = new PackageResource();
		packageResource.setHttpHeaders(httpHeaders);
		packageResource.createAssetFromBinary("http://localhost:8080/drools-guvnor/rest/packages/Eligibility/assets/EligibilityModel/", new FileInputStream("src/test/resources/rulesmodel-1.0-SNAPSHOT.jar"));*/
	}

}
