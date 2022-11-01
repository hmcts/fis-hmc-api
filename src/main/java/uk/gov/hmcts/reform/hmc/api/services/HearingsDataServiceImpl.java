package uk.gov.hmcts.reform.hmc.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingsRequest;
import uk.gov.hmcts.reform.hmc.api.model.response.*;

import java.util.Arrays;

@Service

public class HearingsDataServiceImpl implements HearingsDataService {
    @Value("${idam.s2s-auth.microservice}")
    private String microserviceName;

//    @Autowired
//    ServiceAuthorisationTokenApi serviceAuthorisationTokenApi;

    @Autowired
    CaseApiService caseApiService;

    AuthTokenGenerator authTokenGenerator;

    public static final String HEARING_SUB_CHANNEL = "HearingSubChannel";
    public static final String CASE_URL = " https://manage-case.demo.platform.hmcts.net/cases/case-details/";
    private static Logger log = LoggerFactory.getLogger(HearingsDataServiceImpl.class);

    public Hearings getCaseData(HearingsRequest hearingsRequest, String authorisation)
        throws JsonProcessingException {
        String microserviceName = "fis_hmc_api";
//        String serviceToken =
//            serviceAuthorisationTokenApi.serviceToken(
//                MicroserviceInfo.builder().microservice(microserviceName.trim()).build());

        String serviceAuthToken = authTokenGenerator.generate();

        System.out.println("microserviceName:" + microserviceName);
//        System.out.println("serviceToken:" + serviceToken);
        System.out.print("auth token : " + authorisation);
        CaseDetails caseDetails = caseApiService.getCaseDetails(
            hearingsRequest.getCaseReference(), authorisation, serviceAuthToken);

//        log.info("caseTypeOfApplication: {}" , caseDetails.getData().get("caseTypeOfApplication"));
//        log.info("get data details : {} " , caseDetails.getData().get("applicantCaseName"));
//        log.info("Applicant first name : {} ", caseDetails.getData().get("applicantsFL401"));

        String hmctsInternalCaseNameMapper = hearingsRequest.getCaseReference() + "_" + caseDetails.getData().get("applicantCaseName");

        String publicCaseNameMapper;
        if (caseDetails.getData().get("caseTypeOfApplication").equals("FL401")){
            publicCaseNameMapper = "Re-Minor";
        } else if (caseDetails.getData().get("caseTypeOfApplication").equals("C100"))
        {
           publicCaseNameMapper = "Re-Minor";
        } else {
            publicCaseNameMapper = "NA";
        }

        Hearings hearings = Hearings.hearingsWith()
            .hmctsServiceID("BBA3")
            .hmctsInternalCaseName(hmctsInternalCaseNameMapper)
            .publicCaseName(publicCaseNameMapper)
            .caseAdditionalSecurityFlag(false)
            .caseCategories(CaseCategories.caseCategoriesWith()
                                .categoryType("NA")
                                .categoryValue("NA")
                                .categoryParent("NA").build())
            .caseDeepLink(CASE_URL + hearingsRequest.getCaseReference())
            .caseRestrictedFlag(false)
            .externalCaseReference("NA")
            .caseManagementLocationCode("NA")
            .caseSlaStartDate("NA")
            .autoListFlag(false)
            .hearingType("NA")
            .hearingWindow(HearingWindow.hearingWindowWith()
                               .dateRangeStart("NA")
                               .dateRangeEnd("NA")
                               .firstDateTimeMustBe("NA")
                               .build())
            .duration(60)
            .hearingPriorityType("NA")
            .numberOfPhysicalAttendees(0)
            .hearingInWelshFlag(false)
            .hearingLocations(HearingLocation.hearingLocationWith().locationId("NA").locationId("NA").build())
            .facilitiesRequired(Arrays.asList("NA"))
            .listingComments("NA")
            .hearingRequester("NA")
            .privateHearingRequiredFlag(false)
            .caseInterpreterRequiredFlag(false)
            .panelRequirements(PanelRequirements.panelRequirementsWith().panelRequirements("NA").build())
            .leadJudgeContractType("NA")
            .judiciary(Judiciary.judiciaryWith().build())
            .hearingIsLinkedFlag(false)
            .parties(PartyDetails.partyDetailsWith().build())
            .screenFlow(ScreenNavigation.screenNavigationWith().build())
            .vocabulary(Vocabulary.vocabularyWith().build())
            .hearingChannels(Arrays.asList("NA"))
            .build();
        return hearings;

    }
}

