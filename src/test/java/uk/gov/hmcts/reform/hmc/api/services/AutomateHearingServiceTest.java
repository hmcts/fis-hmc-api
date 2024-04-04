package uk.gov.hmcts.reform.hmc.api.services;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.joda.time.DateTime;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.mapper.AutomatedHearingTransactionRequestMapper;
import uk.gov.hmcts.reform.hmc.api.mapper.AutomatedHearingTransformer;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith({MockitoExtension.class})
@ActiveProfiles("test")
@PropertySource("classpath:application.yaml")
class AutomateHearingServiceTest {

    @InjectMocks HearingsServiceImpl hearingsService;

    @Mock private AuthTokenGenerator authTokenGenerator;

    @Mock private IdamTokenGenerator idamTokenGenerator;

    @Mock private AutomatedHearingTransformer automatedHearingTransformer;

    @Mock private AutomatedHearingRequest automatedHearingRequest;

    @Mock private AutomatedHearingTransactionRequestMapper automatedHearingTransactionRequestMapper;

    @Mock private HearingApiClient hearingApiClient;

    public static final String hearingRequestID = "123";
    DateTime hearingDate = DateTime.now();

    @Test
    void shouldReturnAutomateHearingTest() {

        CaseDetails caseDetails = getCaseDetails();

        HearingResponse hearingResponse =
            HearingResponse.builder()
                .hearingRequestID("123")
                .status("200")
                .timeStamp(DateTime.now())
                .build();


        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");

        when(hearingApiClient.createHearingDetails(anyString(), any(), any()))
                .thenReturn(hearingResponse);

        HearingResponse hearingsResponse =
                hearingsService.createAutomatedHearings(caseDetails);
        Assertions.assertEquals("123", hearingsResponse.getHearingRequestID());
        Assertions.assertEquals("200", hearingsResponse.getStatus());
    }

    private static CaseDetails getCaseDetails() {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantCaseName", "PrivateLaw");
        caseDataMap.put("caseTypeOfApplication", "C100");
        caseDataMap.put("issueDate", "test date");
        CaseDetails caseDetails =
            CaseDetails.builder().id(123L).caseTypeId("PrivateLaw").data(caseDataMap).build();
        return caseDetails;
    }


    @Test
    void shouldReturnAutomateHearingsByCaseRefNoFeignExceptionTest()
            throws IOException, ParseException {
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(hearingApiClient.createHearingDetails(anyString(), any(), any()))
                .thenThrow(feignException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Not found"));

        HearingResponse hearingResponse = hearingsService.createAutomatedHearings(null);
        Assertions.assertEquals(null, hearingResponse.getStatus());

    }



    @Test
    void shouldReturnAutomateHearingsExceptionTest()
        throws IOException, ParseException {
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");

        when(hearingApiClient.createHearingDetails(anyString(), any(), any()))
            .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));
        HearingResponse hearingResponse = hearingsService.createAutomatedHearings(null);
        Assertions.assertEquals(null, hearingResponse.getStatus());
    }

    @Test
    void shouldReturnAutomateHearingsErrorTest()
        throws IOException, ParseException {
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");

        when(hearingApiClient.createHearingDetails(anyString(), any(), any()))
            .thenThrow(new RuntimeException());
        HearingResponse hearingResponse = hearingsService.createAutomatedHearings(null);
        Assertions.assertEquals(null, hearingResponse.getStatus());
    }

    public static FeignException feignException(int status, String message) {
        return FeignException.errorStatus(
            message,
            Response.builder()
                .status(status)
                .request(Request.create(GET, EMPTY, Map.of(), new byte[] {}, UTF_8, null))
                .build());
    }
}
