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

    // Royal Enfield Locators
    public By newBikesMenu = By.xpath("//span[normalize-space()='NEW BIKES']");
    public By upcomingBikesOption = By.xpath("//a[@data-track-label='nav-upcoming-bikes']");
    public By royalEnfieldBrand = By.xpath("//a[@title='upcoming Royal Enfield bikes']");
    public By bikeNames = By.xpath("//strong[contains(@class,'lnk-hvr')]");
    public By bikePrices = By.xpath("//div[contains(@class,'fnt-15')]");
    public By bikeLaunchDates = By.xpath("//div[contains(@class,'clr-try fnt-14')]");

    // Used Cars Chennai Locators
    public By moreMenu = By.xpath("//span[@class='c-p']");
    public By usedCarsLink = By.xpath("//a[normalize-space()='Used Cars']");
    public By chennaiCity = By.xpath("//a[@class='searchFilter' and @data-value='Chennai']");
    public By popularModelCheckboxes = By.xpath("//ul[contains(@class,'popularModels')]//input[@type='checkbox']");
    public By carNames = By.xpath("//a[@data-track-label='Car-name']");
    public By carPrices = By.xpath("//span[contains(@class,'zw-cmn-price')]");

    // Updated Login Locators
    public By loginIcon = By.id("des_lIcon");
    public By googleBtn = By.className("googleSignIn"); // Updated based on provided snippet
    public By emailField = By.id("identifierId");
    public By googleErrorHeader = By.xpath("//span[contains(text(),'Couldn’t sign you in')]");
}