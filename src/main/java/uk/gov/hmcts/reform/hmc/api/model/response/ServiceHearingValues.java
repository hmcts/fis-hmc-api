package uk.gov.hmcts.reform.hmc.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.simple.JSONArray;

@Getter
@Setter
@Builder(builderMethodName = "hearingsDataWith")
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHearingValues {

    private String hmctsServiceID;

    private String hmctsInternalCaseName;

    private String publicCaseName;

    private Boolean caseAdditionalSecurityFlag;

    private List<CaseCategories> caseCategories;

    private String caseDeepLink;

    @JsonProperty("caserestrictedFlag")
    private Boolean caseRestrictedFlag;

    private String externalCaseReference;

    private String caseManagementLocationCode;

    @JsonProperty("caseSLAStartDate")
    private String caseSlaStartDate;

    private Boolean autoListFlag;

    private String hearingType;

    private HearingWindow hearingWindow;

    private int duration;

    private String hearingPriorityType;

    private int numberOfPhysicalAttendees;

    private Boolean hearingInWelshFlag;

    private List<HearingLocation> hearingLocations;

    private List<String> facilitiesRequired;

    private String listingComments;

    private String hearingRequester;

    private Boolean privateHearingRequiredFlag;

    private Boolean caseInterpreterRequiredFlag;

    private PanelRequirements panelRequirements;

    private String leadJudgeContractType;

    private Judiciary judiciary;

    private Boolean hearingIsLinkedFlag;

    private List<PartyDetailsModel> parties;

    private CaseFlags caseFlags;

    private JSONArray screenFlow;

    private List<Vocabulary> vocabulary;

    private List<String> hearingChannels;


}
