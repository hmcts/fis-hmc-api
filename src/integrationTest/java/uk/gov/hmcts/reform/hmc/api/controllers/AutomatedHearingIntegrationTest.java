package uk.gov.hmcts.reform.hmc.api.controllers;

import groovy.util.logging.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingResponse;
import uk.gov.hmcts.reform.hmc.api.services.HearingApiClient;
import uk.gov.hmcts.reform.hmc.api.services.IdamAuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.hmc.api.utils.TestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.hmc.api.utils.TestConstants.AUTOMATED_HEARINGS_ENDPOINT;
import static uk.gov.hmcts.reform.hmc.api.utils.TestConstants.SERVICE_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.hmc.api.utils.TestConstants.TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.hmc.api.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.hmc.api.utils.TestResourceUtil.readFileFrom;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class AutomatedHearingIntegrationTest {


    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean private AuthTokenGenerator authTokenGenerator;

    @MockBean private IdamAuthService idamAuthService;

    @MockBean
    HearingApiClient hearingApiClient;

    private static final String AUTOMATED_HEARING_REQUEST_BODY_JSON =
            "classpath:requests/automated-hearing-request.json";

    @BeforeEach
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void automatedHearing_creation_success() throws Exception {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        Mockito.when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        HearingResponse response = new HearingResponse();
        response.setStatus("200");
        response.setHearingRequestID("1235");
        when(hearingApiClient.createHearingDetails(
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
        when(hearingApiClient.createHearingDetails(
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
