package ua.com.namely;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Test {

    private WebDriver driver;

    @org.junit.jupiter.api.Test
    public void test() { 
        driver= new ChromeDriver();
        driver.get("http://namesite.test/namedetails/tsvitko/");
        Assertions.assertFalse(driver.getTitle().isEmpty());
        driver.quit();
    }

    @org.junit.jupiter.api.Test
    public void testStatus() {

        // Initialize WebDriver (Chrome in this case)
        WebDriver driver = new ChromeDriver();

        // Open a website
        driver.get("http://namesite.test/namedetails/tsvitko/");

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
}