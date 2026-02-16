package com.zigwheels.stepDefinitions;

import com.zigwheels.base.BaseClass;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class Hooks extends BaseClass {

    @Before
    public void setup() {
        initialization(); // This will trigger the JOption dialog box
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
