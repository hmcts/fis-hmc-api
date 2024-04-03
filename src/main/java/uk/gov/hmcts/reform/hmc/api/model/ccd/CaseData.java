package uk.gov.hmcts.reform.hmc.api.model.ccd;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import uk.gov.hmcts.reform.hmc.api.model.ccd.caseflagsv2.AllPartyFlags;
import uk.gov.hmcts.reform.hmc.api.model.ccd.caselinksdata.CaseLinkData;
import uk.gov.hmcts.reform.hmc.api.model.ccd.caselinksdata.CaseLinkElement;

@Data
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class CaseData implements MappableObject {

    private String familymanCaseNumber;
    private String dateSubmitted;
    private String caseTypeOfApplication;

    private List<Element<PartyDetails>> applicants;

    private List<Element<PartyDetails>> respondents;

    private List<Element<PartyDetails>> otherPartyInTheCaseRevised;

    private String applicantSolicitorEmailAddress;

    private String solicitorName;

    private String courtName;

    @JsonProperty("applicantsFL401")
    private PartyDetails applicantsFL401;

    @JsonProperty("respondentsFL401")
    private PartyDetails respondentsFL401;

    @JsonProperty("caseManagementLocation")
    private CaseManagementLocation caseManagementLocation;

    @JsonProperty("caseLinks")
    public List<CaseLinkElement<CaseLinkData>> caseLinks;

    @JsonAlias({"applicantCaseName", "applicantOrRespondentCaseName"})
    private String applicantCaseName;
    private AllPartyFlags allPartyFlags;

    @JsonUnwrapped
    private final ManageOrders manageOrders;
}
