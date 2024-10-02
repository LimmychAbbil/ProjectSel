package ua.com.namely;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
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
    private static final String MAIN_URL = "https://namely.com.ua/";

    public static List<Page> getSiteMapURLs() throws Exception {
        List<Page> siteMapURLs = new ArrayList<>();

        Properties properties = new Properties();
        properties.load(SiteMapReader.class.getResourceAsStream("/application.properties"));
        String lookupURL = String.valueOf(Optional.of(properties.get("site.main.url")).orElseThrow());

        HttpRequest httpRequest = HttpRequest.newBuilder(new URI(MAIN_URL + RESOURCE_MAP_URL_PATH)).GET().build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());

        DocumentBuilder builder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
        Document xmlDoc = null;
        try {
            xmlDoc = builder.parse(new ByteArrayInputStream(response.body().getBytes(StandardCharsets.UTF_8)));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }

        NodeList nodeList = xmlDoc.getElementsByTagName("url");

        boolean isSameSite = lookupURL.equals(MAIN_URL);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String location = node.getChildNodes().item(1).getTextContent();
            if (!isSameSite) {
                StringBuilder locationBuilder = new StringBuilder(lookupURL).append(location.substring(MAIN_URL.length()));
                location = locationBuilder.toString();
            }
            PageType type = getPageTypeByURL(location);
            siteMapURLs.add(new Page(location, type));
        }

        return siteMapURLs;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getSiteMapURLs());
    }


    private static PageType getPageTypeByURL(String location) {
        if (location == null || location.isEmpty()) {
            return PageType.UNKNOWN;
        } else if (location.endsWith("boys/") || location.endsWith("girls/")) {
            return PageType.GENDER;
        } else if (location.contains("/boys/") || location.contains("/girls")) {
            return PageType.ALPHABET;
        } else if (location.contains("/blog/") || location.endsWith("/blog")) {
            return PageType.BLOG;
        } else if (location.equals("https://namely.com.ua/") || location.equals("https://namely.com.ua/en/")){
            return PageType.MAIN;
        } else if (location.contains("/namedetails/")) {
            return PageType.NAME;
        } else {
            return PageType.UNKNOWN;
        }
    }
}
