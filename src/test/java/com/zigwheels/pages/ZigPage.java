package com.zigwheels.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class ZigPage {
    WebDriver driver;
    public ZigPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public By bikeByBrand = By.xpath("/html/body/div[8]/div[2]/div[1]/div/ul/li[2]");
    public By hondaBrand = By.xpath("/html/body/div[8]/div[2]/div[2]/div[2]/div/div[1]/div/ul/li[6]/a");
    public By readMore = By.xpath("//*[@id=\"seoCont\"]/span/span");
    public By tableRows = By.xpath("//table[contains(@class,'tbl')]/tbody/tr");

    public By moreMenu = By.xpath("//*[@id=\"headerNewVNavWrap\"]/nav/ul/li[5]/span");
    public By usedCarsLink = By.xpath("//*[@id=\"headerNewVNavWrap\"]/nav/ul/li[5]/ul/li[2]/a");
    public By chennaiCity = By.xpath("//*[@id=\"popularCityList\"]/li[7]/a");

    public By loginIcon = By.xpath("//*[@id=\"des_lIcon\"]");
    public By googleBtn = By.xpath("//*[@id=\"myModal3-modal-content\"]/div[1]/div/div[3]/div[6]/div/span[2]");
    public By emailField = By.xpath("//*[@id=\"identifierId\"]");
    // Updated for the header text capture
    public By errorMsg = By.xpath("//span[contains(text(),'Couldn’t sign you in')]");
}