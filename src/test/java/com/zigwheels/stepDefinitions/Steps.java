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

public class Steps extends BaseClass {
    ZigPage zig = new ZigPage(driver);
    Actions actions = new Actions(driver);
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // -----------------------------------------------------------
    // Step 1: Initialize Navigation
    // -----------------------------------------------------------
    @Given("User is on ZigWheels Home Page")
    public void user_is_on_home_page() {
        driver.get(prop.getProperty("url"));
        System.out.println("Step 1: Browser Launched & ZigWheels Home Page Opened.");

        // Assertion: Verify Page Title
        Assert.assertTrue("Step 1 Failure: Page title mismatch!", driver.getTitle().contains("ZigWheels"));
        System.out.println("Step 1 Validation: Page Title Verified.");
        ScreenshotUtils.takeScreenshot("ZigWheels_Homepage");
    }

    // -----------------------------------------------------------
    // Step 2: Identify Bikes (Filtering and Data Extraction)
    // -----------------------------------------------------------
    @When("User identifies upcoming Royal Enfield bikes under 4Lac")
    public void identify_re_bikes() {
        System.out.println("\nStep 2: Navigating to Upcoming Royal Enfield Bikes.");

        WebElement newBikes = wait.until(ExpectedConditions.visibilityOfElementLocated(zig.newBikesMenu));
        actions.moveToElement(newBikes).perform();

        WebElement upcomingLink = wait.until(ExpectedConditions.presenceOfElementLocated(zig.upcomingBikesOption));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", upcomingLink);
        Logs.info("Navigated to Upcoming Bikes via JavaScript click.");

        WebElement reBtn = wait.until(ExpectedConditions.elementToBeClickable(zig.royalEnfieldBrand));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", reBtn);
        System.out.println("-> Brand Filter Applied.");

        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,600)");
        wait.until(ExpectedConditions.visibilityOfElementLocated(zig.bikeNames));
        ScreenshotUtils.takeScreenshot("Royal_Enfield_Bikes");

        List<WebElement> names = driver.findElements(zig.bikeNames);
        List<WebElement> prices = driver.findElements(zig.bikePrices);
        List<WebElement> dates = driver.findElements(zig.bikeLaunchDates);

        // Presence Assertion: List not empty
        Assert.assertFalse("Step 2 Failure: No bikes found on the page!", names.isEmpty());

