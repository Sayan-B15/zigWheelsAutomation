package com.zigwheels.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.zigwheels.stepDefinitions",
        plugin = {
                "pretty",                      // Displays logs in the console
                "html:target/cucumber-reports", // Basic HTML report
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:", // Extent Report
                "timeline:target/timeline"      // Displays a visual timeline of tests
        }
)
public class TestRunner extends AbstractTestNGCucumberTests {}