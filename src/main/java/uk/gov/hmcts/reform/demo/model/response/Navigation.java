package uk.gov.hmcts.reform.demo.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Navigation {
    @JsonProperty("resultValue")
    public String getResultValue() {
        return this.resultValue;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }

    String resultValue;

    @JsonProperty("conditionOperator")
    public String getConditionOperator() {
        return this.conditionOperator;
    }

    public void setConditionOperator(String conditionOperator) {
        this.conditionOperator = conditionOperator;
    }

    String conditionOperator;

    @JsonProperty("conditionValue")
    public String getConditionValue() {
        return this.conditionValue;
    }

    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }

    String conditionValue;
}
