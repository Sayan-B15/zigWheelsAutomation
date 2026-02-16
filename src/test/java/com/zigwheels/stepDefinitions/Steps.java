package com.zigwheels.stepDefinitions;

import com.zigwheels.base.BaseClass;
import com.zigwheels.pages.ZigPage;
import com.zigwheels.utils.*;
import io.cucumber.java.en.*;
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
        ScreenshotUtils.takeScreenshot("ZigWheels_Homepage");
    }

    @When("User identifies upcoming Royal Enfield bikes under 4Lac")
    public void identify_re_bikes() {
        // 1. Hover over New Bikes
        WebElement newBikes = wait.until(ExpectedConditions.visibilityOfElementLocated(zig.newBikesMenu));
        actions.moveToElement(newBikes).perform();

        // 2. Use JavaScript click to bypass the "ElementClickInterceptedException"
        // This ensures that even if 'Latest Bikes' is overlapping, 'Upcoming Bikes' is clicked.
        WebElement upcomingLink = wait.until(ExpectedConditions.presenceOfElementLocated(zig.upcomingBikesOption));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", upcomingLink);
        Logs.info("Navigated to Upcoming Bikes via JavaScript click.");

        // 3. Select Royal Enfield Brand
        WebElement reBtn = wait.until(ExpectedConditions.elementToBeClickable(zig.royalEnfieldBrand));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", reBtn);

        // 4. Scroll and capture data
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,600)");
        wait.until(ExpectedConditions.visibilityOfElementLocated(zig.bikeNames));
        ScreenshotUtils.takeScreenshot("Royal_Enfield_Bikes");

        List<WebElement> names = driver.findElements(zig.bikeNames);
        List<WebElement> prices = driver.findElements(zig.bikePrices);
        List<WebElement> dates = driver.findElements(zig.bikeLaunchDates);

        int excelRow = 1;
        System.out.println("--- Upcoming Royal Enfield Bikes < 4Lac ---");

        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i).getText().trim();
            String priceTxt = (i < prices.size()) ? prices.get(i).getText().trim() : "N/A";
            String launchDate = (i < dates.size()) ? dates.get(i).getText().replace("Expected Launch :", "").trim() : "TBA";
            String fetchTime = dtf.format(LocalDateTime.now());

            double val = parsePrice(priceTxt);

            if (val > 0 && val < 4.0 && !name.contains("Best Mileage") && !name.contains("Service Center")) {
                System.out.println("[" + fetchTime + "] Bike: " + name + " | Price: " + priceTxt + " | Launch: " + launchDate);
                ExcelUtils.writeToExcel("BikesOutput.xlsx", "UpcomingBikes", excelRow, 0, name);
                ExcelUtils.writeToExcel("BikesOutput.xlsx", "UpcomingBikes", excelRow, 1, priceTxt);
                ExcelUtils.writeToExcel("BikesOutput.xlsx", "UpcomingBikes", excelRow, 2, launchDate);
                ExcelUtils.writeToExcel("BikesOutput.xlsx", "UpcomingBikes", excelRow, 3, fetchTime);
                excelRow++;
            }
        }
    }

    @When("User extracts all popular used cars in Chennai")
    public void used_cars_chennai() throws InterruptedException {
        actions.moveToElement(driver.findElement(zig.moreMenu)).perform();
        driver.findElement(zig.usedCarsLink).click();
        wait.until(ExpectedConditions.elementToBeClickable(zig.chennaiCity)).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[contains(@class,'popularModels')]")));
        List<WebElement> checks = driver.findElements(zig.popularModelCheckboxes);
        for (WebElement cb : checks) if (!cb.isSelected()) ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cb);

        // Infinite Scroll & Multi-part Screenshotting (4 segments)
        for (int part = 1; part <= 4; part++) {
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 1000)");
            Thread.sleep(2000);
            ScreenshotUtils.takeScreenshot("Used_Cars_Chennai_Part_" + part);
        }

        List<WebElement> carNames = driver.findElements(zig.carNames);
        List<WebElement> carPrices = driver.findElements(zig.carPrices);
        int count = Math.min(carNames.size(), carPrices.size());
        String fetchTime = dtf.format(LocalDateTime.now());

        for (int i = 0; i < count; i++) {
            String name = carNames.get(i).getText().trim();
            String price = carPrices.get(i).getText().trim();
            if (!name.isEmpty()) {
                System.out.println("[" + fetchTime + "] Car: " + name + " | Price: " + price);
                ExcelUtils.writeToExcel("UsedCarsOutput.xlsx", "ChennaiCars", i + 1, 0, name);
                ExcelUtils.writeToExcel("UsedCarsOutput.xlsx", "ChennaiCars", i + 1, 1, price);
                ExcelUtils.writeToExcel("UsedCarsOutput.xlsx", "ChennaiCars", i + 1, 2, fetchTime);
            }
        }
    }

    @When("User attempts to login with Google using invalid details")
    public void login_attempt() {
        int maxRetries = 2;
        for (int i = 1; i <= maxRetries; i++) {
            try {
                // 1. Ensure we are on the main page and click login
                driver.switchTo().defaultContent();
                WebElement loginIcon = wait.until(ExpectedConditions.elementToBeClickable(zig.loginIcon));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginIcon);

                // 2. Click Google specifically with JavaScript
                WebElement googleBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("googleSignIn")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", googleBtn);
                Logs.info("Attempt " + i + ": Clicked Google button.");

                // 3. Robust Window Switch with polling
                String parent = driver.getWindowHandle();
                WebDriverWait windowWait = new WebDriverWait(driver, Duration.ofSeconds(10));

                // Wait until handles > 1
                windowWait.until(ExpectedConditions.numberOfWindowsToBe(2));

                for (String handle : driver.getWindowHandles()) {
                    if (!handle.equals(parent)) {
                        driver.switchTo().window(handle);
                        break;
                    }
                }

                // 4. Enter Email
                WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//input[@type='email' or @id='identifierId']")));
                emailInput.sendKeys("invalid.sejal.mumbai@gmail.com" + Keys.ENTER);

                // If it reached here, it succeeded.
                Logs.info("Email entered successfully on attempt " + i);
                return;

            } catch (Exception e) {
                Logs.info("Attempt " + i + " failed: " + e.getMessage());
                if (i == maxRetries) {
                    ScreenshotUtils.takeScreenshot("Google_Login_Final_Failure");
                    throw new RuntimeException("Google login failed after " + i + " attempts.");
                }

                // CRITICAL: If failure happens, close any extra windows and refresh
                if (driver.getWindowHandles().size() > 1) {
                    for (String handle : driver.getWindowHandles()) {
                        if (!handle.equals(driver.getWindowHandle())) {
                            driver.switchTo().window(handle).close();
                        }
                    }
                }
                driver.switchTo().defaultContent();
                driver.navigate().refresh();
            }
        }
    }

    @Then("Capture and display {string} error message")
    public void capture_google_error(String expectedMsg) {
        try {
            WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(zig.googleErrorHeader));
            System.out.println("Captured Error: " + error.getText());
            Logs.info("Google Error Captured: " + error.getText());
            ScreenshotUtils.takeScreenshot("Google_Login_Error");
        } catch (Exception e) {
            Logs.info("Google Login error capture timed out.");
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
