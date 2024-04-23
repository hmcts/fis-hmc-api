package uk.gov.hmcts.reform.hmc.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.hmc.api.utils.TestResourceUtil.readFileFrom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingResponse;
import uk.gov.hmcts.reform.hmc.api.services.HearingApiClient;
import uk.gov.hmcts.reform.hmc.api.utils.ServiceAuthenticationGenerator;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application_e2e.yaml")
public class HearingsContFunctionalTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    public static final String SERV_AUTH_HEADER = "ServiceAuthorization";

    public static final String AUTHORIZATION = "Authorization";
    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String TEST_LOCAL_HOST = "http://localhost:4550";
    public static final String FIS_TEST_URL = "CASE_API_TEST_URL";

    private static final String HEARING_VALUES_REQUEST_BODY_JSON =
            "classpath:requests/hearing-values.json";

    private static final String AUTOMATED_HEARING_REQUEST_BODY_JSON =
            "classpath:requests/automated-hearing-request.json";


    @Autowired ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired protected IdamTokenGenerator idamTokenGenerator;

    @MockBean
    HearingApiClient hearingApiClient;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String targetInstance =
            StringUtils.defaultIfBlank(System.getenv(FIS_TEST_URL), TEST_LOCAL_HOST);

    private final RequestSpecification request =
            RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Before
    public void setUp() {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    @Test
    @Disabled
    public void givenHearingValuesWhenGetHearingsDataThen200Response() throws Exception {
        String hearingValuesRequest = readFileFrom(HEARING_VALUES_REQUEST_BODY_JSON);
        Response response =
                request.header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForRefData())
                        .header(SERV_AUTH_HEADER, serviceAuthenticationGenerator.generate())
                        .when()
                        .contentType(JSON_CONTENT_TYPE)
                        .body(hearingValuesRequest)
                        .post("serviceHearingValues");

        response.then().assertThat().statusCode(HttpStatus.OK.value());
    }

    @Test
    @Disabled
    public void givenHearingValuesWhenGetHearingsLinkCasesThen200Response() throws Exception {
        String hearingValuesRequest = readFileFrom(HEARING_VALUES_REQUEST_BODY_JSON);
        Response response =
                request.header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForRefData())
                        .header(SERV_AUTH_HEADER, serviceAuthenticationGenerator.generate())
                        .when()
                        .contentType(JSON_CONTENT_TYPE)
                        .body(hearingValuesRequest)
                        .post("serviceLinkedCases");

        response.then().assertThat().statusCode(HttpStatus.OK.value());
    }

    @Test
    @Disabled
    public void givenCaseRefNoWhenGetHearingsThen200Response() throws Exception {
        Response response =
                request.header(AUTHORIZATION, idamTokenGenerator.getSysUserToken())
                        .header(SERV_AUTH_HEADER, serviceAuthenticationGenerator.generate())
                        .header("caseReference", "1675335865166401")
                        .when()
                        .contentType(JSON_CONTENT_TYPE)
                        .get("hearings");

        response.then().assertThat().statusCode(HttpStatus.OK.value());
    }

    @Test
    public void automatedHearingCreationSuccess() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
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
    public void automatedHearingCreationFailure() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
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
    public void automatedHearingCreationUnauthorised() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        String hearingValuesRequest = readFileFrom(AUTOMATED_HEARING_REQUEST_BODY_JSON);
        request.header("Authorization","incorrectAuth")
                .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
                .when()
                .contentType(JSON_CONTENT_TYPE)
                .body(hearingValuesRequest)
                .post("automated-hearing")
                .then().assertThat().statusCode(401);
    }

    @Test
    public void automatedHearingCreationUnauthorisedWhenS2sIsIncorrect() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        String hearingValuesRequest = readFileFrom(AUTOMATED_HEARING_REQUEST_BODY_JSON);
        request.header("Authorization", idamTokenGenerator.generateIdamTokenForRefData())
                .header("ServiceAuthorization", "inCorrectS2s")
                .when()
                .contentType(JSON_CONTENT_TYPE)
                .body(hearingValuesRequest)
                .post("automated-hearing")
                .then().assertThat().statusCode(401);
    }
}
