package uk.gov.hmcts.reform.hmc.api.model.ccd;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DontKnow {
    @JsonProperty("dontKnow")
    dontKnow("Don't know");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static DontKnow getValue(String key) {
        return DontKnow.valueOf(key);
    }
}
