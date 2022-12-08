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
import java.util.stream.Collectors;
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
import uk.gov.hmcts.reform.hmc.api.model.ccd.caselinksdata.CaseLinkData;
import uk.gov.hmcts.reform.hmc.api.model.ccd.caselinksdata.CaseLinkElement;
import uk.gov.hmcts.reform.hmc.api.model.ccd.caselinksdata.CaseReason;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseCategories;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingLocation;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingWindow;
import uk.gov.hmcts.reform.hmc.api.model.response.Judiciary;
import uk.gov.hmcts.reform.hmc.api.model.response.ServiceHearingValues;
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

    @Autowired CaseFlagDataServiceImpl caseFlagDataService;

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
                        applicantMap.get(Constants.LAST_NAME)
                                + Constants.UNDERSCORE
                                + respondentTableMap.get(Constants.LAST_NAME);
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
        ServiceHearingValues serviceHearingValues =
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
                        .facilitiesRequired(Arrays.asList())
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
                        .hearingChannels(Arrays.asList())
                        .build();
        caseFlagDataService.setCaseFlagData(serviceHearingValues, caseDetails);
        log.info("serviceHearingValues {}", serviceHearingValues);
        return serviceHearingValues;
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

    public CaseDetailResponse getCcdCaseData(CaseDetails caseDetails) throws IOException {
        ObjectMapper objectMapper = FisHmcObjectMapper.getObjectMapper();
        return objectMapper.convertValue(caseDetails, CaseDetailResponse.class);
    }

    @Override
    public List<HearingLinkData> getHearingLinkData(
            HearingValues hearingValues, String authorisation, String serviceAuthorization)
            throws IOException, ParseException {
        CaseDetails caseDetails =
                caseApiService.getCaseDetails(
                        hearingValues.getCaseReference(), authorisation, serviceAuthorization);
        CaseDetailResponse ccdResponse = getCcdCaseData(caseDetails);

        List listOfReasons = new ArrayList();
        List<CaseLinkElement<CaseLinkData>> caseLinkDataList =
                ccdResponse.getCaseData().getCaseLinks();

        if (caseLinkDataList != null) {
            List<List<Element<CaseReason>>> reasonList =
                    caseLinkDataList.stream()
                            .map(e -> e.getValue().getReasonForLink())
                            .collect(Collectors.toList());
            List<Element<CaseReason>> flattenedReasonList = flatten(reasonList);
            listOfReasons =
                    flattenedReasonList.stream()
                            .map(e -> e.getValue().reason)
                            .collect(Collectors.toList());
        }

        String applicantCaseName =
                (String) caseDetails.getData().get(Constants.APPLICANT_CASE_NAME);

        HearingLinkData hearingLinkData =
                HearingLinkData.hearingLinkDataWith()
                        .caseReference(hearingValues.getCaseReference())
                        .caseName(applicantCaseName)
                        .reasonsForLink((List<String>) listOfReasons)
                        .build();
        log.info("hearingLinkData {}", hearingLinkData);
        List serviceLinkedCases = new ArrayList();
        serviceLinkedCases.add(hearingLinkData);

        return serviceLinkedCases;
    }

    public static <T> List<T> flatten(List<List<T>> listOfLists) {
        return listOfLists.stream().flatMap(List::stream).collect(Collectors.toList());
    }
}
