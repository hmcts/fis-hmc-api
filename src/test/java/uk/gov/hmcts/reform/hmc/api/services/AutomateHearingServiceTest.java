package uk.gov.hmcts.reform.hmc.api.services;

import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.model.ccd.AttendHearing;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseData;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseManagementLocation;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Element;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingChannelsEnum;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingData;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingPriorityTypeEnum;
import uk.gov.hmcts.reform.hmc.api.model.ccd.ManageOrders;
import uk.gov.hmcts.reform.hmc.api.model.ccd.caseflagsv2.AllPartyFlags;
import uk.gov.hmcts.reform.hmc.api.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.hmc.api.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith({MockitoExtension.class})
@ActiveProfiles("test")
@PropertySource("classpath:application.yaml")
class AutomateHearingServiceTest {

    @InjectMocks HearingsServiceImpl hearingsService;

    @Mock private AuthTokenGenerator authTokenGenerator;

    @Mock private IdamTokenGenerator idamTokenGenerator;


    @Mock private HearingApiClient hearingApiClient;


    private static final String TEST_UUID = "00000000-0000-0000-0000-000000000000";

    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    @Test
    void shouldReturnAutomateHearingTest() throws IOException, ParseException {

        HearingResponse hearingResponse = HearingResponse.builder()
            .hearingRequestID("123")
            .status("200")
            .build();

        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn(SERVICE_AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(AUTHORIZATION_TOKEN);
        when(hearingApiClient.createHearingDetails(any(), any(), any()))
            .thenReturn(hearingResponse);

        DynamicListElement dynamicListElement = DynamicListElement.builder().code(TEST_UUID).label(" ").build();
        DynamicList dynamicList = DynamicList.builder()
            .listItems(List.of(dynamicListElement))
            .value(dynamicListElement)
            .build();


        HearingData hearingData = HearingData.builder()
            .hearingTypes(dynamicList)
            .confirmedHearingDates(dynamicList)
            .hearingChannels(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingVideoChannels(dynamicList)
            .hearingTelephoneChannels(dynamicList)
            .courtList(dynamicList)
            .localAuthorityHearingChannel(dynamicList)
            .hearingListedLinkedCases(dynamicList)
            .applicantSolicitorHearingChannel(dynamicList)
            .respondentHearingChannel(dynamicList)
            .respondentSolicitorHearingChannel(dynamicList)
            .cafcassHearingChannel(dynamicList)
            .cafcassCymruHearingChannel(dynamicList)
            .applicantHearingChannel(dynamicList)
            .additionalHearingDetails("Test")
            .instructionsForRemoteHearing("Test")
            .hearingEstimatedHours("5")
            .hearingEstimatedMinutes("40")
            .hearingEstimatedDays("15")
            .allPartiesAttendHearingSameWayYesOrNo("Yes")
            .hearingJudgePersonalCode("test")
            .hearingJudgeLastName("test")
            .hearingJudgeEmailAddress("Test")
            .applicantName("Test")
            .hearingChannelsEnum(HearingChannelsEnum.DEFAULT)
            .hearingPriorityTypeEnum(HearingPriorityTypeEnum.StandardPriority)
            .build();

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        hearingDataList.add(element(hearingData));


        ManageOrders manageOrders = ManageOrders.builder()
            .ordersHearingDetails(hearingDataList)
            .build();


        CaseData caseData = CaseData
            .caseDataBuilder()
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("123")
            .caseManagementLocation(CaseManagementLocation.builder().baseLocation("test").build())
            .attendHearing(AttendHearing.builder().isWelshNeeded(Boolean.FALSE).build())
            .allPartyFlags(AllPartyFlags.builder().build())
            .hearingData(hearingData)
            .issueDate(LocalDate.now())
            .manageOrders(manageOrders)
            .build();

        HearingResponse hearingsResponse =
            hearingsService.createAutomatedHearings(caseData);
        assertThat(hearingsResponse.getStatus()).isEqualTo("200");
    }


    public static <T> Element<T> element(T element) {
        return Element.<T>builder()
            .id(UUID.randomUUID())
            .value(element)
            .build();
    }

    @Test
    void shouldReturnAutomateHearingsByCaseRefNoFeignExceptionTest()
            throws IOException, ParseException {
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        HearingResponse hearingResponse = hearingsService.createAutomatedHearings(null);
        Assertions.assertEquals(null,hearingResponse);
    }


    void shouldReturnAutomateHearingsExceptionTest()
        throws IOException, ParseException {
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        HearingResponse hearingResponse = hearingsService.createAutomatedHearings(null);
        Assertions.assertNull(hearingResponse);
    }

    @Test
    void shouldReturnAutomateHearingsErrorTest()
        throws IOException, ParseException {
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn("MOCK_AUTH_TOKEN");
        HearingResponse hearingResponse = hearingsService.createAutomatedHearings(null);
        Assertions.assertEquals(null,hearingResponse);
    }


}
