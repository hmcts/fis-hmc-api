package uk.gov.hmcts.reform.hmc.api.services;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseHearing;
import uk.gov.hmcts.reform.hmc.api.model.response.Categories;
import uk.gov.hmcts.reform.hmc.api.model.response.Category;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.model.response.JudgeDetail;
import uk.gov.hmcts.reform.hmc.api.restclient.HmcHearingApi;

@ExtendWith({MockitoExtension.class})
@ActiveProfiles("test")
class HearingCftServiceTest {

    @InjectMocks HearingsServiceImpl hearingsService;

    @Mock RestTemplate restTemplate;

    @Mock private RefDataServiceImpl refDataService;

    @Mock private RefDataJudicialServiceImpl refDataJudicialService;

    @Mock HmcHearingApi hearingApi;

    @Mock private AuthTokenGenerator authTokenGenerator;

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
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),
                        ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<Hearings>>any()))
                .thenReturn(response);
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataService.getCourtDetails("231596")).thenReturn(courtDetail);
        when(refDataJudicialService.getJudgeDetails("4925644")).thenReturn(judgeDetail);

        List<Category> listOfCategory = new ArrayList<>();

        final Category category =
                Category.builder()
                        .categoryKey("HearingType")
                        .key("ABA5-FHR")
                        .valueEn("First Hearing")
                        .build();

        listOfCategory.add(category);

        final Categories categories = Categories.builder().listOfCategory(listOfCategory).build();

        when(hearingApi.retrieveListOfValuesByCategoryId("Auth", "sauth", "HearingTyoe", "ABA5"))
                .thenReturn(categories);

        ReflectionTestUtils.setField(hearingsService, "categoryId", "HearingTyoe");

        Hearings hearings =
                hearingsService.getHearingsByCaseRefNo("1671620456009274", "Auth", "sauth");
        Assertions.assertEquals("ABA5", hearings.getHmctsServiceCode());
    }

    @Test
    void shouldReturnCtfHearingsAuthExceptionTest() throws IOException, ParseException {
        when(restTemplate.exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),
                        ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<Hearings>>any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_GATEWAY));

        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");

        Assertions.assertEquals(null, hearingsService.getHearingsByCaseRefNo("123", "", ""));
    }

    @Test
    void shouldReturnCtfHearingsExceptionTest() throws IOException, ParseException {
        when(restTemplate.exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),
                        ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<Hearings>>any()))
                .thenThrow(new NullPointerException("Null Point Exception"));

        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");

        Assertions.assertEquals(
                null, hearingsService.getHearingsByCaseRefNo("123", "Auth", "sauth"));
    }
}
