package uk.gov.hmcts.reform.hmc.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingsRequest;
import uk.gov.hmcts.reform.hmc.api.model.request.MicroserviceInfo;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseCategories;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.restclient.ServiceAuthorisationTokenApi;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class HearingsServiceTest {

    @InjectMocks
    private HearingsServiceImpl hearingservice ;

    @Mock
    private CaseApiService caseApiService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private ServiceAuthorisationTokenApi serviceAuthorisationTokenApi;

    @Test
    public void shouldReturnHearingDetailsTest() throws JsonProcessingException {

        HearingsRequest hearingsRequest = HearingsRequest.hearingRequestWith().hearingId("123")
            .caseReference("123").build();
        Hearings hearings = Hearings.hearingsWith()
            .hmctsServiceID("BBA3")
            .hmctsInternalCaseName(hearingsRequest.getCaseReference())
            .publicCaseName("John Smith")
            .caseAdditionalSecurityFlag(false)
            .caseCategories(CaseCategories.caseCategoriesWith()
                                .categoryType("NA")
                                .categoryValue("NA")
                                .categoryParent("NA").build()).build();

        String authorisation = "xyz";

        Map<String,Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantCaseName","PrivateLaw");
        caseDataMap.put("caseTypeOfApplication","FL401");

        CaseDetails caseDetails = CaseDetails.builder().id(123L).caseTypeId("PrivateLaw").data(caseDataMap)
            .build();
        String microservice = "zzxy";
        MicroserviceInfo microserviceName = MicroserviceInfo.builder().microservice(microservice).build();
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
//        when(serviceAuthorisationTokenApi.serviceToken(microserviceName)).thenReturn("12321");
        when(caseApiService.getCaseDetails(anyString(),anyString(),anyString())).thenReturn(caseDetails);
        Hearings hearingsResponse = hearingservice.getCaseData(hearingsRequest, authorisation);
        Assertions.assertEquals("BBA3", hearingsResponse.getHmctsServiceID());
    }


}
