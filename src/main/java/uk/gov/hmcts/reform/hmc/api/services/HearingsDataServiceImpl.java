package uk.gov.hmcts.reform.hmc.api.services;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.C100;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.FL401;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import uk.gov.hmcts.reform.hmc.api.mapper.FisHmcObjectMapper;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseDetailResponse;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Element;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Flags;
import uk.gov.hmcts.reform.hmc.api.model.ccd.PartyDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.flagdata.FlagDetail;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseCategories;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseFlags;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingLocation;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingWindow;
import uk.gov.hmcts.reform.hmc.api.model.response.IndividualDetailsModel;
import uk.gov.hmcts.reform.hmc.api.model.response.Judiciary;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyDetailsModel;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyFlagsModel;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyType;
import uk.gov.hmcts.reform.hmc.api.model.response.ServiceHearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.Vocabulary;
import uk.gov.hmcts.reform.hmc.api.model.response.linkdata.HearingLinkData;
import uk.gov.hmcts.reform.hmc.api.utils.Constants;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class HearingsDataServiceImpl implements HearingsDataService {

    public static final String EMPTYSTRING = " ";

    @Value("${ccd.ui.url}")
    private String ccdBaseUrl;

    @Autowired CaseApiService caseApiService;

    @Autowired private ResourceLoader resourceLoader;

    protected static final List<String> HEARING_CHANNELS =
            Arrays.asList("INTER", "TEL", "VID", "ONPPRS");
    protected static final List<String> FACILITIES_REQUIRED = Arrays.asList("9", "11", "14");

    /**
     * This method will fetch the hearingsData info based on the hearingValues passed.
     *
     * @param authorisation User authorization token.
     * @param serviceAuthorization S2S authorization token.
     * @param hearingValues combination of caseRefNo and hearingId to fetch hearingsData.
     * @return hearingsData, response data for the input hearingValues.
     */
    @Override
    public ServiceHearingValues getCaseData(
            HearingValues hearingValues, String authorisation, String serviceAuthorization)
            throws IOException, ParseException {
        CaseDetails caseDetails =
                caseApiService.getCaseDetails(
                        hearingValues.getCaseReference(), authorisation, serviceAuthorization);
        String publicCaseNameMapper;
        if (FL401.equals(caseDetails.getData().get(CASE_TYPE_OF_APPLICATION))) {
            Map applicantMap =
                    (LinkedHashMap) caseDetails.getData().get(Constants.FL401_APPLICANT_TABLE);
            Map respondentTableMap =
                    (LinkedHashMap) caseDetails.getData().get(Constants.FL401_RESPONDENT_TABLE);
            if (applicantMap != null && respondentTableMap != null) {
                publicCaseNameMapper =
                        applicantMap.get("lastName")
                                + Constants.UNDERSCORE
                                + respondentTableMap.get("lastName");
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
        getCcdCaseData(caseDetails);
        JSONObject screenFlowJson = null;
        JSONParser parser = new JSONParser();
        Resource resource = resourceLoader.getResource("classpath:ScreenFlow.json");

        try (InputStream inputStream = resource.getInputStream()) {
            screenFlowJson = (JSONObject) parser.parse(new InputStreamReader(inputStream, "UTF-8"));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        ServiceHearingValues hearingsData =
                ServiceHearingValues.hearingsDataWith()
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
                        .hearingType(Constants.HEARING_TYPE)
                        .hearingWindow(
                                HearingWindow.hearingWindowWith()
                                        .dateRangeStart(Constants.EMPTY)
                                        .dateRangeEnd(Constants.EMPTY)
                                        .firstDateTimeMustBe(Constants.EMPTY)
                                        .build())
                        .duration(0)
                        .hearingPriorityType(Constants.HEARING_PRIORITY)
                        .numberOfPhysicalAttendees(0)
                        .hearingInWelshFlag(Constants.FALSE)
                        .hearingLocations(
                                Arrays.asList(
                                        HearingLocation.hearingLocationWith()
                                                .locationType(Constants.EMPTY)
                                                .locationId(Constants.CASE_MANAGEMENT_LOCATION)
                                                .build()))
                        .facilitiesRequired(FACILITIES_REQUIRED)
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
                        .hearingChannels(HEARING_CHANNELS)
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

    public void setCaseFlagData(ServiceHearingValues hearingsData) {
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
        IndividualDetailsModel individualDetailsModel =
                IndividualDetailsModel.individualDetailsWith()
                        .firstName("Jane")
                        .lastName("Smith")
                        .build();

        PartyDetailsModel partyDetailsModel =
                PartyDetailsModel.partyDetailsWith()
                        .partyID(partyFlagsModel.getPartyId())
                        .partyName(partyFlagsModel.getPartyName())
                        .partyType(PartyType.IND)
                        .partyRole(Constants.APPLICANT)
                        .individualDetails(individualDetailsModel)
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

    public CaseDetailResponse getCcdCaseData(CaseDetails caseDetails) throws IOException {
        ObjectMapper objectMapper = FisHmcObjectMapper.getObjectMapper();

        CaseDetailResponse ccdResponse =
                objectMapper.convertValue(caseDetails, CaseDetailResponse.class);

        return ccdResponse;
    }

    public void getCaseFlagData(ServiceHearingValues hearingsData, CaseDetails caseDetails)
            throws IOException {

        List<PartyFlagsModel> partiesFlagslList = new ArrayList<>();
        List<PartyDetailsModel> partyDetailsModelList = new ArrayList<>();
        CaseDetailResponse ccdResponse = getCcdCaseData(caseDetails);
        List<Element<PartyDetails>> applicantLst = ccdResponse.getCaseData().getApplicants();

        addPartyFlagData(partiesFlagslList, partyDetailsModelList, applicantLst);

        List<Element<PartyDetails>> respondedLst = ccdResponse.getCaseData().getRespondents();

        addPartyFlagData(partiesFlagslList, partyDetailsModelList, respondedLst);

        CaseFlags caseFlags = CaseFlags.caseFlagsWith().flags(partiesFlagslList).build();

        hearingsData.setCaseFlags(caseFlags);

        hearingsData.setParties(partyDetailsModelList);
    }

    private void addPartyFlagData(
            List<PartyFlagsModel> partiesFlagslList,
            List<PartyDetailsModel> partyDetailsModelList,
            List<Element<PartyDetails>> applicantLst) {
        IndividualDetailsModel individualDetailsModel;
        String uuid;
        PartyDetails partyDetails;
        PartyDetailsModel partyDetailsModel;
        for (Element<PartyDetails> party : applicantLst) {
            uuid = party.getId().toString();
            partyDetails = party.getValue();
            partiesFlagslList.addAll(getPartyFlagsModel(partyDetails, uuid));

            individualDetailsModel =
                    IndividualDetailsModel.individualDetailsWith()
                            .firstName(partyDetails.getFirstName())
                            .lastName(partyDetails.getLastName())
                            .build();

            partyDetailsModel =
                    PartyDetailsModel.partyDetailsWith()
                            .partyID(uuid)
                            .partyName(
                                    partyDetails.getFirstName()
                                            + EMPTYSTRING
                                            + partyDetails.getLastName())
                            .partyType(PartyType.IND)
                            .partyRole(Constants.APPLICANT)
                            .individualDetails(individualDetailsModel)
                            .build();

            partyDetailsModelList.add(partyDetailsModel);
        }
    }

    private List<PartyFlagsModel> getPartyFlagsModel(PartyDetails partyDetails, String uuid) {
        PartyFlagsModel partyFlagsModel = null;
        List<PartyFlagsModel> partyFlagsModelList = new ArrayList<>();
        Flags flag = partyDetails.getPartyLevelFlag();
        List<Element<FlagDetail>> detailsLST = flag.getDetails();

        for (Element<FlagDetail> flagDetailElement : detailsLST) {
            FlagDetail flagDetail = flagDetailElement.getValue();

            partyFlagsModel =
                    PartyFlagsModel.partyFlagsModelWith()
                            .partyId(uuid)
                            .partyName(
                                    partyDetails.getFirstName()
                                            + EMPTYSTRING
                                            + partyDetails.getLastName())
                            .flagId(flagDetail.getData().getFlagCode())
                            .flagStatus(flagDetail.getData().getStatus())
                            .flagParentId("")
                            .flagDescription(flagDetail.getData().getFlagComment())
                            .build();
            partyFlagsModelList.add(partyFlagsModel);
        }

        return partyFlagsModelList;
    }
}
