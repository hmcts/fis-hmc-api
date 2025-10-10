package uk.gov.hmcts.reform.hmc.api.services;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ABA5;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.AND;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.APPLICANT_CASE_NAME;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.C100;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_FILE_VIEW;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_SUB_TYPE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_TYPE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CATEGORY_VALUE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CCD_DEMO_LINK;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.COURT;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.DEMO;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.EMPTY;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.EMPTY_STRING;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.FALSE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.FL401;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.FL401_APPLICANT_TABLE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.FL401_RESPONDENT_TABLE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.HEARING_PRIORITY;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.HEARING_TYPE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ISSUE_DATE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.LAST_NAME;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.RE_MINOR;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.SCREEN_FLOW;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.TRUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.hmc.api.mapper.FisHmcObjectMapper;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseDetailResponse;
import uk.gov.hmcts.reform.hmc.api.model.ccd.caselinksdata.CaseLinkData;
import uk.gov.hmcts.reform.hmc.api.model.ccd.caselinksdata.CaseLinkElement;
import uk.gov.hmcts.reform.hmc.api.model.ccd.request.Query;
import uk.gov.hmcts.reform.hmc.api.model.ccd.request.QueryParam;
import uk.gov.hmcts.reform.hmc.api.model.ccd.request.Terms;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseCategories;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingLocation;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingWindow;
import uk.gov.hmcts.reform.hmc.api.model.response.Judiciary;
import uk.gov.hmcts.reform.hmc.api.model.response.ServiceHearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.linkdata.HearingLinkData;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class HearingsDataServiceImpl implements HearingsDataService {

    @Value("${ccd.ui.url}")
    private String ccdBaseUrl;

    @Value("${hearing.search-case-type-id}")
    private String caseTypeId;

    @Value("${ccd.elastic-search-api.result-size}")
    private String ccdElasticSearchApiResultSize;

    @Value("${ccd.elastic-search-api.boost}")
    private String ccdElasticSearchApiBoost;

    private final CaseApiService caseApiService;
    private final ResourceLoader resourceLoader;
    private final CaseFlagV2DataServiceImpl caseFlagV2DataService;
    private final ElasticSearch elasticSearch;
    private final AuthTokenGenerator authTokenGenerator;

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
            throws IOException {
        CaseDetails caseDetails =
                caseApiService.getCaseDetails(
                        hearingValues.getCaseReference(), authorisation, serviceAuthorization);
        String publicCaseNameMapper = EMPTY;
        Boolean privateHearingRequiredFlagMapper = FALSE;
        if (FL401.equals(caseDetails.getData().get(CASE_TYPE_OF_APPLICATION))) {
            Map<String, String> applicantMap =
                    (LinkedHashMap<String, String>) caseDetails.getData().get(FL401_APPLICANT_TABLE);
            Map<String, String> respondentTableMap =
                    (LinkedHashMap<String, String>) caseDetails.getData().get(FL401_RESPONDENT_TABLE);
            if (applicantMap != null && respondentTableMap != null) {
                publicCaseNameMapper =
                        applicantMap.get(LAST_NAME) + AND + respondentTableMap.get(LAST_NAME);
            } else {
                publicCaseNameMapper = EMPTY;
            }
        } else if (C100.equals(caseDetails.getData().get(CASE_TYPE_OF_APPLICATION))) {
            publicCaseNameMapper = RE_MINOR;
            privateHearingRequiredFlagMapper = TRUE;
        }
        String caseSlaStartDateMapper = (String) caseDetails.getData().get(ISSUE_DATE);
        JSONObject screenFlowJson = null;
        JSONParser parser = new JSONParser();
        Resource resource = resourceLoader.getResource("classpath:ScreenFlow.json");

        try (InputStream inputStream = resource.getInputStream()) {
            screenFlowJson =
                    (JSONObject)
                            parser.parse(
                                    new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        if (null != ccdBaseUrl && ccdBaseUrl.contains(DEMO)) {
            ccdBaseUrl = CCD_DEMO_LINK;
        }

        ServiceHearingValues serviceHearingValues =
                ServiceHearingValues.hearingsDataWith()
                        .hmctsServiceID(ABA5)
                        .hmctsInternalCaseName(caseDetails.getData().get(APPLICANT_CASE_NAME).toString())
                        .publicCaseName(publicCaseNameMapper)
                        .caseAdditionalSecurityFlag(FALSE)
                        .caseCategories(getCaseCategories())
                        .caseDeepLink(
                                ccdBaseUrl + hearingValues.getCaseReference() + CASE_FILE_VIEW)
                        .caseRestrictedFlag(FALSE)
                        .externalCaseReference(EMPTY)
                        .caseManagementLocationCode(CASE_MANAGEMENT_LOCATION)
                        .caseSlaStartDate(caseSlaStartDateMapper)
                        .autoListFlag(FALSE)
                        .hearingType(HEARING_TYPE)
                        .hearingWindow(
                                HearingWindow.hearingWindowWith()
                                        .dateRangeStart(EMPTY)
                                        .dateRangeEnd(EMPTY)
                                        .firstDateTimeMustBe(EMPTY)
                                        .build())
                        .duration(0)
                        .hearingPriorityType(HEARING_PRIORITY)
                        .numberOfPhysicalAttendees(0)
                        .hearingInWelshFlag(FALSE)
                        .hearingLocations(
                                List.of(
                                        HearingLocation.hearingLocationWith()
                                                .locationType(COURT)
                                                .locationId(CASE_MANAGEMENT_LOCATION)
                                                .build()))
                        .facilitiesRequired(List.of())
                        .listingComments(EMPTY)
                        .hearingRequester(EMPTY)
                        .privateHearingRequiredFlag(privateHearingRequiredFlagMapper)
                        .caseInterpreterRequiredFlag(FALSE)
                        .panelRequirements(null)
                        .leadJudgeContractType(EMPTY)
                        .judiciary(Judiciary.judiciaryWith().build())
                        .hearingIsLinkedFlag(FALSE)
                        .screenFlow(
                                screenFlowJson != null
                                        ? (JSONArray) screenFlowJson.get(SCREEN_FLOW)
                                        : null)
                        .hearingChannels(List.of())
                        .build();

            caseFlagV2DataService.setCaseFlagsV2Data(serviceHearingValues, caseDetails);

        return serviceHearingValues;
    }

    private List<CaseCategories> getCaseCategories() {
        List<CaseCategories> caseCategoriesList = new ArrayList<>();
        CaseCategories caseCategories =
                CaseCategories.caseCategoriesWith()
                        .categoryType(CASE_TYPE)
                        .categoryValue(CATEGORY_VALUE)
                        .build();

        CaseCategories caseSubCategories =
                CaseCategories.caseCategoriesWith()
                        .categoryType(CASE_SUB_TYPE)
                        .categoryValue(CATEGORY_VALUE)
                        .categoryParent(CATEGORY_VALUE)
                        .build();

        caseCategoriesList.add(caseCategories);
        caseCategoriesList.add(caseSubCategories);
        return caseCategoriesList;
    }

    public CaseDetailResponse getCcdCaseData(CaseDetails caseDetails) {
        ObjectMapper objectMapper = FisHmcObjectMapper.getObjectMapper();
        return objectMapper.convertValue(caseDetails, CaseDetailResponse.class);
    }

    @Override
    public List<HearingLinkData> getHearingLinkData(
            HearingValues hearingValues, String authorisation, String serviceAuthorization)
            throws IOException {
        CaseDetails caseDetails =
                caseApiService.getCaseDetails(
                        hearingValues.getCaseReference(), authorisation, serviceAuthorization);
        CaseDetailResponse ccdResponse = getCcdCaseData(caseDetails);

        List<CaseLinkElement<CaseLinkData>> caseLinkDataList =
                ccdResponse.getCaseData().getCaseLinks();

        List<HearingLinkData> serviceLinkedCases = new ArrayList<>();
        if (caseLinkDataList != null) {

            List<String> caseReferences =
                    caseLinkDataList.stream()
                            .map(e -> e.getValue().getCaseReference())
                            .collect(Collectors.toList());

            final SearchResult searchResult =
                    getCaseNameForCaseReference(authorisation, caseReferences);

            for (CaseLinkElement<CaseLinkData> caseLinkDataObj : caseLinkDataList) {
                CaseLinkData caseLinkData = caseLinkDataObj.getValue();
                if (caseLinkData.getReasonForLink() != null) {
                    List<String> reasonList =
                            caseLinkData.getReasonForLink().stream()
                                    .map(e -> e.getValue().getReason())
                                    .collect(Collectors.toList());
                    HearingLinkData hearingLinkData =
                            HearingLinkData.hearingLinkDataWith()
                                    .caseReference(caseLinkData.getCaseReference())
                                    .reasonsForLink(reasonList)
                                    .caseName(
                                            getCaseName(
                                                    searchResult, caseLinkData.getCaseReference()))
                                    .build();
                    serviceLinkedCases.add(hearingLinkData);
                }
            }
        }

        return serviceLinkedCases;
    }

    private String getCaseName(SearchResult searchResult, String caseReference) {

        if (searchResult != null && searchResult.getCases() != null) {
            final List<CaseDetails> cases = searchResult.getCases();

            return cases.stream()
                    .filter(e -> caseReference.equals(e.getId().toString()))
                    .findFirst()
                    .map(caseDetails -> (String) caseDetails.getData().get(APPLICANT_CASE_NAME))
                    .orElse(EMPTY_STRING);
        }
        return EMPTY_STRING;
    }

    private SearchResult getCaseNameForCaseReference(
            String authorisation, List<String> caseReferences) throws JsonProcessingException {

        final QueryParam elasticSearchQueryParam = buildCcdQueryParam(caseReferences);
        ObjectMapper objectMapper = FisHmcObjectMapper.getObjectMapper();
        String searchString = objectMapper.writeValueAsString(elasticSearchQueryParam);
        return elasticSearch.searchCases(
                authorisation, searchString, authTokenGenerator.generate(), caseTypeId);
    }

    private QueryParam buildCcdQueryParam(List<String> caseReference) {
        Terms terms =
                Terms.builder()
                        .caseReference(caseReference)
                        .boost(ccdElasticSearchApiBoost)
                        .build();
        Query query = Query.builder().terms(terms).build();
        return QueryParam.builder().query(query).size(ccdElasticSearchApiResultSize).build();
    }
}
