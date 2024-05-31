package uk.gov.hmcts.reform.hmc.api.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.api.enums.State.DECISION_OUTCOME;
import static uk.gov.hmcts.reform.hmc.api.enums.State.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.COMPLETED;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.hmc.api.enums.State;
import uk.gov.hmcts.reform.hmc.api.model.ccd.NextHearingDetails;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseHearing;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.utils.Constants;

@ExtendWith({MockitoExtension.class})
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NextHearingDetailsServiceTest {

    @InjectMocks NextHearingDetailsServiceImpl nextHearingDetailsService;

    @Mock private PrlUpdateServiceImpl prlUpdateService;

    @BeforeAll
    public void setup() {}

    @Test
    void shouldUpdateNextHearingDetailsTest() {
        LocalDateTime testNextHearingDate1 = LocalDateTime.now().plusDays(5).withNano(1);

        LocalDateTime testNextHearingDate2 = LocalDateTime.now().plusDays(5).withNano(1);

        LocalDateTime testNextHearingDate3 = LocalDateTime.now().plusDays(5).withNano(1);

        LocalDateTime testNextHearingDate4 = LocalDateTime.now().plusDays(5).withNano(1);

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
                        .hearingID(123L)
                        .hmcStatus("LISTED")
                        .hearingDaySchedule(hearingDayScheduleList1)
                        .build();

        CaseHearing caseHearing2 =
                CaseHearing.caseHearingWith()
                        .hearingID(333L)
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

        when(prlUpdateService.updatePrlServiceWithNextHearingDate(any(), any())).thenReturn(true);

        Boolean isUpdated = nextHearingDetailsService.updateNextHearingDetails("auth", hearings);
        Assertions.assertEquals(Boolean.TRUE, isUpdated);
    }


    @Test
    void testUpdateNextHearingDetailsForUnknownHmcStatus() {
        LocalDateTime testNextHearingDate1 = LocalDateTime.now().plusDays(5).withNano(1);

        LocalDateTime testNextHearingDate2 = LocalDateTime.now().plusDays(5).withNano(1);

        LocalDateTime testNextHearingDate3 = LocalDateTime.now().plusDays(5).withNano(1);

        LocalDateTime testNextHearingDate4 = LocalDateTime.now().plusDays(5).withNano(1);

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
                .hearingID(123L)
                .hmcStatus("UNKNOWN")
                .hearingDaySchedule(hearingDayScheduleList1)
                .build();

        CaseHearing caseHearing2 =
            CaseHearing.caseHearingWith()
                .hearingID(333L)
                .hmcStatus("UNKNOWN")
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

        Boolean isUpdatedFalse = nextHearingDetailsService.updateNextHearingDetails("auth", hearings);
        Assertions.assertEquals(Boolean.FALSE, isUpdatedFalse);
    }

    @Test
    void shouldReturnNextHearingDetailsTest() {
        LocalDateTime testNextHearingDate1 = LocalDateTime.now().plusDays(5).withNano(1);

        LocalDateTime testNextHearingDate2 = LocalDateTime.now().plusDays(5).withNano(1);

        LocalDateTime testNextHearingDate3 = LocalDateTime.now().plusDays(5).withNano(1);

        LocalDateTime testNextHearingDate4 = LocalDateTime.now().plusDays(5).withNano(1);

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
                    .hearingID(123L)
                        .hmcStatus("LISTED")
                        .hearingDaySchedule(hearingDayScheduleList1)
                        .build();

        CaseHearing caseHearing2 =
                CaseHearing.caseHearingWith()
                    .hearingID(456L)
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
                testNextHearingDate3, nextHearingDetailsResponse.getHearingDateTime());
        Assertions.assertEquals(123L, nextHearingDetailsResponse.getHearingID());
    }

    @Test
    void shouldFetchStateToUpdateWhenCurrHmcIsCompletedButSomeHearingInFuture() {
        LocalDateTime testNextHearingDate1 = LocalDateTime.now().plusDays(5).withNano(1);

        LocalDateTime testNextHearingDate2 = LocalDateTime.now().plusDays(5).withNano(1);

        LocalDateTime testNextHearingDate3 = LocalDateTime.now().plusDays(5).withNano(1);

        LocalDateTime testNextHearingDate4 = LocalDateTime.now().plusDays(5).withNano(1);

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
        String currHearingHmcStatus = Constants.LISTED;
        State finalCaseState =
                nextHearingDetailsService.fetchStateForUpdate(hearings, currHearingHmcStatus);
        Assertions.assertEquals(PREPARE_FOR_HEARING_CONDUCT_HEARING, finalCaseState);
    }

    @Test
    void shouldFetchStateToUpdateWhenCurrHmcIsCompleted() {
        Hearings hearings = getHearingData();
        State finalCaseState =
            nextHearingDetailsService.fetchStateForUpdate(hearings, COMPLETED);
        Assertions.assertEquals(PREPARE_FOR_HEARING_CONDUCT_HEARING, finalCaseState);
    }

    @Test
    void shouldFetchStateToUpdateWhenCurrHmcIsCompletedNoHearingInFuture() {
        LocalDateTime testNextHearingDate1 = LocalDateTime.now();

        LocalDateTime testNextHearingDate2 = LocalDateTime.now();

        LocalDateTime testNextHearingDate3 = LocalDateTime.now();

        LocalDateTime testNextHearingDate4 = LocalDateTime.now();

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

        Hearings hearings = Hearings.hearingsWith()
            .caseRef("123")
            .caseHearings(caseHearingList)
            .hmctsServiceCode("BBA3")
            .build();
        State finalCaseState =
            nextHearingDetailsService.fetchStateForUpdate(hearings, COMPLETED);
        Assertions.assertEquals(DECISION_OUTCOME, finalCaseState);
    }

    private static Hearings getHearingData() {
        LocalDateTime testNextHearingDate1 = LocalDateTime.now().plusDays(5).withNano(1);

        LocalDateTime testNextHearingDate2 = LocalDateTime.now().plusDays(5).withNano(1);

        LocalDateTime testNextHearingDate3 = LocalDateTime.now().plusDays(5).withNano(1);

        LocalDateTime testNextHearingDate4 = LocalDateTime.now().plusDays(5).withNano(1);

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

        return Hearings.hearingsWith()
                .caseRef("123")
                .caseHearings(caseHearingList)
                .hmctsServiceCode("BBA3")
                .build();
    }

    @Test
    void shouldFetchStateToUpdateWhenCurrHmcIsCompletedButNoHearingInFuture() {

        LocalDateTime pastHearingDate1 = LocalDateTime.of(2023, 01, 18, 1, 0);

        LocalDateTime pastHearingDate2 = LocalDateTime.of(2023, 02, 20, 1, 0);

        HearingDaySchedule hearingDaySchedule1 =
                HearingDaySchedule.hearingDayScheduleWith()
                        .hearingStartDateTime(pastHearingDate1)
                        .build();
        List<HearingDaySchedule> hearingDayScheduleList1 = new ArrayList<>();
        hearingDayScheduleList1.add(hearingDaySchedule1);

        HearingDaySchedule hearingDaySchedule2 =
                HearingDaySchedule.hearingDayScheduleWith()
                        .hearingStartDateTime(pastHearingDate2)
                        .build();
        List<HearingDaySchedule> hearingDayScheduleList2 = new ArrayList<>();
        hearingDayScheduleList2.add(hearingDaySchedule2);

        CaseHearing caseHearing1 =
                CaseHearing.caseHearingWith()
                        .hmcStatus("CANCELLED")
                        .hearingDaySchedule(hearingDayScheduleList2)
                        .build();

        CaseHearing caseHearing2 =
                CaseHearing.caseHearingWith()
                        .hmcStatus("COMPLETED")
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
        State finalCaseState =
                nextHearingDetailsService.fetchStateForUpdate(hearings, COMPLETED);
        Assertions.assertEquals(DECISION_OUTCOME, finalCaseState);
    }

    @Test
    void shouldFetchStateToUpdateWhenAllCompleteddButNoHearingInFuture() {

        LocalDateTime pastHearingDate1 = LocalDateTime.of(2023, 01, 18, 1, 0);

        LocalDateTime pastHearingDate2 = LocalDateTime.of(2023, 02, 20, 1, 0);

        HearingDaySchedule hearingDaySchedule1 =
                HearingDaySchedule.hearingDayScheduleWith()
                        .hearingStartDateTime(pastHearingDate1)
                        .build();
        List<HearingDaySchedule> hearingDayScheduleList1 = new ArrayList<>();
        hearingDayScheduleList1.add(hearingDaySchedule1);

        HearingDaySchedule hearingDaySchedule2 =
                HearingDaySchedule.hearingDayScheduleWith()
                        .hearingStartDateTime(pastHearingDate2)
                        .build();
        List<HearingDaySchedule> hearingDayScheduleList2 = new ArrayList<>();
        hearingDayScheduleList2.add(hearingDaySchedule2);

        CaseHearing caseHearing1 =
                CaseHearing.caseHearingWith()
                        .hmcStatus("COMPLETED")
                        .hearingDaySchedule(hearingDayScheduleList2)
                        .build();

        CaseHearing caseHearing2 =
                CaseHearing.caseHearingWith()
                        .hmcStatus("COMPLETED")
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

        State finalCaseState =
                nextHearingDetailsService.fetchStateForUpdate(hearings, COMPLETED);
        Assertions.assertEquals(DECISION_OUTCOME, finalCaseState);
    }

    @Test
    void shouldFetchStateToUpdateWhenNoHearingInFuture() {

        LocalDateTime pastHearingDate1 = LocalDateTime.of(2023, 01, 18, 1, 0);

        LocalDateTime pastHearingDate2 = LocalDateTime.of(2023, 02, 20, 1, 0);

        HearingDaySchedule hearingDaySchedule1 =
                HearingDaySchedule.hearingDayScheduleWith()
                        .hearingStartDateTime(pastHearingDate1)
                        .build();
        List<HearingDaySchedule> hearingDayScheduleList1 = new ArrayList<>();
        hearingDayScheduleList1.add(hearingDaySchedule1);

        HearingDaySchedule hearingDaySchedule2 =
                HearingDaySchedule.hearingDayScheduleWith()
                        .hearingStartDateTime(pastHearingDate2)
                        .build();
        List<HearingDaySchedule> hearingDayScheduleList2 = new ArrayList<>();
        hearingDayScheduleList2.add(hearingDaySchedule2);

        CaseHearing caseHearing1 =
                CaseHearing.caseHearingWith()
                        .hmcStatus("COMPLETED")
                        .hearingDaySchedule(hearingDayScheduleList2)
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
                        .hmctsServiceCode("ABA5")
                        .build();
        String currHearingHmcStatus = Constants.LISTED;

        State finalCaseState =
                nextHearingDetailsService.fetchStateForUpdate(hearings, currHearingHmcStatus);
        Assertions.assertEquals(PREPARE_FOR_HEARING_CONDUCT_HEARING, finalCaseState);
    }
}
