package uk.gov.hmcts.reform.hmc.api.controllers;


import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingResponse;
import uk.gov.hmcts.reform.hmc.api.services.HearingApiClient;
import uk.gov.hmcts.reform.hmc.api.utils.ServiceAuthenticationGenerator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.hmc.api.utils.TestResourceUtil.readFileFrom;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class AutomatedHearingFunctionalTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @MockBean
    HearingApiClient hearingApiClient;

    private static final String AUTOMATED_HEARING_REQUEST_BODY_JSON =
            "classpath:requests/automated-hearing-request.json";
    private final String targetInstance =
            StringUtils.defaultIfBlank(
                    System.getenv("TEST_URL"),
                    "http://localhost:4550"
            );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @BeforeEach
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }


    @Test
    public void automatedHearing_creation_success() throws Exception {
        HearingResponse response = new HearingResponse();
        response.setStatus("200");
        response.setHearingRequestID("1235");
        when(hearingApiClient.createHearingDetails(
                anyString(),
                anyString(),
                any(AutomatedHearingRequest.class))).thenReturn(response);
        String hearingValuesRequest = readFileFrom(AUTOMATED_HEARING_REQUEST_BODY_JSON);
        mockMvc.perform(post("/automated-hearing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", idamTokenGenerator.generateIdamTokenForRefData())
                        .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
                        .content(hearingValuesRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hearingRequestID").value("1235"))
                .andExpect(jsonPath("status").value("200"))
                .andReturn();
    }

    @Test
    public void automatedHearing_creation_failure() throws Exception {
        when(hearingApiClient.createHearingDetails(
                anyString(),
                anyString(),
                any(AutomatedHearingRequest.class))).thenThrow(new RuntimeException("some error"));
        String hearingValuesRequest = readFileFrom(AUTOMATED_HEARING_REQUEST_BODY_JSON);
        mockMvc.perform(post("/automated-hearing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", idamTokenGenerator.generateIdamTokenForRefData())
                        .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
                        .content(hearingValuesRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andReturn();
    }


    @Test
    public void automatedHearing_creation_unauthorised() throws Exception {
        String hearingValuesRequest = readFileFrom(AUTOMATED_HEARING_REQUEST_BODY_JSON);
        mockMvc.perform(post("/automated-hearing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "failure")
                        .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
                        .content(hearingValuesRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void automatedHearing_creation_unauthorised_when_s2sIsIncorrect() throws Exception {
        String hearingValuesRequest = readFileFrom(AUTOMATED_HEARING_REQUEST_BODY_JSON);
        mockMvc.perform(post("/automated-hearing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", idamTokenGenerator.generateIdamTokenForRefData())
                        .header("ServiceAuthorization", "incorrects2s")
                        .content(hearingValuesRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }
}
