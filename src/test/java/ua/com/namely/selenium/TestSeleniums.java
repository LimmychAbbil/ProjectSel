package ua.com.namely.selenium;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import ua.com.namely.SiteMapReader;
import ua.com.namely.model.Lang;
import ua.com.namely.model.Page;
import ua.com.namely.model.PageType;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@EnabledIfSystemProperty(named = TestSeleniums.MAVEN_PROFILE_KEY, matches = "true")
@Slf4j
public class TestSeleniums {

    protected static final String MAVEN_PROFILE_KEY = "projectSel.selenium.enabled";

    private static List<Page> pageList;

    private static WebDriver driver;

    @BeforeAll
    public static void setUp() throws Exception {
        pageList = SiteMapReader.getSiteMapURLs();
        Collections.shuffle(pageList);
    }

    @BeforeEach
    public void setUpDriver() {
        ChromeOptions options = new ChromeOptions();
        if (!"true".equalsIgnoreCase(System.getProperty("browser.visible"))) {
            options.addArguments("--headless");
            options.addArguments("--nogpu");
            options.addArguments("--disable-gpu");
        }

        options.addArguments("--enable-javascript");
        options.addArguments("--user-agent=Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:132.0) Gecko/20100101 Firefox/132.0");

        driver = new ChromeDriver(options);
    }

    @Test
    void testLikeTheNameFromNamePageOpensLikedWindow() {
        Page namePage = pageList.stream().filter(page -> page.getPageType() == PageType.NAME).findAny().orElseThrow();
        findLikeButtonAndClick(namePage.getLocation());
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait.until(d -> d.findElement(By.className("modal-content")).isDisplayed());
    }

    @Test
    void testLikeIsKeptOnNewTab() {
        Page namePage = pageList.stream().filter(page -> page.getPageType() == PageType.NAME).findAny().orElseThrow();
        findLikeButtonAndClick(namePage.getLocation());

        driver.switchTo().newWindow(WindowType.TAB);
        driver.get(namePage.getLocation());

        WebElement heartElementDiv = driver.findElement(By.className("favorites-header"));
        Assertions.assertEquals("1", heartElementDiv.getText(),
                "There should be 1 liked name, but actual value of heart element dir was "
                        + heartElementDiv.getText());

    }

    @Test
    void testMainPageContainsBlogBlock() {
        String blogHeaderUA = "Статті блогу";
        driver.get(pageList.stream().filter(page ->
                page.getPageType().equals(PageType.MAIN) && page.getLanguage().equals(Lang.UA))
                .findFirst().get().getLocation());

        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        WebElement footer = wait.until(d -> d.findElement(By.className("footer-item")));
        Assertions.assertTrue(footer.getText().contains(blogHeaderUA),
                "Footer div should have contained " + blogHeaderUA + " but was " + footer.getText());
        WebElement linksDiv = footer.findElement(By.className("links"));
        List<WebElement> links = linksDiv.findElements(By.tagName("a"));
        Assertions.assertFalse(links.isEmpty(), "Expected blog links is not empty");
    }

    @ParameterizedTest
    @EnumSource(value = PageType.class, names = {"MAIN", "GENDER", "ALPHABET"})
    void testPageContainsSearch(PageType pageType) {
        String pageURL = pageList.stream()
                .filter(page -> page.getPageType() == pageType && page.getLanguage() == Lang.UA)
                .findAny().get().getLocation();

        driver.get(pageURL);

        WebElement searchDiv = driver.findElement(By.id("app-search"));

        Assertions.assertEquals(1, searchDiv.findElements(By.tagName("input")).size());
        Assertions.assertEquals(1, searchDiv.findElements(By.tagName("button")).size());
    }

    @Test
    void testSearchForRandomNameReturnsThatName() {
        Page mainPage = pageList.stream()
                .filter(page -> page.getPageType() == PageType.MAIN && page.getLanguage() == Lang.UA)
                .findAny().get();

        List<Page> allUaNamePages = pageList.stream()
                .filter(page -> page.getPageType() == PageType.NAME && page.getLanguage() == Lang.UA).toList();


        Page randomNamePage = allUaNamePages.get((int) Math.round(Math.random() * allUaNamePages.size()));
        driver.get(randomNamePage.getLocation());
        String name = driver.findElement(By.id("app")).getText();

        driver.switchTo().newWindow(WindowType.TAB).get(mainPage.getLocation());
        WebElement searchDiv = driver.findElement(By.id("app-search"));
        WebElement searchTextInput = searchDiv.findElement(By.tagName("input"));
        WebElement searchButton = searchDiv.findElement(By.tagName("button"));
        searchTextInput.sendKeys(name);
        searchButton.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        WebElement searchResultListOL = wait.until(driver -> driver.findElement(By.id("app-search")).findElement(By.tagName("ol")));

        Assertions.assertNotNull(searchResultListOL,
                "app-search div result doesn't contain element for name " + name);
        List<WebElement> searchResultListLI = searchResultListOL.findElements(By.tagName("li"));
        Assertions.assertTrue(!searchResultListLI.isEmpty() && searchResultListLI.size() <= 20,
                "We expect at least 1 and no more that 20 search results for name " + name);
        Assertions.assertTrue(searchResultListLI.get(0).getText().contains(name),
                "The first <li> element in search result should contain " + name);
    }

    @Test
    void testMapDivContainsMapDescription() {
        List<Page> namePages = pageList.stream().filter(page -> page.getPageType() == PageType.NAME).toList();

        WebElement mapDiv = null;
        int i = 0;
        do {
            Page namePage = namePages.get(i++);
            driver.get(namePage.getLocation());
            try {
                mapDiv = driver.findElement(By.id("app-country-map"));
            } catch (WebDriverException e) {
                log.debug("No map found for " + namePage.getLocation() + ", continuing test");
            }
        } while (mapDiv == null || i == namePages.size());

        Assertions.assertNotNull(mapDiv, "Name map div with id 'app-country-map' not found on every name page");

        WebElement ul = mapDiv.findElement(By.tagName("ul"));
        List<WebElement> liList = ul.findElements(By.tagName("li"));
        Assertions.assertNotNull(liList, "UL tag in map div block doesn't contain any li element");
        Assertions.assertFalse(liList.isEmpty(), "UL tag in map div block doesn't contain any li element");
        liList.forEach(webElement ->
                Assertions.assertEquals("list-group-item", webElement.getDomAttribute("class"),
                        "LI element class for map description is not 'list-group-item'"));

    }

    @Test
    @Disabled(value = "This is just an example")
    public void testExample() {

        // Initialize WebDriver (Chrome in this case)
        driver = new ChromeDriver();

        // Open a website
        driver.get("http://namely.com.ua/namedetails/tsvitko/");

        // Set up WebDriverWait
        WebDriverWait wait; // 10 seconds timeout
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Wait for an element to be visible on the page
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
            System.out.println("Page loaded successfully. Element found.");
        } catch (Exception e) {
            System.out.println("Page did not load properly or element not found.");
        } finally {
            // Clean up and close the browser
            driver.quit();
        }
    }

    @AfterEach
    public void closeSelenium() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void findLikeButtonAndClick(String location) {
        driver.get(location);

        Assertions.assertFalse(driver.findElement(By.className("modal-content")).isDisplayed());

        WebElement likeButton = driver.findElement(By.className("fa-heart-o"));

        Assertions.assertNotNull(likeButton);

        likeButton.click();
    }
}
