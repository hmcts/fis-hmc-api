package uk.gov.hmcts.reform.hmc.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingsRequest;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseCategories;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingLocation;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingWindow;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingsData;
import uk.gov.hmcts.reform.hmc.api.model.response.Judiciary;
import uk.gov.hmcts.reform.hmc.api.model.response.PanelRequirements;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyDetails;
import uk.gov.hmcts.reform.hmc.api.model.response.ScreenNavigation;
import uk.gov.hmcts.reform.hmc.api.model.response.Vocabulary;

@Service
@RequiredArgsConstructor
public class HearingsDataServiceImpl implements HearingsDataService {

    @Value("${ccd.ui.url}")
    private String ccdBaseUrl;

    CaseApiService caseApiService;

    AuthTokenGenerator authTokenGenerator;

    private static Logger log = LoggerFactory.getLogger(HearingsDataServiceImpl.class);

    @Override
    public HearingsData getCaseData(
            HearingsRequest hearingsRequest, String authorisation, String serviceAuthorization)
            throws JsonProcessingException {
        CaseDetails caseDetails =
                caseApiService.getCaseDetails(
                        hearingsRequest.getCaseReference(), authorisation, serviceAuthorization);
        String hmctsInternalCaseNameMapper =
                hearingsRequest.getCaseReference()
                        + "_"
                        + caseDetails.getData().get("applicantCaseName");

        String publicCaseNameMapper;
        if ("FL401".equals(caseDetails.getData().get("caseTypeOfApplication"))) {
            publicCaseNameMapper = "Re-Minor";
        } else if ("C100".equals(caseDetails.getData().get("caseTypeOfApplication"))) {
            publicCaseNameMapper = "Re-Minor";
        } else {
            publicCaseNameMapper = "NA";
        }

        HearingsData hearingsData =
                HearingsData.hearingsDataWith()
                        .hmctsServiceID("BBA3")
                        .hmctsInternalCaseName(hmctsInternalCaseNameMapper)
                        .publicCaseName(publicCaseNameMapper)
                        .caseAdditionalSecurityFlag(false)
                        .caseCategories(
                                CaseCategories.caseCategoriesWith()
                                        .categoryType("")
                                        .categoryValue("")
                                        .categoryParent("")
                                        .build())
                        .caseDeepLink(ccdBaseUrl + hearingsRequest.getCaseReference())
                        .caseRestrictedFlag(false)
                        .externalCaseReference("")
                        .caseManagementLocationCode("")
                        .caseSlaStartDate("")
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
                                HearingLocation.hearingLocationWith()
                                        .locationId("")
                                        .locationId("")
                                        .build())
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
                        .parties(PartyDetails.partyDetailsWith().build())
                        .screenFlow(ScreenNavigation.screenNavigationWith().build())
                        .vocabulary(Vocabulary.vocabularyWith().build())
                        .hearingChannels(Arrays.asList(""))
                        .build();
        log.info("hearingsData {}", hearingsData);
        return hearingsData;
    }
}
