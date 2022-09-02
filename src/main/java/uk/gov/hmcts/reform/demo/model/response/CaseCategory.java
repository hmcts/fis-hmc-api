package uk.gov.hmcts.reform.demo.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CaseCategory {
    @JsonProperty("categoryType")
    public String getCategoryType() {
        return this.categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    String categoryType;

    @JsonProperty("categoryValue")
    public String getCategoryValue() {
        return this.categoryValue;
    }

    public void setCategoryValue(String categoryValue) {
        this.categoryValue = categoryValue;
    }

    String categoryValue;

    @JsonProperty("categoryParent")
    public String getCategoryParent() {
        return this.categoryParent;
    }

    public void setCategoryParent(String categoryParent) {
        this.categoryParent = categoryParent;
    }

    String categoryParent;
}
