package uk.gov.hmcts.reform.hmc.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
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


@ExtendWith({MockitoExtension.class})
@SpringBootTest
@ActiveProfiles("test")
class HearingsServiceTest {

    @InjectMocks
    HearingsServiceImpl hearingservice ;

    @Mock
    CaseApiService caseApiService;

    @MockBean
    ServiceAuthorisationTokenApi serviceAuthorisationTokenApi;

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

        CaseDetails caseDetails = CaseDetails.builder().id(123L).caseTypeId("PrivateLaw").data(caseDataMap)
            .build();
        String microservice = "zzxy";
        MicroserviceInfo microserviceName = MicroserviceInfo.builder().microservice(microservice).build();
        when(caseApiService.getCaseDetails(anyString(),anyString(),anyString())).thenReturn(caseDetails);
        when(serviceAuthorisationTokenApi.serviceToken(microserviceName)).thenReturn("12321");
        Hearings hearingsResponse = hearingservice.getCaseData(hearingsRequest, authorisation);
        Assertions.assertEquals("BBA3", hearingsResponse.getHmctsServiceID());
    }


}
