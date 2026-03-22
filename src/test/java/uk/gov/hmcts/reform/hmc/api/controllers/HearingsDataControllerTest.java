package uk.gov.hmcts.reform.hmc.api.controllers;

import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.ServiceHearingValues;
import uk.gov.hmcts.reform.hmc.api.services.HearingsDataService;
import uk.gov.hmcts.reform.hmc.api.services.IdamAuthService;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static uk.gov.hmcts.reform.hmc.api.controllers.HearingsControllerTest.feignException;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class HearingsDataControllerTest {

    @InjectMocks
    private HearingsController hearingsController;

    @Spy
    private final IdamAuthService idamAuthService = Mockito.mock(IdamAuthService.class);

    @Spy private HearingsDataService hearingsDataService;

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
    void hearingsDataControllerInternalServerErrorTest() throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);

        HearingValues hearingValues =
            HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();
        Mockito.when(hearingsDataService.getCaseData(hearingValues, "", ""))
            .thenThrow(new RuntimeException());
        ResponseEntity<Object> hearingsData1 =
            hearingsController.getHearingsData("", "", hearingValues);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, hearingsData1.getStatusCode());
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
            hearingsController.getHearingsData("Authorization", "ServiceAuthorization", hearingValues);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, hearingsData1.getStatusCode());
    }
}
