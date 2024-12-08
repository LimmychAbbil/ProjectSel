package ua.com.namely.selenium;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import ua.com.namely.SiteMapReader;
import ua.com.namely.model.Page;
import ua.com.namely.model.PageType;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@EnabledIfSystemProperty(named = TestSeleniums.MAVEN_PROFILE_KEY, matches = "true")
public class TestSeleniums {

    protected static final String MAVEN_PROFILE_KEY = "projectSel.selenium.enabled";

    private static List<Page> pageList;

    private WebDriver driver;

    @BeforeAll
    public static void setUp() throws Exception {
        pageList = SiteMapReader.getSiteMapURLs();
        Collections.shuffle(pageList);
    }

    @BeforeEach
    public void setUpDriver() {
        driver = new ChromeDriver();
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
        Assertions.assertEquals("1", heartElementDiv.getText());

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
