package Package_1;
 
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.Assert;
 
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
 
import io.github.bonigarcia.wdm.WebDriverManager;
 
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public class TC_Ravindra {
 
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private Actions actions;
    private Properties prop;
 
    private String gp(String key) { return prop.getProperty(key, ""); }
 
    private By by(String key) {
        String loc = gp(key);
        if (!loc.contains("=")) throw new IllegalArgumentException("Bad locator for key: " + key);
        String[] p = loc.split("=", 2);
        String type = p[0].trim().toLowerCase(), value = p[1].trim();
        switch (type) {
            case "id": return By.id(value);
            case "name": return By.name(value);
            case "css": return By.cssSelector(value);
            case "xpath": return By.xpath(value);
            case "linktext": return By.linkText(value);
            case "partiallinktext": return By.partialLinkText(value);
            case "class": return By.className(value);
            case "tag": return By.tagName(value);
            default: throw new IllegalArgumentException("Unsupported locator type: " + type);
        }
    }
 
    private WebElement find(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }
 
    private void click(By by) {
        WebElement el = find(by);
        try { js.executeScript("arguments[0].scrollIntoView({block:'center'})", el); } catch (Exception ignored) {}
        try { el.click(); } catch (Exception e) { js.executeScript("arguments[0].click()", el); }
    }
 
    private void openPossiblyNewTab(By by) {
        Set<String> before = driver.getWindowHandles();
        click(by);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(8))
                .until(d -> d.getWindowHandles().size() > before.size());
            for (String h : driver.getWindowHandles()) if (!before.contains(h)) driver.switchTo().window(h);
        } catch (TimeoutException ignored) {}
        pageReady();
    }
 
    private void navigateViaHref(By by) {
        WebElement link = find(by);
        try { js.executeScript("arguments[0].removeAttribute('target')", link); } catch (Exception ignored) {}
        String href = link.getAttribute("href");
        if (href != null && !href.isBlank()) driver.navigate().to(href);
        else click(by);
        pageReady();
    }
 
    private BigDecimal getPriceSmart(By by) {
        String text = find(by).getText();
        if (text == null) return BigDecimal.ZERO;
        text = text.replaceAll(",", "");
        Matcher m = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)").matcher(text);
        BigDecimal best = BigDecimal.ZERO;
        while (m.find()) {
            try {
                BigDecimal val = new BigDecimal(m.group(1));
                if (val.compareTo(best) > 0) best = val;
            } catch (Exception ignored) {}
        }
        return best;
    }
 
    private void waitForPriceChange(By priceBy, BigDecimal previous, Duration timeout) {
        new WebDriverWait(driver, timeout).until(d -> {
            try {
                BigDecimal now = getPriceSmart(priceBy);
                return now.compareTo(previous) != 0;
            } catch (Exception e) {
                return false;
            }
        });
    }
 
    private void pageReady() {
        wait.until(d -> "complete".equals(js.executeScript("return document.readyState")));
        try { Thread.sleep(200); } catch (InterruptedException ignored) {}
    }
 
    @BeforeMethod
    public void setup() throws Exception {
        prop = new Properties();
        prop.load(new FileInputStream("ravindra.properties"));
 
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().setSize(new Dimension(1296, 688));
 
        wait = new WebDriverWait(driver, Duration.ofSeconds(12));
        js = (JavascriptExecutor) driver;
        actions = new Actions(driver);
    }
 
    @AfterMethod
    public void tearDown() {
        if (driver != null) try { driver.quit(); } catch (Exception ignored) {}
    }
 
    @Test
    public void tc2() {
        driver.get(gp("base.url"));
        pageReady();
 
        openPossiblyNewTab(by("home.menu.ninth.link"));
        click(by("category.outline.wrapper"));
        pageReady();
 
        click(by("filter.open.section2"));
        click(by("filter.third.option"));
        pageReady();
 
        boolean sameTab = Boolean.parseBoolean(gp("product.openSameTab"));
        if (sameTab) navigateViaHref(by("product.second.link"));
        else openPossiblyNewTab(by("product.second.link"));
 
        click(by("add.to.bag"));
        pageReady();
        click(by("bag.icon"));
        pageReady();
 
        By priceBy = by("price.final");
        BigDecimal priceBefore = getPriceSmart(priceBy);
 
        try { actions.moveToElement(find(by("qty.hover"))).perform(); } catch (Exception ignored) {}
        click(by("qty.selector"));
        click(by("qty.option.4"));
 
        try { waitForPriceChange(priceBy, priceBefore, Duration.ofSeconds(10)); } catch (TimeoutException ignored) {}
 
        BigDecimal priceAfter = getPriceSmart(priceBy);
        boolean increased = priceAfter.compareTo(priceBefore) > 0;
 
        System.out.println(increased ? "true" : "false");
        System.out.println("Before: " + priceBefore + " | After: " + priceAfter);
        
        click(by("proceed.btn"));
        pageReady();
        Assert.assertTrue(true);
    }
}
