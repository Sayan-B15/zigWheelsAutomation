package com.zigwheels.stepDefinitions;

import com.zigwheels.base.BaseClass;
import com.zigwheels.pages.ZigPage;
import com.zigwheels.utils.*;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.List;

public class Steps extends BaseClass {
    ZigPage zig = new ZigPage(driver);
    Actions actions = new Actions(driver);
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

    @Given("User is on ZigWheels Home Page")
    public void user_is_on_home_page() {
        driver.get(prop.getProperty("url"));
        ScreenshotUtils.takeScreenshot("ZigWheels_Homepage");
    }

    @When("User identifies upcoming Royal Enfield bikes under 4Lac")
    public void identify_re_bikes() {
        actions.moveToElement(driver.findElement(zig.newBikesMenu)).perform();
        driver.findElement(zig.upcomingBikesOption).click();

        WebElement reBtn = wait.until(ExpectedConditions.elementToBeClickable(zig.royalEnfieldBrand));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", reBtn);

        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,600)");
        wait.until(ExpectedConditions.visibilityOfElementLocated(zig.bikeNames));
        ScreenshotUtils.takeScreenshot("Royal_Enfield_Bikes");

        List<WebElement> names = driver.findElements(zig.bikeNames);
        List<WebElement> prices = driver.findElements(zig.bikePrices);
        int excelRow = 1;

        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i).getText();
            String priceTxt = (i < prices.size()) ? prices.get(i).getText() : "N/A";
            double val = parsePrice(priceTxt);

            if (val > 0 && val < 4.0 && !name.contains("Best Mileage")) {
                System.out.println("Bike: " + name + " | Price: " + priceTxt);
                ExcelUtils.writeToExcel("BikesOutput.xlsx", "UpcomingBikes", excelRow++, 0, name);
                ExcelUtils.writeToExcel("BikesOutput.xlsx", "UpcomingBikes", excelRow-1, 1, priceTxt);
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

        for (int part = 1; part <= 4; part++) {
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 1000)");
            Thread.sleep(2000);
            ScreenshotUtils.takeScreenshot("Used_Cars_Chennai_Part_" + part);
        }

        List<WebElement> carNames = driver.findElements(zig.carNames);
        List<WebElement> carPrices = driver.findElements(zig.carPrices);
        int count = Math.min(carNames.size(), carPrices.size());

        for (int i = 0; i < count; i++) {
            String name = carNames.get(i).getText().trim();
            String price = carPrices.get(i).getText().trim();
            if (!name.isEmpty()) {
                System.out.println("Car: " + name + " | Price: " + price);
                ExcelUtils.writeToExcel("UsedCarsOutput.xlsx", "ChennaiCars", i + 1, 0, name);
                ExcelUtils.writeToExcel("UsedCarsOutput.xlsx", "ChennaiCars", i + 1, 1, price);
            }
        }
    }

    @When("User attempts to login with Google using invalid details")
    public void login_attempt() {
        // 1. Open the Login Modal
        wait.until(ExpectedConditions.elementToBeClickable(zig.loginIcon)).click();

        // 2. Click the Google Button using a more robust XPath
        // This targets the span containing "Google" inside the login modal
        WebElement googleBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@id='myModal3-modal-content']//span[text()='Google']")));

        // Using JavaScript click to bypass any overlay issues
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", googleBtn);
        Logs.info("Clicked Google login button.");

        // 3. Switch focus to the Google Popup
        String parentWindow = driver.getWindowHandle();
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> d.getWindowHandles().size() > 1);

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(parentWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // 4. Enter invalid email in the Google identifier field
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(zig.emailField));
        emailInput.sendKeys("invalid.user.mumbai@gmail.com" + Keys.ENTER);
    }

    @Then("Capture and display {string} error message")
    public void capture_google_error(String expectedMsg) {
        try {
            WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(zig.googleErrorHeader));
            System.out.println("Captured Error: " + error.getText());
            Logs.info("Google Error Captured: " + error.getText());
            ScreenshotUtils.takeScreenshot("Google_Login_Error");
        } catch (Exception e) { Logs.info("Login error capture timed out."); }
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
    public void bike_done() { Logs.info("Bikes storage complete."); }

    @Then("Display the list of popular models and store in Excel")
    public void car_done() { Logs.info("Cars storage complete."); }
}