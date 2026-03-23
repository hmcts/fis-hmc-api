package uk.gov.hmcts.reform.hmc.api.controllers;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.model.ccd.NextHearingDetails;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.services.HearingsService;
import uk.gov.hmcts.reform.hmc.api.services.IdamAuthService;
import uk.gov.hmcts.reform.hmc.api.services.NextHearingDetailsService;

import java.time.LocalDateTime;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class HearingControllerNextHearingDetailsTest {

    @InjectMocks private HearingsController hearingsController;

    @Mock private IdamAuthService idamAuthService;

    @Mock private AuthTokenGenerator authTokenGenerator;

    @Mock private HearingsService hearingsService;

    @Mock private NextHearingDetailsService nextHearingDetailsService;

    @Test
    void hearingsControllerNextHearingDateTest() {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        ResponseEntity<Object> nextHearingDetailsResponse =
            hearingsController.updateNextHearingDetails(
                "Bearer auth", "Bearer sauth", "caseRef");
        Assertions.assertEquals(HttpStatus.OK, nextHearingDetailsResponse.getStatusCode());
    }

    @Test
    void nextHearingDateByCaseRefNoControllerUnauthorisedExceptionTest() {
        ResponseEntity<Object> nextHearingDetails =
            hearingsController.updateNextHearingDetails("", "", "caseRef");
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, nextHearingDetails.getStatusCode());
    }

    @Test
    void nextHearingDateByCaseRefNoControllerFeignExceptionTest() {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        Mockito.when(nextHearingDetailsService.updateNextHearingDetails(Mockito.eq("auth"), Mockito.any()))
            .thenThrow(feignException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Not found"));
        ResponseEntity<Object> nextHearingUpdateResp =
            hearingsController.updateNextHearingDetails("auth", "sauth", "testcase");
        Assertions.assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR, nextHearingUpdateResp.getStatusCode());
    }

    @Test
    void nextHearingDateByCaseRefNoInternalServerErrorTest() {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        Mockito.when(nextHearingDetailsService.updateNextHearingDetails(Mockito.eq("auth"), Mockito.any()))
            .thenThrow(new RuntimeException());
        ResponseEntity<Object> nextHearingUpdateResp =
            hearingsController.updateNextHearingDetails("auth", "sauth", "testcase");
        Assertions.assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR, nextHearingUpdateResp.getStatusCode());
    }

    @Test
    void getNextHearingDateTest() {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        LocalDateTime testNextHearingDate = LocalDateTime.of(2024, 4, 28, 1, 0);
        NextHearingDetails nextHearingDetails =
            NextHearingDetails.builder()
                .hearingID(123L)
                .hearingDateTime(testNextHearingDate)
                .build();
        Mockito.when(nextHearingDetailsService.getNextHearingDate(Mockito.any()))
            .thenReturn(nextHearingDetails);
        Mockito.when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        Hearings hearingsObj = Hearings.hearingsWith().caseRef("123").hmctsServiceCode("BBA3").build();
        Mockito.when(hearingsService.getHearingsByCaseRefNo("caseRef", "Bearer auth", "MOCK_S2S_TOKEN"))
            .thenReturn(hearingsObj);
        ResponseEntity<Object> nextHearingDetailsResponse =
            hearingsController.getNextHearingDate("Bearer auth", "Bearer sauth", "caseRef");
        Assertions.assertEquals(HttpStatus.OK, nextHearingDetailsResponse.getStatusCode());
    }

    @Test
    void getNextHearingDateInternalServiceErrorTest() {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        Mockito.when(nextHearingDetailsService.getNextHearingDate(Mockito.any()))
            .thenThrow(new RuntimeException());
        Mockito.when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        ResponseEntity<Object> nextHearingDetailsResponse =
            hearingsController.getNextHearingDate("", "", "caseRef");
        Assertions.assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR, nextHearingDetailsResponse.getStatusCode());
    }

    @Test
    void getNextHearingDateFeignExceptionTest() {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        Mockito.when(nextHearingDetailsService.getNextHearingDate(Mockito.any()))
            .thenThrow(feignException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Not found"));
        ResponseEntity<Object> nextHearingDetailsResponse =
            hearingsController.getNextHearingDate("auth", "sauth", "testcase");
        Assertions.assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR, nextHearingDetailsResponse.getStatusCode());
    }

    @Test
    void getNextHearingDateUnauthorisedExceptionTest() {
        ResponseEntity<Object> nextHearingDetails =
            hearingsController.getNextHearingDate("", "", "caseRef");

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, nextHearingDetails.getStatusCode());
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
