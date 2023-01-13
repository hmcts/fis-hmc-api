package uk.gov.hmcts.reform.hmc.api.services;

import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.ServiceHearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.linkdata.HearingLinkData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class ServiceHearingValuesServiceTest {

    @InjectMocks private HearingsDataServiceImpl hearingservice;

    @Mock private CaseApiService caseApiService;

    @Mock private CaseFlagDataServiceImpl caseFlagDataService;

    @Mock private AuthTokenGenerator authTokenGenerator;

    @Mock ResourceLoader resourceLoader;

    @Mock private  ElasticSearch elasticSearch;

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnHearingDetailsTest() throws IOException, ParseException {

        LinkedHashMap applicantMap = new LinkedHashMap();
        applicantMap.put("lastName", "lastName");

        LinkedHashMap respondentMap = new LinkedHashMap();
        respondentMap.put("lastName", "lastName");

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantCaseName", "PrivateLaw");
        caseDataMap.put("caseTypeOfApplication", "FL401");
        caseDataMap.put("issueDate", "test date");
        caseDataMap.put("fl401ApplicantTable", applicantMap);
        caseDataMap.put("fl401RespondentTable", respondentMap);
        CaseDetails caseDetails =
                CaseDetails.builder().id(123L).caseTypeId("PrivateLaw").data(caseDataMap).build();
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(caseApiService.getCaseDetails(anyString(), anyString(), anyString()))
                .thenReturn(caseDetails);
        String authorisation = "xyz";
        String serviceAuthorisation = "xyz";
        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();
        ServiceHearingValues hearingsResponse =
                hearingservice.getCaseData(hearingValues, authorisation, serviceAuthorisation);
        hearingservice.getCaseData(hearingValues, authorisation, serviceAuthorisation);
        Assertions.assertEquals("ABA5", hearingsResponse.getHmctsServiceID());
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
        ServiceHearingValues serviceHearingValues = ServiceHearingValues.hearingsDataWith().build();
        caseFlagDataService.setCaseFlagData(serviceHearingValues, caseDetails);

        String authorisation = "xyz";
        String serviceAuthorisation = "xyz";
        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();
        ServiceHearingValues hearingsResponse =
                hearingservice.getCaseData(hearingValues, authorisation, serviceAuthorisation);
        Assertions.assertEquals("ABA5", hearingsResponse.getHmctsServiceID());
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
        ServiceHearingValues hearingsResponse =
                hearingservice.getCaseData(hearingValues, authorisation, serviceAuthorisation);
        Assertions.assertEquals("ABA5", hearingsResponse.getHmctsServiceID());
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
        ServiceHearingValues hearingsResponse =
                hearingservice.getCaseData(hearingValues, authorisation, serviceAuthorisation);
        Assertions.assertEquals("ABA5", hearingsResponse.getHmctsServiceID());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnHearingLinkDetailsTest() throws IOException, ParseException {

        LinkedHashMap reasonMap = new LinkedHashMap();
        reasonMap.put("Reason", "CLRC017");

        LinkedHashMap reasonForLinkMap = new LinkedHashMap();
        reasonForLinkMap.put("value", reasonMap);

        List reasonForLinkList = new ArrayList();
        reasonForLinkList.add(reasonForLinkMap);

        LinkedHashMap valueMap = new LinkedHashMap();
        valueMap.put("ReasonForLink", reasonForLinkList);

        LinkedHashMap caseLinkMap = new LinkedHashMap();
        caseLinkMap.put("value", valueMap);

        List caseLinksList = new ArrayList();
        caseLinksList.add(caseLinkMap);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantCaseName", "Test Case 1 DA 31");
        caseDataMap.put("caseLinks", caseLinksList);
        CaseDetails caseDetails =
                CaseDetails.builder().id(123L).caseTypeId("PrivateLaw").data(caseDataMap).build();
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(caseApiService.getCaseDetails(anyString(), anyString(), anyString()))
                .thenReturn(caseDetails);
        SearchResult searchResult = SearchResult.builder().build();
        when(elasticSearch.searchCases(anyString(),anyString(),any(),any())).thenReturn(searchResult);
        String authorisation = "xyz";
        String serviceAuthorisation = "xyz";
        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();
        List<HearingLinkData> lst =
                hearingservice.getHearingLinkData(
                        hearingValues, authorisation, serviceAuthorisation);
        // Assertions.assertEquals("Test Case 1 DA 31", lst.get(0).caseName);
        Assertions.assertFalse(lst.isEmpty());
    }
}
