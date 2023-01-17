package uk.gov.hmcts.reform.hmc.api.controllers;

import static uk.gov.hmcts.reform.hmc.api.utils.TestResourceUtil.readFileFrom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.utils.ServiceAuthenticationGenerator;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application_e2e.yaml")
public class HearingsContFunctionalTest {

    public static final String SERV_AUTH_HEADER = "ServiceAuthorization";
    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String TEST_LOCAL_HOST = "http://localhost:4550";
    public static final String FIS_TEST_URL = "CASE_API_TEST_URL";

    private static final String HEARING_VALUES_REQUEST_BODY_JSON =
            "classpath:requests/hearing-values.json";

    @Autowired ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired protected IdamTokenGenerator idamTokenGenerator;
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
                request.header("Authorization", idamTokenGenerator.generateIdamTokenForRefData())
                        .header(SERV_AUTH_HEADER, serviceAuthenticationGenerator.generate())
                        .when()
                        .contentType(JSON_CONTENT_TYPE)
                        .body(hearingValuesRequest)
                        .post("serviceHearingValues");

        response.then().assertThat().statusCode(HttpStatus.OK.value());
    }

    @Test
    public void givenHearingValuesWhenGetHearingsLinkCasesThen200Response() throws Exception {
        String hearingValuesRequest = readFileFrom(HEARING_VALUES_REQUEST_BODY_JSON);
        Response response =
                request.header("Authorization", idamTokenGenerator.generateIdamTokenForRefData())
                        .header(SERV_AUTH_HEADER, serviceAuthenticationGenerator.generate())
                        .when()
                        .contentType(JSON_CONTENT_TYPE)
                        .body(hearingValuesRequest)
                        .post("serviceLinkedCases");

        response.then().assertThat().statusCode(HttpStatus.OK.value());
    }

    @Test
    public void givenCaseRefNoWhenGetHearingsThen200Response() throws Exception {
        Response response =
                request.header("Authorization", idamTokenGenerator.generateIdamTokenForRefData())
                        .header(SERV_AUTH_HEADER, serviceAuthenticationGenerator.generate())
                        .header("caseReference", "1667867755895004")
                        .when()
                        .contentType(JSON_CONTENT_TYPE)
                        .get("hearings");

        response.then().assertThat().statusCode(HttpStatus.OK.value());
    }
}
