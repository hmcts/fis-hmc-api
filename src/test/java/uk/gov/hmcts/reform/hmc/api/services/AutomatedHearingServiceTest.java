package uk.gov.hmcts.reform.hmc.api.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.api.model.ccd.AttendHearing;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseData;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseManagementLocation;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingChannelsEnum;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingData;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingDataApplicantDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingDataRespondentDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingPriorityTypeEnum;
import uk.gov.hmcts.reform.hmc.api.model.ccd.PartyDetails;
import uk.gov.hmcts.reform.hmc.api.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.hmc.api.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingCaseCategories;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingCaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;
import uk.gov.hmcts.reform.hmc.api.model.request.PanelRequirements;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingLocation;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.hmc.api.services.AutomateHearingServiceTest.element;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.C100;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.COURT;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.FL401;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.NO;

@ExtendWith(MockitoExtension.class)
class AutomatedHearingServiceTest {

    private static final List<AutomatedHearingCaseCategories> CASE_CATEGORIES = List.of(
        new AutomatedHearingCaseCategories("caseType", "ABA5-PRL", null),
        new AutomatedHearingCaseCategories("caseSubType", "ABA5-PRL", "ABA5-PRL")
    );

    private static final DynamicList INTER = DynamicList.builder()
        .value(DynamicListElement.builder()
                   .code("INTER")
                   .label("Inter")
                   .build())
        .build();

    @Spy
    private CaseFlagV2DataServiceImpl caseFlagV2DataService;

    @InjectMocks
    private AutomatedHearingService automatedHearingService;

    @Test
    void shouldMapC100CaseCorrectly() {
        CaseData caseData = getC100BaseCaseData();

        AutomatedHearingRequest result = automatedHearingService.mapCaseDataToAutoHearingRequest(caseData, "http://ccd/");

        AutomatedHearingCaseDetails expectedCaseDetails = AutomatedHearingCaseDetails.automatedHearingCaseDetailsWith()
            .hmctsServiceCode("ABA5")
            .caseRef("12345")
            .caseDeepLink("http://ccd/12345#Case File View")
            .hmctsInternalCaseName("Applicant Name")
            .publicCaseName("Re-Minor")
            .externalCaseReference("")
            .caseAdditionalSecurityFlag(false)
            .caseInterpreterRequiredFlag(false)
            .caseCategories(CASE_CATEGORIES)
            .caseManagementLocationCode("123456")
            .caseRestrictedFlag(false)
            .caseSlaStartDate("2026-03-20")
            .build();

        assertThat(result).isNotNull();
        assertThat(result.getCaseDetails()).isEqualTo(expectedCaseDetails);

        AutomatedHearingDetails expectedHearingDetails = AutomatedHearingDetails.automatedHearingDetailsWith()
            .autoListFlag(true)
            .numberOfPhysicalAttendees(4) // applicant + respondent + Cafcass + Local Authority
            .hearingInWelshFlag(false)
            .hearingType("HT")
            .duration(150)
            .hearingPriorityType("Standard")
            .hearingRequester("Judge123")
            .leadJudgeContractType("")
            .privateHearingRequiredFlag(true)
            .hearingChannels(List.of("INTER"))
            .hearingLocations(List.of(new HearingLocation(COURT, "COURT123")))
            .panelRequirements(new PanelRequirements())
            .facilitiesRequired(Collections.emptyList())
            .hearingIsLinkedFlag(false)
            .build();

        assertThat(result.getHearingDetails()).isEqualTo(expectedHearingDetails);

        assertThat(result.getPartyDetails()).isNotNull();
        assertThat(result.getPartyDetails()).hasSize(2);
    }

    @Test
    void shouldMapFL401CaseCorrectly() {
        CaseData caseData = getFL401BaseCaseData();

        AutomatedHearingRequest result = automatedHearingService.mapCaseDataToAutoHearingRequest(caseData, "http://ccd/");

        AutomatedHearingCaseDetails expectedCaseDetails = AutomatedHearingCaseDetails.automatedHearingCaseDetailsWith()
            .hmctsServiceCode("ABA5")
            .caseRef("12345")
            .caseDeepLink("http://ccd/12345#Case File View")
            .hmctsInternalCaseName("Applicant Name")
            .publicCaseName("Smith and Jones")
            .externalCaseReference("")
            .caseAdditionalSecurityFlag(false)
            .caseInterpreterRequiredFlag(false)
            .caseCategories(CASE_CATEGORIES)
            .caseManagementLocationCode("123456")
            .caseRestrictedFlag(false)
            .caseSlaStartDate("2026-03-20")
            .build();

        assertThat(result).isNotNull();
        assertThat(result.getCaseDetails()).isEqualTo(expectedCaseDetails);

        AutomatedHearingDetails expectedHearingDetails = AutomatedHearingDetails.automatedHearingDetailsWith()
            .autoListFlag(true)
            .numberOfPhysicalAttendees(3) // applicant + respondent + Local Authority
            .hearingInWelshFlag(false)
            .hearingType("HT")
            .duration(150)
            .hearingPriorityType("Standard")
            .hearingRequester("Judge123")
            .leadJudgeContractType("")
            .privateHearingRequiredFlag(false) // false for FL401s ???
            .hearingChannels(List.of("INTER"))
            .hearingLocations(List.of(new HearingLocation(COURT, "COURT123")))
            .panelRequirements(new PanelRequirements())
            .facilitiesRequired(Collections.emptyList())
            .hearingIsLinkedFlag(false)
            .build();

        assertThat(result.getHearingDetails()).isEqualTo(expectedHearingDetails);

        assertThat(result.getPartyDetails()).isNotNull();
        assertThat(result.getPartyDetails()).hasSize(2);
    }

