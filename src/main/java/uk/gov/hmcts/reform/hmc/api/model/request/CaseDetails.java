package uk.gov.hmcts.reform.hmc.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseDetails {

    @JsonProperty("hmctsServiceCode")
    private String hmctsServiceCode;

    @JsonProperty("caseRef")
    private String caseRef;

    @JsonProperty("requestTimeStamp")
    private DateTime requestTimeStamp;

    @JsonProperty("externalCaseReference")
    private String externalCaseReference;

    @JsonProperty("caseDeepLink")
    private String caseDeepLink;

    @JsonProperty("hmctsInternalCaseName")
    private String hmctsInternalCaseName;

    @JsonProperty("publicCaseName")
    private String publicCaseName;

    @JsonProperty("caseAdditionalSecurityFlag")
    private Boolean caseAdditionalSecurityFlag;

    @JsonProperty("caseInterpreterRequiredFlag")
    private Boolean caseInterpreterRequiredFlag;

    @JsonProperty("caseCategories")
    private CaseCategories caseCategories;

    @JsonProperty("caseManagementLocationCode")
    private String caseManagementLocationCode;

    @JsonProperty("caseRestrictedFlag")
    private Boolean caseRestrictedFlag;

    @JsonProperty("caseSLAStartDate")
    private String caseSlaStartDate;

}
