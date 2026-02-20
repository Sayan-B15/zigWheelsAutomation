package com.zigwheels.base;

import java.io.InputStream;
import java.util.Properties;
import java.time.Duration;
import javax.swing.JOptionPane;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;

public class BaseClass {
    public static WebDriver driver;
    public static Properties prop;
    private static String selectedBrowser;

    public BaseClass() {
        try {
            prop = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream("config/config.properties");
            if (is == null) is = getClass().getClassLoader().getResourceAsStream("config.properties");
            if (is != null) prop.load(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initialization() {
        String browserFromSystem = System.getProperty("browser");

        if (browserFromSystem != null) {
            selectedBrowser = browserFromSystem;
        } else if (selectedBrowser == null) {
            String[] options = {"Chrome", "Edge", "Firefox"};
            selectedBrowser = (String) JOptionPane.showInputDialog(null, "Select Browser:",
                    "ZigWheels Setup", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (selectedBrowser == null) selectedBrowser = "Chrome";
        }

        if (selectedBrowser.equalsIgnoreCase("Chrome")) {
            ChromeOptions options = new ChromeOptions();
            if (browserFromSystem != null) {
                options.addArguments("--headless=new"); // Critical for Jenkins
                options.addArguments("--window-size=1920,1080");
                options.addArguments("--disable-gpu");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");
            }
            driver = new ChromeDriver(options);
        } else if (selectedBrowser.equalsIgnoreCase("Edge")) {
            EdgeOptions options = new EdgeOptions();
            if (browserFromSystem != null) {
                options.addArguments("--headless=new");
                options.addArguments("--disable-gpu");
                options.addArguments("--window-size=1920,1080");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");

                // Use a folder named 'EdgeProfile' inside your current Jenkins workspace
                String userDir = System.getProperty("user.dir") + "\\target\\EdgeProfile";
                options.addArguments("--user-data-dir=" + userDir);
            }
            driver = new EdgeDriver(options);
        } else if (selectedBrowser.equalsIgnoreCase("Firefox")) {
            driver = new FirefoxDriver();
        }

        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        // Maximize window only if running locally (not in Jenkins headless)
        if (browserFromSystem == null) {
            driver.manage().window().maximize();
        }

        System.out.println("Execution Browser: " + selectedBrowser);
    }
}