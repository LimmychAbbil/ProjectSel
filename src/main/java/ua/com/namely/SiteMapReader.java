package ua.com.namely;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ua.com.namely.model.Lang;
import ua.com.namely.model.Page;
import ua.com.namely.model.PageType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Slf4j
public class SiteMapReader {

    private static final String RESOURCE_MAP_URL_PATH = "sitemap.xml";

    private static String lookupUrl;

    public static List<Page> getSiteMapURLs() throws Exception {
        List<Page> siteMapURLs = new ArrayList<>();

        String siteMapBody = getSiteMapAsString();

        DocumentBuilder builder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
        Document xmlDoc = null;
        try {
            xmlDoc = builder.parse(new ByteArrayInputStream(siteMapBody.getBytes(StandardCharsets.UTF_8)));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }

        NodeList nodeList = xmlDoc.getElementsByTagName("url");

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String locationText = node.getChildNodes().item(1).getTextContent();
			URI locationURI = new URI(locationText);
			String location = lookupUrl + locationURI.getPath().substring(1); // Remove leading slash from path
            
            PageType type = getPageTypeByURL(location);
            Lang language = getLang(location);
            siteMapURLs.add(new Page(location, type, language));
        }

        return siteMapURLs;
    }

    protected static String getSiteMapAsString() throws Exception {
        Properties properties = new Properties();
        properties.load(SiteMapReader.class.getResourceAsStream("/application.properties"));
        lookupUrl = java.lang.String.valueOf(
			Optional.of(properties.get("site.main.url"))
			.orElseThrow(() -> new IllegalStateException("Property 'site.main.url' not found in application.properties")));

        HttpRequest httpRequest = HttpRequest.newBuilder(new URI(lookupUrl + RESOURCE_MAP_URL_PATH)).GET().build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }


    private static Lang getLang(String location) {
        return location.contains("/en/") ? Lang.EN : Lang.UA;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getSiteMapURLs());
    }


    private static PageType getPageTypeByURL(String location) {
        if (location == null || location.isEmpty()) {
            return PageType.UNKNOWN;
        } else if (location.endsWith("boys/") || location.endsWith("girls/")) {
            return PageType.GENDER;
        } else if (location.contains("/boys/") || location.contains("/girls/")) {
            return PageType.ALPHABET;
        } else if (location.contains("/blog/") || location.endsWith("/blog")) {
            return PageType.BLOG;
        } else if (location.equals(lookupUrl) || location.equals(lookupUrl + "en/")){
            return PageType.MAIN;
        } else if (location.contains("/namedetails/")) {
            return PageType.NAME;
        } else {
            return PageType.UNKNOWN;
        }
    }
}
