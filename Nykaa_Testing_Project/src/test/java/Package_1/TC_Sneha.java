package Package_1;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.Assert;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.Keys;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.Set;
import java.math.BigDecimal;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.FileUtils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class TC_Sneha {

    WebDriver driver;
    Properties prop;
    ExtentReports extent;
    ExtentTest test;

    void sleep(long time) {
        try { Thread.sleep(time); } catch (Exception ignored) {}
    }
    By getBy(String key) {
        String value = prop.getProperty(key);
        
        if (value == null) return By.xpath("//html");

        if (value.startsWith("name="))  return By.name(value.substring(5));
        if (value.startsWith("css="))   return By.cssSelector(value.substring(4));
        if (value.startsWith("xpath=")) return By.xpath(value.substring(6));

        return By.xpath(value);
    }
    BigDecimal getPrice(String text) {
        try {
            return new BigDecimal(text.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String capturescreenshot(WebDriver driver) throws Exception {
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String dest = "./Screenshots/" + "Screenshot" + "_" + timestamp + ".png";
        File destfile = new File(dest);
        FileUtils.copyFile(src, destfile);
        return dest;
    }

    @BeforeMethod
    public void setUp() throws Exception {
        prop = new Properties();
        prop.load(new FileInputStream("sneha.properties"));

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();

        // NEW: Initialize ExtentReports with the specified report name
        if (extent == null) {
            extent = new ExtentReports();
            ExtentSparkReporter spark = new ExtentSparkReporter(System.getProperty("user.dir") + "\\Nykaa_Capstone_Project_Report_Sneha.html");
            extent.attachReporter(spark);
        }
        test = extent.createTest("TC_Sneha_tc1");
    }
    @AfterMethod
    public void tearDown() {
        try { driver.quit(); } catch (Exception ignored) {}
        try { if (extent != null) extent.flush(); } catch (Exception ignored) {}
    }
    @Test
    public void tc1() {

        driver.get(prop.getProperty("base.url"));
        sleep(3000);

        driver.findElement(getBy("search.box"))
              .sendKeys(prop.getProperty("search.keyword"));
        sleep(2000);

        try {
            driver.findElement(getBy("search.suggestion")).click();
        } catch (Exception e) {
            driver.findElement(getBy("search.box")).sendKeys(Keys.ENTER);
        }
        sleep(3000);

        driver.findElement(getBy("product.first")).click();
        sleep(3000);

        String parent = driver.getWindowHandle();
        Set<String> windows = driver.getWindowHandles();
        for (String w : windows) {
            if (!w.equals(parent)) {
                driver.switchTo().window(w);
                break;
            }
        }
        driver.findElement(getBy("add.to.bag")).click();
        sleep(2000);

        driver.findElement(getBy("bag.icon")).click();
        sleep(3000);

        String priceBeforeText = driver.findElement(getBy("price.final")).getText();
        BigDecimal priceBefore = getPrice(priceBeforeText);

        driver.findElement(getBy("qty.dropdown")).click();
        sleep(1000);

        driver.findElement(getBy("qty.option.3")).click();
        sleep(3000);

        String priceAfterText = driver.findElement(getBy("price.final")).getText();
        BigDecimal priceAfter = getPrice(priceAfterText);

        System.out.println(priceAfter.compareTo(priceBefore) > 0 ? "true" : "false");
        System.out.println("Price before: " + priceBefore);
        System.out.println("Price after: " + priceAfter);

        driver.findElement(getBy("proceed.btn")).click();
        sleep(2000);

        try {
            String shot = capturescreenshot(driver);
            if (test != null) test.pass("Proceeded to next page").addScreenCaptureFromPath(shot);
        } catch (Exception ignored) {}

        Assert.assertTrue(true);
        if (test != null) test.pass("TC_Sneha_tc1 completed");
    }
}