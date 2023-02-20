package uk.gov.hmcts.reform.hmc.api.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    @Before
    public void setup() {}

    @Test
    void shouldReturnNextHearingDetailsTest() {

        LocalDateTime testNextHearingDate1 = LocalDateTime.of(2023, 02, 23, 1, 0);

        LocalDateTime testNextHearingDate2 = LocalDateTime.of(2023, 02, 24, 1, 0);

        LocalDateTime testNextHearingDate3 = LocalDateTime.of(2023, 02, 21, 1, 0);

        LocalDateTime testNextHearingDate4 = LocalDateTime.of(2023, 02, 25, 1, 0);

        HearingDaySchedule hearingDaySchedule1 =
                HearingDaySchedule.hearingDayScheduleWith()
                        .hearingStartDateTime(testNextHearingDate1)
                        .build();

        HearingDaySchedule hearingDaySchedule2 =
                HearingDaySchedule.hearingDayScheduleWith()
                        .hearingStartDateTime(testNextHearingDate2)
                        .build();

        List<HearingDaySchedule> hearingDayScheduleList1 = new ArrayList<>();
        hearingDayScheduleList1.add(hearingDaySchedule1);
        hearingDayScheduleList1.add(hearingDaySchedule2);

        HearingDaySchedule hearingDaySchedule3 =
                HearingDaySchedule.hearingDayScheduleWith()
                        .hearingStartDateTime(testNextHearingDate3)
                        .build();

        HearingDaySchedule hearingDaySchedule4 =
                HearingDaySchedule.hearingDayScheduleWith()
                        .hearingStartDateTime(testNextHearingDate4)
                        .build();
        List<HearingDaySchedule> hearingDayScheduleList2 = new ArrayList<>();
        hearingDayScheduleList2.add(hearingDaySchedule3);
        hearingDayScheduleList2.add(hearingDaySchedule4);

        CaseHearing caseHearing1 =
                CaseHearing.caseHearingWith()
                        .hmcStatus("LISTED")
                        .hearingDaySchedule(hearingDayScheduleList1)
                        .build();

        CaseHearing caseHearing2 =
                CaseHearing.caseHearingWith()
                        .hmcStatus("LISTED")
                        .hearingDaySchedule(hearingDayScheduleList2)
                        .build();

        List<CaseHearing> caseHearingList = new ArrayList<>();
        caseHearingList.add(caseHearing1);
        caseHearingList.add(caseHearing2);

        Hearings hearings =
                Hearings.hearingsWith()
                        .caseRef("123")
                        .caseHearings(caseHearingList)
                        .hmctsServiceCode("BBA3")
                        .build();

        NextHearingDetails nextHearingDetailsResponse =
                nextHearingDetailsService.getNextHearingDate(hearings);
        Assertions.assertEquals(
                testNextHearingDate3, nextHearingDetailsResponse.getNextHearingDate());
    }
}