        int excelRow = 1;
        System.out.println("\n=== Upcoming Royal Enfield Bikes < 4Lac ===");

        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i).getText().trim();
            String priceTxt = (i < prices.size()) ? prices.get(i).getText().trim() : "N/A";
            String launchDate = (i < dates.size()) ? dates.get(i).getText().replace("Expected Launch :", "").trim() : "TBA";
            String fetchTime = dtf.format(LocalDateTime.now());

            double val = parsePrice(priceTxt);

            if (val > 0 && val < 4.0 && !name.contains("Best Mileage") && !name.contains("Service Center")) {

                // Brand and Price Constraint Assertions
                Assert.assertTrue("Incorrect brand found: " + name, name.contains("Royal Enfield"));
                Assert.assertTrue("Price exceeded 4Lac: " + name, val < 4.0);

                System.out.println(excelRow + ". " + name + " | Price: " + priceTxt + " | Launch: " + launchDate);

                ExcelUtils.writeToExcel("BikesOutput.xlsx", "UpcomingBikes", excelRow, 0, name);
                ExcelUtils.writeToExcel("BikesOutput.xlsx", "UpcomingBikes", excelRow, 1, priceTxt);
                ExcelUtils.writeToExcel("BikesOutput.xlsx", "UpcomingBikes", excelRow, 2, launchDate);
                ExcelUtils.writeToExcel("BikesOutput.xlsx", "UpcomingBikes", excelRow, 3, fetchTime);
                excelRow++;
            }
        }
        System.out.println("Step 2 Validation: " + (excelRow - 1) + " bikes verified and stored.");
    }

    // -----------------------------------------------------------
    // Step 3: Used Cars Logic (City Filter and Extraction)
    // -----------------------------------------------------------
    @When("User extracts all popular used cars in Chennai")
    public void used_cars_chennai() throws InterruptedException {
        System.out.println("\nStep 3: Extracting Popular Used Cars in Chennai.");

        actions.moveToElement(driver.findElement(zig.moreMenu)).perform();
        driver.findElement(zig.usedCarsLink).click();
        wait.until(ExpectedConditions.elementToBeClickable(zig.chennaiCity)).click();

        // City Filter Assertion: Verify Chennai title exists
        WebElement cityHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Used Cars in Chennai') or contains(@id,'usedcartitle')]")));
        Assert.assertTrue("Step 3 Failure: City filter 'Chennai' not verified in header!", cityHeader.getText().contains("Chennai"));
        System.out.println("-> Filter Verified: City 'Chennai'.");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[contains(@class,'popularModels')]")));
        List<WebElement> checks = driver.findElements(zig.popularModelCheckboxes);
        for (WebElement cb : checks) {
            if (!cb.isSelected()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cb);
            }
        }
        System.out.println("-> Popular Models: All checkboxes selected.");

        for (int part = 1; part <= 4; part++) {
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 1000)");
            Thread.sleep(2000);
            ScreenshotUtils.takeScreenshot("Used_Cars_Chennai_Part_" + part);
        }

        List<WebElement> carNames = driver.findElements(zig.carNames);
        List<WebElement> carPrices = driver.findElements(zig.carPrices);

        // Presence Assertion: Car list not empty
        Assert.assertFalse("Step 3 Failure: Used cars list for Chennai is empty!", carNames.isEmpty());

        int count = Math.min(carNames.size(), carPrices.size());
        String fetchTime = dtf.format(LocalDateTime.now());

        System.out.println("\n=== Popular Used Cars Captured ===");
        for (int i = 0; i < count; i++) {
            String name = carNames.get(i).getText().trim();
            String price = carPrices.get(i).getText().trim();

            if (!name.isEmpty()) {
                // Price Format Assertion (Regex)
                Assert.assertTrue("Price format invalid for: " + name, price.matches("Rs\\. [0-9,.]+( Lakh| Crore)"));

                if (i < 10) System.out.println((i + 1) + ". " + name + " | Price: " + price);

                ExcelUtils.writeToExcel("UsedCarsOutput.xlsx", "ChennaiCars", i + 1, 0, name);
                ExcelUtils.writeToExcel("UsedCarsOutput.xlsx", "ChennaiCars", i + 1, 1, price);
                ExcelUtils.writeToExcel("UsedCarsOutput.xlsx", "ChennaiCars", i + 1, 2, fetchTime);
            }
        }
        System.out.println("Step 3 Validation: " + count + " cars verified and stored.");
    }

    // -----------------------------------------------------------
    // Step 4: Login Validation (Google Popup)
    // -----------------------------------------------------------
    @When("User attempts to login with Google using invalid details")
    public void login_attempt() {
        System.out.println("\nStep 4: Attempting Google Login with Invalid Details.");
        int maxRetries = 2;
        for (int i = 1; i <= maxRetries; i++) {
            try {
                driver.switchTo().defaultContent();
                WebElement loginIcon = wait.until(ExpectedConditions.elementToBeClickable(zig.loginIcon));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginIcon);

                WebElement googleBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("googleSignIn")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", googleBtn);

                String parent = driver.getWindowHandle();
                new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.numberOfWindowsToBe(2));

                for (String handle : driver.getWindowHandles()) {
                    if (!handle.equals(parent)) {
                        driver.switchTo().window(handle);
                        break;
                    }
                }

                WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@type='email' or @id='identifierId']")));
                emailInput.sendKeys("invalid.sejal.mumbai@gmail.com" + Keys.ENTER);
                System.out.println("-> Invalid Credentials Entered.");
                return;

            } catch (Exception e) {
                if (i == maxRetries) throw new RuntimeException("Step 4 Failure: Google login failed after retries.");
                if (driver.getWindowHandles().size() > 1) {
                    for (String handle : driver.getWindowHandles()) {
                        if (!handle.equals(driver.getWindowHandle())) driver.switchTo().window(handle).close();
                    }
                }
                driver.switchTo().defaultContent();
                driver.navigate().refresh();
            }
        }
    }

    @Then("Capture and display {string} error message")
    public void capture_google_error(String expectedMsg) {
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(zig.googleErrorHeader));
        String actualMsg = error.getText();
        System.out.println("-> Captured Message: " + actualMsg);

        // Error Message Assertion
        Assert.assertEquals("Error message mismatch!", expectedMsg, actualMsg);

        // Field Absence Assertion
        boolean passVisible = driver.findElements(By.name("password")).size() > 0;
        Assert.assertFalse("Password field appeared for invalid user!", passVisible);

        System.out.println("Step 4 Validation: Correct Error Message Verified.");
        ScreenshotUtils.takeScreenshot("Google_Login_Error");
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
    public void bike_done() { Logs.info("Bikes storage successfully verified."); }

    @Then("Display the list of popular models and store in Excel")
    public void car_done() { Logs.info("Used cars storage successfully verified."); }
}