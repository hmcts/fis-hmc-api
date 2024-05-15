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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.model.ccd.NextHearingDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseHearing;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.model.response.ServiceHearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.linkdata.HearingLinkData;
import uk.gov.hmcts.reform.hmc.api.services.HearingsDataService;
import uk.gov.hmcts.reform.hmc.api.services.HearingsService;
import uk.gov.hmcts.reform.hmc.api.services.IdamAuthService;
import uk.gov.hmcts.reform.hmc.api.services.NextHearingDetailsService;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@SuppressWarnings("unchecked")
class HearingsControllerTest {

    @InjectMocks private HearingsController hearingsController;

    @Mock private IdamAuthService idamAuthService;

    @Mock private AuthTokenGenerator authTokenGenerator;

    @Mock private HearingsDataService hearingsDataService;

    @Mock private HearingsService hearingsService;

    @Mock private NextHearingDetailsService nextHearingDetailsService;

    private Hearings hearings;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
        LocalDateTime testNextHearingDate = LocalDateTime.of(2024, 04, 28, 1, 0);

        HearingDaySchedule hearingDaySchedule =
                HearingDaySchedule.hearingDayScheduleWith()
                        .hearingVenueId("231596")
                        .hearingJudgeId("4925644")
                        .hearingStartDateTime(testNextHearingDate)
                        .build();
        List<HearingDaySchedule> hearingDayScheduleList = new ArrayList<>();
        hearingDayScheduleList.add(hearingDaySchedule);

