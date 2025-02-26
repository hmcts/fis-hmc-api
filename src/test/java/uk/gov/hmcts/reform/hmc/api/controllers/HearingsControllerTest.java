package uk.gov.hmcts.reform.hmc.api.controllers;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseData;
import uk.gov.hmcts.reform.hmc.api.model.ccd.NextHearingDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.model.response.linkdata.HearingLinkData;
import uk.gov.hmcts.reform.hmc.api.services.HearingsDataService;
import uk.gov.hmcts.reform.hmc.api.services.HearingsService;
import uk.gov.hmcts.reform.hmc.api.services.IdamAuthService;
import uk.gov.hmcts.reform.hmc.api.services.NextHearingDetailsService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@SuppressWarnings("unchecked")
class HearingsControllerTest {

    @InjectMocks private HearingsController hearingsController;

    @Spy
    private  final IdamAuthService idamAuthService = Mockito.mock(IdamAuthService.class);

    @Spy private  AuthTokenGenerator authTokenGenerator;

    @Spy private  HearingsDataService hearingsDataService;

    @Spy private  HearingsService hearingsService;

    @Spy private  NextHearingDetailsService nextHearingDetailsService;

    private Hearings hearings;

    @Test
    void hearingsControllerTest() throws IOException, ParseException {

        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        Hearings hearingsObj = Hearings.hearingsWith().caseRef("123").hmctsServiceCode("BBA3").build();
        Mockito.when(hearingsService.getHearingsByCaseRefNo("caseRef", "Auth", "sauth"))
                .thenReturn(hearingsObj);
        ResponseEntity<Object> hearingsResponse =
                hearingsController.getHearingsByCaseRefNo("Auth", "sauth", "caseRef");
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
    void allFutureHearingsByCaseRefNoControllerTest() {

        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(Boolean.TRUE);
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        Hearings hearingsObj = Hearings.hearingsWith().caseRef("123").hmctsServiceCode("ABA5").build();

        Mockito.when(hearingsService.getFutureHearings("testCaseRefno")).thenReturn(hearingsObj);
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

    @Test
    void createAutomatedHearingsTest() throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(true);
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);
        Mockito.when(hearingsService.createAutomatedHearings(any())).thenReturn(null);
        //CaseDetails caseDetails = CaseDetails.builder().id(Long.valueOf(1232344523)).build();
        CaseData caseData = CaseData.caseDataBuilder().build();
        ResponseEntity<Object> hearingsForAllCasesResponse =
                hearingsController.createAutomatedHearings("auth", "sauth", caseData);
        Assertions.assertEquals(
                HttpStatus.OK, hearingsForAllCasesResponse.getStatusCode());
    }

    @Test
    void createAutomatedHearingsTestInternalServerExceptionTest() throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(true);
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);
        Mockito.when(hearingsService.createAutomatedHearings(any())).thenThrow(new RuntimeException());
        CaseData caseData = CaseData.caseDataBuilder().build();
        ResponseEntity<Object> hearingsForAllCasesResponse =
                hearingsController.createAutomatedHearings("auth", "sauth", caseData);
        Assertions.assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR, hearingsForAllCasesResponse.getStatusCode());
    }


    @Test
    void createAutomatedHearingsNoUnauthorisedExceptionTest() throws IOException, ParseException {
        CaseData caseData = CaseData.caseDataBuilder().build();
        ResponseEntity<Object> hearingsForAllCasesResponse =
                hearingsController.createAutomatedHearings("auth", "sauth", caseData);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, hearingsForAllCasesResponse.getStatusCode());
    }

    @Test
    void createAutomatedHearingsFeignExceptionTest() throws IOException, ParseException {
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(true);
        Mockito.when(idamAuthService.authoriseService(any())).thenReturn(true);
        Mockito.when(hearingsService.createAutomatedHearings(any()))
                .thenThrow(feignException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Not found"));
        CaseData caseData = CaseData.caseDataBuilder().build();
        ResponseEntity<Object> hearingsForAllCasesResponse =
                hearingsController.createAutomatedHearings("auth", "sauth", caseData);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, hearingsForAllCasesResponse.getStatusCode());
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
        Mockito.when(nextHearingDetailsService.getNextHearingDate(any())).thenReturn(NextHearingDetails.builder().build());
        ResponseEntity<Object> response2 = hearingsController
            .getNextHearingDate("auth", "sauth", "caseId");
        Assertions.assertEquals(HttpStatus.OK, response2.getStatusCode());
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
        ResponseEntity<Object> response2 = hearingsController
            .getNextHearingDate("auth", "sauth", "caseId");
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response2.getStatusCode());
    }
}
