package uk.gov.hmcts.reform.hmc.api.services;

import org.json.simple.parser.ParseException;
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
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.PRIVATE_LAW;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.REASON;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.REASON_FOR_LINK;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.REASON_TEST_VALUE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.REP_FIRST_NAME_TEST_VALUE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.REP_LAST_NAME_TEST_VALUE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.RESPONDENTS;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.RESPONDENTS_FL401;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.TEST;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.VALUE;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class CaseFlagV2DataServiceImplTest {

    @InjectMocks
    private CaseFlagV2DataServiceImpl caseFlagV2DataService;

    @Test
    @SuppressWarnings("unchecked")
    void shouldSetCaseFlagTest() throws IOException, ParseException {

        LinkedHashMap reasonMap = new LinkedHashMap();
        reasonMap.put(REASON, REASON_TEST_VALUE);

        LinkedHashMap reasonForLinkMap = new LinkedHashMap();
        reasonForLinkMap.put(VALUE, reasonMap);

        List reasonForLinkList = new ArrayList();
        reasonForLinkList.add(reasonForLinkMap);

        LinkedHashMap valueMap = new LinkedHashMap();
        valueMap.put(REASON_FOR_LINK, reasonForLinkList);

        LinkedHashMap caseLinkMap = new LinkedHashMap();
        caseLinkMap.put(VALUE, valueMap);

        List caseLinksList = new ArrayList();
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
    void shouldAddPartyDetailsModelForOrgTest() throws IOException, ParseException {

        LinkedHashMap reasonMap = new LinkedHashMap();
        reasonMap.put(REASON, REASON_TEST_VALUE);

        LinkedHashMap reasonForLinkMap = new LinkedHashMap();
        reasonForLinkMap.put(VALUE, reasonMap);

        List reasonForLinkList = new ArrayList();
        reasonForLinkList.add(reasonForLinkMap);

        LinkedHashMap valueMap = new LinkedHashMap();
        valueMap.put(REASON_FOR_LINK, reasonForLinkList);

        LinkedHashMap caseLinkMap = new LinkedHashMap();
        caseLinkMap.put(VALUE, valueMap);

        List caseLinksList = new ArrayList();
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
    void shouldAddPartyDetailsModelForSolicitorTest() throws IOException, ParseException {

        LinkedHashMap reasonMap = new LinkedHashMap();
        reasonMap.put(REASON, REASON_TEST_VALUE);

        LinkedHashMap reasonForLinkMap = new LinkedHashMap();
        reasonForLinkMap.put(VALUE, reasonMap);

        List reasonForLinkList = new ArrayList();
        reasonForLinkList.add(reasonForLinkMap);

        LinkedHashMap valueMap = new LinkedHashMap();
        valueMap.put(REASON_FOR_LINK, reasonForLinkList);

        LinkedHashMap caseLinkMap = new LinkedHashMap();
        caseLinkMap.put(VALUE, valueMap);

        List caseLinksList = new ArrayList();
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
    void shouldAddPartyDetailsModelWithVulnerabilityTest() throws IOException, ParseException {

        LinkedHashMap reasonMap = new LinkedHashMap();
        reasonMap.put(REASON, REASON_TEST_VALUE);

        LinkedHashMap reasonForLinkMap = new LinkedHashMap();
        reasonForLinkMap.put(VALUE, reasonMap);

        List reasonForLinkList = new ArrayList();
        reasonForLinkList.add(reasonForLinkMap);

        LinkedHashMap valueMap = new LinkedHashMap();
        valueMap.put(REASON_FOR_LINK, reasonForLinkList);

        LinkedHashMap caseLinkMap = new LinkedHashMap();
        caseLinkMap.put(VALUE, valueMap);

        List caseLinksList = new ArrayList();
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
    void mapsActiveFlags_only() throws IOException, ParseException {
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
    void noFlags_whenNoneActive() throws IOException, ParseException {
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


}