    @Test
    void shouldHavePartyFlagsMappedCorrectly() {

    }

    @Test
    void shouldMapSolicitorDetailsAndFlagsCorrectly() {

    }

    @Test
    void shouldHaveCorrectPreferredHearingChannels() {

    }

    private CaseData getC100BaseCaseData() {
        DynamicList hearingTypes = DynamicList.builder()
            .value(DynamicListElement.builder()
                       .code("HT")
                       .label("HearingType")
                       .build())
            .build();

        DynamicList courtList = DynamicList.builder()
            .value(DynamicListElement.builder()
                       .code("COURT123:SomeCourt")
                       .label("SomeCourt")
                       .build())
            .build();

        HearingData hearingData = HearingData.builder()
            .hearingTypes(hearingTypes)
            .hearingChannelsEnum(HearingChannelsEnum.INTER)
            .allPartiesAttendHearingSameWayYesOrNo("YES")
            .hearingPriorityTypeEnum(HearingPriorityTypeEnum.StandardPriority)
            .courtList(courtList)
            .hearingEstimatedHours("2")
            .hearingEstimatedMinutes("30")
            .hearingJudgePersonalCode("Judge123")
            .hearingDataApplicantDetails(HearingDataApplicantDetails.hearingDataApplicantDetails()
                                             .applicantName1("John Smith")
                                             .applicantHearingChannel1(INTER)
                                             .build())
            .hearingDataRespondentDetails(HearingDataRespondentDetails.hearingDataRespondentDetails()
                                              .respondentName1("Jane Jones")
                                              .respondentHearingChannel1(INTER)
                                              .build())
            .hearingListedLinkedCases(DynamicList.builder()
                                          .value(null) // hearing is not linked
                                          .build())
            .build();

        return CaseData.caseDataBuilder()
            .id(12345L)
            .caseTypeOfApplication(C100)
            .applicantCaseName("Applicant Name")
            .issueDate(LocalDate.of(2026, 3, 20))
            .caseManagementLocation(new CaseManagementLocation("1", "123456"))
            .applicants(List.of(element(PartyDetails.builder()
                                            .partyId(UUID.randomUUID())
                                            .lastName("Smith")
                                            .build())))
            .respondents(List.of(element(PartyDetails.builder()
                                            .partyId(UUID.randomUUID())
                                            .lastName("Jones")
                                     .build())))
            .hearingData(hearingData)
            .attendHearing(new AttendHearing(NO, NO))
            .build();
    }

    private CaseData getFL401BaseCaseData() {
        DynamicList hearingTypes = DynamicList.builder()
            .value(DynamicListElement.builder()
                       .code("HT")
                       .label("HearingType")
                       .build())
            .build();

        DynamicList courtList = DynamicList.builder()
            .value(DynamicListElement.builder()
                       .code("COURT123:SomeCourt")
                       .label("SomeCourt")
                       .build())
            .build();

        HearingData hearingData = HearingData.builder()
            .hearingTypes(hearingTypes)
            .hearingChannelsEnum(HearingChannelsEnum.INTER)
            .allPartiesAttendHearingSameWayYesOrNo("YES")
            .hearingPriorityTypeEnum(HearingPriorityTypeEnum.StandardPriority)
            .courtList(courtList)
            .hearingEstimatedHours("2")
            .hearingEstimatedMinutes("30")
            .hearingJudgePersonalCode("Judge123")
            .applicantName("John Smith")
            .respondentName("Jane Jones")
            .hearingListedLinkedCases(DynamicList.builder()
                                          .value(null) // hearing is not linked
                                          .build())
            .build();

        return CaseData.caseDataBuilder()
            .id(12345L)
            .caseTypeOfApplication(FL401)
            .applicantCaseName("Applicant Name")
            .issueDate(LocalDate.of(2026, 3, 20))
            .caseManagementLocation(new CaseManagementLocation("1", "123456"))
            .applicantsFL401(PartyDetails.builder()
                                 .partyId(UUID.randomUUID())
                                 .lastName("Smith")
                                 .build())
            .respondentsFL401(PartyDetails.builder()
                                  .partyId(UUID.randomUUID())
                                  .lastName("Jones")
                                  .build())
            .hearingData(hearingData)
            .attendHearing(new AttendHearing(NO, NO))
            .build();
    }

}
