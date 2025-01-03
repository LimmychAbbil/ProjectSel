package ua.com.namely;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ua.com.namely.model.Page;
import ua.com.namely.model.PageType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TestSiteMapGets {

    private static List<Page> siteMap;

    @BeforeAll
    public static void readSiteMap() throws Exception {
        siteMap = SiteMapReader.getSiteMapURLs();
    }

    @Test
    void readMainPage() {
        siteMap.stream().filter(page -> page.getPageType().equals(PageType.MAIN)).findFirst().ifPresentOrElse(
                (page -> {
                    HttpResponse<String> response = executeGET(page);
                    Assertions.assertEquals(200, response.statusCode());
                }), Assertions::fail);
    }

    @Test
    void readAlphabetPages() {
        AtomicInteger count = new AtomicInteger(0);
        siteMap.stream().filter(page -> page.getPageType().equals(PageType.ALPHABET)).parallel().forEach(
                (page -> {
                    count.incrementAndGet();
                    HttpResponse<String> response = executeGET(page);
                    Assertions.assertEquals(200, response.statusCode(),
                            "Response code was not 200 for page " + page.getLocation());
                }));

        Assertions.assertEquals(2 * 30 + 2 * 24, count.get());
    }

    @ParameterizedTest
    @ValueSource(ints = {15})
    void readRandomNamePages(int number) {
        Set<Integer> usedIndexes = new HashSet<>();
        List<Page> namePages = siteMap.stream().filter(page -> page.getPageType().equals(PageType.NAME)).toList();

        for (int i = 0; i < number; i++) {
            int index;
            do {
                 index = (int) Math.round(Math.random() * namePages.size());
            }
            while (usedIndexes.contains(index));

            usedIndexes.add(index);
            HttpResponse<String> response = executeGET(namePages.get(index));
            Assertions.assertEquals(200, response.statusCode(),
                    "Response code was not 200 for page " + namePages.get(index).getLocation());
        }
    }

    private HttpResponse<String> executeGET(Page page) {
        log.info("GET for {}", page);
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(new URI(page.getLocation())).GET().build();
            return HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}