package uk.gov.hmcts.reform.hmc.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Address;
import uk.gov.hmcts.reform.hmc.api.model.ccd.AttendHearing;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseData;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseManagementLocation;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Element;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Flags;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingChannelsEnum;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingData;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingDataApplicantDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingDataRespondentDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingPriorityTypeEnum;
import uk.gov.hmcts.reform.hmc.api.model.ccd.ManageOrders;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Organisation;
import uk.gov.hmcts.reform.hmc.api.model.ccd.OtherPersonRelationshipToChild;
import uk.gov.hmcts.reform.hmc.api.model.ccd.PartyDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.caseflagsv2.AllPartyFlags;
import uk.gov.hmcts.reform.hmc.api.model.ccd.flagdata.FlagDetail;
import uk.gov.hmcts.reform.hmc.api.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.hmc.api.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.NO;

@SpringBootTest
@ActiveProfiles("test")
@PropertySource("classpath:application.yaml")
class AutomateHearingServiceTest {

    @Autowired
    private HearingsServiceImpl hearingsService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private IdamTokenGenerator idamTokenGenerator;

    @MockBean
    private HearingApiClient hearingApiClient;

    private static final String TEST_UUID = "00000000-0000-0000-0000-000000000000";

    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    CaseData caseData;

    HearingData hearingData;

    PartyDetails partyDetails;

    ManageOrders manageOrders;

    DynamicList dynamicList;

    HearingDataApplicantDetails hearingDataApplicantDetails;

    HearingDataRespondentDetails hearingDataRespondentDetails;


    @BeforeEach
    void setup() {

        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code(TEST_UUID).codes(UUID.randomUUID()).label("test").build();
        dynamicList = DynamicList.builder()
            .listItems(List.of(dynamicListElement))
            .value(dynamicListElement)
            .build();

        hearingDataApplicantDetails =
            HearingDataApplicantDetails.hearingDataApplicantDetails().build();

        hearingDataRespondentDetails =
            HearingDataRespondentDetails.hearingDataRespondentDetails().build();

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        hearingDataList.add(element(hearingData));

        FlagDetail flagDetail = FlagDetail.builder()
            .flagCode("RA0042")
            .status("test")
            .flagComment("testing")
            .subTypeKey("123")
            .hearingRelevant("test")
            .subTypeValue("test")
            .build();

        List<Element<FlagDetail>> flagDetailList = new ArrayList<>();
        flagDetailList.add(element(flagDetail));

        Flags flags = Flags.builder()
            .partyName("test")
            .roleOnCase("test")
            .details(flagDetailList)
            .build();


        manageOrders = ManageOrders.builder()
            .ordersHearingDetails(hearingDataList)
            .build();

        OtherPersonRelationshipToChild otherPersonRelationshipToChild = new  OtherPersonRelationshipToChild();

        List<Element<OtherPersonRelationshipToChild>> otherPersonRelationshipToChildlList = new ArrayList<>();
        otherPersonRelationshipToChildlList.add(element(otherPersonRelationshipToChild));


        partyDetails = PartyDetails.builder()
            .firstName("test")
            .lastName("lastname")
            .previousName("test")
            .otherPersonRelationshipToChildren(otherPersonRelationshipToChildlList)
            .solicitorOrg(Organisation.organisation("123"))
            .solicitorAddress(Address.builder().build())
            .dxNumber("345")
            .solicitorReference("test")
            .representativeFirstName("test")
            .representativeLastName("test")
            .sendSignUpLink("test")
            .solicitorEmail("test")
            .phoneNumber("123")
            .email("test@test.com")
            .address(Address.builder().build())
            .solicitorTelephone("12345")
            .caseTypeOfApplication("C100")
            .partyLevelFlag(flags)
            .partyId(UUID.randomUUID())
            .solicitorOrgUuid(UUID.randomUUID())
            .solicitorPartyId(UUID.randomUUID())
            .build();

    }

