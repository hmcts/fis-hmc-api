package uk.gov.hmcts.reform.hmc.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingsRequest;
import uk.gov.hmcts.reform.hmc.api.model.request.MicroserviceInfo;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseCategories;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.restclient.HmcHearingApi;
import uk.gov.hmcts.reform.hmc.api.restclient.ServiceAuthorisationTokenApi;

@Service
@RequiredArgsConstructor
public class HearingsServiceImpl implements HearingsService{
    @Value("${idam.s2s-auth.microservice}")
    private String microserviceName;

    public static final String HEARING_SUB_CHANNEL = "HearingSubChannel";
    private final HmcHearingApi hmcHearingApi;
    private final ServiceAuthorisationTokenApi serviceAuthorisationTokenApi;
    private final CaseApiService caseApiService;


    public Hearings getCaseData(HearingsRequest hearingsRequest, String authorisation)
        throws JsonProcessingException {
        String microserviceName = "fis_hmc_api";
        String serviceToken =
            serviceAuthorisationTokenApi.serviceToken(
                MicroserviceInfo.builder().microservice(microserviceName.trim()).build());
        System.out.println("microserviceName:" + microserviceName);
        System.out.println("serviceToken:" + serviceToken);
        System.out.print("auth token : " + authorisation);
        CaseDetails caseDetails = caseApiService.getCaseDetails(
            hearingsRequest.getCaseReference(), authorisation, serviceToken);

//        System.out.println("caseTypeOfApplication:  "+caseDetails.getData().get("caseTypeOfApplication"));
//        System.out.println("get data details"+caseDetails.getData().get("applicantCaseName"));


        Hearings hearings = Hearings.hearingsWith()
            .hmctsServiceID("BBA3")
            .hmctsInternalCaseName(hearingsRequest.getCaseReference())
            .publicCaseName("John Smith")
            .caseAdditionalSecurityFlag(false)
            .caseCategories(CaseCategories.caseCategoriesWith()
                                .categoryType("NA")
                                .categoryValue("NA")
                                .categoryParent("NA").build()).build();
        return hearings;

    }
}

