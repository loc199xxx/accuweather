package com.accuweather.steps;

import com.accuweather.hooks.CucumberHooks;
import com.accuweather.pages.SearchPage;
import io.cucumber.java.en.*;

import static org.testng.Assert.*;

public class SearchSteps {

    private SearchPage searchPage;

    private SearchPage page() {
        if (searchPage == null) searchPage = new SearchPage(CucumberHooks.getDriver());
        return searchPage;
    }

    @Given("mo trang tim kiem")
    public void mo_trang_tim_kiem() {
        page().open();
    }

    @When("tim kiem {string}")
    public void tim_kiem(String cityName) {
        page().searchCity(cityName);
        page().dismissAllPopups();
    }

    @When("chon ket qua tim kiem dau tien")
    public void chon_ket_qua_dau_tien() {
        page().selectFirstResult();
        page().dismissAllPopups();
    }

    @Then("URL phai chua {string}")
    public void url_phai_chua(String slug) {
        page().waitForPageLoad();
        String expected = slug;
        String url = page().getCurrentUrl();
        assertTrue(url.contains(expected),
                "URL phai chua '" + expected + "', nhung nhan duoc: " + url);
    }
}