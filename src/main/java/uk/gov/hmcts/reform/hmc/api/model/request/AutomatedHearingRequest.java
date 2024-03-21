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
@Builder(builderMethodName = "automatedHearingRequestWith")
public class AutomatedHearingRequest {

    @JsonProperty("caseDetails")
    private CaseDetails caseDetails;

    @JsonProperty("hearingDetails")
    private HearingDetails hearingDetails;

    @JsonProperty("partyDetails")
    private PartyDetails partyDetails;
}