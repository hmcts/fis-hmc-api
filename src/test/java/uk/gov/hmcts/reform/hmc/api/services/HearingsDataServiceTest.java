package uk.gov.hmcts.reform.hmc.api.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.hmc.api.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.ServiceHearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.linkdata.HearingLinkData;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HearingsDataServiceTest {

    @InjectMocks private HearingsDataServiceImpl hearingservice;

    @Mock private CaseApiService caseApiService;

    @Mock private CaseFlagV2DataServiceImpl caseFlagV2DataService;

    @Mock private AuthTokenGenerator authTokenGenerator;

    @Mock ResourceLoader resourceLoader;

    @Mock private ElasticSearch elasticSearch;

    @Mock private Resource mockResource;

    private InputStream inputStream;

    @BeforeAll
    public void setup() {
        inputStream = getClass().getResourceAsStream("/ScreenFlow.json");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnHearingDetailsTest() throws IOException, ParseException {

        ReflectionTestUtils.setField(
                hearingservice,
                "ccdBaseUrl",
                "https://manage-case.demo.platform.hmcts.net/cases/case-details/");

        ReflectionTestUtils.setField(hearingservice, "resourceLoader", resourceLoader);

        when(resourceLoader.getResource(any())).thenReturn(mockResource);
        when(mockResource.getInputStream()).thenReturn(inputStream);

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
        Assertions.assertEquals("lastName and lastName", hearingsResponse.getPublicCaseName());
        Assertions.assertEquals("https://manage-case-hearings-int.demo.platform.hmcts.net/cases/case-details/123#Case File View", hearingsResponse.getCaseDeepLink());
        Assertions.assertFalse(hearingsResponse.getCaseCategories().isEmpty());

    }

    @Test
    public void shouldReturnHearingDetailsTestForfl401() throws IOException, ParseException {

        ReflectionTestUtils.setField(
                hearingservice,
                "ccdBaseUrl",
                "https://manage-case.demo.platform.hmcts.net/cases/case-details/");



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
        caseFlagV2DataService.setCaseFlagData(serviceHearingValues, caseDetails);
        String authorisation = "xyz";
        String serviceAuthorisation = "xyz";
        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();
        ServiceHearingValues hearingsResponse =
                hearingservice.getCaseData(hearingValues, authorisation, serviceAuthorisation);
        Assertions.assertEquals("ABA5", hearingsResponse.getHmctsServiceID());
        verify(caseFlagV2DataService, times(1)).setCaseFlagsV2Data(any(), any());
    }

    @Test
    public void shouldReturnHearingDetailsTestForC100() throws IOException, ParseException {
        ReflectionTestUtils.setField(
                hearingservice,
                "ccdBaseUrl",
                "https://manage-case.demo.platform.hmcts.net/cases/case-details/");
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
        Assertions.assertEquals("Re-Minor", hearingsResponse.getPublicCaseName());
        Assertions.assertNotNull(hearingsResponse.getCaseDeepLink());
        verify(caseFlagV2DataService, times(2)).setCaseFlagData(any(), any());
    }

    @Test
    public void shouldReturnHearingDetailsTestForOtherCases() throws IOException, ParseException {
        ReflectionTestUtils.setField(
                hearingservice,
                "ccdBaseUrl",
                "https://manage-case.demo.platform.hmcts.net/cases/case-details/");
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
        ReflectionTestUtils.setField(
                hearingservice,
                "ccdBaseUrl",
                "https://manage-case.demo.platform.hmcts.net/cases/case-details/");
        LinkedHashMap reasonMap = new LinkedHashMap();
        reasonMap.put("Reason", "CLRC017");

        LinkedHashMap reasonForLinkMap = new LinkedHashMap();
        reasonForLinkMap.put("value", reasonMap);

        List reasonForLinkList = new ArrayList();
        reasonForLinkList.add(reasonForLinkMap);

        LinkedHashMap valueMap = new LinkedHashMap();
        valueMap.put("ReasonForLink", reasonForLinkList);
        valueMap.put("CaseReference", "123");

        LinkedHashMap caseLinkMap = new LinkedHashMap();
        caseLinkMap.put("value", valueMap);

        List caseLinksList = new ArrayList<>();
        caseLinksList.add(caseLinkMap);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantCaseName", "Test Case 1 DA 31");
        caseDataMap.put("caseLinks", caseLinksList);
        CaseDetails caseDetails =
                CaseDetails.builder().id(123L).caseTypeId("PrivateLaw").data(caseDataMap).build();
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(caseApiService.getCaseDetails(anyString(), anyString(), anyString()))
                .thenReturn(caseDetails);

        List<CaseDetails> cases = new ArrayList<>();
        cases.add(caseDetails);
        SearchResult searchResult = SearchResult.builder().cases(cases).build();
        when(elasticSearch.searchCases(anyString(),
                                       eq("{\"query\":{\"terms\":{\"boost\":null,\"reference\":[\"123\"]}},\"size\":null}"),
                                       any(), any()))
                .thenReturn(searchResult);
        String authorisation = "xyz";
        String serviceAuthorisation = "xyz";
        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();
        List<HearingLinkData> lst =
                hearingservice.getHearingLinkData(
                        hearingValues, authorisation, serviceAuthorisation);
        Assertions.assertFalse(lst.isEmpty());
        Assertions.assertEquals("Test Case 1 DA 31", lst.get(0).getCaseName());
        Assertions.assertNotNull(lst.get(0).getReasonsForLink());
        Assertions.assertNotNull(searchResult);
    }

    @AfterAll
    public void closeFile() throws IOException {
        inputStream.close();
    }


}
