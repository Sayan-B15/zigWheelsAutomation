package com.zigwheels.base;

import java.time.Duration;
import java.util.Properties;
import java.io.FileInputStream;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;

public class BaseClass {
    public static WebDriver driver;
    public static Properties prop;

    public static void setup() {
        try {
            prop = new Properties();
            FileInputStream fis = new FileInputStream("src/test/resources/config.properties");
            prop.load(fis);

            String browser = prop.getProperty("browser");
            if(browser.equalsIgnoreCase("chrome")) driver = new ChromeDriver();
            else driver = new EdgeDriver();

            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        } catch (Exception e) { e.printStackTrace(); }
    }
}