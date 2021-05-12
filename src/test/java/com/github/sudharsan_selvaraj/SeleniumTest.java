package com.github.sudharsan_selvaraj;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;

import java.io.File;
import java.time.Duration;

public class SeleniumTest {

    protected ChromeDriver driver;
    protected DevTools devTools;

    @BeforeSuite
    public void downloadWebDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeMethod
    public void setupWebDriver() {
        driver = new ChromeDriver();
        devTools = driver.getDevTools();
        devTools.createSession();

        driver.get("https://www.google.com");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownDriver() {
       try {
           driver.quit();
       } catch (Exception e) {
           //ignore
       }
    }

    public void sleep(long millisecs) {
        try {
            Thread.sleep(millisecs);
        } catch (Exception e) {
            //ignore
        }
    }

    protected String getDownloadDirectory() {
        return System.getProperty("java.io.tmpdir");
    }

    protected void deleteFromDownloadFolder(String fileName) {
        new File(getDownloadDirectory() + File.separator + fileName).delete();
    }

    protected WebElement findElement(By by) {
        return new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(ExpectedConditions.presenceOfElementLocated(by));
    }
}