        CaseHearing caseHearing =
                CaseHearing.caseHearingWith()
                        .hmcStatus("LISTED")
                        .hearingDaySchedule(hearingDayScheduleList)
                        .build();
        List<CaseHearing> caseHearingList = new ArrayList<>();
        caseHearingList.add(caseHearing);
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
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        Hearings hearings = Hearings.hearingsWith().caseRef("123").hmctsServiceCode("BBA3").build();
        Mockito.when(hearingsService.getHearingsByCaseRefNo("caseRef", "Auth", "sauth"))
                .thenReturn(hearings);
        ResponseEntity<Object> hearingsResponse =
                hearingsController.getHearingsByCaseRefNo("auth", "sauth", "caseRef");
        Assertions.assertNotNull(hearingsResponse.getBody());
    }

    @Test
    void hearingsByCaseRefNoControllerUnauthorisedExceptionTest()
            throws IOException, ParseException {

        ResponseEntity<Object> hearingsData1 =
                hearingsController.getHearingsByCaseRefNo("auth", "sauth", "caseRef");

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, hearingsData1.getStatusCode());
    }

    @Test
    void hearingsByCaseRefNoControllerFeignExceptionTest() throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(true);
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);

        Mockito.when(hearingsService.getHearingsByCaseRefNo("", "", ""))
                .thenThrow(feignException(HttpStatus.BAD_REQUEST.value(), "Not found"));

        ResponseEntity<Object> hearingsData1 =
                hearingsController.getHearingsByCaseRefNo("auth", "sauth", "caseRef");
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, hearingsData1.getStatusCode());
    }

    @Test
    void hearingsByCaseRefNoControllerInternalServiceErrorTest()
            throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(true);
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);

        Mockito.when(hearingsService.getHearingsByCaseRefNo("", "Auth", "sauth"))
                .thenThrow(feignException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Not found"));

        ResponseEntity<Object> hearingsData1 =
                hearingsController.getHearingsByCaseRefNo("auth", "sauth", "caseRef");

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, hearingsData1.getStatusCode());
    }

    @Test
    void hearingsByListOfCaseIdsControllerTest() throws IOException, ParseException {

        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        Hearings hearings = Hearings.hearingsWith().caseRef("123").hmctsServiceCode("ABA5").build();

        List<Hearings> hearingsForAllCases = new ArrayList<>();
        hearingsForAllCases.add(hearings);

        Map<String, String> caseIdWithRegionId = new HashMap<>();
        caseIdWithRegionId.put("caseref1", "RegionId");

        Mockito.when(
                        hearingsService.getHearingsByListOfCaseIds(
                                caseIdWithRegionId, "Auth", "sauth"))
                .thenReturn(hearingsForAllCases);
        ResponseEntity<Object> hearingsForAllCasesResponse =
                hearingsController.getHearingsByListOfCaseIds("auth", "sauth", caseIdWithRegionId);
        Assertions.assertNotNull(hearingsForAllCasesResponse.getBody());
    }

    @Test
    void hearingsByListOfCaseIdsControllerUnauthorisedExceptionTest()
            throws IOException, ParseException {

        Map<String, String> caseIdWithRegionId = new HashMap<>();
        caseIdWithRegionId.put("caseref1", "RegionId");

        Hearings hearings = Hearings.hearingsWith().caseRef("123").hmctsServiceCode("ABA5").build();
        List<Hearings> hearingsForAllCases = new ArrayList<>();
        hearingsForAllCases.add(hearings);
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.FALSE);
        ResponseEntity<Object> hearingsForAllCasesResponse =
                hearingsController.getHearingsByListOfCaseIds("auth", "sauth", caseIdWithRegionId);

        Assertions.assertEquals(
                HttpStatus.UNAUTHORIZED, hearingsForAllCasesResponse.getStatusCode());
    }

    @Test
    void hearingsByListOfCaseIdsControllerFeignExceptionTest() throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(true);
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);

        Map<String, String> caseIdWithRegionId = new HashMap<>();
        caseIdWithRegionId.put("caseref1", "RegionId");

        Hearings hearings = Hearings.hearingsWith().caseRef("123").hmctsServiceCode("ABA5").build();
        List<Hearings> hearingsForAllCases = new ArrayList<>();
        hearingsForAllCases.add(hearings);

        Mockito.when(hearingsService.getHearingsByListOfCaseIds(caseIdWithRegionId, "", ""))
                .thenThrow(feignException(HttpStatus.BAD_REQUEST.value(), "Not found"));

        ResponseEntity<Object> hearingsForAllCasesResponse =
                hearingsController.getHearingsByListOfCaseIds("auth", "sauth", caseIdWithRegionId);

        Assertions.assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR, hearingsForAllCasesResponse.getStatusCode());
    }

    @Test
    void hearingsByListOfCaseIdsControllerExceptionTest() throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(true);
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);

        Map<String, String> caseIdWithRegionId = new HashMap<>();
        caseIdWithRegionId.put("caseref1", "RegionId");

        Hearings hearings = Hearings.hearingsWith().caseRef("123").hmctsServiceCode("ABA5").build();
        List<Hearings> hearingsForAllCases = new ArrayList<>();
        hearingsForAllCases.add(hearings);
        Mockito.when(hearingsService.getHearingsByListOfCaseIds(caseIdWithRegionId, "", ""))
                .thenThrow(new RuntimeException());

        ResponseEntity<Object> hearingsForAllCasesResponse =
                hearingsController.getHearingsByListOfCaseIds("auth", "sauth", caseIdWithRegionId);
        Assertions.assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR, hearingsForAllCasesResponse.getStatusCode());
    }

    @Test
    void hearingsControllerNextHearingDateTest() throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        ResponseEntity<Object> nextHearingDetailsResponse =
                hearingsController.updateNextHearingDetails(
                        "Bearer auth", "Bearer sauth", "caseRef");
        Assertions.assertEquals(HttpStatus.OK, nextHearingDetailsResponse.getStatusCode());
    }

    @Test
    void nextHearingDateByCaseRefNoControllerUnauthorisedExceptionTest()
            throws IOException, ParseException {

        ResponseEntity<Object> nextHearingDetails =
                hearingsController.updateNextHearingDetails("", "", "caseRef");

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, nextHearingDetails.getStatusCode());
    }

    @Test
    void nextHearingDateByCaseRefNoControllerFeignExceptionTest()
            throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);

        Mockito.when(nextHearingDetailsService.updateNextHearingDetails("auth", hearings))
                .thenThrow(feignException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Not found"));

        ResponseEntity<Object> nextHearingUpdateResp =
                hearingsController.updateNextHearingDetails("auth", "sauth", "testcase");

        Assertions.assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR, nextHearingUpdateResp.getStatusCode());
    }

    @Test
    void nextHearingDateByCaseRefNoControllerInternalServiceErrorTest()
            throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);

        Mockito.when(nextHearingDetailsService.updateNextHearingDetails("auth", hearings))
                .thenThrow(feignException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Not found"));

        ResponseEntity<Object> nextHearingUpdateResp =
                hearingsController.updateNextHearingDetails("", "", "caseRef");

        Assertions.assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR, nextHearingUpdateResp.getStatusCode());
    }

    @Test
    void getNextHearingDateTest() throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        LocalDateTime testNextHearingDate = LocalDateTime.of(2024, 04, 28, 1, 0);
        NextHearingDetails nextHearingDetails =
                NextHearingDetails.builder()
                        .hearingID(123L)
                        .hearingDateTime(testNextHearingDate)
                        .build();
        Mockito.when(nextHearingDetailsService.getNextHearingDate(hearings))
                .thenReturn(nextHearingDetails);
        Mockito.when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        ResponseEntity<Object> nextHearingDetailsResponse =
                hearingsController.getNextHearingDate("Bearer auth", "Bearer sauth", "caseRef");
        Assertions.assertEquals(HttpStatus.OK, nextHearingDetailsResponse.getStatusCode());
    }

    @Test
    void getNextHearingDateInternalServiceErrorTest() throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);

        Mockito.when(nextHearingDetailsService.getNextHearingDate(hearings))
                .thenThrow(feignException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Not found"));
        Mockito.when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");

        ResponseEntity<Object> nextHearingDetailsResponse =
                hearingsController.getNextHearingDate("", "", "caseRef");

        Assertions.assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR, nextHearingDetailsResponse.getStatusCode());
    }

    @Test
    void getNextHearingDateFeignExceptionTest() throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);

        Mockito.when(nextHearingDetailsService.getNextHearingDate(hearings))
                .thenThrow(feignException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Not found"));

        ResponseEntity<Object> nextHearingDetailsResponse =
                hearingsController.getNextHearingDate("auth", "sauth", "testcase");

        Assertions.assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR, nextHearingDetailsResponse.getStatusCode());
    }

    @Test
    void getNextHearingDateUnauthorisedExceptionTest() throws IOException, ParseException {

        ResponseEntity<Object> nextHearingDetails =
                hearingsController.getNextHearingDate("", "", "caseRef");

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, nextHearingDetails.getStatusCode());
    }

    @Test
    void allFutureHearingsByCaseRefNoControllerTest() throws IOException, ParseException {

        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        Hearings hearings = Hearings.hearingsWith().caseRef("123").hmctsServiceCode("ABA5").build();

        Mockito.when(hearingsService.getFutureHearings("testCaseRefNo")).thenReturn(hearings);
        ResponseEntity<Object> hearingsForCaseRefNoResponse =
                hearingsController.getFutureHearings("auth", "sauth", "testCaseRefno");
        Assertions.assertNotNull(hearingsForCaseRefNoResponse.getBody());
    }

    @Test
    void allFutureHearingsByCaseRefNoUnauthorisedExceptionTest() {

        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.FALSE);
        ResponseEntity<Object> hearingsForCaseRefNoResponse =
                hearingsController.getFutureHearings("auth", "sauth", "testCaseRefno");

        Assertions.assertEquals(
                HttpStatus.UNAUTHORIZED, hearingsForCaseRefNoResponse.getStatusCode());
    }

    @Test
    void allFutureHearingsByCaseRefNoControllerFeignExceptionTest()
            throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(true);
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);

        Mockito.when(hearingsService.getFutureHearings(anyString()))
                .thenThrow(feignException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Not found"));

        ResponseEntity<Object> hearingsForCaseRefNoResponse =
                hearingsController.getFutureHearings("auth", "sauth", "testCaseRefNo");

        Assertions.assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR, hearingsForCaseRefNoResponse.getStatusCode());
    }

    @Test
    void allFutureHearingsByCaseRefNoControllerExceptionTest() throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(true);
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);

        Mockito.when(hearingsService.getFutureHearings("testCaseRefNo"))
                .thenThrow(new RuntimeException());

        ResponseEntity<Object> hearingsForAllCasesResponse =
                hearingsController.getFutureHearings("auth", "sauth", "testCaseRefNo");
        Assertions.assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR, hearingsForAllCasesResponse.getStatusCode());
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
    void getHearingsLinkDataTest() throws IOException, ParseException {

        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);

        HearingLinkData hearingLinkData = HearingLinkData.hearingLinkDataWith()
            .caseReference("BBA3").caseName("123").reasonsForLink(List.of("reasonLink")).build();
        List<HearingLinkData> hearingLinkDataList = new ArrayList<>();
        hearingLinkDataList.add(hearingLinkData);
        Mockito.when(hearingsDataService.getHearingLinkData(any(), anyString(), anyString()))
            .thenReturn(hearingLinkDataList);

        HearingValues hearingValues =
            HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();

        ResponseEntity<Object> hearingsData1 =
            hearingsController.getHearingsLinkData("Auth", "sauth", hearingValues);
        Assertions.assertEquals(HttpStatus.OK, hearingsData1.getStatusCode());
    }

    @Test
    void hearingsLinkDataUnauthorisedExceptionTest() throws IOException, ParseException {

        HearingValues hearingValues =
            HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();

        ResponseEntity<Object> hearingsData1 =
            hearingsController.getHearingsLinkData("", "", hearingValues);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, hearingsData1.getStatusCode());
    }

    @Test
    void hearingsLinkDataUnauthorisedFeignExceptionTest() throws IOException, ParseException {
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
    void assignRoleThrowUnauthorizedExceptionTest() {
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.FALSE);
        Throwable exception = assertThrows(ResponseStatusException.class, () -> hearingsController.assignRole("Auth"));
        Assertions.assertEquals("401 UNAUTHORIZED", exception.getMessage());

    }


}
