package uk.gov.hmcts.reform.hmc.api.model.ccd;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum HearingPriorityTypeEnum {

    @JsonProperty("StandardPriority")
    StandardPriority("StandardPriority","Standard priority"),
    @JsonProperty("UrgentPriority")
    UrgentPriority("UrgentPriority","Urgent priority");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static HearingPriorityTypeEnum getValue(String key) {
        return HearingPriorityTypeEnum.valueOf(key);
    }
}

