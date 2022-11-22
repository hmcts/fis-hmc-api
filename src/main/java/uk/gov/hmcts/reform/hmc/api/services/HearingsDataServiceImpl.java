package uk.gov.hmcts.reform.hmc.api.services;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.C100;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.FL401;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.ApplicantTable;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseCategories;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseFlags;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingLocation;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingWindow;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingsData;
import uk.gov.hmcts.reform.hmc.api.model.response.Judiciary;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyDetailsModel;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyFlagsModel;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyType;
import uk.gov.hmcts.reform.hmc.api.model.response.RespondentTable;
import uk.gov.hmcts.reform.hmc.api.model.response.Vocabulary;
import uk.gov.hmcts.reform.hmc.api.model.response.linkdata.HearingLinkData;
import uk.gov.hmcts.reform.hmc.api.utils.Constants;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class HearingsDataServiceImpl implements HearingsDataService {

    @Value("${ccd.ui.url}")
    private String ccdBaseUrl;

    @Autowired CaseApiService caseApiService;

    @Autowired private ResourceLoader resourceLoader;

    /**
     * This method will fetch the hearingsData info based on the hearingValues passed.
     *
     * @param authorisation User authorization token.
     * @param serviceAuthorization S2S authorization token.
     * @param hearingValues combination of caseRefNo and hearingId to fetch hearingsData.
     * @return hearingsData, response data for the input hearingValues.
     */
    @Override
    public HearingsData getCaseData(
            HearingValues hearingValues, String authorisation, String serviceAuthorization)
            throws IOException, ParseException {
        CaseDetails caseDetails =
                caseApiService.getCaseDetails(
                        hearingValues.getCaseReference(), authorisation, serviceAuthorization);
        String publicCaseNameMapper;
        if (FL401.equals(caseDetails.getData().get(CASE_TYPE_OF_APPLICATION))) {
            ApplicantTable applicantTable =
                    (ApplicantTable) caseDetails.getData().get(Constants.FL401_APPLICANT_TABLE);
            RespondentTable respondentTable =
                    (RespondentTable) caseDetails.getData().get(Constants.FL401_RESPONDENT_TABLE);
            if (applicantTable != null && respondentTable != null) {
                publicCaseNameMapper =
                        applicantTable.getLastName()
                                + Constants.UNDERSCORE
                                + respondentTable.getLastName();
            } else {
                publicCaseNameMapper = Constants.EMPTY;
            }
        } else if (C100.equals(caseDetails.getData().get(CASE_TYPE_OF_APPLICATION))) {
            publicCaseNameMapper = Constants.RE_MINOR;
        } else {
            publicCaseNameMapper = Constants.EMPTY;
        }
        String hmctsInternalCaseNameMapper =
                hearingValues.getCaseReference()
                        + Constants.UNDERSCORE
                        + caseDetails.getData().get(Constants.APPLICANT_CASE_NAME);
        String caseSlaStartDateMapper = (String) caseDetails.getData().get(Constants.ISSUE_DATE);
        JSONObject screenFlowJson = null;
        JSONParser parser = new JSONParser();
        Resource resource = resourceLoader.getResource("classpath:ScreenFlow.json");

        try (InputStream inputStream = resource.getInputStream()) {
            screenFlowJson = (JSONObject) parser.parse(new InputStreamReader(inputStream, "UTF-8"));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        HearingsData hearingsData =
                HearingsData.hearingsDataWith()
                        .hmctsServiceID(Constants.ABA5)
                        .hmctsInternalCaseName(hmctsInternalCaseNameMapper)
                        .publicCaseName(publicCaseNameMapper)
                        .caseAdditionalSecurityFlag(Constants.FALSE)
                        .caseCategories(getCaseCategories())
                        .caseDeepLink(ccdBaseUrl + hearingValues.getCaseReference())
                        .caseRestrictedFlag(Constants.FALSE)
                        .externalCaseReference(Constants.EMPTY)
                        .caseManagementLocationCode(Constants.CASE_MANAGEMENT_LOCATION)
                        .caseSlaStartDate(caseSlaStartDateMapper)
                        .autoListFlag(Constants.FALSE)
                        .hearingType(Constants.EMPTY)
                        .hearingWindow(
                                HearingWindow.hearingWindowWith()
                                        .dateRangeStart(Constants.EMPTY)
                                        .dateRangeEnd(Constants.EMPTY)
                                        .firstDateTimeMustBe(Constants.EMPTY)
                                        .build())
                        .duration(0)
                        .hearingPriorityType(Constants.EMPTY)
                        .numberOfPhysicalAttendees(0)
                        .hearingInWelshFlag(Constants.FALSE)
                        .hearingLocations(
                                Arrays.asList(
                                        HearingLocation.hearingLocationWith()
                                                .locationType(Constants.EMPTY)
                                                .locationId(Constants.EMPTY)
                                                .build()))
                        .facilitiesRequired(Arrays.asList(Constants.EMPTY))
                        .listingComments(Constants.EMPTY)
                        .hearingRequester(Constants.EMPTY)
                        .privateHearingRequiredFlag(Constants.FALSE)
                        .caseInterpreterRequiredFlag(Constants.FALSE)
                        .panelRequirements(null)
                        .leadJudgeContractType(Constants.EMPTY)
                        .judiciary(Judiciary.judiciaryWith().build())
                        .hearingIsLinkedFlag(Constants.FALSE)
                        .screenFlow(
                                screenFlowJson != null
                                        ? (JSONArray) screenFlowJson.get(Constants.SCREEN_FLOW)
                                        : null)
                        .vocabulary(Arrays.asList(Vocabulary.vocabularyWith().build()))
                        .hearingChannels(Arrays.asList(Constants.EMPTY))
                        .build();
        setCaseFlagData(hearingsData);
        log.info("hearingsData {}", hearingsData);
        return hearingsData;
    }

    private List<CaseCategories> getCaseCategories() {
        List<CaseCategories> caseCategoriesList = new ArrayList<>();
        CaseCategories caseCategories =
                CaseCategories.caseCategoriesWith()
                        .categoryType(Constants.CASE_TYPE)
                        .categoryValue(Constants.CATEGORY_VALUE)
                        .build();

        CaseCategories caseSubCategories =
                CaseCategories.caseCategoriesWith()
                        .categoryType(Constants.CASE_SUB_TYPE)
                        .categoryValue(Constants.CATEGORY_VALUE)
                        .categoryParent(Constants.CATEGORY_VALUE)
                        .build();

        caseCategoriesList.add(caseCategories);
        caseCategoriesList.add(caseSubCategories);
        return caseCategoriesList;
    }

    public void setCaseFlagData(HearingsData hearingsData) {
        String uuid = UUID.randomUUID().toString();
        PartyFlagsModel partyFlagsModel =
                PartyFlagsModel.partyFlagsModelWith()
                        .partyId(uuid)
                        .partyName("Jane Smith")
                        .flagId("RA0042")
                        .flagStatus("ACTIVE")
                        .flagParentId("")
                        .flagDescription("Sign language interpreter required")
                        .build();
        List<PartyFlagsModel> partyFlagsModelList = new ArrayList<>();
        partyFlagsModelList.add(partyFlagsModel);
        CaseFlags caseFlags = CaseFlags.caseFlagsWith().flags(partyFlagsModelList).build();

        hearingsData.setCaseFlags(caseFlags);

        PartyDetailsModel partyDetailsModel =
                PartyDetailsModel.partyDetailsWith()
                        .partyID(partyFlagsModel.getPartyId())
                        .partyName(partyFlagsModel.getPartyName())
                        .partyType(PartyType.IND)
                        .partyRole(Constants.APPLICANT)
                        .build();

        List<PartyDetailsModel> partyDetailsModelList = new ArrayList<>();
        partyDetailsModelList.add(partyDetailsModel);
        hearingsData.setParties(partyDetailsModelList);
    }

    @Override
    public List<HearingLinkData> getHearingLinkData(
            HearingValues hearingValues, String authorisation, String serviceAuthorization) {
        return new ArrayList<>();
    }
}
