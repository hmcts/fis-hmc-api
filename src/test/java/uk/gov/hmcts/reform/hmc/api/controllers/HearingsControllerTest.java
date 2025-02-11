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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
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

@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("test")
class HearingsControllerTest {

    @InjectMocks private HearingsController hearingsController;

    @Mock private IdamAuthService idamAuthService;

    @Mock private HearingsDataService hearingsDataService;

    @Mock private NextHearingDetailsService nextHearingDetailsService;

    @Mock private HearingsService hearingsService;

    @Mock private AuthTokenGenerator authTokenGenerator;


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

    @Test
    void hearingsControllerTest() throws IOException, ParseException {

        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        Hearings hearingsObj = Hearings.hearingsWith().caseRef("123").hmctsServiceCode("BBA3").build();
        Mockito.when(hearingsService.getHearingsByCaseRefNo("caseRef", "Auth", "sauth"))
                .thenReturn(hearingsObj);
        ResponseEntity<Object> hearingsResponse =
                hearingsController.getHearingsByCaseRefNo("auth", "sauth", "caseRef");
        Assertions.assertEquals(HttpStatus.OK, hearingsResponse.getStatusCode());
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

        Mockito.when(hearingsService.getHearingsByCaseRefNo("caseRef", "auth", "sauth"))
                .thenThrow(feignException(HttpStatus.BAD_REQUEST.value(), "Not found"));

        ResponseEntity<Object> hearingsData1 =
                hearingsController.getHearingsByCaseRefNo("auth", "sauth", "caseRef");
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, hearingsData1.getStatusCode());
    }

    @Test
    void hearingsByCaseRefNoControllerInternalServerErrorTest() throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(true);
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);

        Mockito.when(hearingsService.getHearingsByCaseRefNo("caseRef", "auth", "sauth"))
            .thenThrow(new RuntimeException());

        ResponseEntity<Object> hearingsData1 =
            hearingsController.getHearingsByCaseRefNo("auth", "sauth", "caseRef");
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, hearingsData1.getStatusCode());
    }

    @Test
    void hearingsByCaseRefNoControllerInternalServiceErrorTest()
            throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(true);
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);

        Mockito.when(hearingsService.getHearingsByCaseRefNo("caseRef", "auth", "sauth"))
                .thenThrow(feignException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Not found"));

        ResponseEntity<Object> hearingsData1 =
                hearingsController.getHearingsByCaseRefNo("auth", "sauth", "caseRef");

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, hearingsData1.getStatusCode());
    }

    @Test
    void hearingsByListOfCaseIdsControllerTest() {

        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        Hearings hearingsObj = Hearings.hearingsWith().caseRef("123").hmctsServiceCode("ABA5").build();

        List<Hearings> hearingsForAllCases = new ArrayList<>();
        hearingsForAllCases.add(hearingsObj);

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

        Hearings hearingsObj = Hearings.hearingsWith().caseRef("123").hmctsServiceCode("ABA5").build();
        List<Hearings> hearingsForAllCases = new ArrayList<>();
        hearingsForAllCases.add(hearingsObj);
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

        Hearings hearingsObj = Hearings.hearingsWith().caseRef("123").hmctsServiceCode("ABA5").build();
        List<Hearings> hearingsForAllCases = new ArrayList<>();
        hearingsForAllCases.add(hearingsObj);

        Mockito.when(hearingsService.getHearingsByListOfCaseIds(caseIdWithRegionId, "auth", "sauth"))
                .thenThrow(feignException(HttpStatus.BAD_REQUEST.value(), "Not found"));

        ResponseEntity<Object> hearingsForAllCasesResponse =
                hearingsController.getHearingsByListOfCaseIds("auth", "sauth", caseIdWithRegionId);

        Assertions.assertEquals(
                HttpStatus.BAD_REQUEST, hearingsForAllCasesResponse.getStatusCode());
    }

    @Test
    void hearingsByListOfCaseIdsControllerExceptionTest() throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(true);
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);

        Map<String, String> caseIdWithRegionId = new HashMap<>();
        caseIdWithRegionId.put("caseref1", "RegionId");

        Hearings hearingsObj = Hearings.hearingsWith().caseRef("123").hmctsServiceCode("ABA5").build();
        List<Hearings> hearingsForAllCases = new ArrayList<>();
        hearingsForAllCases.add(hearingsObj);
        Mockito.when(hearingsService.getHearingsByListOfCaseIds(caseIdWithRegionId, "auth", "sauth"))
                .thenThrow(new RuntimeException());

        ResponseEntity<Object> hearingsForAllCasesResponse =
                hearingsController.getHearingsByListOfCaseIds("auth", "sauth", caseIdWithRegionId);
        Assertions.assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR, hearingsForAllCasesResponse.getStatusCode());
    }

    @Test
    void allFutureHearingsByCaseRefNoControllerTest() {

        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        Hearings hearingsObj = Hearings.hearingsWith().caseRef("123").hmctsServiceCode("ABA5").build();

        Mockito.when(hearingsService.getFutureHearings("testCaseRefNo")).thenReturn(hearingsObj);
        ResponseEntity<Object> hearingsForCaseRefNoResponse =
                hearingsController.getFutureHearings("auth", "sauth", "testCaseRefno");

        Assertions.assertEquals(
            HttpStatus.OK, hearingsForCaseRefNoResponse.getStatusCode());
        ResponseEntity<Object> hearingsForAllCasesResponse =
            hearingsController.getHearingsByListOfCaseIdsWithoutCourtVenueDetails("auth", "sauth", List.of("test"));
        ResponseEntity<Object> hearingsForAllCasesResponse2 =
            hearingsController.getListedHearingsForAllCaseIdsOnCurrentDate("auth", "sauth", List.of("test"));
        Assertions.assertNotNull(hearingsForAllCasesResponse.getBody());
        Assertions.assertNotNull(hearingsForAllCasesResponse2.getBody());
    }

    @Test
    void allFutureHearingsByCaseRefNoUnauthorisedExceptionTest() {

        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.FALSE);
        ResponseEntity<Object> hearingsForCaseRefNoResponse =
                hearingsController.getFutureHearings("auth", "sauth", "testCaseRefno");

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, hearingsForCaseRefNoResponse.getStatusCode());
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, hearingsController
            .getHearingsByListOfCaseIdsWithoutCourtVenueDetails("auth", "sauth", List.of("test"))
            .getStatusCode());
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, hearingsController
            .getListedHearingsForAllCaseIdsOnCurrentDate("auth", "sauth", List.of("test"))
            .getStatusCode());
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
        Mockito.when(hearingsService.getHearingsByListOfCaseIdsWithoutCourtVenueDetails(Mockito.any(),
                                                                                        Mockito.anyString(),
                                                                                        Mockito.anyString()))
            .thenThrow(feignException(HttpStatus.BAD_REQUEST.value(), "Not found"));
        Mockito.when(hearingsService.getHearingsListedForCurrentDateByListOfCaseIdsWithoutCourtVenueDetails(Mockito.any(),
                                                                                                            Mockito.anyString(),
                                                                                                            Mockito.anyString()))
            .thenThrow(feignException(HttpStatus.BAD_REQUEST.value(), "Not found"));
        ResponseEntity<Object> hearingsForAllCasesResponse =
            hearingsController.getListedHearingsForAllCaseIdsOnCurrentDate("auth", "sauth", List.of("test"));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, hearingsForAllCasesResponse.getStatusCode());
        ResponseEntity<Object> hearingsForAllCasesResponse2 =
            hearingsController.getHearingsByListOfCaseIdsWithoutCourtVenueDetails("auth", "sauth", List.of("test"));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, hearingsForAllCasesResponse2.getStatusCode());
    }

    @Test
    void allFutureHearingsByCaseRefNoControllerExceptionTest() {
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(true);
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);

        Mockito.when(hearingsService.getFutureHearings("testCaseRefNo"))
                .thenThrow(new RuntimeException());

        ResponseEntity<Object> hearingsForAllCasesResponse =
                hearingsController.getFutureHearings("auth", "sauth", "testCaseRefNo");
        Assertions.assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR, hearingsForAllCasesResponse.getStatusCode());
        Mockito.when(hearingsService.getHearingsByListOfCaseIdsWithoutCourtVenueDetails(Mockito.any(),
                                                                                        Mockito.anyString(),
                                                                                        Mockito.anyString()))
            .thenThrow(new RuntimeException());
        Mockito.when(hearingsService.getHearingsListedForCurrentDateByListOfCaseIdsWithoutCourtVenueDetails(Mockito.any(),
                                                                                                            Mockito.anyString(),
                                                                                                            Mockito.anyString()))
            .thenThrow(new RuntimeException());
        ResponseEntity<Object> hearingsForAllCasesResponse1 =
            hearingsController.getListedHearingsForAllCaseIdsOnCurrentDate("auth", "sauth", List.of("test"));
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, hearingsForAllCasesResponse1.getStatusCode());
        ResponseEntity<Object> hearingsForAllCasesResponse2 =
            hearingsController.getHearingsByListOfCaseIdsWithoutCourtVenueDetails("auth", "sauth", List.of("test"));
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, hearingsForAllCasesResponse2.getStatusCode());
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
    void hearingsLinkDataInternalServerErrorTest() throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);

        HearingValues hearingValues =
            HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();

        Mockito.when(hearingsDataService.getHearingLinkData(hearingValues, "", ""))
            .thenThrow(new RuntimeException());

        ResponseEntity<Object> hearingsData1 =
            hearingsController.getHearingsLinkData("", "", hearingValues);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, hearingsData1.getStatusCode());
    }

    @Test
    void testupdateNextHearingDetails() {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(true);
        Mockito.when(hearingsService.getHearingsByCaseRefNo(any(), any(), any())).thenReturn(Hearings.hearingsWith().build());
        Mockito.when(nextHearingDetailsService.updateNextHearingDetails(any(), any())).thenReturn(true);
        ResponseEntity<Object> response = hearingsController
            .updateNextHearingDetails("auth", "sauth", "caseId");
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testupdateNextHearingDetailsException() {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(true);
        Mockito.when(hearingsService.getHearingsByCaseRefNo(any(), any(), any()))
            .thenThrow(new RuntimeException());
        ResponseEntity<Object> response = hearingsController
            .updateNextHearingDetails("auth", "sauth", "caseId");
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testupdateNextHearingDate() {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(true);
        Mockito.when(hearingsService.getHearingsByCaseRefNo(any(), any(), any())).thenReturn(Hearings.hearingsWith().build());
        Mockito.when(nextHearingDetailsService.getNextHearingDate(any())).thenReturn(NextHearingDetails.builder().build());
        ResponseEntity<Object> response = hearingsController
            .getNextHearingDate("auth", "sauth", "caseId");
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testupdateNextHearingDateException() {
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(true);
        Mockito.when(hearingsService.getHearingsByCaseRefNo(any(), any(), any()))
            .thenThrow(new RuntimeException());
        ResponseEntity<Object> response = hearingsController
            .getNextHearingDate("auth", "sauth", "caseId");
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