    @Test
    void shouldReturnAutomateHearingTestC100() {

        HearingResponse response = HearingResponse.builder()
            .hearingRequestID("123")
            .status("200")
            .build();

        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn(SERVICE_AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(AUTHORIZATION_TOKEN);
        when(hearingApiClient.createHearingDetails(any(), any(), any()))
            .thenReturn(response);

        getHearingData(HearingData.builder()
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
                           .hearingEstimatedHours("1")
                           .hearingEstimatedMinutes("30")
                           .hearingEstimatedDays("2"), "No");

        List<Element<PartyDetails>> partyDetailsList = new ArrayList<>();
        partyDetailsList.add(element(partyDetails));


        caseData = CaseData
            .caseDataBuilder()
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("123")
            .applicantsFL401(partyDetails)
            .caseManagementLocation(CaseManagementLocation.builder().baseLocation("test").build())
            .attendHearing(AttendHearing.builder().isWelshNeeded(NO).build())
            .allPartyFlags(AllPartyFlags.builder().build())
            .hearingData(hearingData)
            .applicants(partyDetailsList)
            .respondents(partyDetailsList)
            .issueDate(LocalDate.now())
            .manageOrders(manageOrders)
            .build();

        HearingResponse hearingsResponse =
            hearingsService.createAutomatedHearings(caseData);
        assertThat(hearingsResponse.getStatus()).isEqualTo("200");
        assertThat(hearingsResponse.getHearingRequestID()).isEqualTo("123");
    }

    private void getHearingData(HearingData.HearingDataBuilder dynamicList, String yesOrNo) {
        hearingData = dynamicList
            .allPartiesAttendHearingSameWayYesOrNo(yesOrNo)
            .hearingChannelsEnum(HearingChannelsEnum.INTER)
            .hearingJudgePersonalCode("test")
            .hearingJudgeLastName("test")
            .hearingJudgeEmailAddress("Test")
            .applicantName("Test")
            .applicantSolicitor("test")
            .respondentName("testrespondent")
            .respondentSolicitor("testsolicitor")
            .firstDateOfTheHearing(LocalDate.now())
            .hearingMustTakePlaceAtHour("02")
            .hearingMustTakePlaceAtMinute("30")
            .earliestHearingDate(LocalDate.now())
            .latestHearingDate(LocalDate.now())
            .hearingPriorityTypeEnum(HearingPriorityTypeEnum.StandardPriority)
            .isRenderingRequiredFlag("true")
            .fillingFormRenderingInfo("test")
            .hearingDataApplicantDetails(hearingDataApplicantDetails)
            .hearingDataRespondentDetails(hearingDataRespondentDetails)
            .isCafcassCymru("test")
            .additionalDetailsForHearingDateOptions("test")
            .build();
    }

    @Test
    void shouldReturnAutomateHearingTestC100ForParties() {

        HearingResponse response = HearingResponse.builder()
            .hearingRequestID("123")
            .status("200")
            .build();

        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn(SERVICE_AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(AUTHORIZATION_TOKEN);
        when(hearingApiClient.createHearingDetails(any(), any(), any()))
            .thenReturn(response);

        getHearingData(HearingData.builder()
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
                           .hearingEstimatedHours("2")
                           .hearingEstimatedMinutes("12")
                           .hearingEstimatedDays("1"), "Yes");

        List<Element<PartyDetails>> partyDetailsList = new ArrayList<>();
        partyDetailsList.add(element(partyDetails));


        caseData = CaseData
            .caseDataBuilder()
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("123")
            .applicantsFL401(partyDetails)
            .caseManagementLocation(CaseManagementLocation.builder().baseLocation("test").build())
            .attendHearing(AttendHearing.builder().isWelshNeeded(NO).build())
            .allPartyFlags(AllPartyFlags.builder().build())
            .hearingData(hearingData)
            .applicants(partyDetailsList)
            .respondents(partyDetailsList)
            .issueDate(LocalDate.now())
            .manageOrders(manageOrders)
            .build();

        HearingResponse hearingsResponse =
            hearingsService.createAutomatedHearings(caseData);
        assertThat(hearingsResponse.getStatus()).isEqualTo("200");
        assertThat(hearingsResponse.getHearingRequestID()).isEqualTo("123");
    }

    @Test
    void shouldReturnAutomateHearingTestF401() {

        HearingResponse response = HearingResponse.builder()
            .hearingRequestID("123")
            .status("200")
            .build();

        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn(SERVICE_AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(AUTHORIZATION_TOKEN);
        when(hearingApiClient.createHearingDetails(any(), any(), any()))
            .thenReturn(response);

        getHearingData(HearingData.builder()
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
                           .hearingEstimatedMinutes("38"), "Yes");

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        hearingDataList.add(element(hearingData));

        caseData = CaseData
            .caseDataBuilder()
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("123")
            .applicantsFL401(partyDetails)
            .caseManagementLocation(CaseManagementLocation.builder().baseLocation("test").build())
            .attendHearing(AttendHearing.builder().isWelshNeeded(NO).build())
            .allPartyFlags(AllPartyFlags.builder().build())
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .hearingData(hearingData)
            .issueDate(LocalDate.now())
            .manageOrders(manageOrders)
            .build();

        HearingResponse hearingsResponse =
            hearingsService.createAutomatedHearings(caseData);
        assertThat(hearingsResponse.getStatus()).isEqualTo("200");
        assertThat(hearingsResponse.getHearingRequestID()).isEqualTo("123");
    }


    @Test
    void shouldReturnAutomateHearingTestF401ForParties() {

        HearingResponse response = HearingResponse.builder()
            .hearingRequestID("123")
            .status("200")
            .build();

        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn(SERVICE_AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(AUTHORIZATION_TOKEN);
        when(hearingApiClient.createHearingDetails(any(), any(), any()))
            .thenReturn(response);

        getHearingData(HearingData.builder()
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
                           .instructionsForRemoteHearing("Test")
                           .hearingEstimatedMinutes("40"), "No");

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        hearingDataList.add(element(hearingData));

        caseData = CaseData
            .caseDataBuilder()
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("123")
            .applicantsFL401(partyDetails)
            .caseManagementLocation(CaseManagementLocation.builder().baseLocation("test").build())
            .attendHearing(AttendHearing.builder().isWelshNeeded(NO).build())
            .allPartyFlags(AllPartyFlags.builder().build())
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .hearingData(hearingData)
            .issueDate(LocalDate.now())
            .manageOrders(manageOrders)
            .build();

        HearingResponse hearingsResponse =
            hearingsService.createAutomatedHearings(caseData);
        assertThat(hearingsResponse.getStatus()).isEqualTo("200");
        assertThat(hearingsResponse.getHearingRequestID()).isEqualTo("123");
    }


    public static <T> Element<T> element(T element) {
        return Element.<T>builder()
            .id(UUID.randomUUID())
            .value(element)
            .build();
    }


    @Test
    void shouldReturnAutomateHearingsByExceptionTest() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(idamTokenGenerator.generateIdamTokenForHearingCftData()).thenReturn(AUTHORIZATION_TOKEN);

        getHearingData(HearingData.builder()
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
                           .customDetails("Test")
                           .instructionsForRemoteHearing("Test")
                           .hearingEstimatedHours("02"), "Yes");

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        hearingDataList.add(element(hearingData));

        caseData = CaseData
            .caseDataBuilder()
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("123")
            .applicantsFL401(partyDetails)
            .caseManagementLocation(CaseManagementLocation.builder().baseLocation("test").build())
            .attendHearing(AttendHearing.builder().isWelshNeeded(NO).build())
            .allPartyFlags(AllPartyFlags.builder().build())
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .hearingData(hearingData)
            .issueDate(LocalDate.now())
            .manageOrders(manageOrders)
            .build();

        assertThrows(NullPointerException.class, () -> hearingsService.createAutomatedHearings(caseData));
    }

}
