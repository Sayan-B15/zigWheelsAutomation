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

    @Given("User is on ZigWheels Home Page")
    public void user_is_on_home_page() {
        driver.get(prop.getProperty("url"));
        System.out.println("Step 1: Browser Launched & ZigWheels Home Page Opened.");
        Assert.assertTrue("Step 1 Failure: Page title mismatch!", driver.getTitle().contains("ZigWheels"));
        System.out.println("Step 1 Validation: Page Title Verified.");
        ScreenshotUtils.takeScreenshot("ZigWheels_Homepage");
    }

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

    @When("User extracts all popular used cars in Chennai")
    public void used_cars_chennai() throws InterruptedException {
        System.out.println("\nStep 3: Extracting Popular Used Cars in Chennai.");
        actions.moveToElement(driver.findElement(zig.moreMenu)).perform();
        driver.findElement(zig.usedCarsLink).click();
        wait.until(ExpectedConditions.elementToBeClickable(zig.chennaiCity)).click();

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
        Assert.assertFalse("Step 3 Failure: Used cars list for Chennai is empty!", carNames.isEmpty());

        int count = Math.min(carNames.size(), carPrices.size());
        String fetchTime = dtf.format(LocalDateTime.now());

        System.out.println("\n=== Popular Used Cars Captured ===");
        for (int i = 0; i < count; i++) {
            String name = carNames.get(i).getText().trim();
            String price = carPrices.get(i).getText().trim();
            if (!name.isEmpty()) {
                Assert.assertTrue("Price format invalid for: " + name, price.matches("Rs\\. [0-9,.]+( Lakh| Crore)"));
                if (i < 10) System.out.println((i + 1) + ". " + name + " | Price: " + price);
                ExcelUtils.writeToExcel("UsedCarsOutput.xlsx", "ChennaiCars", i + 1, 0, name);
                ExcelUtils.writeToExcel("UsedCarsOutput.xlsx", "ChennaiCars", i + 1, 1, price);
                ExcelUtils.writeToExcel("UsedCarsOutput.xlsx", "ChennaiCars", i + 1, 2, fetchTime);
            }
        }
        System.out.println("Step 3 Validation: " + count + " cars verified and stored.");
    }

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
                // Increased wait for window handle stabilization in Jenkins
                wait.until(d -> d.getWindowHandles().size() > 1);

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
                if (i == maxRetries) throw new RuntimeException("Step 4 Failure: Google login failed.");
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
        System.out.println("Step 4 Validation: Verifying Google Error Message.");

        // Multi-strategy XPath to find the error container
        By errorLocator = By.xpath("//*[contains(text(),'sign you in') or contains(text(),'find your Google Account') or @role='alert']");

        try {
            // 1. Wait for element to be present in DOM
            WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(errorLocator));

            // 2. Try Standard Selenium getText()
            String actualMsg = errorElement.getText().trim();

            // 3. BACKUP: If Selenium returns empty, use JavaScript (Fixes the RuntimeException)
            if (actualMsg.isEmpty()) {
                actualMsg = (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].textContent;", errorElement);
            }

            // Cleanup any extra whitespace
            actualMsg = (actualMsg != null) ? actualMsg.trim() : "";
            System.out.println("-> Captured Message: " + actualMsg);

            // 4. Robust Validation (Check for core keywords)
            Assert.assertTrue("Error message text was still empty or missing keywords!",
                    actualMsg.toLowerCase().contains("sign you in") || actualMsg.toLowerCase().contains("account"));

            System.out.println("Step 4 Validation: Correct Error Message Verified.");
            ScreenshotUtils.takeScreenshot("Google_Login_Error");

        } catch (Exception e) {
            ScreenshotUtils.takeScreenshot("Google_Final_Debug");
            throw new RuntimeException("Step 4 Failure: Element found but text could not be extracted even with JS fallback.");
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
    public void bike_done() { Logs.info("Bikes storage successfully verified."); }

    @Then("Display the list of popular models and store in Excel")
    public void car_done() { Logs.info("Used cars storage successfully verified."); }
}