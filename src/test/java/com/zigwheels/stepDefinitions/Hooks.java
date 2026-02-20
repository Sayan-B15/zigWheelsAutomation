package com.zigwheels.stepDefinitions;

import com.zigwheels.base.BaseClass;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

public class Hooks extends BaseClass {

    @Before
    public void setup() {
        initialization();
    }

    // This method will attach a screenshot to every step in the report
    @AfterStep
    public void addScreenshot(Scenario scenario) {
        // You can also wrap this in 'if(scenario.isFailed())' if you only want failure shots
        final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        scenario.attach(screenshot, "image/png", "Step Status");
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}