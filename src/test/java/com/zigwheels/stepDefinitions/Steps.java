package com.zigwheels.stepDefinitions;

import com.zigwheels.base.BaseClass;
import com.zigwheels.pages.ZigPage;
import com.zigwheels.utils.Logs;
import com.zigwheels.utils.ExcelUtils;
import com.zigwheels.utils.ScreenshotUtils;
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
        Logs.info("Navigated to ZigWheels Home Page.");
    }

    @When("User identifies upcoming Honda bikes under 4Lac")
    public void identify_bikes() {
        WebElement bikeBtn = wait.until(ExpectedConditions.elementToBeClickable(zig.bikeByBrand));
        actions.moveToElement(bikeBtn).perform();
        bikeBtn.click();

        WebElement honda = wait.until(ExpectedConditions.elementToBeClickable(zig.hondaBrand));
        actions.moveToElement(honda).perform();
        honda.click();

        WebElement readMoreBtn = wait.until(ExpectedConditions.elementToBeClickable(zig.readMore));
        readMoreBtn.click();

        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,500)");
        wait.until(ExpectedConditions.visibilityOfElementLocated(zig.tableRows));
        ScreenshotUtils.takeScreenshot("Upcoming_Honda_Bikes");

        List<WebElement> rows = driver.findElements(zig.tableRows);
        int excelRow = 1;
        System.out.println("--- Upcoming Honda Bikes < 4Lac ---");

        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() == 2) {
                String model = cells.get(0).getText();
                String priceText = cells.get(1).getText();
                int price = parsePrice(priceText);

                if (price > 0 && price < 400000 && !model.contains("Best Mileage") && !model.contains("Honda Bike")) {
                    System.out.println("Model: " + model + " | Price: " + priceText);
                    ExcelUtils.writeToExcel("BikesOutput.xlsx", "UpcomingBikes", excelRow++, 0, model);
                    ExcelUtils.writeToExcel("BikesOutput.xlsx", "UpcomingBikes", excelRow - 1, 1, priceText);
                }
            }
        }
    }

    @Then("The bike details should be displayed and stored")
    public void the_bike_details_should_be_displayed_and_stored() {
        Logs.info("Bike extraction completed.");
    }

    @When("User extracts used cars in Chennai")
    public void used_cars() {
        WebElement moreMenu = wait.until(ExpectedConditions.visibilityOfElementLocated(zig.moreMenu));
        actions.moveToElement(moreMenu).perform();
        driver.findElement(zig.usedCarsLink).click();
        wait.until(ExpectedConditions.elementToBeClickable(zig.chennaiCity)).click();

        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[contains(text(),'Applying filters')]")));
        } catch (Exception e) {}

        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,400)");

        By carXpath = By.xpath("//a[@data-track-label='Car-name']");
        By priceXpath = By.xpath("//span[contains(@class,'zw-cmn-price')]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(carXpath));
        ScreenshotUtils.takeScreenshot("Used_Cars_Chennai");

        // FIX: Re-locate both lists and use the smaller size to prevent IndexOutOfBounds
        List<WebElement> carNames = driver.findElements(carXpath);
        List<WebElement> carPrices = driver.findElements(priceXpath);
        int count = Math.min(carNames.size(), carPrices.size());

        System.out.println("--- Popular Used Cars in Chennai ---");
        for (int i = 0; i < count; i++) {
            try {
                String name = carNames.get(i).getText();
                String price = carPrices.get(i).getText();
                System.out.println("Car: " + name + " | Price: " + price);
                ExcelUtils.writeToExcel("UsedCarsOutput.xlsx", "ChennaiCars", i + 1, 0, name);
                ExcelUtils.writeToExcel("UsedCarsOutput.xlsx", "ChennaiCars", i + 1, 1, price);
            } catch (Exception e) {
                Logs.info("Skipped a car entry due to loading issue.");
            }
        }
    }

    @Then("Display the list of popular models")
    public void display_the_list_of_popular_models() {
        Logs.info("Used car models displayed and stored.");
    }

    @When("User attempts to login with Google using invalid details")
    public void user_attempts_to_login_with_google_using_invalid_details() {
        wait.until(ExpectedConditions.elementToBeClickable(zig.loginIcon)).click();
        wait.until(ExpectedConditions.elementToBeClickable(zig.googleBtn)).click();

        String parentWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(parentWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(zig.emailField));
        emailInput.sendKeys("invalid.user.HUMAN28@gmail.com" + Keys.ENTER);
    }

    @Then("Capture and display the error message")
    public void capture_and_display_the_error_message() {
        try {
            WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//span[contains(text(),'Couldn’t sign you in')]")));
            String errorMessage = error.getText();
            System.out.println("Captured Error: " + errorMessage);
            Logs.info("Google Error Captured: " + errorMessage);
            ScreenshotUtils.takeScreenshot("Google_Login_Error");
        } catch (Exception e) {
            Logs.info("Failed to capture specific Google error message.");
        }
    }

    private int parsePrice(String priceText) {
        priceText = priceText.replace("Rs.", "").trim();
        try {
            if (priceText.contains("Lakh")) {
                priceText = priceText.replace("Lakh", "").trim();
                return (int) (Double.parseDouble(priceText) * 100000);
            } else {
                return Integer.parseInt(priceText.replaceAll("[^0-9]", ""));
            }
        } catch (Exception e) { return -1; }
    }
}