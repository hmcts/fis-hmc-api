package uk.gov.hmcts.reform.hmc.api.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(builderMethodName = "automatedHearingCaseDetailsWith")
public class AutomatedHearingCaseDetails {

    @JsonProperty("hmctsServiceCode")
    private String hmctsServiceCode;

    @JsonProperty("caseRef")
    private String caseRef;

    @JsonProperty("requestTimeStamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
    private LocalDateTime requestTimeStamp;

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
    private List<AutomatedHearingCaseCategories> caseCategories;

    @JsonProperty("caseManagementLocationCode")
    private String caseManagementLocationCode;

    @JsonProperty("caseRestrictedFlag")
    private Boolean caseRestrictedFlag;

    @JsonProperty("caseSLAStartDate")
    private String caseSlaStartDate;

}
