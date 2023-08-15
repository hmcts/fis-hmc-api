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
import uk.gov.hmcts.reform.hmc.api.model.response.ServiceHearingValues;

import java.io.IOException;
import java.util.ArrayList;
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
class CaseFlagDataServiceTest {

    @InjectMocks private CaseFlagDataServiceImpl caseFlagDataService;

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
        caseFlagDataService.setCaseFlagData(serviceHearingValues, caseDetails);
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
        caseFlagDataService.setCaseFlagData(serviceHearingValues, caseDetails);
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
        caseFlagDataService.setCaseFlagData(serviceHearingValues, caseDetails);
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
        caseFlagDataService.setCaseFlagData(serviceHearingValues, caseDetails);
        Assertions.assertEquals(ABA5, serviceHearingValues.getHmctsServiceID());
    }
}
