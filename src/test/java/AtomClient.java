import java.util.Date;
import java.util.TimerTask;

public class AtomClient extends TimerTask {
	private Date lastUpdateDate;
	private String feedURL;

	public AtomClient(String feedURL) {
		this.feedURL = feedURL;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	/*public void run() {
		try {
			System.out.println("-----Polling feed----");
			List<Entry> entries = createFeed().getEntries();
			for (Entry entry : entries) {
				if (isEntryUpdated(entry)) {
					String title = entry.getTitle();
					System.out.println("-------Found a new entry -");
					System.out.println("Title: " + title);
					System.out.println("Updated: " + entry.getUpdated());
					String contentSrcPath = entry.getLinks().get(0).getHref().toString();
					InputStream is = getPackageBinary(contentSrcPath);

					String NS = "";
					QName METADATA = new QName(NS, "metadata");
					QName STATE = new QName(NS, "stage");
					ExtensibleElement metadataExtension = entry.getExtension(METADATA);
					String state = (metadataExtension != null) ? metadataExtension.getSimpleExtension(STATE) : null;
					System.out.println("state: " + state);
					// We can copy package binary to different locations based
					// on state info.

					File f = new File(title + ".pkg");
					OutputStream out = new FileOutputStream(f);

					int read = 0;
					byte[] bytes = new byte[1024];
					while ((read = is.read(bytes)) != -1) {
						out.write(bytes, 0, read);
					}

					is.close();
					out.flush();
					out.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Feed createFeed() throws IOException, URISyntaxException {
		Abdera abdera = new Abdera();
		AbderaClient client = new AbderaClient(abdera);
		Credentials defaultcreds = new UsernamePasswordCredentials("admin", "admin");
		client.addCredentials("http://localhost:8080/drools-guvnor", AuthScope.ANY_REALM, AuthScope.ANY_SCHEME, defaultcreds);
		ClientResponse resp = client.get(feedURL);
		if (resp.getType() == ResponseType.SUCCESS) {
			Document<?> doc = resp.getDocument();
			return (Feed) doc.getRoot();
		} else {
			// there was an error
			return null;
		}
	}

	private InputStream getPackageBinary(String contentSrcPath) throws IOException, URISyntaxException {
		Abdera abdera = new Abdera();
		AbderaClient client = new AbderaClient(abdera);
		Credentials defaultcreds = new UsernamePasswordCredentials("admin", "admin");
		client.addCredentials("http://localhost:8080/drools-guvnor", AuthScope.ANY_REALM, AuthScope.ANY_SCHEME, defaultcreds);
		ClientResponse resp = client.get(contentSrcPath + "/source");
		return resp.getInputStream();
	}

	private boolean isEntryUpdated(Entry entry) {
		Date updateDate = entry.getUpdated();
		if (updateDate == null) {
			updateDate = entry.getPublished();
		}
		if (updateDate == null) {
			return true;
		}

		if (lastUpdateDate != null && (updateDate.before(lastUpdateDate) || updateDate.equals(lastUpdateDate))) {
			return false;
		}
		lastUpdateDate = updateDate;
		return true;
	}

	public static void main(String[] args) {
		int pollingInterval = 20;
		String feedURL = "http://localhost:8080/drools-guvnor/rest/packages/mortgages/versions";
		AtomClient atomTask = new AtomClient(feedURL);
		Timer timer = new Timer();
		timer.schedule(atomTask, 1000, pollingInterval * 1000);
	}*/
}