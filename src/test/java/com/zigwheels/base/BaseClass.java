package com.zigwheels.base;

import java.io.InputStream;
import java.util.Properties;
import java.time.Duration;
import javax.swing.JOptionPane;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class BaseClass {

    public static WebDriver driver;
    public static Properties prop;
    private static String selectedBrowser; // Persists selection for all tests

    /**
     * Constructor: Loads the config.properties file using ClassLoader.
     * This is the safest way to handle file paths when running on Jenkins or local machines.
     */
    public BaseClass() {
        try {
            prop = new Properties();
            // Looks into src/test/resources/config/
            InputStream is = getClass().getClassLoader().getResourceAsStream("config/config.properties");
            if (is == null) {
                // Fallback if the file is directly in src/test/resources/
                is = getClass().getClassLoader().getResourceAsStream("config.properties");
            }

            if (is != null) {
                prop.load(is);
            } else {
                System.out.println("CRITICAL: config.properties not found in resources folder.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialization: Sets up the WebDriver.
     * Features:
     * 1. Jenkins Compatibility: Checks for -Dbrowser property from Maven.
     * 2. Local Compatibility: Shows a one-time Dialog Box if no property is provided.
     */
    public static void initialization() {
        // 1. Check if a browser is passed via Jenkins/Maven Command Line (-Dbrowser=Chrome)
        String browserFromSystem = System.getProperty("browser");

        // 2. Logic to determine which browser to use
        if (browserFromSystem != null) {
            selectedBrowser = browserFromSystem;
        } else if (selectedBrowser == null) {
            // Only show the Dialog Box if NOT running in Jenkins and browser isn't set yet
            String[] options = {"Chrome", "Edge", "Firefox"};
            selectedBrowser = (String) JOptionPane.showInputDialog(
                    null,
                    "Select the browser for execution:",
                    "ZigWheels Automation Setup",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );
            // Default to Chrome if user clicks 'Cancel' or 'X'
            if (selectedBrowser == null) selectedBrowser = "Chrome";
        }

        // 3. Driver Instantiation
        if (selectedBrowser.equalsIgnoreCase("Chrome")) {
            driver = new ChromeDriver();
        } else if (selectedBrowser.equalsIgnoreCase("Edge")) {
            driver = new EdgeDriver();
        } else if (selectedBrowser.equalsIgnoreCase("Firefox")) {
            driver = new FirefoxDriver();
        } else {
            // Fallback default
            driver = new ChromeDriver();
        }

        // 4. Browser Environment Setup
        try {
            driver.manage().window().maximize();
        } catch (Exception e) {
            System.out.println("Maximize not supported in current environment (likely Jenkins).");
        }

        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        System.out.println("-----------------------------------------------------------");
        System.out.println("Execution Browser: " + selectedBrowser);
        System.out.println("Environment: " + (browserFromSystem != null ? "Jenkins/Maven" : "Local UI"));
        System.out.println("-----------------------------------------------------------");
    }
}