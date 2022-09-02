package uk.gov.hmcts.reform.demo.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HearingLocation {
    @JsonProperty("locationType")
    public String getLocationType() {
        return this.locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    String locationType;

    @JsonProperty("locationId")
    public String getLocationId() {
        return this.locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    String locationId;
}
