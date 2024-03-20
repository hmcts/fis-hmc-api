package uk.gov.hmcts.reform.hmc.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(builderMethodName = "automatedCaseDetailsWith")
public class CaseDetails {

    @JsonProperty("hmctsServiceCode")
    private String hmctsServiceCode;

    @JsonProperty("caseRef")
    private String caseRef;

    @JsonProperty("requestTimeStamp")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
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
    private CaseCategories caseCategories;

    @JsonProperty("caseManagementLocationCode")
    private String caseManagementLocationCode;

    @JsonProperty("caseRestrictedFlag")
    private Boolean caseRestrictedFlag;

    @JsonProperty("caseSLAStartDate")
    private String caseSlaStartDate;

}
