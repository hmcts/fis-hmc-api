package uk.gov.hmcts.reform.hmc.api.services;

import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.hmc.api.model.ccd.NextHearingDetails;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseHearing;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

@ExtendWith({MockitoExtension.class})
@ActiveProfiles("test")
class NextHearingDetailsServiceTest {

    @InjectMocks NextHearingDetailsServiceImpl nextHearingDetailsService;

    @Mock private HearingsServiceImpl hearingsService;

    @Test
    void shouldReturnNextHearingDetailsTest() {

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

        Hearings hearings =
                Hearings.hearingsWith()
                        .caseRef("123")
                        .caseHearings(caseHearingList)
                        .hmctsServiceCode("BBA3")
                        .build();

        when(hearingsService.getHearingsByCaseRefNo("1671620456009274")).thenReturn(hearings);

        NextHearingDetails nextHearingDetailsResponse =
                nextHearingDetailsService.getNextHearingDateByCaseRefNo("1671620456009274");
        Assertions.assertEquals(
                testNextHearingDate, nextHearingDetailsResponse.getNextHearingDate());
    }
}
