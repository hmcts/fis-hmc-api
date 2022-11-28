package uk.gov.hmcts.reform.hmc.api.controllers;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mockito.ArgumentMatchers.any;

import feign.FeignException;
import feign.Request;
import feign.Response;
import java.io.IOException;
import java.util.List;
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
import uk.gov.hmcts.reform.hmc.api.model.response.ServiceHearingValues;
import uk.gov.hmcts.reform.hmc.api.services.AuthorisationService;
import uk.gov.hmcts.reform.hmc.api.services.HearingsDataService;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@SuppressWarnings("unchecked")
public class HearingsControllerGetHearingsLinkDataTest {

    @InjectMocks private HearingsController hearingsController;

    @Mock private AuthorisationService authorisationService;

    @Mock private HearingsDataService hearingsDataService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    public static FeignException feignException(int status, String message) {
        return FeignException.errorStatus(
                message,
                Response.builder()
                        .status(status)
                        .request(Request.create(GET, EMPTY, Map.of(), new byte[] {}, UTF_8, null))
                        .build());
    }

    @Test
    public void hearingsLinkCaseDataControllerTest() throws IOException, ParseException {

        Mockito.when(authorisationService.authoriseService(any())).thenReturn(Boolean.TRUE);

        HearingValues hearingsRequestData =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();

        ResponseEntity<List> hearingsLinkCaseDataLst =
                hearingsController.getHearingsLinkData("Auth", "sauth", hearingsRequestData);
        Assertions.assertEquals(HttpStatus.OK, hearingsLinkCaseDataLst.getStatusCode());
    }

    @Test
    public void hearingsLinkCaseDataControllerUnauthorisedExceptionTest()
            throws IOException, ParseException {

        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();

        ResponseEntity<ServiceHearingValues> hearingsData1 =
                hearingsController.getHearingsLinkData("", "", hearingValues);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, hearingsData1.getStatusCode());
    }

    @Test
    public void hearingsLinkCaseDataControllerFeignExceptionTest()
            throws IOException, ParseException {

        Mockito.when(authorisationService.authoriseService(any())).thenReturn(true);

        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();

        Mockito.when(hearingsDataService.getHearingLinkData(hearingValues, "", ""))
                .thenThrow(feignException(HttpStatus.BAD_REQUEST.value(), "Not found"));

        ResponseEntity<ServiceHearingValues> hearingsData1 =
                hearingsController.getHearingsLinkData("", "", hearingValues);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, hearingsData1.getStatusCode());
    }

    @Test
    public void hearingsLinkCaseDataControllerInternalServiceErrorTest()
            throws IOException, ParseException {
        Mockito.when(authorisationService.authoriseService(any())).thenReturn(true);

        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();

        Mockito.when(
                        hearingsDataService.getHearingLinkData(
                                hearingValues, "Authorization", "ServiceAuthorization"))
                .thenThrow(feignException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Not found"));

        ResponseEntity<ServiceHearingValues> hearingsData1 =
                hearingsController.getHearingsLinkData("", "", hearingValues);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, hearingsData1.getStatusCode());
    }
}
