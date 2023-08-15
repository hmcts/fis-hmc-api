package uk.gov.hmcts.reform.hmc.api.controllers;

import feign.FeignException;
import feign.Request;
import feign.Response;
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
import uk.gov.hmcts.reform.hmc.api.services.HearingsDataService;
import uk.gov.hmcts.reform.hmc.api.services.IdamAuthService;

import java.io.IOException;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@SuppressWarnings("unchecked")
class HearingsControllerGetHearingsLinkDataTest {

    @InjectMocks private HearingsController hearingsController;

    @Mock private IdamAuthService idamAuthService;

    @Mock private HearingsDataService hearingsDataService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
    void hearingsLinkCaseDataControllerTest() throws IOException, ParseException {

        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);

        HearingValues hearingsRequestData =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();

        ResponseEntity<Object> hearingsLinkCaseDataLst =
                hearingsController.getHearingsLinkData("Auth", "sauth", hearingsRequestData);
        Assertions.assertEquals(HttpStatus.OK, hearingsLinkCaseDataLst.getStatusCode());
    }

    @Test
    void hearingsLinkCaseDataControllerUnauthorisedExceptionTest()
            throws IOException, ParseException {

        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();

        ResponseEntity<Object> hearingsData1 =
                hearingsController.getHearingsLinkData("", "", hearingValues);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, hearingsData1.getStatusCode());
    }

    @Test
    void hearingsLinkCaseDataControllerFeignExceptionTest() throws IOException, ParseException {

        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);

        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();

        Mockito.when(hearingsDataService.getHearingLinkData(hearingValues, "", ""))
                .thenThrow(feignException(HttpStatus.BAD_REQUEST.value(), "Not found"));

        ResponseEntity<Object> hearingsData1 =
                hearingsController.getHearingsLinkData("", "", hearingValues);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, hearingsData1.getStatusCode());
    }

    @Test
    void hearingsLinkCaseDataControllerInternalServiceErrorTest()
            throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);

        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();

        Mockito.when(
                        hearingsDataService.getHearingLinkData(
                                hearingValues, "Authorization", "ServiceAuthorization"))
                .thenThrow(feignException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Not found"));

        ResponseEntity<Object> hearingsData1 =
                hearingsController.getHearingsLinkData("", "", hearingValues);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, hearingsData1.getStatusCode());
    }
}
