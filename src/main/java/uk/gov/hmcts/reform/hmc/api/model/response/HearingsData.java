package uk.gov.hmcts.reform.hmc.api.model.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "hearingsDataWith")
@NoArgsConstructor
@AllArgsConstructor
public class HearingsData  {

    private String hmctsServiceID;

    private String hmctsInternalCaseName;

    private String publicCaseName;

    private Boolean caseAdditionalSecurityFlag;

    private CaseCategories caseCategories;

    private String caseDeepLink;

    private Boolean caseRestrictedFlag;

    private String externalCaseReference;

    private String caseManagementLocationCode;

    private String caseSlaStartDate;

    private Boolean autoListFlag;

    private String hearingType;

    private HearingWindow hearingWindow;

    private int duration;

    private String hearingPriorityType;

    private int numberOfPhysicalAttendees;

    private Boolean hearingInWelshFlag;

    private HearingLocation hearingLocations;

    private List<String> facilitiesRequired;

    private String listingComments;

    private String hearingRequester;

    private Boolean privateHearingRequiredFlag;

    private Boolean caseInterpreterRequiredFlag;

    private PanelRequirements panelRequirements;

    private String leadJudgeContractType;

    private Judiciary judiciary;

    private Boolean hearingIsLinkedFlag;

    private PartyDetails parties;

    private CaseFlags caseFlags;
    private ScreenNavigation screenFlow;

    private Vocabulary vocabulary;

    private List<String> hearingChannels;


    }
