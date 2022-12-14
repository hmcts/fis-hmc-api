package uk.gov.hmcts.reform.hmc.api.controllers;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import feign.FeignException;
import feign.Request;
import feign.Response;
import java.io.IOException;
import java.util.Map;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.model.response.ServiceHearingValues;
import uk.gov.hmcts.reform.hmc.api.services.HearingsDataService;
import uk.gov.hmcts.reform.hmc.api.services.HearingsService;
import uk.gov.hmcts.reform.hmc.api.services.IdamAuthService;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@SuppressWarnings("unchecked")
class HearingsControllerTest {

    @InjectMocks private HearingsController hearingsController;

    @Mock private IdamAuthService idamAuthService;

    @Mock private HearingsDataService hearingsDataService;

    @Mock private HearingsService hearingsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void hearingsDataControllerTest() throws IOException, ParseException {

        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);

        ServiceHearingValues hearingsData =
                ServiceHearingValues.hearingsDataWith()
                        .hmctsServiceID("BBA3")
                        .hmctsInternalCaseName("123")
                        .publicCaseName("John Smith")
                        .caseAdditionalSecurityFlag(false)
                        .build();

        Mockito.when(hearingsDataService.getCaseData(any(), anyString(), anyString()))
                .thenReturn(hearingsData);

        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();

        ResponseEntity<Object> hearingsData1 =
                hearingsController.getHearingsData("Auth", "sauth", hearingValues);
        Assertions.assertEquals(HttpStatus.OK, hearingsData1.getStatusCode());
    }

    @Test
    void hearingsDataControllerUnauthorisedExceptionTest() throws IOException, ParseException {

        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();

        ResponseEntity<Object> hearingsData1 =
                hearingsController.getHearingsData("", "", hearingValues);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, hearingsData1.getStatusCode());
    }

    static FeignException feignException(int status, String message) {
        return FeignException.errorStatus(
                message,
                Response.builder()
                        .status(status)
                        .request(Request.create(GET, EMPTY, Map.of(), new byte[] {}, UTF_8, null))
                        .build());
    }

    @Test
    void hearingsDataControllerUnauthorisedFeignExceptionTest() throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);

        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();

        Mockito.when(hearingsDataService.getCaseData(hearingValues, "", ""))
                .thenThrow(feignException(HttpStatus.BAD_REQUEST.value(), "Not found"));

        ResponseEntity<Object> hearingsData1 =
                hearingsController.getHearingsData("", "", hearingValues);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, hearingsData1.getStatusCode());
    }

    @Test
    void hearingsDataControllerInternalServiceErrorTest() throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);

        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();

        Mockito.when(
                        hearingsDataService.getCaseData(
                                hearingValues, "Authorization", "ServiceAuthorization"))
                .thenThrow(feignException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Not found"));

        ResponseEntity<Object> hearingsData1 =
                hearingsController.getHearingsData("", "", hearingValues);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, hearingsData1.getStatusCode());
    }

    @Test
    void hearingsControllerTest() throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Hearings hearings = Hearings.hearingsWith().caseRef("123").hmctsServiceCode("BBA3").build();
        Mockito.when(hearingsService.getHearingsByCaseRefNo(anyString())).thenReturn(hearings);
        ResponseEntity<Object> hearingsResponse =
                hearingsController.getHearingsByCaseRefNo("Auth", "sauth", "caseRef");
        Assertions.assertEquals("123", ((Hearings) hearingsResponse.getBody()).getCaseRef());
    }

    @Test
    void hearingsByCaseRefNoControllerUnauthorisedExceptionTest()
            throws IOException, ParseException {

        ResponseEntity<Object> hearingsData1 =
                hearingsController.getHearingsByCaseRefNo("", "", "caseRef");

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, hearingsData1.getStatusCode());
    }

    @Test
    void hearingsByCaseRefNoControllerFeignExceptionTest() throws IOException, ParseException {

        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);

        Mockito.when(hearingsService.getHearingsByCaseRefNo(""))
                .thenThrow(feignException(HttpStatus.BAD_REQUEST.value(), "Not found"));

        ResponseEntity<Object> hearingsData1 =
                hearingsController.getHearingsByCaseRefNo("", "", "");

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, hearingsData1.getStatusCode());
    }

    @Test
    void hearingsByCaseRefNoControllerInternalServiceErrorTest()
            throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);

        Mockito.when(hearingsService.getHearingsByCaseRefNo(""))
                .thenThrow(feignException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Not found"));

        ResponseEntity<Object> hearingsData1 =
                hearingsController.getHearingsByCaseRefNo("", "", "caseRef");

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, hearingsData1.getStatusCode());
    }
}
