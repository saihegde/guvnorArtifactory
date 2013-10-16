package org.guvnor.tools.utils.webdav;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Utilities for dealing with streams.
 */
public class StreamProcessingUtils {

	private static final SimpleDateFormat CREATION_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	private static final SimpleDateFormat LAST_MODIFIED_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	protected static final SimpleDateFormat GUNVOR_TOOLS_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.getDefault());

	public static String getStreamContents(InputStream is) throws IOException {
		byte[] buffer = new byte[1000];
		int c = 0;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		while ((c = is.read(buffer)) != -1) {
			bout.write(buffer, 0, c);
		}

		return new String(bout.toByteArray());
	}

	private static String DAV_NS = "DAV:"; //$NON-NLS-1$

	public static Map<String, ResourceProperties> parseListing(String base, InputStream is) throws Exception {
		Map<String, ResourceProperties> res = new HashMap<String, ResourceProperties>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder parser = factory.newDocumentBuilder();
		Document doc = parser.parse(is);
		NodeList nl = doc.getElementsByTagNameNS(DAV_NS, "response"); //$NON-NLS-1$
		for (int i = 0; i < nl.getLength(); i++) {
			Element oneElem = (Element) nl.item(i);
			NodeList resName = oneElem.getElementsByTagNameNS(DAV_NS, "href"); //$NON-NLS-1$
			assert (resName.getLength() == 1);
			String bareName = extractOverlap(base, URLDecoder.decode(resName.item(0).getTextContent(), "UTF-8"));
			if (bareName.trim().length() > 0) {
				ResourceProperties props = new ResourceProperties();
				NodeList propList = oneElem.getElementsByTagNameNS(DAV_NS, "resourcetype"); //$NON-NLS-1$
				assert (propList.getLength() == 1);
				NodeList resTypeList = ((Element) propList.item(0)).getElementsByTagNameNS(DAV_NS, "collection"); //$NON-NLS-1$
				assert (resTypeList.getLength() < 2);
				if (resTypeList.getLength() == 1) {
					props.setDirectory(true);
				}
				propList = oneElem.getElementsByTagNameNS(DAV_NS, "creationdate"); //$NON-NLS-1$
				if (propList.getLength() > 0) {
					props.setCreationDate(propList.item(0).getTextContent());
				}
				propList = oneElem.getElementsByTagNameNS(DAV_NS, "getlastmodified"); //$NON-NLS-1$
				if (propList.getLength() > 0) {
					props.setLastModifiedDate(propList.item(0).getTextContent());
				}
				String normBase = base.trim().endsWith("/") ? base.trim() : base.trim() + "/"; //$NON-NLS-1$ //$NON-NLS-2$
				props.setBase(normBase);
				res.put(bareName, props);
			}
		}
		return res;
	}

	private static String extractOverlap(String base, String extension) {
		String res = null;
		int pos = 0;
		int i = 1;
		boolean done = false;
		while (i < extension.length() && !done) {
			pos = base.lastIndexOf(extension.substring(0, i));
			if (pos == -1) {
				res = extension.substring(i - 1);
				done = true;
			}
			i++;
		}
		// If we got to the end of the loop above but !done,
		// that means the entire extension string matched in
		// base, so remove the entire extension contents
		if (!done) {
			res = ""; //$NON-NLS-1$
		}
		if (res.startsWith("/") && res.length() > 1) { //$NON-NLS-1$
			res = res.substring(1);
		}
		// System.out.println("Returning " + res + " from " + base + ", " +
		// extension);
		return res;
	}

	public static String parseISODateFormat(String dateStr) {

		if (dateStr == null)
			return null;

		String val = dateStr;

		try {
			if (dateStr.indexOf('T') == 10 && dateStr.endsWith("Z")) {
				Date date = CREATION_DATE_FORMAT.parse(dateStr);
				val = GUNVOR_TOOLS_DATE_FORMAT.format(date);

			} else if (dateStr.indexOf('T') == 10) {
				Date date = LAST_MODIFIED_DATE_FORMAT.parse(dateStr);
				val = GUNVOR_TOOLS_DATE_FORMAT.format(date);
			}

		} catch (ParseException e) {

		}

		return val;
	}
}
