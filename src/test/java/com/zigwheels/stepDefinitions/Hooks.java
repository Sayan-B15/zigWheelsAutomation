package com.zigwheels.stepDefinitions;

import com.zigwheels.base.BaseClass;
import com.zigwheels.utils.Logs;
import com.zigwheels.utils.ScreenshotUtils;
import io.cucumber.java.*;

public class Hooks extends BaseClass {
    @Before
    public void startScenario(Scenario scenario) {
        setup();
        Logs.info("Started Scenario: " + scenario.getName());
    }

    @After
    public void endScenario(Scenario scenario) {
        if (scenario.isFailed()) {
            ScreenshotUtils.takeScreenshot(scenario.getName().replace(" ", "_"));
            Logs.info("Scenario Failed: " + scenario.getName());
        } else {
            Logs.info("Scenario Passed: " + scenario.getName());
        }
        if (driver != null) {
            driver.quit();
        }
    }
}