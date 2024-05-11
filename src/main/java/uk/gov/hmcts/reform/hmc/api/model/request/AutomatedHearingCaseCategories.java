package uk.gov.hmcts.reform.hmc.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(builderMethodName = "AutomatedHearingCaseCategoriesWith")
public class AutomatedHearingCaseCategories {

    @JsonProperty("categoryType")
    private String categoryType;

    @JsonProperty("categoryValue")
    private String categoryValue;

    @JsonProperty("categoryParent")
    private String categoryParent;

}
