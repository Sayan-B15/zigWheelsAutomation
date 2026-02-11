package com.zigwheels.utils;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logs {
    private static final String LOG_FILE = "logs.txt";

    public static void info(String message) {
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            out.println(timestamp + " [INFO] : " + message);
        } catch (Exception e) {
            System.err.println("Could not write to log file: " + e.getMessage());
        }
    }
}