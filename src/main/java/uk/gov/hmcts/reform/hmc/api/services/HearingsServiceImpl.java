package uk.gov.hmcts.reform.hmc.api.services;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.LISTED;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseHearing;
import uk.gov.hmcts.reform.hmc.api.model.response.Categories;
import uk.gov.hmcts.reform.hmc.api.model.response.Category;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.model.response.JudgeDetail;
import uk.gov.hmcts.reform.hmc.api.restclient.HmcHearingApi;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class HearingsServiceImpl implements HearingsService {

    @Value("${hearing_component.api.url}")
    private String basePath;

    @Value("${hearing.cateogry-id}")
    private String categoryId;

    @Autowired AuthTokenGenerator authTokenGenerator;

    @Autowired IdamTokenGenerator idamTokenGenerator;

    @Autowired RefDataService refDataService;

    @Autowired RefDataJudicialService refDataJudicialService;

    @Autowired HmcHearingApi hearingApi;

    RestTemplate restTemplate = new RestTemplate();
    private static Logger log = LoggerFactory.getLogger(HearingsServiceImpl.class);

    /**
     * This method will fetch all the hearings which belongs to a particular caseRefNumber.
     *
     * @param caseReference CaseRefNumber to take all the hearings belongs to this case.
     * @param authorization authorization header.
     * @param serviceAuthorization serviceAuthorization header
     * @return caseHearingsResponse, all the hearings which belongs to a particular caseRefNumber.
     */
    @Override
    public Hearings getHearingsByCaseRefNo(
            String caseReference, String authorization, String serviceAuthorization) {

        UriComponentsBuilder builder =
                UriComponentsBuilder.newInstance().fromUriString(basePath + caseReference);
        Hearings caseHearingsResponse = null;

        try {
            final String s2sToken = authTokenGenerator.generate();
            MultiValueMap<String, String> inputHeaders =
                    getHttpHeaders(
                            idamTokenGenerator.generateIdamTokenForHearingCftData(),
                            s2sToken
                    );
            HttpEntity<String> httpsHeader = new HttpEntity<>(inputHeaders);
            caseHearingsResponse =
                    restTemplate
                            .exchange(
                                    builder.toUriString(),
                                    HttpMethod.GET,
                                    httpsHeader,
                                    Hearings.class)
                            .getBody();
            log.info("Fetch hearings call completed successfully {}", caseHearingsResponse);

            final Map<String, String> refDataCategoryValueMap =
                    getRefDataCategoryValueMap(
                            authorization, s2sToken, caseHearingsResponse);

            integrateVenueDetails(caseHearingsResponse, refDataCategoryValueMap);

            return caseHearingsResponse;
        } catch (HttpClientErrorException | HttpServerErrorException exception) {
            log.error(
                    "HttpClientErrorException {} during getHearingsByCaseRefNo for case {}",
                    exception,
                    caseReference);
        } catch (Exception exception) {
            log.error(
                    "Exception {} during getHearingsByCaseRefNo for case {}",
                    exception,
                    caseReference);
        }
        return caseHearingsResponse;
    }

    private Map<String, String> getRefDataCategoryValueMap(
            String authorization, String serviceAuthorization, Hearings caseHearingsResponse) {

        // Call hearing api to get hmc status value
        if (caseHearingsResponse != null && caseHearingsResponse.getCaseHearings() != null) {
            final Categories categoriesByCategoryId =
                    hearingApi.retrieveListOfValuesByCategoryId(
                            authorization,
                            serviceAuthorization,
                            categoryId,
                            caseHearingsResponse.getHmctsServiceCode());

            return categoriesByCategoryId.getListOfCategory().stream()
                    .collect(Collectors.toMap(Category::getKey, Category::getValueEn));
        }
        return Collections.emptyMap();
    }

    /**
     * This method will create a map with header inputs.
     *
     * @return inputHeaders, which has all the header-inputs to make an API call.
     */
    private MultiValueMap<String, String> getHttpHeaders(
            String authorization, String serviceAuthorization) {
        MultiValueMap<String, String> inputHeaders = new LinkedMultiValueMap<>();
        inputHeaders.put("Content-Type", Arrays.asList("application/json"));
        inputHeaders.put("Authorization", Arrays.asList(authorization));
        inputHeaders.put("ServiceAuthorization", Arrays.asList(serviceAuthorization));
        return inputHeaders;
    }

    private void integrateVenueDetails(
            Hearings caseHearingsResponse, Map<String, String> refDataCategoryValueMap) {

        if (caseHearingsResponse != null && caseHearingsResponse.getCaseHearings() != null) {
            List<CaseHearing> caseHearings = caseHearingsResponse.getCaseHearings();
            for (CaseHearing caseHearing : caseHearings) {

                // set hearing type value
                caseHearing.setHearingTypeValue(
                        refDataCategoryValueMap.get(caseHearing.getHearingType()));

                if (caseHearing.getHmcStatus().equals(LISTED)
                        && caseHearing.getHearingDaySchedule() != null) {
                    for (HearingDaySchedule hearingSchedule : caseHearing.getHearingDaySchedule()) {
                        String venueId = hearingSchedule.getHearingVenueId();

                        String judgeId = hearingSchedule.getHearingJudgeId();
                        log.info("judgeId {}", judgeId);
                        if (null != venueId) {
                            log.info("VenueId {}", venueId);
                            CourtDetail courtDetail = refDataService.getCourtDetails(venueId);
                            if (courtDetail != null) {
                                hearingSchedule.setHearingVenueName(
                                        courtDetail.getHearingVenueName());
                                hearingSchedule.setHearingVenueAddress(
                                        courtDetail.getHearingVenueAddress());
                                hearingSchedule.setHearingVenueLocationCode(
                                        courtDetail.getHearingVenueLocationCode());
                            }
                        }

                        if (null != judgeId) {
                            log.info("judgeId==> {}", judgeId);
                            JudgeDetail judgeDetail =
                                    refDataJudicialService.getJudgeDetails(judgeId);
                            if (judgeDetail != null) {
                                hearingSchedule.setHearingJudgeName(
                                        judgeDetail.getHearingJudgeName());
                            }
                        }
                    }
                }
            }
            caseHearingsResponse.setCaseHearings(caseHearings);
        }
    }
}
