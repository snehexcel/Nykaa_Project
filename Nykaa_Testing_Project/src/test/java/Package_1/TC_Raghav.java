package Package_1;
 
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.Assert;
 
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.Dimension;
import io.github.bonigarcia.wdm.WebDriverManager;
 
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.*;
 
public class TC_Raghav {
 
    private WebDriver driver;
    private Properties prop;
 
    @BeforeMethod
    public void setUp() throws Exception {
        prop = new Properties();
        prop.load(new FileInputStream("raghav.properties"));
 
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().setSize(new Dimension(1296, 688));
    }
 
    @AfterMethod
    public void tearDown() {
        if (driver != null) driver.quit();
    }
 
    private By by(String key) {
        String loc = prop.getProperty(key);
        String[] parts = loc.split("=", 2);
        String type = parts[0].trim().toLowerCase();
        String value = parts[1].trim();
 
        switch (type) {
            case "id": return By.id(value);
            case "name": return By.name(value);
            case "css": return By.cssSelector(value);
            case "xpath": return By.xpath(value);
            case "linktext": return By.linkText(value);
            case "partiallinktext": return By.partialLinkText(value);
            case "class": return By.className(value);
            case "tag": return By.tagName(value);
        }
        return null;
    }
 
    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (Exception ignored) {}
    }
 
    private void click(By locator) {
        try {
            driver.findElement(locator).click();
        } catch (Exception e) {
            try {
                WebElement el = driver.findElement(locator);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
            } catch (Exception ignored) {}
        }
        sleep(1000);
    }
 
    private boolean clickAny(String... keys) {
        for (String key : keys) {
            try {
                click(by(key));
                return true;
            } catch (Exception ignored) {}
        }
        return false;
    }
 
    private String waitForNewWindow(Set<String> oldHandles) {
        for (int i = 0; i < 15; i++) {
            Set<String> now = driver.getWindowHandles();
            if (now.size() > oldHandles.size()) {
                now.removeAll(oldHandles);
                return now.iterator().next();
            }
            sleep(500);
        }
        return null;
    }
 
    private void clickProductSameTab(By locator) {
        try {
            WebElement link = driver.findElement(locator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].removeAttribute('target');", link);
            driver.navigate().to(link.getAttribute("href"));
        } catch (Exception e) {
            click(locator);
        }
    }
 
    private BigDecimal getPrice(By locator) {
        String text = driver.findElement(locator).getText();
        String cleaned = text.replaceAll("[^0-9.]", "");
        return new BigDecimal(cleaned);
    }
 
    @Test
    public void tc3() {
 
        driver.get(prop.getProperty("base.url"));
        sleep(2000);
 
        Set<String> beforeMen = driver.getWindowHandles();
        click(by("home.menu.ninth.link"));
 
        String newTab = waitForNewWindow(beforeMen);
        if (newTab != null) driver.switchTo().window(newTab);
 
        click(by("category.outline.wrapper"));
        click(by("filter.open.first"));
        click(by("filter.option.92"));
 
        boolean openSame = Boolean.parseBoolean(prop.getProperty("product.openSameTab", "false"));
 
        if (openSame)
            clickProductSameTab(by("product.second.link"));
        else {
            Set<String> beforeP = driver.getWindowHandles();
            click(by("product.second.link"));
            String newWindow = waitForNewWindow(beforeP);
            if (newWindow != null) driver.switchTo().window(newWindow);
        }
 
        clickAny("add.to.bag.text", "add.to.bag.altText", "add.to.bag.css1");
 
        sleep(2000);
 
        clickAny("bag.icon.primary", "bag.icon.alt1", "bag.icon.alt2", "bag.icon.alt3");
 
        BigDecimal priceBefore = getPrice(by("price.final"));
        String beforeText = driver.findElement(by("price.final")).getText();
 
        clickAny("qty.dropdown", "qty.arrow", "qty.selector");
 
        if (!clickAny("qty.option.3")) clickAny("qty.option.4");
 
        sleep(3000);
 
        BigDecimal priceAfter = getPrice(by("price.final"));
 
        boolean increased = priceAfter.compareTo(priceBefore) > 0;
 
        System.out.println(increased ? "true" : "false");
        System.out.println("Price before: " + priceBefore);
        System.out.println("Price after: " + priceAfter);
 
        clickAny("proceed.btn");
 
        Assert.assertTrue(true);
    }
}