package com.github.sudharsan_selvaraj.cdp;

import com.github.sudharsan_selvaraj.SeleniumTest;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.devtools.v85.page.Page;
import org.openqa.selenium.devtools.v91.browser.Browser;
import org.openqa.selenium.devtools.v91.browser.model.*;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

public class BrowserTest extends SeleniumTest {

    /**
     * Close browser gracefully.
     *
     * @see <a href="https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-close">Browser.close</a>
     */
    @Test
    public void browserCloseTest() {
        devTools.send(Browser.close());
        sleep(1000);

        try {
            driver.getTitle();
            fail("Exception not thrown after destroying browser session using close command");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assertTrue(e.getMessage().contains("chrome not reachable"));
        }
    }

    /**
     * Returns browser version information.
     *
     * @see <a href="https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getVersion">Browser.getVersion</a>
     */
    @Test
    public void getVersionTest() {
        Browser.GetVersionResponse response = devTools.send(Browser.getVersion());

        assertNotEquals(response.getJsVersion(), "");
        assertEquals(response.getProtocolVersion(), "1.3");
        assertNotEquals(response.getRevision(), "");
        assertTrue(response.getUserAgent().contains("Chrome/"));
        assertTrue(response.getProduct().contains("Chrome/"));
    }

    /**
     * Cancel a download if in progress.
     * <p>
     * <b>Note:</b>
     * Currently disabled because of error
     * <pre>{"id":7,"error":{"code":-32601,"message":"'Browser.cancelDownload' wasn't found"}</pre>
     * <p/>
     *
     * @see <a href="https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-cancelDownload">Browser.cancelDownload</a>
     */
    @Test(enabled = false)
    public void cancelDownload() {

        String downloadedFileName = "chromedriver_mac64.zip";
        deleteFromDownloadFolder(downloadedFileName);

        devTools.send(Browser.setDownloadBehavior(
                Browser.SetDownloadBehaviorBehavior.ALLOW,
                Optional.empty(),
                Optional.of(getDownloadDirectory()),
                Optional.of(true)
        ));

        devTools.send(Page.enable());
        devTools.addListener(Page.downloadWillBegin(), response -> {
            devTools.send(Browser.cancelDownload(
                    response.getGuid(),
                    Optional.empty()
            ));
        });

        driver.get("https://chromedriver.storage.googleapis.com/index.html?path=91.0.4472.19/");
        findElement(By.linkText(downloadedFileName)).click();
        sleep(10000);
    }

