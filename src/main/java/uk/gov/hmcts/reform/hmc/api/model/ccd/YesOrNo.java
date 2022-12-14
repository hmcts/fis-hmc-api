package uk.gov.hmcts.reform.hmc.api.model.ccd;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum YesOrNo {
    @JsonProperty("Yes")
    YES("Yes"),

    @JsonProperty("No")
    NO("No");

    private final String value;

    @JsonCreator
    public static YesOrNo getValue(String key) {
        return YesOrNo.valueOf(key);
    }

    @JsonValue
    public String getDisplayedValue() {
        return value;
    }
}
