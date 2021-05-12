package com.github.sudharsan_selvaraj;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class DriverProvider {

    private static ThreadLocal<ChromeDriver> driver = new ThreadLocal<ChromeDriver>();

    public static ChromeDriver init() {
        driver.set(new ChromeDriver(chromeOptions()));
        return getDriver();
    }

    public static ChromeDriver getDriver()
    {
        return driver.get();
    }

    private static ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        return options;
    }

}
