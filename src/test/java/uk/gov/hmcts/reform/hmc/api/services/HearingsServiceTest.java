package uk.gov.hmcts.reform.hmc.api.services;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.AWAITING_HEARING_DETAILS;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CANCELLED;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.LISTED;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.OPEN;

import feign.FeignException;
import feign.Request;
import feign.Response;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseHearing;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.model.response.JudgeDetail;

@SpringBootTest
@ActiveProfiles("test")
@PropertySource("classpath:application.yaml")
class HearingsServiceTest {

    @Value("#{'${hearing_component.futureHearingStatus}'.split(',')}")
    private List<String> futureHearingStatusList;

    @Autowired
    private HearingsServiceImpl hearingsService;

    @MockBean
    private HearingApiClient hearingApiClient;

    @MockBean
    private IdamTokenGenerator idamTokenGenerator;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private RefDataServiceImpl refDataService;

    @MockBean
    private RefDataJudicialService refDataJudicialService;

    @Test
    void shouldReturnCtfHearingsAuthExceptionTest() {
        Hearings caseHearings =
            Hearings.hearingsWith()
                .caseRef("123")
                .hmctsServiceCode("ABA5")
                .caseHearings(null)
                .courtName("TEST")
                .courtTypeId("18")
                .build();
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(hearingApiClient.getHearingDetails(eq("MOCK_AUTH_TOKEN"),
                                                eq("MOCK_S2S_TOKEN"),
                                                anyString(),
                                                anyString(),
                                                anyString(),
                                                "123"))
            .thenReturn(caseHearings);
        Hearings response = hearingsService.getHearingsByCaseRefNo("123", "", "");
        Assertions.assertNotNull(response);
        Assertions.assertNull(response.getCaseHearings());

    }

