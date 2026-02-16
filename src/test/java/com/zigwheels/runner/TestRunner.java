package com.zigwheels.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.zigwheels.stepDefinitions",
        plugin = {
                "pretty",
                "html:target/cucumber-reports.html",
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:",
                "timeline:target/timeline",
                "rerun:target/failed_scenarios.txt" // Tracks failed scenarios for rerun
        }
)
public class TestRunner extends AbstractTestNGCucumberTests {}