package uk.gov.hmcts.reform.hmc.api.services;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.ApplicantTable;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingsData;
import uk.gov.hmcts.reform.hmc.api.model.response.RespondentTable;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class HearingsDataServiceTest {

    @InjectMocks private HearingsDataServiceImpl hearingservice;

    @Mock private CaseApiService caseApiService;

    @Mock private AuthTokenGenerator authTokenGenerator;

    @Test
    public void shouldReturnHearingDetailsTest() throws IOException, ParseException {

        ApplicantTable applicantTable =
                ApplicantTable.applicantTableWith().lastName("lastName").build();
        RespondentTable respondentTable =
                RespondentTable.respondentTableWith().lastName("lastName").build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantCaseName", "PrivateLaw");
        caseDataMap.put("caseTypeOfApplication", "FL401");
        caseDataMap.put("issueDate", "test date");
        caseDataMap.put("fl401ApplicantTable", applicantTable);
        caseDataMap.put("fl401RespondentTable", respondentTable);
        CaseDetails caseDetails =
                CaseDetails.builder().id(123L).caseTypeId("PrivateLaw").data(caseDataMap).build();
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(caseApiService.getCaseDetails(anyString(), anyString(), anyString()))
                .thenReturn(caseDetails);
        String authorisation = "xyz";
        String serviceAuthorisation = "xyz";
        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();
        HearingsData hearingsResponse =
                hearingservice.getCaseData(hearingValues, authorisation, serviceAuthorisation);
        Assertions.assertEquals("BBA3", hearingsResponse.getHmctsServiceID());
    }

    @Test
    public void shouldReturnHearingDetailsTestForfl401() throws IOException, ParseException {

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantCaseName", "PrivateLaw");
        caseDataMap.put("caseTypeOfApplication", "FL401");
        caseDataMap.put("issueDate", "test date");
        CaseDetails caseDetails =
                CaseDetails.builder().id(123L).caseTypeId("PrivateLaw").data(caseDataMap).build();
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(caseApiService.getCaseDetails(anyString(), anyString(), anyString()))
                .thenReturn(caseDetails);
        String authorisation = "xyz";
        String serviceAuthorisation = "xyz";
        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();
        HearingsData hearingsResponse =
                hearingservice.getCaseData(hearingValues, authorisation, serviceAuthorisation);
        Assertions.assertEquals("BBA3", hearingsResponse.getHmctsServiceID());
    }

    @Test
    public void shouldReturnHearingDetailsTestForC100() throws IOException, ParseException {

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantCaseName", "PrivateLaw");
        caseDataMap.put("caseTypeOfApplication", "C100");
        caseDataMap.put("issueDate", "test date");
        CaseDetails caseDetails =
                CaseDetails.builder().id(123L).caseTypeId("PrivateLaw").data(caseDataMap).build();
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(caseApiService.getCaseDetails(anyString(), anyString(), anyString()))
                .thenReturn(caseDetails);
        String authorisation = "xyz";
        String serviceAuthorisation = "xyz";
        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();
        HearingsData hearingsResponse =
                hearingservice.getCaseData(hearingValues, authorisation, serviceAuthorisation);
        Assertions.assertEquals("BBA3", hearingsResponse.getHmctsServiceID());
    }

    @Test
    public void shouldReturnHearingDetailsTestForOtherCases() throws IOException, ParseException {

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantCaseName", "PrivateLaw");
        caseDataMap.put("caseTypeOfApplication", "other");
        caseDataMap.put("issueDate", "test date");
        CaseDetails caseDetails =
                CaseDetails.builder().id(123L).caseTypeId("PrivateLaw").data(caseDataMap).build();
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(caseApiService.getCaseDetails(anyString(), anyString(), anyString()))
                .thenReturn(caseDetails);
        String authorisation = "xyz";
        String serviceAuthorisation = "xyz";
        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();
        HearingsData hearingsResponse =
                hearingservice.getCaseData(hearingValues, authorisation, serviceAuthorisation);
        Assertions.assertEquals("BBA3", hearingsResponse.getHmctsServiceID());
    }
}
