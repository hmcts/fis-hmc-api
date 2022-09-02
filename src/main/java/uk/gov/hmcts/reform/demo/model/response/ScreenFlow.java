package uk.gov.hmcts.reform.demo.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ScreenFlow {
    @JsonProperty("screenName")
    public String getScreenName() {
        return this.screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    String screenName;

    @JsonProperty("navigation")
    public ArrayList<Navigation> getNavigation() {
        return this.navigation;
    }

    public void setNavigation(ArrayList<Navigation> navigation) {
        this.navigation = navigation;
    }

    ArrayList<Navigation> navigation;

    @JsonProperty("conditionKey")
    public String getConditionKey() {
        return this.conditionKey;
    }

    public void setConditionKey(String conditionKey) {
        this.conditionKey = conditionKey;
    }

    String conditionKey;
}
