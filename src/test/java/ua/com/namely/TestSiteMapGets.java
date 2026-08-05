package ua.com.namely;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import ua.com.namely.model.Lang;
import ua.com.namely.model.Page;
import ua.com.namely.model.PageType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class TestSiteMapGets {

	private static final int DEFAULT_NUMBER_OF_RANDOM_PAGES = 15;

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
    void readAlphabetPages() throws Exception {
        readSampleAlphabetPages(Lang.UA, 10);
        readSampleAlphabetPages(Lang.EN, 10);
    }

    private void readSampleAlphabetPages(Lang language, int sampleSize) {
        List<Page> alphabetPages = siteMap.stream()
                .filter(page -> page.getPageType().equals(PageType.ALPHABET) && page.getLanguage().equals(language))
                .collect(Collectors.toList());

        Assertions.assertFalse(alphabetPages.isEmpty(), "No alphabet pages were found for language " + language);

        int actualSampleSize = Math.min(sampleSize, alphabetPages.size());
        Set<Integer> selectedIndexes = new HashSet<>();
        Random random = new Random();

        while (selectedIndexes.size() < actualSampleSize) {
            selectedIndexes.add(random.nextInt(alphabetPages.size()));
        }

        selectedIndexes.stream()
                .map(alphabetPages::get)
                .forEach(page -> {
                    HttpResponse<String> response = executeGET(page);
                    Assertions.assertEquals(200, response.statusCode(),
                            "Response code was not 200 for page " + page.getLocation());
                });
    }

    @ParameterizedTest
	@MethodSource("getParameterForNumberOfRandomNamePages")
    void readRandomNamePages(int number) {
        List<Page> namePages = siteMap.stream().filter(page -> page.getPageType().equals(PageType.NAME)).toList();
        Assertions.assertFalse(namePages.isEmpty(), "No name pages found in the sitemap");

        int checks = Math.min(number, namePages.size());
        Set<Integer> usedIndexes = new HashSet<>();
        Random random = new Random();

        for (int i = 0; i < checks; i++) {
            int index;
            do {
                index = random.nextInt(namePages.size());
            } while (usedIndexes.contains(index));

            usedIndexes.add(index);
            HttpResponse<String> response = executeGET(namePages.get(index));
            Assertions.assertEquals(200, response.statusCode(),
                    "Response code was not 200 for page " + namePages.get(index).getLocation());
        }
    }

	private static Stream<Arguments> getParameterForNumberOfRandomNamePages() {
		int parameterValue = Integer.getInteger("testing.names.number", DEFAULT_NUMBER_OF_RANDOM_PAGES);
		return Stream.of(Arguments.of(parameterValue));
	}

    @Test
    void readBlogPages() {
        siteMap.stream().filter(page -> page.getPageType().equals(PageType.BLOG))
                .filter(page -> page.getLocation().endsWith("/")).parallel().forEach(
                (page -> {
                    HttpResponse<String> response = executeGET(page);
                    Assertions.assertEquals(200, response.statusCode(),
                            "Response code was not 200 for page " + page.getLocation());
                }));;
    }

    private HttpResponse<String> executeGET(Page page) {
        try {
            String urlWithAnalytics = appendAnalyticsParam(page.getLocation());
            log.info("GET for {} -> {}", page.getLocation(), urlWithAnalytics);
            HttpRequest httpRequest = HttpRequest.newBuilder(new URI(urlWithAnalytics)).GET().build();
            return HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String appendAnalyticsParam(String url) {
        String analytics = System.getProperty("testing.analytics", "utm_source=test-suite");
        if (analytics == null || analytics.isEmpty()) {
            return url;
        }
        if (url.contains("?")) {
            String result = url + "&" + analytics;
            log.info("GET URL with analytics: {}", result);
            return result;
        } else {
            String result = url + "?" + analytics;
            log.info("GET URL with analytics: {}", result);
            return result;
        }
    }
}