    @Test
    void shouldReturnHearingsByCaseRefNoTest() {

        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");

        HearingDaySchedule hearingDaySchedule =
            HearingDaySchedule.hearingDayScheduleWith()
                .hearingVenueId("231596")
                .hearingJudgeId("4925644")
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
        Hearings caseHearings =
            Hearings.hearingsWith()
                .caseRef("123")
                .hmctsServiceCode("ABA5")
                .caseHearings(caseHearingList)
                .courtName("TEST")
                .courtTypeId("18")
                .build();

        CourtDetail courtDetail = CourtDetail.courtDetailWith()
            .courtTypeId("18")
            .hearingVenueId("231596")
            .hearingVenueName("TEST")
            .hearingVenueAddress("venueAddressTest")
            .hearingVenueLocationCode("venueLocationCode")
            .hearingVenuePostCode("postcodeTest")
            .regionId("RegionId")
            .courtStatus(OPEN)
            .build();

        when(refDataJudicialService.getJudgeDetails("4925644"))
            .thenReturn(JudgeDetail.judgeDetailWith().hearingJudgeName("JudgeA").build());
        when(refDataService.getCourtDetails("231596")).thenReturn(courtDetail);
        when(hearingApiClient.getHearingDetails(eq("MOCK_AUTH_TOKEN"),
                                                eq("MOCK_S2S_TOKEN"),
                                                anyString(),
                                                anyString(),
                                                anyString(),
                                                eq("123")))
            .thenReturn(caseHearings);
        Hearings hearings = hearingsService.getHearingsByCaseRefNo("123", "Auth", "sauth");
        Assertions.assertNotNull(hearings);
        Assertions.assertNotNull(hearings.getCaseHearings());
        HearingDaySchedule hearingDayScheduleResponse = hearings.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0);
        Assertions.assertEquals("venueAddressTest", hearingDayScheduleResponse.getHearingVenueAddress());
        Assertions.assertEquals("TEST", hearingDayScheduleResponse.getHearingVenueName());
        Assertions.assertEquals("venueLocationCode", hearingDayScheduleResponse.getHearingVenueLocationCode());
        Assertions.assertEquals("JudgeA", hearingDayScheduleResponse.getHearingJudgeName());
        Assertions.assertEquals("LISTED", hearings.getCaseHearings().get(0).getHmcStatus());
        Assertions.assertEquals(1, hearings.getCaseHearings().size());
    }

    @Test
    void shouldReturnCtfHearingsByListOfCaseIdsTest() {

        CourtDetail courtDetail =
                CourtDetail.courtDetailWith()
                        .courtTypeId("18")
                        .hearingVenueId("231596")
                        .hearingVenueName("TEST")
                        .hearingVenueLocationCode("LocationCodeTest")
                        .hearingVenueAddress("AddressTest")
                        .hearingVenuePostCode("PostCodeTest")
                        .regionId("RegionId")
                        .courtStatus(OPEN)
                        .build();
        List<CourtDetail> courtDetailsList = new ArrayList<>();
        courtDetailsList.add(courtDetail);

        JudgeDetail judgeDetail = JudgeDetail.judgeDetailWith().hearingJudgeName("test").build();
        List<JudgeDetail> judgeDetailsList = new ArrayList<>();
        judgeDetailsList.add(judgeDetail);

        HearingDaySchedule hearingDaySchedule =
                HearingDaySchedule.hearingDayScheduleWith()
                        .hearingVenueId("231596")
                        .hearingJudgeId("4925644")
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

        Hearings caseHearings =
                Hearings.hearingsWith()
                        .caseRef("123")
                        .hmctsServiceCode("ABA5")
                        .caseHearings(caseHearingList)
                        .courtName("TEST")
                        .courtTypeId("18")
                        .build();

        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(refDataService.getCourtDetailsByServiceCode("ABA5")).thenReturn(courtDetailsList);
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");

        when(hearingApiClient.getListOfHearingDetails(anyString(),
                                                      any(),
                                                      anyString(),
                                                      anyString(),
                                                      anyString(),
                                                      any(),
                                                      anyString()))
                .thenReturn(List.of(caseHearings));

        Map<String, String> caseIdWithRegionId = new HashMap<>();
        caseIdWithRegionId.put("123", "RegionId-231596");

        List<Hearings> hearingsResponse =
                hearingsService.getHearingsByListOfCaseIds(caseIdWithRegionId, "Auth", "sauth");
        CaseHearing caseHearingResp = hearingsResponse.get(0).getCaseHearings().get(0);
        Assertions.assertEquals("ABA5", hearingsResponse.get(0).getHmctsServiceCode());
        Assertions.assertEquals("18", hearingsResponse.get(0).getCourtTypeId());
        Assertions.assertEquals("TEST", hearingsResponse.get(0).getCourtName());
        Assertions.assertNotNull(caseHearingResp.getHmcStatus());
        Assertions.assertEquals("LISTED", caseHearingResp.getHmcStatus());

        HearingDaySchedule hearingScheduleResponse = caseHearingResp.getHearingDaySchedule().get(0);

        Assertions.assertNotNull(hearingScheduleResponse);
        Assertions.assertEquals("TEST", hearingScheduleResponse.getHearingVenueName());
        Assertions.assertEquals("AddressTest PostCodeTest", hearingScheduleResponse.getHearingVenueAddress());
        Assertions.assertEquals("LocationCodeTest", hearingScheduleResponse.getHearingVenueLocationCode());
        Assertions.assertEquals("18", hearingScheduleResponse.getCourtTypeId());
        Assertions.assertNotNull(hearingScheduleResponse.getHearingVenueId());

    }

    @Test
    void shouldReturnCtfHearingsByListOfCaseIdsCompletedCaseHearingTest() {

        CourtDetail courtDetail =
            CourtDetail.courtDetailWith()
                .courtTypeId("18")
                .hearingVenueId("231596")
                .hearingVenueName("TEST")
                .regionId("RegionId")
                .courtStatus(OPEN)
                .build();
        List<CourtDetail> courtDetailsList = new ArrayList<>();
        courtDetailsList.add(courtDetail);

        JudgeDetail judgeDetail = JudgeDetail.judgeDetailWith().hearingJudgeName("test").build();
        List<JudgeDetail> judgeDetailsList = new ArrayList<>();
        judgeDetailsList.add(judgeDetail);

        HearingDaySchedule hearingDaySchedule =
            HearingDaySchedule.hearingDayScheduleWith()
                .hearingVenueId("231596")
                .hearingJudgeId("4925644")
                .build();
        List<HearingDaySchedule> hearingDayScheduleList = new ArrayList<>();
        hearingDayScheduleList.add(hearingDaySchedule);

        CaseHearing caseHearing =
            CaseHearing.caseHearingWith()
                .hmcStatus("COMPLETED")
                .hearingDaySchedule(hearingDayScheduleList)
                .build();
        List<CaseHearing> caseHearingList = new ArrayList<>();
        caseHearingList.add(caseHearing);

        Hearings caseHearings =
            Hearings.hearingsWith()
                .caseRef("123")
                .hmctsServiceCode("ABA5")
                .caseHearings(caseHearingList)
                .courtName("TEST")
                .courtTypeId("18")
                .build();

        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(refDataService.getCourtDetailsByServiceCode("ABA5")).thenReturn(courtDetailsList);
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");

        when(hearingApiClient.getListOfHearingDetails(anyString(),
                                                      any(),
                                                      anyString(),
                                                      anyString(),
                                                      anyString(),
                                                      any(),
                                                      anyString()))
            .thenReturn(List.of(caseHearings));

        Map<String, String> caseIdWithRegionId = new HashMap<>();
        caseIdWithRegionId.put("123", "RegionId-231596");

        List<Hearings> hearingsResponse =
            hearingsService.getHearingsByListOfCaseIds(caseIdWithRegionId, "Auth", "sauth");
        Assertions.assertEquals("ABA5", hearingsResponse.get(0).getHmctsServiceCode());
        Assertions.assertEquals("COMPLETED", hearingsResponse.get(0).getCaseHearings().get(0).getHmcStatus());
    }

    @Test
    void shouldReturnCtfHearingsByListOfCaseIdsCancelledCaseHearingTest() {

        CourtDetail courtDetail =
            CourtDetail.courtDetailWith()
                .courtTypeId("18")
                .hearingVenueId("231596")
                .hearingVenueName("TEST")
                .regionId("RegionId")
                .courtStatus(OPEN)
                .build();
        List<CourtDetail> courtDetailsList = new ArrayList<>();
        courtDetailsList.add(courtDetail);

        JudgeDetail judgeDetail = JudgeDetail.judgeDetailWith().hearingJudgeName("test").build();
        List<JudgeDetail> judgeDetailsList = new ArrayList<>();
        judgeDetailsList.add(judgeDetail);

        HearingDaySchedule hearingDaySchedule =
            HearingDaySchedule.hearingDayScheduleWith()
                .hearingVenueId(null)
                .hearingJudgeId("4925644")
                .build();
        List<HearingDaySchedule> hearingDayScheduleList = new ArrayList<>();
        hearingDayScheduleList.add(hearingDaySchedule);

        CaseHearing caseHearing =
            CaseHearing.caseHearingWith()
                .hmcStatus("CANCELLED")
                .hearingDaySchedule(hearingDayScheduleList)
                .build();
        List<CaseHearing> caseHearingList = new ArrayList<>();
        caseHearingList.add(caseHearing);

        Hearings caseHearings =
            Hearings.hearingsWith()
                .caseRef("123")
                .hmctsServiceCode("ABA5")
                .caseHearings(caseHearingList)
                .courtName("TEST")
                .courtTypeId("18")
                .build();

        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(refDataService.getCourtDetailsByServiceCode("ABA5")).thenReturn(courtDetailsList);
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");

        when(hearingApiClient.getListOfHearingDetails(anyString(),
                                                      any(),
                                                      anyString(),
                                                      anyString(),
                                                      anyString(),
                                                      any(),
                                                      anyString()))
            .thenReturn(List.of(caseHearings));

        Map<String, String> caseIdWithRegionId = new HashMap<>();
        caseIdWithRegionId.put("123", "RegionId-231596");

        List<Hearings> hearingsResponse =
            hearingsService.getHearingsByListOfCaseIds(caseIdWithRegionId, "Auth", "sauth");
        Assertions.assertEquals("ABA5", hearingsResponse.get(0).getHmctsServiceCode());
        Assertions.assertEquals("CANCELLED", hearingsResponse.get(0).getCaseHearings().get(0).getHmcStatus());
    }


    @Test
    void shouldReturnCtfHearingsByListOfCaseIdsCancelledCaseHearingTest1() {
        CourtDetail courtDetail =
            CourtDetail.courtDetailWith()
                .courtTypeId("18")
                .hearingVenueId("231596")
                .hearingVenueName("TEST")
                .regionId("RegionId")
                .courtStatus(OPEN)
                .build();
        List<CourtDetail> courtDetailsList = new ArrayList<>();
        courtDetailsList.add(courtDetail);

        JudgeDetail judgeDetail = JudgeDetail.judgeDetailWith().hearingJudgeName("test").build();
        List<JudgeDetail> judgeDetailsList = new ArrayList<>();
        judgeDetailsList.add(judgeDetail);

        HearingDaySchedule hearingDaySchedule =
            HearingDaySchedule.hearingDayScheduleWith()
                .hearingVenueId("231596")
                .hearingJudgeId("4925644")
                .build();
        List<HearingDaySchedule> hearingDayScheduleList = new ArrayList<>();
        hearingDayScheduleList.add(hearingDaySchedule);

        CaseHearing caseHearing =
            CaseHearing.caseHearingWith()
                .hmcStatus("UNKNOWN")
                .hearingDaySchedule(hearingDayScheduleList)
                .build();
        List<CaseHearing> caseHearingList = new ArrayList<>();
        caseHearingList.add(caseHearing);

        Hearings caseHearings =
            Hearings.hearingsWith()
                .caseRef("123")
                .hmctsServiceCode("ABA5")
                .caseHearings(caseHearingList)
                .courtName("TEST")
                .courtTypeId("18")
                .build();

        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(refDataService.getCourtDetailsByServiceCode("ABA5")).thenReturn(courtDetailsList);
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");

        when(hearingApiClient.getListOfHearingDetails(anyString(),
                                                      any(),
                                                      anyString(),
                                                      anyString(),
                                                      anyString(),
                                                      any(),
                                                      anyString()))
            .thenReturn(List.of(caseHearings));

        Map<String, String> caseIdWithRegionId = new HashMap<>();
        caseIdWithRegionId.put("123", "RegionId-231596");

        List<Hearings> hearingsResponse =
            hearingsService.getHearingsByListOfCaseIds(caseIdWithRegionId, "Auth", "sauth");
        Assertions.assertEquals("ABA5", hearingsResponse.get(0).getHmctsServiceCode());
        Assertions.assertFalse(hearingsResponse.get(0).getCaseHearings().isEmpty());    }



    @Test
    void shouldReturnAllFutureHearingsByCaseRefNoTest() {
        LocalDateTime hearingStartDate = LocalDateTime.now().plusDays(5).withNano(1);
        HearingDaySchedule hearingDaySchedule =
                HearingDaySchedule.hearingDayScheduleWith()
                        .hearingVenueId("231596")
                        .hearingJudgeId("4925644")
                        .hearingStartDateTime(hearingStartDate)
                        .build();
        List<HearingDaySchedule> hearingDayScheduleList = new ArrayList<>();
        hearingDayScheduleList.add(hearingDaySchedule);

        CaseHearing caseHearingWithListedStatus =
                CaseHearing.caseHearingWith()
                        .hmcStatus(LISTED)
                        .hearingDaySchedule(hearingDayScheduleList)
                        .build();
        CaseHearing caseHearingWithCancelled =
                CaseHearing.caseHearingWith()
                        .hmcStatus(CANCELLED)
                        .hearingDaySchedule(hearingDayScheduleList)
                        .build();

        CaseHearing caseHearingWithoutHearingDaySche =
                CaseHearing.caseHearingWith()
                        .hmcStatus("HEARING_REQUESTED")
                        .hearingDaySchedule(null)
                        .build();

        List<CaseHearing> caseHearingList = new ArrayList<>();
        caseHearingList.add(caseHearingWithListedStatus);
        caseHearingList.add(caseHearingWithCancelled);
        caseHearingList.add(caseHearingWithoutHearingDaySche);

        ReflectionTestUtils.setField(
                hearingsService, "futureHearingStatusList", futureHearingStatusList);

        Hearings caseHearings =
                Hearings.hearingsWith()
                        .caseRef("123")
                        .hmctsServiceCode("ABA5")
                        .caseHearings(caseHearingList)
                        .courtName("TEST")
                        .courtTypeId("18")
                        .build();

        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(hearingApiClient.getHearingDetails(anyString(),
                                                any(),
                                                anyString(),
                                                anyString(),
                                                anyString(),
                                                any()))
                .thenReturn(caseHearings);

        Hearings hearingsResponse = hearingsService.getFutureHearings("testCaseRefNo");
        Assertions.assertEquals(LISTED, hearingsResponse.getCaseHearings().get(0).getHmcStatus());
    }

    @Test
    void shouldReturnAllFutureHearingsByCaseRefNoFeignExceptionTest() {
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(hearingApiClient.getHearingDetails(anyString(),
                                                any(),
                                                anyString(),
                                                anyString(),
                                                anyString(),
                                                any()))
                .thenThrow(feignException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Not found"));

        Assertions.assertNull(hearingsService.getFutureHearings(""));
    }

    @Test
    void shouldReturnAllFutureHearingsByCaseRefNoAuthExceptionTest() {
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(hearingApiClient.getHearingDetails(any(),
                                                any(),
                                                anyString(),
                                                anyString(),
                                                anyString(),
                                                any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));
        Assertions.assertNull(hearingsService.getFutureHearings(""));
    }

    @Test
    void shouldReturnAllFutureHearingsByCaseRefNoExceptionTest() {
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(hearingApiClient.getHearingDetails(any(),
                                                any(),
                                                anyString(),
                                                anyString(),
                                                anyString(),
                                                any()))
                .thenThrow(new RuntimeException());
        Assertions.assertNull(hearingsService.getFutureHearings(""));
    }

    @Test
    void shouldReturnCtfHearingsByListOfCaseIdsWithoutVenueTest() {
        HearingDaySchedule hearingDaySchedule =
            HearingDaySchedule.hearingDayScheduleWith()
                .hearingVenueId("231596")
                .hearingJudgeId("4925644")
                .hearingStartDateTime(LocalDateTime.now())
                .build();
        List<HearingDaySchedule> hearingDayScheduleList = new ArrayList<>();
        hearingDayScheduleList.add(hearingDaySchedule);

        CaseHearing caseHearing =
            CaseHearing.caseHearingWith()
                .hmcStatus("LISTED")
                .hearingID(143L)
                .hearingDaySchedule(hearingDayScheduleList)
                .build();
        CaseHearing caseHearing2 =
            CaseHearing.caseHearingWith()
                .hearingID(123L)
                .hmcStatus(AWAITING_HEARING_DETAILS)
                .hearingDaySchedule(hearingDayScheduleList)
                .build();
        List<CaseHearing> caseHearingList = new ArrayList<>();
        caseHearingList.add(caseHearing);
        caseHearingList.add(caseHearing2);
        Hearings caseHearings =
            Hearings.hearingsWith()
                .caseRef("123")
                .hmctsServiceCode("ABA5")
                .caseHearings(caseHearingList)
                .courtName("TEST")
                .courtTypeId("18")
                .build();

        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(hearingApiClient.getListOfHearingDetails(anyString(),
                                                      any(),
                                                      anyString(),
                                                      anyString(),
                                                      anyString(),
                                                      any(),
                                                      anyString()))
            .thenReturn(List.of(caseHearings));

        List<Hearings> hearingsResponse = hearingsService.getHearingsByListOfCaseIdsWithoutCourtVenueDetails(List.of("test"),
                                                                               "Auth", "sauth");
        CaseHearing caseHearingResp = hearingsResponse.get(0).getCaseHearings().get(0);
        Assertions.assertEquals("ABA5", hearingsResponse.get(0).getHmctsServiceCode());
        HearingDaySchedule hearingScheduleResponse = caseHearingResp.getHearingDaySchedule().get(0);
        Assertions.assertNotNull(hearingScheduleResponse);
        Map<String, List<String>> hearingsResponse1 = hearingsService
            .getHearingsListedForCurrentDateByListOfCaseIdsWithoutCourtVenueDetails(List.of("test"),
                                                                                    "Auth", "sauth");
        Assertions.assertNotNull(hearingsResponse1);
    }

    @Test
    void shouldReturnEmptyListIfNoHearings() {
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");

        when(hearingApiClient.getListOfHearingDetails(anyString(),
                                                      any(),
                                                      anyString(),
                                                      anyString(),
                                                      anyString(),
                                                      any(),
                                                      anyString()))
            .thenReturn(List.of());
        List<Hearings> hearingsResponse = hearingsService.getHearingsByListOfCaseIdsWithoutCourtVenueDetails(List.of("test"),
                                                                                     "Auth", "sauth");
        Assertions.assertTrue(hearingsResponse.isEmpty());
    }

    @Test
    void shouldReturnEmptyMapNoFeignExceptionTest() {
        when(hearingApiClient.getListOfHearingDetails(any(),
                                                      any(),
                                                      anyString(),
                                                      anyString(),
                                                      anyString(),
                                                      any(),
                                                      any()))
            .thenThrow(feignException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Not found"));

        Assertions.assertTrue(hearingsService
                                  .getHearingsListedForCurrentDateByListOfCaseIdsWithoutCourtVenueDetails(new ArrayList<>(),
                                                                                                          "", "")
                                  .isEmpty());
    }

    @Test
    void shouldReturnNoFeignExceptionTest() {
        when(hearingApiClient.getListOfHearingDetails(any(),
                                                      any(),
                                                      anyString(),
                                                      anyString(),
                                                      anyString(),
                                                      any(),
                                                      any()))
            .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_GATEWAY, "Bad Gateway", null,
                                                       null, null));
        Map<String, List<String>> response = hearingsService
            .getHearingsListedForCurrentDateByListOfCaseIdsWithoutCourtVenueDetails(new ArrayList<>(),
                                                                                    "", "");
        Assertions.assertTrue(response.isEmpty());
    }

    @Test
    void shouldReturnGeneralExceptionTest() {
        when(hearingApiClient.getListOfHearingDetails(any(),
                                                      any(),
                                                      anyString(),
                                                      anyString(),
                                                      anyString(),
                                                      any(),
                                                      any()))
            .thenThrow(new RuntimeException());
        Map<String, List<String>> response = hearingsService
            .getHearingsListedForCurrentDateByListOfCaseIdsWithoutCourtVenueDetails(new ArrayList<>(),
                                                                                    "", "");
        Assertions.assertTrue(response.isEmpty());
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
