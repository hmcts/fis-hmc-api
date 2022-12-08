package uk.gov.hmcts.reform.hmc.api.model.ccd;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum Gender {
    @JsonProperty("female")
    FEMALE("female", "Female"),
    @JsonProperty("male")
    MALE("male", "Male"),
    @JsonProperty("other")
    OTHER("other", "They identify in another way");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static Gender getValue(String key) {
        return Gender.valueOf(key);
    }

    public static Gender getDisplayedValueFromEnumString(String enteredValue) {
        if ("female".equalsIgnoreCase(enteredValue)) {
            return Gender.FEMALE;
        } else if ("male".equalsIgnoreCase(enteredValue)) {
            return Gender.MALE;
        } else if ("other".equalsIgnoreCase(enteredValue)) {
            return Gender.OTHER;
        }
        return null;
    }
}
