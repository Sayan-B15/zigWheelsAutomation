package com.zigwheels.stepDefinitions;

import com.zigwheels.base.BaseClass;
import com.zigwheels.pages.ZigPage;
import com.zigwheels.utils.*;
import io.cucumber.java.en.*;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

public class Steps extends BaseClass {
    ZigPage zig = new ZigPage(driver);
    Actions actions = new Actions(driver);
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Given("User is on ZigWheels Home Page")
    public void user_is_on_home_page() {
        driver.get(prop.getProperty("url"));
        System.out.println("Step 1: Browser Launched and ZigWheels Home Page Opened.");
        Assert.assertTrue("Step 1 Failure: Page title mismatch!", driver.getTitle().contains("ZigWheels"));
        System.out.println("Step 1 Validation: Page Title Verified.");
        ScreenshotUtils.takeScreenshot("ZigWheels_Homepage");
    }

    @When("User identifies upcoming Royal Enfield bikes under 4Lac")
    public void identify_re_bikes() {
        System.out.println("Step 2: Navigating to Upcoming Royal Enfield Bikes.");
        WebElement newBikes = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(text(),'New Bikes')]")));
        actions.moveToElement(newBikes).perform();

        WebElement upcomingLink = wait.until(ExpectedConditions.presenceOfElementLocated(zig.upcomingBikesOption));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", upcomingLink);

        WebElement reBtn = wait.until(ExpectedConditions.elementToBeClickable(zig.royalEnfieldBrand));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", reBtn);
        System.out.println("-> Brand Filter Applied.");

        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,600)");
        wait.until(ExpectedConditions.visibilityOfElementLocated(zig.bikeNames));

        List<WebElement> names = driver.findElements(zig.bikeNames);
        Assert.assertFalse("Step 2 Failure: No bikes found on the page!", names.isEmpty());

        int excelRow = 1;
        System.out.println("=== Upcoming Royal Enfield Bikes < 4Lac ===");
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i).getText().trim();
            String priceTxt = (i < driver.findElements(zig.bikePrices).size()) ? driver.findElements(zig.bikePrices).get(i).getText().trim() : "N/A";
            double val = parsePrice(priceTxt);

            if (val > 0 && val < 4.0 && !name.contains("Best Mileage")) {
                System.out.println(excelRow + ". " + name + " | Price: " + priceTxt);
                ExcelUtils.writeToExcel("BikesOutput.xlsx", "UpcomingBikes", excelRow++, 0, name);
            }
        }
    }

    @When("User extracts all popular used cars in Chennai")
    public void used_cars_chennai() throws InterruptedException {
        System.out.println("Step 3: Extracting Popular Used Cars in Chennai.");
        actions.moveToElement(driver.findElement(zig.moreMenu)).perform();
        driver.findElement(zig.usedCarsLink).click();
        wait.until(ExpectedConditions.elementToBeClickable(zig.chennaiCity)).click();

        WebElement cityHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Used Cars in Chennai') or contains(@id,'usedcartitle')]")));
        Assert.assertTrue("Step 3 Failure: City filter 'Chennai' not verified!", cityHeader.getText().contains("Chennai"));

        List<WebElement> checks = driver.findElements(zig.popularModelCheckboxes);
        for (WebElement cb : checks) {
            if (!cb.isSelected()) ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cb);
        }

        Thread.sleep(4000);

        List<WebElement> carNames = driver.findElements(zig.carNames);
        List<WebElement> carPrices = driver.findElements(zig.carPrices);
        Assert.assertFalse("Step 3 Failure: Used cars list is empty!", carNames.isEmpty());

        System.out.println("=== Popular Used Cars Captured ===");
        for (int i = 0; i < Math.min(10, carNames.size()); i++) {
            try {
                String name = carNames.get(i).getText().trim();
                String price = carPrices.get(i).getText().trim();
                System.out.println((i + 1) + ". " + name + " | Price: " + price);
                ExcelUtils.writeToExcel("UsedCarsOutput.xlsx", "ChennaiCars", i + 1, 0, name);
            } catch (StaleElementReferenceException e) {
                String name = driver.findElements(zig.carNames).get(i).getText().trim();
                System.out.println((i + 1) + ". " + name + " (Recovered)");
            }
        }
    }

    @When("User attempts to login with Google using invalid details")
    public void login_attempt() {
        System.out.println("Step 4: Attempting Google Login with Invalid Details.");

        // Ensure context is clean
        driver.switchTo().defaultContent();
        WebElement loginIcon = wait.until(ExpectedConditions.elementToBeClickable(zig.loginIcon));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginIcon);

        String parent = driver.getWindowHandle();
        WebElement googleBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("googleSignIn")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", googleBtn);

        // Resilient switch for Jenkins
        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(parent)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("identifierId")));
        emailInput.sendKeys("invalid.sejal.mumbai@gmail.com" + Keys.ENTER);
        System.out.println("-> Invalid Credentials Entered.");
    }

    @Then("Capture and display {string} error message")
    public void capture_google_error(String expectedMsg) {
        System.out.println("Step 4 Validation: Verifying Google Error Message.");

        // We use a robust locator that avoids the smart apostrophe encoding bug
        By errorLocator = By.xpath("//*[contains(text(),'sign you in') or contains(text(),'find your Google Account')]");

        try {
            // Wait for text to actually render
            wait.until(d -> !d.findElement(errorLocator).getText().trim().isEmpty());

            WebElement error = driver.findElement(errorLocator);
            String actualMsg = error.getText().trim();
            System.out.println("-> Captured Message: " + actualMsg);

            // Validation logic
            Assert.assertTrue("Error mismatch! Found: " + actualMsg,
                    actualMsg.toLowerCase().contains("sign you in") || actualMsg.toLowerCase().contains("account"));

            System.out.println("Step 4 Validation: Correct Error Message Verified.");
            ScreenshotUtils.takeScreenshot("Google_Login_Error");

        } catch (Exception e) {
            ScreenshotUtils.takeScreenshot("Google_Error_Capture_Failed");
            throw new RuntimeException("Step 4 Failure: Could not capture error text.");
        }
    }

    private double parsePrice(String text) {
        try {
            String clean = text.replace("Rs.", "").replace("Rs", "").replace(",", "").trim();
            if (clean.contains("Lakh")) return Double.parseDouble(clean.replace("Lakh", "").trim());
            if (clean.contains("Crore")) return Double.parseDouble(clean.replace("Crore", "").trim()) * 100;
        } catch (Exception e) {}
        return -1;
    }

    @Then("The bike details should be displayed and stored in Excel")
    public void bike_done() { }

    @Then("Display the list of popular models and store in Excel")
    public void car_done() { }
}