package uk.gov.hmcts.reform.hmc.api.services;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingsRequest;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingsData;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class HearingsDataServiceTest {

    @InjectMocks private HearingsDataServiceImpl hearingservice;

    @Mock private CaseApiService caseApiService;

    @Mock private AuthTokenGenerator authTokenGenerator;

    @Test
    public void shouldReturnHearingDetailsTest() throws JsonProcessingException {

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantCaseName", "PrivateLaw");
        caseDataMap.put("caseTypeOfApplication", "FL401");
        CaseDetails caseDetails =
                CaseDetails.builder().id(123L).caseTypeId("PrivateLaw").data(caseDataMap).build();
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(caseApiService.getCaseDetails(anyString(), anyString(), anyString()))
                .thenReturn(caseDetails);
        String authorisation = "xyz";
        HearingsRequest hearingsRequest =
                HearingsRequest.hearingRequestWith().hearingId("123").caseReference("123").build();
        HearingsData hearingsResponse = hearingservice.getCaseData(hearingsRequest, authorisation);
        Assertions.assertEquals("BBA3", hearingsResponse.getHmctsServiceID());
    }
}
