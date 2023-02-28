package uk.gov.hmcts.reform.hmc.api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.api.enums.State.DECISION_OUTCOME;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.enums.State;
import uk.gov.hmcts.reform.hmc.api.exceptions.PrlUpdateException;
import uk.gov.hmcts.reform.hmc.api.model.ccd.NextHearingDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingDTO;
import uk.gov.hmcts.reform.hmc.api.model.request.NextHearingDetailsDTO;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseHearing;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.model.response.JudgeDetail;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class PrlUpdateServiceTest {

    @InjectMocks private PrlUpdateServiceImpl prlUpdateService;

    @Mock private PrlUpdateApi prlUpdateApi;

    @Mock private AuthTokenGenerator authTokenGenerator;

    @Mock private HearingsService hearingsService;

    @Test
    public void shouldUpdatePrivateLawInPrlcosTest() throws IOException, ParseException {

        HearingDTO hearingDto =
                HearingDTO.hearingRequestDTOWith()
                        .hearingId("testHearinID")
                        .caseRef("testCaseRef")
                        .hmctsServiceCode("ABA5")
                        .build();
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(prlUpdateApi.prlUpdate(anyString(), any(), any())).thenReturn(ResponseEntity.ok("OK"));
        State caseState = DECISION_OUTCOME;
        Boolean isOK = prlUpdateService.updatePrlServiceWithHearing(hearingDto, caseState);
        assertEquals(true, isOK);
    }

    @Test
    public void shouldNotUpdatePrivateLawInPrlcosTest() throws IOException, ParseException {

        HearingDTO hearingDto =
                HearingDTO.hearingRequestDTOWith()
                        .hearingId("testHearinID")
                        .caseRef("testCaseRef")
                        .hmctsServiceCode("NonBBA3")
                        .build();
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(prlUpdateApi.prlUpdate(anyString(), any(), any())).thenReturn(ResponseEntity.ok("OK"));
        State caseState = DECISION_OUTCOME;
        Boolean isOK = prlUpdateService.updatePrlServiceWithHearing(hearingDto, caseState);
        assertEquals(true, isOK);
    }

    @Test
    public void shouldUpdatePrivateLawInPrlcosReturn401Test() throws IOException, ParseException {

        HearingDTO hearingDto =
                HearingDTO.hearingRequestDTOWith()
                        .hearingId("testHearinID")
                        .caseRef("testCaseRef")
                        .hmctsServiceCode("ABA5")
                        .build();
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(prlUpdateApi.prlUpdate(anyString(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));
        State caseState = DECISION_OUTCOME;
        assertThrows(
                PrlUpdateException.class,
                () -> prlUpdateService.updatePrlServiceWithHearing(hearingDto, caseState));
    }

    @Test
    public void shouldUpdatePrivateLawInPrlcosS2sExceptionTest()
            throws IOException, ParseException {

        HearingDTO hearingDto =
                HearingDTO.hearingRequestDTOWith()
                        .hearingId("testHearinID")
                        .caseRef("testCaseRef")
                        .hmctsServiceCode("ABA5")
                        .build();
        when(authTokenGenerator.generate())
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));
        when(prlUpdateApi.prlUpdate(anyString(), any(), any())).thenReturn(ResponseEntity.ok("OK"));
        State caseState = DECISION_OUTCOME;
        assertThrows(
                PrlUpdateException.class,
                () -> prlUpdateService.updatePrlServiceWithHearing(hearingDto, caseState));
    }

    @Test
    void shouldUpdateNextHearingDateInPrlcosTest() throws IOException, ParseException {

        CourtDetail courtDetail =
                CourtDetail.courtDetailWith().courtTypeId("18").hearingVenueId("231596").build();
        List<CourtDetail> courtDetailsList = new ArrayList<>();
        courtDetailsList.add(courtDetail);

        JudgeDetail judgeDetail = JudgeDetail.judgeDetailWith().hearingJudgeName("test").build();
        List<JudgeDetail> judgeDetailsList = new ArrayList<>();
        judgeDetailsList.add(judgeDetail);

        LocalDateTime someHearingStartDate = LocalDateTime.of(2023, 8, 28, 14, 33, 48, 640000);
        HearingDaySchedule hearingDaySchedule =
                HearingDaySchedule.hearingDayScheduleWith()
                        .hearingVenueId("231596")
                        .hearingJudgeId("4925644")
                        .hearingStartDateTime(someHearingStartDate)
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

        NextHearingDetails nextHearingDetails =
                NextHearingDetails.builder()
                        .nextHearingDate(LocalDateTime.now())
                        .hearingId(4565432L)
                        .build();

        NextHearingDetailsDTO nextHearingDetailsDto =
                NextHearingDetailsDTO.nextHearingDetailsRequestDTOWith()
                        .nextHearingDetails(nextHearingDetails)
                        .caseRef("test")
                        .build();

        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(prlUpdateApi.prlNextHearingDateUpdate(anyString(), anyString(), any()))
                .thenReturn(ResponseEntity.ok("OK"));
        when(hearingsService.getHearingsByCaseRefNo(any(), any(), any())).thenReturn(caseHearings);

        Boolean isOK =
                prlUpdateService.updatePrlServiceWithNextHearingDate("", nextHearingDetailsDto);
        assertEquals(true, isOK);
    }

    @Test
    void shouldUpdateNextHearingDateInPrlcosS2sExceptionTest() throws IOException, ParseException {

        CourtDetail courtDetail =
                CourtDetail.courtDetailWith().courtTypeId("18").hearingVenueId("231596").build();
        List<CourtDetail> courtDetailsList = new ArrayList<>();
        courtDetailsList.add(courtDetail);

        JudgeDetail judgeDetail = JudgeDetail.judgeDetailWith().hearingJudgeName("test").build();
        List<JudgeDetail> judgeDetailsList = new ArrayList<>();
        judgeDetailsList.add(judgeDetail);

        LocalDateTime someHearingStartDate = LocalDateTime.of(2023, 8, 28, 14, 33, 48, 640000);

        HearingDaySchedule hearingDaySchedule =
                HearingDaySchedule.hearingDayScheduleWith()
                        .hearingVenueId("231596")
                        .hearingJudgeId("4925644")
                        .hearingStartDateTime(someHearingStartDate)
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

        when(authTokenGenerator.generate())
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

        when(prlUpdateApi.prlNextHearingDateUpdate(anyString(), anyString(), any()))
                .thenReturn(ResponseEntity.ok("OK"));
        when(hearingsService.getHearingsByCaseRefNo(any(), any(), any())).thenReturn(caseHearings);

        NextHearingDetails nextHearingDetails =
                NextHearingDetails.builder()
                        .nextHearingDate(LocalDateTime.now())
                        .hearingId(4565432L)
                        .build();

        NextHearingDetailsDTO nextHearingDetailsDto =
                NextHearingDetailsDTO.nextHearingDetailsRequestDTOWith()
                        .nextHearingDetails(nextHearingDetails)
                        .caseRef("test")
                        .build();

        assertThrows(
                PrlUpdateException.class,
                () ->
                        prlUpdateService.updatePrlServiceWithNextHearingDate(
                                "auth", nextHearingDetailsDto));
    }
}
