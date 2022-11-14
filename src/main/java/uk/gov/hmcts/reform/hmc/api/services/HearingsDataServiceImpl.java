package uk.gov.hmcts.reform.hmc.api.services;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.C100;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.FL401;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.ApplicantTable;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseCategories;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingLocation;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingWindow;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingsData;
import uk.gov.hmcts.reform.hmc.api.model.response.Judiciary;
import uk.gov.hmcts.reform.hmc.api.model.response.PanelRequirements;
import uk.gov.hmcts.reform.hmc.api.model.response.Parties;
import uk.gov.hmcts.reform.hmc.api.model.response.RespondentTable;
import uk.gov.hmcts.reform.hmc.api.model.response.Vocabulary;
import uk.gov.hmcts.reform.hmc.api.utils.Constants;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class HearingsDataServiceImpl implements HearingsDataService {

    @Value("${ccd.ui.url}")
    private String ccdBaseUrl;

    @Autowired CaseApiService caseApiService;

    private static Logger log = LoggerFactory.getLogger(HearingsDataServiceImpl.class);

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
        try (Stream<String> lines = Files.lines(Paths.get(Constants.SCRN_FLOW_JSON_PATH))) {
            String screenFlowStr = lines.collect(Collectors.joining(""));
            JSONParser parser = new JSONParser();
            screenFlowJson = (JSONObject) parser.parse(screenFlowStr);
        } catch (Exception e) {
            log.info("While reading Screenflow.json file exception {}", e.getMessage());
        }
        HearingsData hearingsData =
                HearingsData.hearingsDataWith()
                        .hmctsServiceID(Constants.BBA3)
                        .hmctsInternalCaseName(hmctsInternalCaseNameMapper)
                        .publicCaseName(publicCaseNameMapper)
                        .caseAdditionalSecurityFlag(Constants.FALSE)
                        .caseCategories(
                                Arrays.asList(
                                        CaseCategories.caseCategoriesWith()
                                                .categoryType(Constants.EMPTY)
                                                .categoryValue(Constants.EMPTY)
                                                .categoryParent(Constants.PRIVATE_LAW)
                                                .build()))
                        .caseDeepLink(ccdBaseUrl + hearingValues.getCaseReference())
                        .caseRestrictedFlag(Constants.FALSE)
                        .externalCaseReference(Constants.EMPTY)
                        .caseManagementLocationCode(Constants.EMPTY)
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
                        .panelRequirements(
                                PanelRequirements.panelRequirementsWith()
                                        .requirementDetails(Constants.EMPTY)
                                        .build())
                        .leadJudgeContractType(Constants.EMPTY)
                        .judiciary(Judiciary.judiciaryWith().build())
                        .hearingIsLinkedFlag(Constants.FALSE)
                        .parties(Arrays.asList(Parties.partyDetailsWith().build()))
                        .screenFlow(
                                screenFlowJson != null
                                        ? (JSONArray) screenFlowJson.get(Constants.SCREEN_FLOW)
                                        : null)
                        .vocabulary(Arrays.asList(Vocabulary.vocabularyWith().build()))
                        .hearingChannels(Arrays.asList(Constants.EMPTY))
                        .build();
        log.info("hearingsData {}", hearingsData);
        return hearingsData;
    }
}
