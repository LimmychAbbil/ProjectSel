package ua.com.namely;

import lombok.extern.slf4j.Slf4j;
import ua.com.namely.model.Page;
import ua.com.namely.model.PageType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

@Slf4j
public class SiteMapReader {

    public static List<Page> getSiteMapURLs() throws Exception {
        List<Page> siteMapURLs = new ArrayList<>();

        String mainURL = "https://namely.com.ua"; //TODO reuse application properties (& maven profile)
        String resourse = "/sitemap.xml"; //TODO can be hardcoded in static final variable

        HttpRequest httpRequest = HttpRequest.newBuilder(new URI(mainURL + resourse)).GET().build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());

        DocumentBuilder builder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
        Document xmlDoc = null;
        try {
            xmlDoc = builder.parse(new ByteArrayInputStream(response.body().getBytes(StandardCharsets.UTF_8)));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }

        NodeList nodeList = xmlDoc.getElementsByTagName("url");

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String location = node.getChildNodes().item(1).getTextContent();
            PageType type = getPageTypeByURL(location);
            siteMapURLs.add(new Page(location, type));
        }

        return siteMapURLs;
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
