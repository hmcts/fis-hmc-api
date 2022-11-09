package uk.gov.hmcts.reform.hmc.api.services;

import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.ApplicantTable;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseCategories;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingLocation;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingWindow;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingsData;
import uk.gov.hmcts.reform.hmc.api.model.response.Judiciary;
import uk.gov.hmcts.reform.hmc.api.model.response.PanelRequirements;
import uk.gov.hmcts.reform.hmc.api.model.response.Parties;
import uk.gov.hmcts.reform.hmc.api.model.response.RespondentTable;
import uk.gov.hmcts.reform.hmc.api.model.response.ScreenNavigation;
import uk.gov.hmcts.reform.hmc.api.model.response.Vocabulary;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class HearingsDataServiceImpl implements HearingsDataService {

    @Value("${ccd.ui.url}")
    private String ccdBaseUrl;

    @Autowired CaseApiService caseApiService;

    AuthTokenGenerator authTokenGenerator;

    private static Logger log = LoggerFactory.getLogger(HearingsDataServiceImpl.class);
    public static final String CASE_TYPE_OF_APPLICATION = "caseTypeOfApplication";
    public static final String FL401_APPLICANT_TABLE = "fl401ApplicantTable";
    public static final String FL401_RESPONDENT_TABLE = "fl401RespondentTable";
    public static final String APPLICANT_CASE_NAME = "applicantCaseName";

    public static final String ISSUE_DATE = "issueDate";
    public static final String BBA3 = "BBA3";
    public static final String FL401 = "FL401";
    public static final String C100 = "C100";
    public static final String RE_MINOR = "Re-Minor";

    @Override
    public HearingsData getCaseData(
            HearingValues hearingValues, String authorisation, String serviceAuthorization)
            throws IOException, ParseException {
        CaseDetails caseDetails =
                caseApiService.getCaseDetails(
                        hearingValues.getCaseReference(), authorisation, serviceAuthorization);
        String publicCaseNameMapper;
        if (FL401.equals(caseDetails.getData().get(CASE_TYPE_OF_APPLICATION))) {
            ApplicantTable applicantTable =
                    (ApplicantTable) caseDetails.getData().get(FL401_APPLICANT_TABLE);
            RespondentTable respondentTable =
                    (RespondentTable) caseDetails.getData().get(FL401_RESPONDENT_TABLE);
            if (applicantTable != null && respondentTable != null) {
                publicCaseNameMapper =
                        applicantTable.getLastName() + '_' + respondentTable.getLastName();
            } else {
                publicCaseNameMapper = "";
            }
        } else if (C100.equals(caseDetails.getData().get(CASE_TYPE_OF_APPLICATION))) {
            publicCaseNameMapper = RE_MINOR;
        } else {
            publicCaseNameMapper = "";
        }

        String hmctsInternalCaseNameMapper =
                hearingValues.getCaseReference()
                        + "_"
                        + caseDetails.getData().get(APPLICANT_CASE_NAME);
        String caseSlaStartDateMapper = (String) caseDetails.getData().get(ISSUE_DATE);

        HearingsData hearingsData =
                HearingsData.hearingsDataWith()
                        .hmctsServiceID(BBA3)
                        .hmctsInternalCaseName(hmctsInternalCaseNameMapper)
                        .publicCaseName(publicCaseNameMapper)
                        .caseAdditionalSecurityFlag(false)
                        .caseCategories(
                                Arrays.asList(
                                        CaseCategories.caseCategoriesWith()
                                                .categoryType("")
                                                .categoryValue("")
                                                .categoryParent("Private Law")
                                                .build()))
                        .caseDeepLink(ccdBaseUrl + hearingValues.getCaseReference())
                        .caseRestrictedFlag(false)
                        .externalCaseReference("")
                        .caseManagementLocationCode("")
                        .caseSlaStartDate(caseSlaStartDateMapper)
                        .autoListFlag(false)
                        .hearingType("")
                        .hearingWindow(
                                HearingWindow.hearingWindowWith()
                                        .dateRangeStart("")
                                        .dateRangeEnd("")
                                        .firstDateTimeMustBe("")
                                        .build())
                        .duration(60)
                        .hearingPriorityType("")
                        .numberOfPhysicalAttendees(0)
                        .hearingInWelshFlag(false)
                        .hearingLocations(
                                Arrays.asList(
                                        HearingLocation.hearingLocationWith()
                                                .locationType("")
                                                .locationId("")
                                                .build()))
                        .facilitiesRequired(Arrays.asList(""))
                        .listingComments("")
                        .hearingRequester("")
                        .privateHearingRequiredFlag(false)
                        .caseInterpreterRequiredFlag(false)
                        .panelRequirements(
                                PanelRequirements.panelRequirementsWith()
                                        .requirementDetails("")
                                        .build())
                        .leadJudgeContractType("")
                        .judiciary(Judiciary.judiciaryWith().build())
                        .hearingIsLinkedFlag(false)
                        .parties(Arrays.asList(Parties.partyDetailsWith().build()))
                        .screenFlow(Arrays.asList(ScreenNavigation.screenNavigationWith().build()))
                        .vocabulary(Arrays.asList(Vocabulary.vocabularyWith().build()))
                        .hearingChannels(Arrays.asList(""))
                        .build();
        log.info("hearingsData {}", hearingsData);
        return hearingsData;
    }
}
