package uk.gov.hmcts.reform.hmc.api.services;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import feign.FeignException;
import feign.Request;
import feign.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseHearing;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.model.response.JudgeDetail;

@ExtendWith({MockitoExtension.class})
@ActiveProfiles("test")
class HearingCftServiceTest {

    @InjectMocks HearingsServiceImpl hearingsService;

    @Mock RestTemplate restTemplate;

    @Mock private RefDataServiceImpl refDataService;

    @Mock private RefDataJudicialServiceImpl refDataJudicialService;

    @Mock private AuthTokenGenerator authTokenGenerator;

    @Mock private IdamTokenGenerator idamTokenGenerator;

    @Mock private HearingApiClient hearingApiClient;

    @Test
    void shouldReturnCtfHearingsTest() {

        CourtDetail courtDetail =
                CourtDetail.courtDetailWith().courtTypeId("18").hearingVenueId("231596").build();
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
                        .build();
        ResponseEntity<Hearings> response = ResponseEntity.ok(caseHearings);
        when(restTemplate.exchange(
                        anyString(),
                        any(HttpMethod.class),
                        ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<Hearings>>any()))
                .thenReturn(response);
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataService.getCourtDetails("231596")).thenReturn(courtDetail);
        when(refDataJudicialService.getJudgeDetails("4925644")).thenReturn(judgeDetail);

        Hearings hearings =
                hearingsService.getHearingsByCaseRefNo("1671620456009274", "Auth", "sauth");
        Assertions.assertEquals("ABA5", hearings.getHmctsServiceCode());
    }

    @Test
    void shouldReturnCtfHearingsAuthExceptionTest() throws IOException, ParseException {
        when(restTemplate.exchange(
                        anyString(),
                        any(HttpMethod.class),
                        ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<Hearings>>any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_GATEWAY));

        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");

        Assertions.assertEquals(null, hearingsService.getHearingsByCaseRefNo("123", "", ""));
    }

    @Test
    void shouldReturnCtfHearingsExceptionTest() throws IOException, ParseException {
        when(restTemplate.exchange(
                        anyString(),
                        any(HttpMethod.class),
                        ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<Hearings>>any()))
                .thenThrow(new NullPointerException("Null Point Exception"));

        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");

        Assertions.assertEquals(
                null, hearingsService.getHearingsByCaseRefNo("123", "Auth", "sauth"));
    }

    @Test
    void shouldReturnCtfHearingsByCaseIdTest() {

        CourtDetail courtDetail =
                CourtDetail.courtDetailWith().courtTypeId("18").hearingVenueId("231596").build();
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
                        .build();

        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataService.getCourtDetails("231596")).thenReturn(courtDetail);
        when(refDataJudicialService.getJudgeDetails("4925644")).thenReturn(judgeDetail);
        when(hearingApiClient.getHearingDetails(anyString(), any(), any()))
                .thenReturn(caseHearings);

        Hearings hearingsResponse =
                hearingsService.getHearingsByCaseId("caserefno", "Auth", "sauth");
        Assertions.assertEquals("ABA5", hearingsResponse.getHmctsServiceCode());
    }

    @Test
    void shouldReturnCtfHearingsByCaseIdFeignExceptionTest() throws IOException, ParseException {
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(hearingApiClient.getHearingDetails(anyString(), any(), any()))
                .thenThrow(feignException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Not found"));

        Assertions.assertEquals(
                null, hearingsService.getHearingsByCaseId("auth", "sauth", "testcase"));
    }

    @Test
    void shouldReturnCtfHearingsByCaseIdAuthExceptionTest() throws IOException, ParseException {
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(hearingApiClient.getHearingDetails(anyString(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

        Assertions.assertEquals(
                null, hearingsService.getHearingsByCaseId("auth", "sauth", "testcase"));
    }

    @Test
    void shouldReturnCtfHearingsByCaseIdExceptionTest() throws IOException, ParseException {
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(hearingApiClient.getHearingDetails(anyString(), any(), any()))
                .thenThrow(new RuntimeException());

        Assertions.assertEquals(
                null, hearingsService.getHearingsByCaseId("auth", "sauth", "testcase"));
    }

    @Test
    void shouldReturnCtfHearingsByListOfCaseIdsTest() {

        CourtDetail courtDetail =
                CourtDetail.courtDetailWith().courtTypeId("18").hearingVenueId("231596").build();
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
                        .build();

        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(hearingApiClient.getHearingDetails(anyString(), any(), any()))
                .thenReturn(caseHearings);
        List<String> caseIds = Arrays.asList("caseref1", "caseref2");
        List<Hearings> hearingsResponse =
                hearingsService.getHearingsByListOfCaseIds(caseIds, "Auth", "sauth");
        Assertions.assertEquals("ABA5", hearingsResponse.get(0).getHmctsServiceCode());
    }

    @Test
    void shouldReturnCtfHearingsByListOfCaseIdsFeignExceptionTest()
            throws IOException, ParseException {
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(hearingApiClient.getHearingDetails(anyString(), any(), any()))
                .thenThrow(feignException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Not found"));
        List<String> caseIds = Arrays.asList("caseref1", "caseref2");
        Assertions.assertTrue(
                hearingsService.getHearingsByListOfCaseIds(caseIds, "sauth", "testcase").isEmpty());
    }

    @Test
    void shouldReturnCtfHearingsByListOfCaseIdsAuthExceptionTest()
            throws IOException, ParseException {
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(hearingApiClient.getHearingDetails(anyString(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

        List<String> caseIds = Arrays.asList("caseref1", "caseref2");
        Assertions.assertTrue(
                hearingsService.getHearingsByListOfCaseIds(caseIds, "sauth", "testcase").isEmpty());
    }

    @Test
    void shouldReturnCtfHearingsByListOfCaseIdsExceptionTest() throws IOException, ParseException {
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(hearingApiClient.getHearingDetails(anyString(), any(), any()))
                .thenThrow(new RuntimeException());

        List<String> caseIds = Arrays.asList("caseref1", "caseref2");
        Assertions.assertTrue(
                hearingsService.getHearingsByListOfCaseIds(caseIds, "sauth", "testcase").isEmpty());
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
