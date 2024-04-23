package uk.gov.hmcts.reform.hmc.api.controllers;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.hmc.api.utils.TestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.hmc.api.utils.TestConstants.AUTOMATED_HEARINGS_ENDPOINT;
import static uk.gov.hmcts.reform.hmc.api.utils.TestConstants.CASE_REFERENCE;
import static uk.gov.hmcts.reform.hmc.api.utils.TestConstants.HEARINGS_BY_LIST_OF_CASE_IDS_ENDPOINT;
import static uk.gov.hmcts.reform.hmc.api.utils.TestConstants.HEARINGS_ENDPOINT;
import static uk.gov.hmcts.reform.hmc.api.utils.TestConstants.SERVICE_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.hmc.api.utils.TestConstants.SERVICE_HEARING_VALUES_ENDPOINT;
import static uk.gov.hmcts.reform.hmc.api.utils.TestConstants.SERVICE_LINKED_CASES_ENDPOINT;
import static uk.gov.hmcts.reform.hmc.api.utils.TestConstants.TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.hmc.api.utils.TestConstants.TEST_CASE_REFERENCE;
import static uk.gov.hmcts.reform.hmc.api.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.hmc.api.utils.TestResourceUtil.readFileFrom;

import groovy.util.logging.Slf4j;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseHearing;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingResponse;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.model.response.ServiceHearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.linkdata.HearingLinkData;
import uk.gov.hmcts.reform.hmc.api.services.HearingApiClient;
import uk.gov.hmcts.reform.hmc.api.services.HearingsDataService;
import uk.gov.hmcts.reform.hmc.api.services.HearingsService;
import uk.gov.hmcts.reform.hmc.api.services.IdamAuthService;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unchecked")
public class HearingsControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired private WebApplicationContext webApplicationContext;

    @MockBean private CoreCaseDataApi coreCaseDataApi;

    @MockBean private AuthTokenGenerator authTokenGenerator;

    @MockBean private IdamAuthService idamAuthService;

    @MockBean private HearingsDataService hearingsDataService;

    @MockBean private HearingsService hearingsService;

    @MockBean
    HearingApiClient hearingApiClient;

    private static final String HEARING_VALUES_REQUEST_BODY_JSON =
            "classpath:requests/hearing-values.json";

    private static final String LIST_OF_CASE_IDS_REQUEST_BODY_JSON =
            "classpath:requests/list-of-case-ids.json";

    private static final String AUTOMATED_HEARING_REQUEST_BODY_JSON =
            "classpath:requests/automated-hearing-request.json";

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void givenHearingServiceRequestHearingsControllerReturnOkStatus() throws Exception {

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

        ServiceHearingValues hearingsData =
                ServiceHearingValues.hearingsDataWith()
                        .hmctsServiceID("ABA5")
                        .hmctsInternalCaseName("123")
                        .publicCaseName("John Smith")
                        .caseAdditionalSecurityFlag(false)
                        .build();

        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        Mockito.when(hearingsDataService.getCaseData(any(), anyString(), anyString()))
                .thenReturn(hearingsData);
        Mockito.when(coreCaseDataApi.getCase(anyString(), anyString(), anyString()))
                .thenReturn(caseDetails);

        String hearingValuesRequestBody = readFileFrom(HEARING_VALUES_REQUEST_BODY_JSON);
        MvcResult res =
                mockMvc.perform(
                                post(SERVICE_HEARING_VALUES_ENDPOINT)
                                        .contentType(APPLICATION_JSON)
                                        .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                                        .header(
                                                SERVICE_AUTHORISATION_HEADER,
                                                TEST_SERVICE_AUTH_TOKEN)
                                        .content(hearingValuesRequestBody)
                                        .accept(APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();
        String json = res.getResponse().getContentAsString();
        assertTrue(json.contains("ABA5"));
    }

    @Test
    public void givenCaseReferenceNoHearingsControllerReturnOkStatus() throws Exception {
        HearingDaySchedule hearingDaySchedule =
                HearingDaySchedule.hearingDayScheduleWith()
                        .hearingVenueId("testVenueId")
                        .hearingJudgeId("testJudgeId")
                        .build();
        List<HearingDaySchedule> hearingDayScheduleList = new ArrayList<>();
        hearingDayScheduleList.add(hearingDaySchedule);

        CaseHearing caseHearing =
                CaseHearing.caseHearingWith()
                        .hmcStatus("LISTED")
                        .hearingDaySchedule(hearingDayScheduleList)
                        .build();
        List<CaseHearing> caseHearingList = new ArrayList<>();
        caseHearingList.add(caseHearing);

        Hearings caseHearings =
                Hearings.hearingsWith()
                        .caseRef("123")
                        .hmctsServiceCode("ABA5")
                        .caseHearings(caseHearingList)
                        .build();

        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        Mockito.when(
                        hearingsService.getHearingsByCaseRefNo(
                                anyString(), eq(TEST_AUTH_TOKEN), eq(TEST_SERVICE_AUTH_TOKEN)))
                .thenReturn(caseHearings);

        MvcResult res =
                mockMvc.perform(
                                get(HEARINGS_ENDPOINT)
                                        .contentType(APPLICATION_JSON)
                                        .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                                        .header(
                                                SERVICE_AUTHORISATION_HEADER,
                                                TEST_SERVICE_AUTH_TOKEN)
                                        .header(CASE_REFERENCE, TEST_CASE_REFERENCE)
                                        .accept(APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();
        String json = res.getResponse().getContentAsString();
        assertTrue(json.contains("testJudgeId"));
        assertTrue(json.contains("testVenueId"));
    }

    @Test
    public void givenListOfCaseIdsToFetchHearingsControllerReturnOkStatus() throws Exception {
        HearingDaySchedule hearingDaySchedule =
                HearingDaySchedule.hearingDayScheduleWith()
                        .hearingVenueId("testVenueId")
                        .hearingJudgeId("testJudgeId")
                        .build();
        List<HearingDaySchedule> hearingDayScheduleList = new ArrayList<>();
        hearingDayScheduleList.add(hearingDaySchedule);

        CaseHearing caseHearing =
                CaseHearing.caseHearingWith()
                        .hmcStatus("LISTED")
                        .hearingDaySchedule(hearingDayScheduleList)
                        .build();
        List<CaseHearing> caseHearingList = new ArrayList<>();
        caseHearingList.add(caseHearing);

        Hearings caseHearings =
                Hearings.hearingsWith()
                        .caseRef("123")
                        .hmctsServiceCode("ABA5")
                        .caseHearings(caseHearingList)
                        .build();
        List<Hearings> listOfHearings = new ArrayList<>();
        listOfHearings.add(caseHearings);

        String listOfCaseIdsRequestBody = readFileFrom(LIST_OF_CASE_IDS_REQUEST_BODY_JSON);

        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        Mockito.when(
                        hearingsService.getHearingsByListOfCaseIds(
                                anyMap(), eq(TEST_AUTH_TOKEN), eq(TEST_SERVICE_AUTH_TOKEN)))
                .thenReturn(listOfHearings);

        MvcResult res =
                mockMvc.perform(
                                get(HEARINGS_BY_LIST_OF_CASE_IDS_ENDPOINT)
                                        .contentType(APPLICATION_JSON)
                                        .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                                        .header(
                                                SERVICE_AUTHORISATION_HEADER,
                                                TEST_SERVICE_AUTH_TOKEN)
                                        .content(listOfCaseIdsRequestBody)
                                        .accept(APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();
        String json = res.getResponse().getContentAsString();
        assertTrue(json.contains("testJudgeId"));
        assertTrue(json.contains("testVenueId"));
    }

    @Test
    public void givenHearingServiceRequestToFetchLinkedCaseDataHearingsControllerReturnOkStatus()
            throws Exception {

        HearingLinkData hearingLinkData =
                HearingLinkData.hearingLinkDataWith()
                        .caseReference("testCaseRefNo")
                        .reasonsForLink(Arrays.asList())
                        .caseName("testCaseRefName")
                        .build();

        List<HearingLinkData> hearingLinkDataList = new ArrayList<>();
        hearingLinkDataList.add(hearingLinkData);

        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        Mockito.when(hearingsDataService.getHearingLinkData(any(), anyString(), anyString()))
                .thenReturn(hearingLinkDataList);
        String hearingValuesRequestBody = readFileFrom(HEARING_VALUES_REQUEST_BODY_JSON);
        MvcResult res =
                mockMvc.perform(
                                post(SERVICE_LINKED_CASES_ENDPOINT)
                                        .contentType(APPLICATION_JSON)
                                        .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                                        .header(
                                                SERVICE_AUTHORISATION_HEADER,
                                                TEST_SERVICE_AUTH_TOKEN)
                                        .content(hearingValuesRequestBody)
                                        .accept(APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();
        String json = res.getResponse().getContentAsString();
        assertTrue(json.contains("testCaseRefNo"));
        assertTrue(json.contains("testCaseRefName"));
    }
    @Test
    public void automatedHearing_creation_success() throws Exception {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        Mockito.when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        HearingResponse response = new HearingResponse();
        response.setStatus("200");
        response.setHearingRequestID("1235");
        Mockito.when(hearingApiClient.createHearingDetails(
                anyString(),
                anyString(),
                any(AutomatedHearingRequest.class))).thenReturn(response);
        String hearingValuesRequest = readFileFrom(AUTOMATED_HEARING_REQUEST_BODY_JSON);
        mockMvc.perform(
                        post(AUTOMATED_HEARINGS_ENDPOINT)
                                .contentType(APPLICATION_JSON)
                                .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                                .header(
                                        SERVICE_AUTHORISATION_HEADER,
                                        TEST_SERVICE_AUTH_TOKEN)
                                .content(hearingValuesRequest)
                                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hearingRequestID").value("1235"))
                .andExpect(jsonPath("status").value("200"))
                .andReturn();
    }

    @Test
    public void automatedHearing_creation_failure() throws Exception {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        Mockito.when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        Mockito.when(hearingApiClient.createHearingDetails(
                anyString(),
                anyString(),
                any(AutomatedHearingRequest.class))).thenThrow(new RuntimeException("some error"));
        String hearingValuesRequest = readFileFrom(AUTOMATED_HEARING_REQUEST_BODY_JSON);
        mockMvc.perform(
                        post(AUTOMATED_HEARINGS_ENDPOINT)
                                .contentType(APPLICATION_JSON)
                                .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                                .header(
                                        SERVICE_AUTHORISATION_HEADER,
                                        TEST_SERVICE_AUTH_TOKEN)
                                .content(hearingValuesRequest)
                                .accept(APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andReturn();
    }

    @Test
    public void automatedHearing_creation_unauthorised() throws Exception {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        String hearingValuesRequest = readFileFrom(AUTOMATED_HEARING_REQUEST_BODY_JSON);
        mockMvc.perform(
                        post(AUTOMATED_HEARINGS_ENDPOINT)
                                .contentType(APPLICATION_JSON)
                                .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                                .header(
                                        SERVICE_AUTHORISATION_HEADER,
                                        TEST_SERVICE_AUTH_TOKEN)
                                .content(hearingValuesRequest)
                                .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void automatedHearing_creation_unauthorised_when_s2sFailure() throws Exception {
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        String hearingValuesRequest = readFileFrom(AUTOMATED_HEARING_REQUEST_BODY_JSON);
        mockMvc.perform(
                        post(AUTOMATED_HEARINGS_ENDPOINT)
                                .contentType(APPLICATION_JSON)
                                .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                                .header(
                                        SERVICE_AUTHORISATION_HEADER,
                                        TEST_SERVICE_AUTH_TOKEN)
                                .content(hearingValuesRequest)
                                .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }
}
