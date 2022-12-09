package uk.gov.hmcts.reform.hmc.api.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import uk.gov.hmcts.reform.hmc.api.model.ccd.PartyDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.flagdata.FlagDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.ServiceHearingValues;
import uk.gov.hmcts.reform.hmc.api.utils.Constants;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class CaseFlagDataServiceTest {

    @InjectMocks private CaseFlagDataServiceImpl caseFlagDataService;

    @Test
    @SuppressWarnings("unchecked")
    void shouldSetCaseFlagTest() throws IOException, ParseException {

        LinkedHashMap reasonMap = new LinkedHashMap();
        reasonMap.put("Reason", "CLRC017");

        LinkedHashMap reasonForLinkMap = new LinkedHashMap();
        reasonForLinkMap.put("value", reasonMap);

        List reasonForLinkList = new ArrayList();
        reasonForLinkList.add(reasonForLinkMap);

        LinkedHashMap valueMap = new LinkedHashMap();
        valueMap.put("ReasonForLink", reasonForLinkList);

        LinkedHashMap caseLinkMap = new LinkedHashMap();
        caseLinkMap.put("value", valueMap);

        List caseLinksList = new ArrayList();
        caseLinksList.add(caseLinkMap);

        FlagDetail flagDetail = FlagDetail.builder().hearingRelevant("test").build();
        Element<FlagDetail> flagDetailElement =
                Element.<FlagDetail>builder().id(UUID.randomUUID()).value(flagDetail).build();

        List<Element<FlagDetail>> flagDetails = new ArrayList<>();
        flagDetails.add(flagDetailElement);

        Flags flags =
                Flags.builder().partyName("test").roleOnCase("test").details(flagDetails).build();

        PartyDetails partyDetails =
                PartyDetails.builder().firstName("test").partyLevelFlag(flags).build();
        Element<PartyDetails> partyDetailsElement =
                Element.<PartyDetails>builder().id(UUID.randomUUID()).value(partyDetails).build();

        List<Element<PartyDetails>> applicants = new ArrayList<>();
        applicants.add(partyDetailsElement);

        List<Element<PartyDetails>> respondents = new ArrayList<>();
        respondents.add(partyDetailsElement);

        CaseManagementLocation caseManagementLocation =
                CaseManagementLocation.builder().region("test").baseLocation("test").build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantCaseName", "Test Case 1 DA 31");
        caseDataMap.put("caseLinks", caseLinksList);
        caseDataMap.put("caseManagementLocation", caseManagementLocation);
        caseDataMap.put("applicants", applicants);
        caseDataMap.put("respondents", respondents);
        caseDataMap.put("applicantsFL401", partyDetails);
        caseDataMap.put("respondentsFL401", partyDetails);
        ServiceHearingValues serviceHearingValues =
                ServiceHearingValues.hearingsDataWith().hmctsServiceID(Constants.ABA5).build();
        CaseDetails caseDetails =
                CaseDetails.builder().id(123L).caseTypeId("PrivateLaw").data(caseDataMap).build();
        caseFlagDataService.setCaseFlagData(serviceHearingValues, caseDetails);
        Assertions.assertEquals("ABA5", serviceHearingValues.getHmctsServiceID());
    }
}
