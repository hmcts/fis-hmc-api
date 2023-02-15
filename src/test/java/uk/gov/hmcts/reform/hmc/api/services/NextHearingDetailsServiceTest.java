package uk.gov.hmcts.reform.hmc.api.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.model.ccd.NextHearingDetails;
import uk.gov.hmcts.reform.hmc.api.model.response.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
@ActiveProfiles("test")
class NextHearingDetailsServiceTest {

    @InjectMocks
    NextHearingDetailsServiceImpl nextHearingDetailsService;

    @Mock
    RestTemplate restTemplate;

    @Mock private AuthTokenGenerator authTokenGenerator;

    @Mock private IdamTokenGenerator idamTokenGenerator;

    @Mock private RefDataServiceImpl refDataService;

    @Mock private RefDataJudicialServiceImpl refDataJudicialService;

    @Mock private HearingsServiceImpl hearingsService;

    @Test
    void shouldReturnNextHearingDetailsTest() {

        LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2024, 04, 28, 1, 0);

        HearingDaySchedule hearingDaySchedule =
            HearingDaySchedule.hearingDayScheduleWith()
                .hearingVenueId("231596")
                .hearingJudgeId("4925644")
                .hearingStartDateTime(LOCAL_DATE_TIME)
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

        Hearings hearings = Hearings.hearingsWith().caseRef("123").caseHearings(caseHearingList).hmctsServiceCode("BBA3").build();

        when(hearingsService.getHearingsByCaseRefNo("1671620456009274")).thenReturn(hearings);

        NextHearingDetails nextHearingDetailsResponse = nextHearingDetailsService.getNextHearingDateByCaseRefNo("1671620456009274");
        Assertions.assertEquals(LOCAL_DATE_TIME, nextHearingDetailsResponse.getNextHearingDate());
    }
}
