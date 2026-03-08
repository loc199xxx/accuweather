package com.accuweather.steps;

import com.accuweather.hooks.CucumberHooks;
import com.accuweather.pages.BasePage;
import io.cucumber.java.en.*;

import java.io.File;

import static org.testng.Assert.*;

public class ScreenshotSteps {

    private String screenshotPath;

    @When("chup anh man hinh voi ten {string}")
    public void chup_anh_man_hinh(String name) {
        BasePage page = new BasePage(CucumberHooks.getDriver());
        screenshotPath = page.takeScreenshot(name + "_" + System.currentTimeMillis());
    }

    @Then("tep anh chup phai ton tai")
    public void tep_anh_chup_phai_ton_tai() {
        assertNotNull(screenshotPath, "Duong dan anh chup bi null");
        File file = new File(screenshotPath);
        assertTrue(file.exists(), "Tep anh chup phai ton tai: " + screenshotPath);
        assertTrue(file.getName().endsWith(".png"), "Anh chup phai la tep .png");
    }
}