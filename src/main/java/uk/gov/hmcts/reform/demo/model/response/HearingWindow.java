package uk.gov.hmcts.reform.demo.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HearingWindow {
    @JsonProperty("dateRangeStart")
    public String getDateRangeStart() {
        return this.dateRangeStart;
    }

    public void setDateRangeStart(String dateRangeStart) {
        this.dateRangeStart = dateRangeStart;
    }

    String dateRangeStart;
}
