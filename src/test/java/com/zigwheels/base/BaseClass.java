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
    private static String selectedBrowser; // Static to persist choice

    public BaseClass() {
        try {
            prop = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream("config/config.properties");
            if (is == null) is = getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(is);
        } catch (Exception e) {
            System.out.println("CRITICAL: config.properties not found.");
        }
    }

    public static void initialization() {
        // Show dialog only if selectedBrowser is null (First run)
        if (selectedBrowser == null) {
            String[] options = {"Chrome", "Edge", "Firefox"};
            selectedBrowser = (String) JOptionPane.showInputDialog(
                    null, "Select Browser:", "ZigWheels cross-browser Setup",
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (selectedBrowser == null) selectedBrowser = "Chrome";
        }

        // Initialize driver based on the persisted selection
        if (selectedBrowser.equalsIgnoreCase("Chrome")) driver = new ChromeDriver();
        else if (selectedBrowser.equalsIgnoreCase("Edge")) driver = new EdgeDriver();
        else if (selectedBrowser.equalsIgnoreCase("Firefox")) driver = new FirefoxDriver();

        try { driver.manage().window().maximize(); } catch (Exception e) {}
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        System.out.println("Execution started on browser: " + selectedBrowser);
    }
}