    /**
     * Crashes browser on the main thread.
     *
     * @see <a href="https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-crash">Browser.crash</a>
     */
    @Test
    public void crash() {
        try {
            devTools.send(Browser.crash());
            fail("Exception not thrown after crashing browser session using crash command");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("java.util.concurrent.TimeoutException"));
        }
    }

    /**
     * Invoke custom browser commands used by telemetry.
     * <p>
     * <b>Note:</b>
     * Currently disabled because of error
     * <pre>{"error":{"code":-32600,"message":"Browser command not supported. BrowserCommandId: closeTabSearch"}}</pre>
     * <p/>
     *
     * @see <a href="https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-executeBrowserCommand">Browser.executeBrowserCommand</a>
     */
    @Test(enabled = false)
    public void executeBrowserCommand() {
        devTools.send(Browser.executeBrowserCommand(BrowserCommandId.CLOSETABSEARCH));
    }

    /**
     * Returns the command line switches for the browser process if, and only if --enable-automation is on the commandline.
     *
     * @see <a href="https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getBrowserCommandLine">getBrowserCommandLine</a>
     */
    @Test
    public void getBrowserCommandLine() {
        List<String> arguments = devTools.send(Browser.getBrowserCommandLine());
        assertEquals(arguments.get(arguments.size() - 1), "data:,");
    }

    /**
     * Get a Chrome histogram by name.
     *
     * @see <a href="https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getHistogram">getHistogram</a>
     */
    @Test
    public void getHistogram() {
        Histogram histogram = devTools.send(Browser.getHistogram("API.StorageAccess.AllowedRequests", Optional.empty()));

        assertEquals(histogram.getName(), "API.StorageAccess.AllowedRequests");
    }

    /**
     * Get Chrome histograms.
     *
     * @see <a href="https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getHistograms">getHistograms</a>
     */
    @Test
    public void getHistograms() {
        List<Histogram> histograms = devTools.send(Browser.getHistograms(Optional.empty(), Optional.empty()));

        assertTrue(histograms.stream().map(histogram -> histogram.getName()).collect(Collectors.toList()).contains("API.StorageAccess.AllowedRequests"));
    }

    /**
     * Get position and size of the browser window.
     *
     * @see <a href="https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getWindowBounds">getWindowBounds</a>
     */
    @Test
    public void getWindowBounds() {
        driver.manage().window().setSize(new Dimension(1200, 700));
        Bounds bounds = devTools.send(Browser.getWindowBounds(new WindowID(1)));

        assertEquals(bounds.getHeight().get().intValue(), 700);
        assertEquals(bounds.getWidth().get().intValue(), 1200);
        assertEquals(bounds.getWindowState().get().toString(), "normal");
    }

    /**
     * Get the browser window that contains the devtools target.
     *
     * @see <a href="https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getWindowForTarget">getWindowForTarget</a>
     */
    @Test
    public void getWindowForTarget() {
        driver.manage().window().setSize(new Dimension(1200, 700));
        Browser.GetWindowForTargetResponse response = devTools.send(Browser.getWindowForTarget(Optional.empty()));

        assertEquals(response.getBounds().getHeight().get().intValue(), 700);
        assertEquals(response.getBounds().getWidth().get().intValue(), 1200);
        assertEquals(response.getBounds().getWindowState().get().toString(), "normal");
        assertEquals(response.getWindowId().toString(), "1");
    }

    /**
     * Grant specific permissions to the given origin and reject all others.
     *
     * @see <a href="https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-grantPermissions">grantPermissions</a>
     */
    @Test
    public void grantPermissions() {
        By permissionRequestButton = By.xpath(".//button[text()='Click here to allow access to microphone identifiers']");

        driver.get("https://mictests.com/");
        findElement(By.cssSelector(".done_micDetectedOne"));
        assertTrue(findElement(permissionRequestButton).isDisplayed());

        devTools.send(
                Browser.grantPermissions(
                        Collections.singletonList(PermissionType.AUDIOCAPTURE),
                        Optional.empty(),
                        Optional.empty()
                )
        );
        driver.navigate().refresh();

        findElement(By.cssSelector(".done_micDetectedOne"));
        assertEquals(driver.findElements(permissionRequestButton).size(), 0);
    }

    /**
     * Reset all permission management for all origins.
     *
     * @see <a href="https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-resetPermissions">resetPermissions</a>
     */
    @Test
    public void resetPermissions() {
        By permissionRequestButton = By.xpath(".//button[text()='Click here to allow access to microphone identifiers']");
        devTools.send(
                Browser.grantPermissions(
                        Collections.singletonList(PermissionType.AUDIOCAPTURE),
                        Optional.empty(),
                        Optional.empty()
                )
        );

        driver.get("https://mictests.com/");
        findElement(By.cssSelector(".done_micDetectedOne"));
        assertEquals(driver.findElements(permissionRequestButton).size(), 0);

        devTools.send(Browser.resetPermissions(Optional.empty()));
        driver.navigate().refresh();
        findElement(By.cssSelector(".done_micDetectedOne"));
        assertTrue(findElement(permissionRequestButton).isDisplayed());
    }

    /**
     * Set dock tile details, platform-specific.
     *
     * @see <a href="https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-setDockTile">setDockTile</a>
     */
    @Test
    public void setDockTile() throws IOException {

        File icon = new File(getClass()
                .getClassLoader()
                .getResource("testninja_logo.png")
                .getFile());
        byte[] fileContent = FileUtils.readFileToByteArray(icon);
        String encodedString = Base64
                .getEncoder()
                .encodeToString(fileContent);
        devTools.send(Browser.setDockTile(Optional.of("TestNinja"), Optional.of(encodedString)));
        sleep(10000);
    }

    /**
     * Set the behavior when downloading a file.
     *
     * @see <a href="https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-setDownloadBehavior">setDownloadBehavior</a>
     */
    @Test
    public void setDownloadBehavior() {
        String downloadedFileName = "chromedriver_mac64.zip";
        deleteFromDownloadFolder(downloadedFileName);

        devTools.send(Browser.setDownloadBehavior(
                Browser.SetDownloadBehaviorBehavior.ALLOW,
                Optional.empty(),
                Optional.of(getDownloadDirectory()),
                Optional.of(true)
        ));

        driver.get("https://chromedriver.storage.googleapis.com/index.html?path=91.0.4472.19/");
        findElement(By.linkText(downloadedFileName)).click();
        sleep(15000);
        assertTrue(new File(getDownloadDirectory() + File.separator + downloadedFileName).exists());
    }

    /**
     * Set permission settings for given origin.
     *
     * @see <a href="https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-setPermission">setPermission</a>
     */
    @Test
    public void setPermission() {
        By permissionRequestButton = By.xpath(".//button[text()='Click here to allow access to microphone identifiers']");

        driver.get("https://mictests.com/");
        findElement(By.cssSelector(".done_micDetectedOne"));
        assertTrue(findElement(permissionRequestButton).isDisplayed());

        devTools.send(
                Browser.setPermission(
                        new PermissionDescriptor("microphone",
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()
                        ),
                        PermissionSetting.GRANTED,
                        Optional.empty(),
                        Optional.empty()
                )
        );
        driver.navigate().refresh();

        findElement(By.cssSelector(".done_micDetectedOne"));
        assertEquals(driver.findElements(permissionRequestButton).size(), 0);
    }

    /**
     * Set position and/or size of the browser window.
     *
     * @see <a href="https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-setWindowBounds">setWindowBounds</a>
     */
    @Test
    public void setWindowBounds() {
        devTools.send(Browser.setWindowBounds(new WindowID(1),
                new Bounds(Optional.empty(),
                        Optional.empty(),
                        Optional.of(1200),
                        Optional.of(700),
                        Optional.empty()))
        );

        assertEquals(driver.manage().window().getSize(), new Dimension(1200, 700));
    }
}
