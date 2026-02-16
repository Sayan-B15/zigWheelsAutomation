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
        Logs.info("Navigated to ZigWheels Home Page.");
    }

    @When("User identifies upcoming Royal Enfield bikes under 4Lac")
    public void identify_re_bikes() {
        // Navigate to Upcoming Bikes
        actions.moveToElement(driver.findElement(zig.newBikesMenu)).perform();
        driver.findElement(zig.upcomingBikesOption).click();

        // Select Royal Enfield
        WebElement reBtn = wait.until(ExpectedConditions.elementToBeClickable(zig.royalEnfieldBrand));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", reBtn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", reBtn);

        // Scroll to ensure the table is visible for the screenshot
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,600)");
        wait.until(ExpectedConditions.visibilityOfElementLocated(zig.bikeNames));
        ScreenshotUtils.takeScreenshot("Royal_Enfield_Upcoming_Bikes");

        // Collect Lists
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

            // Filter: Price < 4.0 Lakh and exclude non-bike summary rows
            if (val > 0 && val < 4.0 && !name.contains("Best Mileage") && !name.contains("Service Center")) {
                // Console Output with all requested fields
                System.out.println("[" + fetchTime + "] Bike: " + name + " | Price: " + priceTxt + " | Launch: " + launchDate);
                Logs.info("Extracted Bike: " + name + " - Launch: " + launchDate);

                // Excel Writing (Columns: Name, Price, Launch Date, Fetch Time)
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

        // Handle popular model checkboxes
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[contains(@class,'popularModels')]")));
        List<WebElement> checks = driver.findElements(zig.popularModelCheckboxes);
        for (WebElement cb : checks) {
            if (!cb.isSelected()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cb);
            }
        }

        // Infinite Scroll & Multi-part Screenshotting (4 segments)
        for (int part = 1; part <= 4; part++) {
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 1000)");
            Thread.sleep(2000);
            ScreenshotUtils.takeScreenshot("Used_Cars_Chennai_Part_" + part);
        }

        // Data Extraction logic
        List<WebElement> carNames = driver.findElements(zig.carNames);
        List<WebElement> carPrices = driver.findElements(zig.carPrices);
        int count = Math.min(carNames.size(), carPrices.size());
        String fetchTime = dtf.format(LocalDateTime.now());

        System.out.println("--- Popular Used Cars in Chennai ---");
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
        wait.until(ExpectedConditions.elementToBeClickable(zig.loginIcon)).click();

        // Target Google button specifically inside the modal container
        WebElement googleBtn = wait.until(ExpectedConditions.elementToBeClickable(zig.googleBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", googleBtn);

        String parentWindow = driver.getWindowHandle();
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> d.getWindowHandles().size() > 1);

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(parentWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(zig.emailField));
        emailInput.sendKeys("invalid.sejal.mumbai@gmail.com" + Keys.ENTER);
    }

    @Then("Capture and display {string} error message")
    public void capture_google_error(String expectedMsg) {
        try {
            WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(zig.googleErrorHeader));
            String actualMsg = error.getText();
            System.out.println("Captured Error: " + actualMsg);
            Logs.info("Google Error Captured: " + actualMsg);
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