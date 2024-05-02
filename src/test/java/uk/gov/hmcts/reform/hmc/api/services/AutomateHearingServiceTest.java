package uk.gov.hmcts.reform.hmc.api.services;

import org.json.simple.parser.ParseException;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.model.ccd.AttendHearing;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseData;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseManagementLocation;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingChannelsEnum;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingData;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingPriorityTypeEnum;
import uk.gov.hmcts.reform.hmc.api.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingResponse;

import java.io.IOException;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
@ActiveProfiles("test")
@PropertySource("classpath:application.yaml")
class AutomateHearingServiceTest {

    @InjectMocks HearingsServiceImpl hearingsService;

    @Mock private AuthTokenGenerator authTokenGenerator;

    @Mock private IdamTokenGenerator idamTokenGenerator;


    @Mock private HearingApiClient hearingApiClient;


    @Ignore("ignore")
    void shouldReturnAutomateHearingTest() throws IOException, ParseException {
        HearingResponse hearingResponse =
            HearingResponse.builder().build();

        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(hearingApiClient.createHearingDetails(anyString(), any(), any()))
                .thenReturn(hearingResponse);

        AttendHearing attendHearing = AttendHearing.builder().isInterpreterNeeded(false).isWelshNeeded(false).build();

        CaseManagementLocation caseManagementLocation = CaseManagementLocation.builder().build();

        HearingData hearingData = HearingData.builder()
            .hearingTypes(DynamicList.builder().build())
            .hearingPriorityTypeEnum(HearingPriorityTypeEnum.valueOf("StandardPriority"))
            .courtList(DynamicList.builder().build())
            .hearingChannelsEnum(HearingChannelsEnum.valueOf("INTER"))
            .build();


        CaseData caseData = CaseData.caseDataBuilder()
            .attendHearing(attendHearing)
            .caseManagementLocation(caseManagementLocation)
            .issueDate(LocalDate.parse("2024-02-05"))
            .hearingData(hearingData)
            .build();

        HearingResponse hearingsResponse =
                hearingsService.createAutomatedHearings(caseData);
        Assertions.assertEquals("123", hearingsResponse.getHearingRequestID());
        Assertions.assertEquals("200", hearingsResponse.getStatus());
    }

    @Ignore("ignore")
    void shouldReturnAutomateHearingsByCaseRefNoFeignExceptionTest()
            throws IOException, ParseException {
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        HearingResponse hearingResponse = hearingsService.createAutomatedHearings(null);
        Assertions.assertNull(hearingResponse.getStatus());
    }

    @Ignore("ignore")
    void shouldReturnAutomateHearingsExceptionTest()
        throws IOException, ParseException {
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        HearingResponse hearingResponse = hearingsService.createAutomatedHearings(null);
        Assertions.assertNull(hearingResponse.getStatus());
    }

    @Ignore("ignore")
    void shouldReturnAutomateHearingsErrorTest()
        throws IOException, ParseException {
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        HearingResponse hearingResponse = hearingsService.createAutomatedHearings(null);
        Assertions.assertNull(hearingResponse.getStatus());
    }


}
