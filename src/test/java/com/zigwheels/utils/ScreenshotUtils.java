package com.zigwheels.utils;

import com.zigwheels.base.BaseClass;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import java.io.File;

public class ScreenshotUtils extends BaseClass {
    public static void takeScreenshot(String fileName) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(src, new File("./Screenshots/" + fileName + ".png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}