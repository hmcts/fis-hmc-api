package uk.gov.hmcts.reform.hmc.api.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseManagementLocation;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Element;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Flags;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Organisation;
import uk.gov.hmcts.reform.hmc.api.model.ccd.PartyDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.flagdata.FlagDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyFlagsModel;
import uk.gov.hmcts.reform.hmc.api.model.response.ServiceHearingValues;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ABA5;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.APPLICANTS;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.APPLICANTS_FL401;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.APPLICANT_CASE_NAME;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.APPLICANT_CASE_NAME_TEST_VALUE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_LINKS;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_MNGEMNT_LOC;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ORGANISATION_TEST_ID;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ORGANISATION_TEST_NAME;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.PF0002;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.PF0020;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.PF0021;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.PRIVATE_LAW;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.RA0013;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.REASON;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.REASON_FOR_LINK;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.REASON_TEST_VALUE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.REP_FIRST_NAME_TEST_VALUE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.REP_LAST_NAME_TEST_VALUE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.RESPONDENTS;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.RESPONDENTS_FL401;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.SM0002;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.TEST;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.VALUE;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class CaseFlagV2DataServiceImplTest {

    @InjectMocks
    private CaseFlagV2DataServiceImpl caseFlagV2DataService;

    @Test
    @SuppressWarnings("unchecked")
    void shouldSetCaseFlagTest() throws IOException {

        Map<String, Object> reasonMap = new LinkedHashMap<>();
        reasonMap.put(REASON, REASON_TEST_VALUE);

        Map<String, Object> reasonForLinkMap = new LinkedHashMap<>();
        reasonForLinkMap.put(VALUE, reasonMap);

        List<Map<String, Object>> reasonForLinkList = new ArrayList<>();
        reasonForLinkList.add(reasonForLinkMap);

        Map<String, Object> valueMap = new LinkedHashMap<>();
        valueMap.put(REASON_FOR_LINK, reasonForLinkList);

        Map<String, Object> caseLinkMap = new LinkedHashMap<>();
        caseLinkMap.put(VALUE, valueMap);

        List<Map<String, Object>> caseLinksList = new ArrayList<>();
        caseLinksList.add(caseLinkMap);

        FlagDetail flagDetail = FlagDetail.builder().hearingRelevant(TEST).flagCode(PF0002).build();
        Element<FlagDetail> flagDetailElement =
            Element.<FlagDetail>builder().id(UUID.randomUUID()).value(flagDetail).build();

        List<Element<FlagDetail>> flagDetails = new ArrayList<>();
        flagDetails.add(flagDetailElement);

        Flags flags = Flags.builder().partyName(TEST).roleOnCase(TEST).details(flagDetails).build();

        Organisation organisation =
            Organisation.builder().organisationID(null).organisationName(null).build();

        PartyDetails partyDetails =
            PartyDetails.builder()
                .firstName(TEST)
                .solicitorOrg(organisation)
                .representativeFirstName(TEST)
                .representativeLastName(TEST)
                .solicitorTelephone("07654445674")
                .partyLevelFlag(flags)
                .build();
        Element<PartyDetails> partyDetailsElement =
            Element.<PartyDetails>builder().id(UUID.randomUUID()).value(partyDetails).build();

        List<Element<PartyDetails>> applicants = new ArrayList<>();
        applicants.add(partyDetailsElement);

        List<Element<PartyDetails>> respondents = new ArrayList<>();
        respondents.add(partyDetailsElement);

        CaseManagementLocation caseManagementLocation =
            CaseManagementLocation.builder().region(TEST).baseLocation(TEST).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(APPLICANT_CASE_NAME, APPLICANT_CASE_NAME_TEST_VALUE);
        caseDataMap.put(CASE_LINKS, caseLinksList);
        caseDataMap.put(CASE_MNGEMNT_LOC, caseManagementLocation);
        caseDataMap.put("caseTypeOfApplication", "C100");
        caseDataMap.put(APPLICANTS, applicants);
        caseDataMap.put(RESPONDENTS, respondents);
        caseDataMap.put(APPLICANTS_FL401, partyDetails);
        caseDataMap.put(RESPONDENTS_FL401, partyDetails);
        ServiceHearingValues serviceHearingValues =
            ServiceHearingValues.hearingsDataWith().hmctsServiceID(ABA5).build();
        CaseDetails caseDetails =
            CaseDetails.builder().id(123L).caseTypeId(PRIVATE_LAW).data(caseDataMap).build();
        caseFlagV2DataService.setCaseFlagsV2Data(serviceHearingValues, caseDetails);
        Assertions.assertEquals(ABA5, serviceHearingValues.getHmctsServiceID());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldAddPartyDetailsModelForOrgTest() throws IOException {

        Map<String, Object> reasonMap = new LinkedHashMap<>();
        reasonMap.put(REASON, REASON_TEST_VALUE);

        Map<String, Object> reasonForLinkMap = new LinkedHashMap<>();
        reasonForLinkMap.put(VALUE, reasonMap);

        List<Map<String, Object>> reasonForLinkList = new ArrayList<>();
        reasonForLinkList.add(reasonForLinkMap);

        Map<String, Object> valueMap = new LinkedHashMap<>();
        valueMap.put(REASON_FOR_LINK, reasonForLinkList);

        Map<String, Object> caseLinkMap = new LinkedHashMap<>();
        caseLinkMap.put(VALUE, valueMap);

        List<Map<String, Object>> caseLinksList = new ArrayList<>();
        caseLinksList.add(caseLinkMap);

        FlagDetail flagDetail = FlagDetail.builder().hearingRelevant(TEST).flagCode(PF0002).build();
        Element<FlagDetail> flagDetailElement =
            Element.<FlagDetail>builder().id(UUID.randomUUID()).value(flagDetail).build();

        List<Element<FlagDetail>> flagDetails = new ArrayList<>();
        flagDetails.add(flagDetailElement);

        Flags flags = Flags.builder().partyName(TEST).roleOnCase(TEST).details(flagDetails).build();

        Organisation organisation =
            Organisation.builder()
                .organisationID(ORGANISATION_TEST_ID)
                .organisationName(ORGANISATION_TEST_NAME)
                .build();

        PartyDetails partyDetails =
            PartyDetails.builder()
                .firstName(TEST)
                .phoneNumber("98765454334")
                .solicitorOrg(organisation)
                .partyLevelFlag(flags)
                .build();
        Element<PartyDetails> partyDetailsElement =
            Element.<PartyDetails>builder().id(UUID.randomUUID()).value(partyDetails).build();

        List<Element<PartyDetails>> applicants = new ArrayList<>();
        applicants.add(partyDetailsElement);

        List<Element<PartyDetails>> respondents = new ArrayList<>();
        respondents.add(partyDetailsElement);

        CaseManagementLocation caseManagementLocation =
            CaseManagementLocation.builder().region(TEST).baseLocation(TEST).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(APPLICANT_CASE_NAME, APPLICANT_CASE_NAME_TEST_VALUE);
        caseDataMap.put(CASE_LINKS, caseLinksList);
        caseDataMap.put(CASE_MNGEMNT_LOC, caseManagementLocation);
        caseDataMap.put(APPLICANTS, applicants);
        caseDataMap.put(RESPONDENTS, respondents);
        caseDataMap.put("caseTypeOfApplication", "C100");
        caseDataMap.put(APPLICANTS_FL401, partyDetails);
        caseDataMap.put(RESPONDENTS_FL401, partyDetails);
        ServiceHearingValues serviceHearingValues =
            ServiceHearingValues.hearingsDataWith().hmctsServiceID(ABA5).build();
        CaseDetails caseDetails =
            CaseDetails.builder().id(123L).caseTypeId(PRIVATE_LAW).data(caseDataMap).build();
        caseFlagV2DataService.setCaseFlagsV2Data(serviceHearingValues, caseDetails);
        Assertions.assertEquals(ABA5, serviceHearingValues.getHmctsServiceID());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldAddPartyDetailsModelForSolicitorTest() throws IOException {

        Map<String, Object> reasonMap = new LinkedHashMap<>();
        reasonMap.put(REASON, REASON_TEST_VALUE);

        Map<String, Object> reasonForLinkMap = new LinkedHashMap<>();
        reasonForLinkMap.put(VALUE, reasonMap);

        List<Map<String, Object>> reasonForLinkList = new ArrayList<>();
        reasonForLinkList.add(reasonForLinkMap);

        Map<String, Object> valueMap = new LinkedHashMap<>();
        valueMap.put(REASON_FOR_LINK, reasonForLinkList);

        Map<String, Object> caseLinkMap = new LinkedHashMap<>();
        caseLinkMap.put(VALUE, valueMap);

        List<Map<String, Object>> caseLinksList = new ArrayList<>();
        caseLinksList.add(caseLinkMap);

        FlagDetail flagDetail = FlagDetail.builder().hearingRelevant(TEST).flagCode(PF0002).build();
        Element<FlagDetail> flagDetailElement =
            Element.<FlagDetail>builder().id(UUID.randomUUID()).value(flagDetail).build();

        List<Element<FlagDetail>> flagDetails = new ArrayList<>();
        flagDetails.add(flagDetailElement);

        Flags flags = Flags.builder().partyName(TEST).roleOnCase(TEST).details(flagDetails).build();

        Organisation organisation =
            Organisation.builder().organisationID(null).organisationName(null).build();

        PartyDetails partyDetails =
            PartyDetails.builder()
                .firstName(TEST)
                .solicitorOrg(organisation)
                .partyLevelFlag(flags)
                .representativeFirstName(REP_FIRST_NAME_TEST_VALUE)
                .representativeLastName(REP_LAST_NAME_TEST_VALUE)
                .build();
        Element<PartyDetails> partyDetailsElement =
            Element.<PartyDetails>builder().id(UUID.randomUUID()).value(partyDetails).build();

        List<Element<PartyDetails>> applicants = new ArrayList<>();
        applicants.add(partyDetailsElement);

        List<Element<PartyDetails>> respondents = new ArrayList<>();
        respondents.add(partyDetailsElement);

        CaseManagementLocation caseManagementLocation =
            CaseManagementLocation.builder().region(TEST).baseLocation(TEST).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(APPLICANT_CASE_NAME, APPLICANT_CASE_NAME_TEST_VALUE);
        caseDataMap.put(CASE_LINKS, caseLinksList);
        caseDataMap.put(CASE_MNGEMNT_LOC, caseManagementLocation);
        caseDataMap.put(APPLICANTS, applicants);
        caseDataMap.put(RESPONDENTS, respondents);
        caseDataMap.put(APPLICANTS_FL401, partyDetails);
        caseDataMap.put(RESPONDENTS_FL401, partyDetails);
        ServiceHearingValues serviceHearingValues =
            ServiceHearingValues.hearingsDataWith().hmctsServiceID(ABA5).build();
        CaseDetails caseDetails =
            CaseDetails.builder().id(123L).caseTypeId(PRIVATE_LAW).data(caseDataMap).build();
        caseFlagV2DataService.setCaseFlagsV2Data(serviceHearingValues, caseDetails);
        Assertions.assertEquals(ABA5, serviceHearingValues.getHmctsServiceID());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldAddPartyDetailsModelWithVulnerabilityTest() throws IOException {

        Map<String, Object> reasonMap = new LinkedHashMap<>();
        reasonMap.put(REASON, REASON_TEST_VALUE);

        Map<String, Object> reasonForLinkMap = new LinkedHashMap<>();
        reasonForLinkMap.put(VALUE, reasonMap);

        List<Map<String, Object>> reasonForLinkList = new ArrayList<>();
        reasonForLinkList.add(reasonForLinkMap);

        Map<String, Object> valueMap = new LinkedHashMap<>();
        valueMap.put(REASON_FOR_LINK, reasonForLinkList);

        Map<String, Object> caseLinkMap = new LinkedHashMap<>();
        caseLinkMap.put(VALUE, valueMap);

        List<Map<String, Object>> caseLinksList = new ArrayList<>();
        caseLinksList.add(caseLinkMap);

        FlagDetail flagDetail = FlagDetail.builder().hearingRelevant(TEST).flagCode(PF0020).build();
        Element<FlagDetail> flagDetailElement =
            Element.<FlagDetail>builder().id(UUID.randomUUID()).value(flagDetail).build();

        List<Element<FlagDetail>> flagDetails = new ArrayList<>();
        flagDetails.add(flagDetailElement);

        Flags flags = Flags.builder().partyName(TEST).roleOnCase(TEST).details(flagDetails).build();

        Organisation organisation =
            Organisation.builder()
                .organisationID(ORGANISATION_TEST_ID)
                .organisationName(ORGANISATION_TEST_NAME)
                .build();

        PartyDetails partyDetails =
            PartyDetails.builder()
                .firstName(TEST)
                .solicitorOrg(organisation)
                .partyLevelFlag(flags)
                .build();
        Element<PartyDetails> partyDetailsElement =
            Element.<PartyDetails>builder().id(UUID.randomUUID()).value(partyDetails).build();

        List<Element<PartyDetails>> applicants = new ArrayList<>();
        applicants.add(partyDetailsElement);

        List<Element<PartyDetails>> respondents = new ArrayList<>();
        respondents.add(partyDetailsElement);

        CaseManagementLocation caseManagementLocation =
            CaseManagementLocation.builder().region(TEST).baseLocation(TEST).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(APPLICANT_CASE_NAME, APPLICANT_CASE_NAME_TEST_VALUE);
        caseDataMap.put(CASE_LINKS, caseLinksList);
        caseDataMap.put(CASE_MNGEMNT_LOC, caseManagementLocation);
        caseDataMap.put(APPLICANTS, applicants);
        caseDataMap.put(RESPONDENTS, respondents);
        caseDataMap.put(APPLICANTS_FL401, partyDetails);
        caseDataMap.put(RESPONDENTS_FL401, partyDetails);
        ServiceHearingValues serviceHearingValues =
            ServiceHearingValues.hearingsDataWith().hmctsServiceID(ABA5).build();
        CaseDetails caseDetails =
            CaseDetails.builder().id(123L).caseTypeId(PRIVATE_LAW).data(caseDataMap).build();
        caseFlagV2DataService.setCaseFlagsV2Data(serviceHearingValues, caseDetails);
        Assertions.assertEquals(ABA5, serviceHearingValues.getHmctsServiceID());
    }

    private Element<PartyDetails> samplePartyElement() {
        PartyDetails pd = PartyDetails.builder()
            .firstName(TEST)
            .lastName(TEST)
            .email("x@y.com")
            .phoneNumber("07123456789")
            .build();
        return Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(pd)
            .build();
    }

    private Flags flagsWith(String partyName, String code, String status) {
        FlagDetail fd = FlagDetail.builder()
            .flagCode(code)
            .status(status)
            .flagComment("comment")
            .build();
        Element<FlagDetail> fde = Element.<FlagDetail>builder()
            .id(UUID.randomUUID())
            .value(fd).build();
        return Flags.builder()
            .partyName(partyName)
            .details(Collections.singletonList(fde))
            .build();
    }

    private Map<String, Object> baseCaseDataC100(List<Element<PartyDetails>> applicants,
                                                 List<Element<PartyDetails>> respondents) {
        CaseManagementLocation cml = CaseManagementLocation.builder()
            .region(TEST).baseLocation(TEST).build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(APPLICANT_CASE_NAME, APPLICANT_CASE_NAME_TEST_VALUE);
        caseDataMap.put(CASE_MNGEMNT_LOC, cml);
        caseDataMap.put("caseTypeOfApplication", "C100");
        caseDataMap.put(APPLICANTS, applicants);
        caseDataMap.put(RESPONDENTS, respondents);
        // FL401 singletons present but unused in C100 path
        caseDataMap.put(APPLICANTS_FL401, applicants.get(0).getValue());
        caseDataMap.put(RESPONDENTS_FL401, respondents.get(0).getValue());
        return caseDataMap;
    }

    @Test
    void mapsActiveFlagsOnly() throws IOException {
        // parties
        Element<PartyDetails> applicant1 = samplePartyElement();
        Element<PartyDetails> respondent1 = samplePartyElement();
        List<Element<PartyDetails>> applicants = Collections.singletonList(applicant1);
        List<Element<PartyDetails>> respondents = Collections.singletonList(respondent1);

        Map<String, Object> caseData = baseCaseDataC100(applicants, respondents);

        // C100 external/internal keys the service will read:
        // Applicant #1 external: caApplicant1ExternalFlags
        // Respondent #1 internal: caRespondent1InternalFlags
        Flags applicantActive = flagsWith("Applicant One", PF0002, "Active");
        Flags respondentRequested = flagsWith("Respondent One", PF0020, "Requested"); // should be filtered OUT

        caseData.put("caApplicant1ExternalFlags", applicantActive);
        caseData.put("caRespondent1InternalFlags", respondentRequested);

        ServiceHearingValues shv = ServiceHearingValues.hearingsDataWith().hmctsServiceID(ABA5).build();
        CaseDetails cd = CaseDetails.builder().id(123L).caseTypeId(PRIVATE_LAW).data(caseData).build();

        caseFlagV2DataService.setCaseFlagsV2Data(shv, cd);

        // Assert only the ACTIVE one made it through
        Assertions.assertNotNull(shv.getCaseFlags());
        List<PartyFlagsModel> flags = shv.getCaseFlags().getFlags();
        Assertions.assertEquals(1, flags.size(), "Only ACTIVE flags should be included");
        Assertions.assertEquals(PF0002, flags.get(0).getFlagId());
        Assertions.assertEquals("Active", flags.get(0).getFlagStatus());
    }

    @Test
    void noFlagsWhenNoneActive() throws IOException {
        Element<PartyDetails> applicant1 = samplePartyElement();
        List<Element<PartyDetails>> applicants = Collections.singletonList(applicant1);
        List<Element<PartyDetails>> respondents = Collections.singletonList(samplePartyElement());

        Map<String, Object> caseData = baseCaseDataC100(applicants, respondents);
        // Only a Requested flag present
        caseData.put("caApplicant1InternalFlags", flagsWith("Applicant One", PF0002, "Requested"));

        ServiceHearingValues shv = ServiceHearingValues.hearingsDataWith().hmctsServiceID(ABA5).build();
        CaseDetails cd = CaseDetails.builder().id(456L).caseTypeId(PRIVATE_LAW).data(caseData).build();

        caseFlagV2DataService.setCaseFlagsV2Data(shv, cd);

        Assertions.assertNotNull(shv.getCaseFlags());
        Assertions.assertTrue(shv.getCaseFlags().getFlags().isEmpty(), "Requested flags must be filtered out");
    }

    @Test
    void pvpFlagMarksAdditionalSecurityTrue() throws IOException {
        // parties
        Element<PartyDetails> applicant1 = samplePartyElement();
        List<Element<PartyDetails>> applicants = Collections.singletonList(applicant1);
        List<Element<PartyDetails>> respondents = Collections.singletonList(samplePartyElement());

        Map<String, Object> caseData = baseCaseDataC100(applicants, respondents);

        // PF0021 (Potentially Violent Person) as ACTIVE on Applicant #1 internal flags
        Flags pvpActive = flagsWith("Applicant One", PF0021, "Active");
        caseData.put("caApplicant1InternalFlags", pvpActive);

        ServiceHearingValues shv = ServiceHearingValues.hearingsDataWith().hmctsServiceID(ABA5).build();
        CaseDetails cd = CaseDetails.builder().id(789L).caseTypeId(PRIVATE_LAW).data(caseData).build();

        caseFlagV2DataService.setCaseFlagsV2Data(shv, cd);

        // Assert: security flag is set and PF0021 is present in outgoing flags
        boolean security = java.util.Optional
            .ofNullable(shv.getCaseAdditionalSecurityFlag())
            .orElse(false);

        Assertions.assertTrue(security, "PF0021 Active should set caseAdditionalSecurityFlag");

        Assertions.assertNotNull(shv.getCaseFlags());
        Assertions.assertTrue(
            shv.getCaseFlags().getFlags().stream()
                .anyMatch(f -> PF0021.equals(f.getFlagId()) && "Active".equalsIgnoreCase(f.getFlagStatus())),
            "PF0021 Active should be included in case flags"
        );
    }


    @Test
    void pvpRequestedDoesNotMarkAdditionalSecurity() throws IOException {
        Element<PartyDetails> applicant1 = samplePartyElement();
        List<Element<PartyDetails>> applicants = Collections.singletonList(applicant1);
        List<Element<PartyDetails>> respondents = Collections.singletonList(samplePartyElement());

        Map<String, Object> caseData = baseCaseDataC100(applicants, respondents);

        // PF0021 as Requested (not Active) — should NOT set additional security
        Flags pvpRequested = flagsWith("Applicant One", PF0021, "Requested");
        caseData.put("caApplicant1ExternalFlags", pvpRequested);

        ServiceHearingValues shv = ServiceHearingValues.hearingsDataWith().hmctsServiceID(ABA5).build();
        CaseDetails cd = CaseDetails.builder().id(790L).caseTypeId(PRIVATE_LAW).data(caseData).build();

        caseFlagV2DataService.setCaseFlagsV2Data(shv, cd);

        boolean security = java.util.Optional
            .ofNullable(shv.getCaseAdditionalSecurityFlag())
            .orElse(false);

        Assertions.assertFalse(security, "PF0021 Requested should NOT set caseAdditionalSecurityFlag");  // depending on your ACTIVE-only filtering, PF0021 Requested may also be absent from flags:
        if (shv.getCaseFlags() != null) {
            Assertions.assertTrue(
                shv.getCaseFlags().getFlags().stream().noneMatch(f -> PF0021.equals(f.getFlagId())),
                "PF0021 Requested should not be included in outgoing flags when filtering to Active");
        }
    }

    @Test
    void raAndSmActiveFlowIntoCaseFlagsAndPartyDerivedList() throws IOException {
        // parties
        Element<PartyDetails> applicant1 = samplePartyElement();
        List<Element<PartyDetails>> applicants = Collections.singletonList(applicant1);
        List<Element<PartyDetails>> respondents = Collections.singletonList(samplePartyElement());

        Map<String, Object> caseData = baseCaseDataC100(applicants, respondents);

        // Put RA0013 and SM0002 as ACTIVE for Applicant #1 (mix internal/external just to show both read)
        Flags raActive = flagsWith("Applicant One", RA0013, "Active");
        Flags smActive = flagsWith("Applicant One", SM0002, "Active");
        caseData.put("caApplicant1InternalFlags", raActive);
        caseData.put("caApplicant1ExternalFlags", smActive);

        ServiceHearingValues shv = ServiceHearingValues.hearingsDataWith().hmctsServiceID(ABA5).build();
        CaseDetails cd = CaseDetails.builder().id(999L).caseTypeId(PRIVATE_LAW).data(caseData).build();

        caseFlagV2DataService.setCaseFlagsV2Data(shv, cd);

        // 1) Top-level flags contain both codes
        Assertions.assertNotNull(shv.getCaseFlags());
        List<PartyFlagsModel> out = shv.getCaseFlags().getFlags();
        Assertions.assertTrue(out.stream().anyMatch(f -> RA0013.equals(f.getFlagId()) && "Active".equalsIgnoreCase(f.getFlagStatus())),
                              "RA0013 Active should be present in caseFlags.flags");
        Assertions.assertTrue(out.stream().anyMatch(f -> SM0002.equals(f.getFlagId()) && "Active".equalsIgnoreCase(f.getFlagStatus())),
                              "SM0002 Active should be present in caseFlags.flags");

        // 2) Derived party reasonableAdjustments contains both codes
        Assertions.assertNotNull(shv.getParties());
        // Find the applicant party by partyID match
        String applicantId = applicant1.getId().toString();
        List<String> raList = shv.getParties().stream()
            .filter(p -> applicantId.equals(p.getPartyID()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Applicant party not found"))
            .getIndividualDetails()
            .getReasonableAdjustments();

        Assertions.assertTrue(raList.contains(RA0013), "reasonableAdjustments should contain RA0013");
        Assertions.assertTrue(raList.contains(SM0002), "reasonableAdjustments should contain SM0002");
    }

}
