package uk.gov.hmcts.reform.hmc.api.services;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.LISTED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.model.response.JudgeDetail;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class HearingsServiceImpl implements HearingsService {

    @Value("${hearing_component.api.url}")
    private String basePath;

    @Autowired AuthTokenGenerator authTokenGenerator;

    @Autowired IdamTokenGenerator idamTokenGenerator;

    @Autowired RefDataService refDataService;

    @Autowired RefDataJudicialService refDataJudicialService;

    @Autowired HearingApiClient hearingApiClient;

    private Hearings hearingDetails;

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
            log.info("Fetching hearings for casereference - {}", caseReference);
            final String s2sToken = authTokenGenerator.generate();
            MultiValueMap<String, String> inputHeaders =
                    getHttpHeaders(
                            idamTokenGenerator.generateIdamTokenForHearingCftData(), s2sToken);
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

            integrateVenueDetails(caseHearingsResponse);
            log.info(
                    "Number of hearings fetched for casereference - {} is {}",
                    caseReference,
                    caseHearingsResponse.getCaseHearings() != null
                            ? caseHearingsResponse.getCaseHearings().size()
                            : null);

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

    private void integrateVenueDetails(Hearings caseHearingsResponse) {

        if (caseHearingsResponse != null && caseHearingsResponse.getCaseHearings() != null) {
            List<CaseHearing> caseHearings = caseHearingsResponse.getCaseHearings();
            for (CaseHearing caseHearing : caseHearings) {

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

    @Override
    public Hearings getHearingsByCaseId(
            String caseReference, String authorization, String serviceAuthorization) {

        final String userToken = idamTokenGenerator.generateIdamTokenForHearingCftData();
        final String s2sToken = authTokenGenerator.generate();

        try {
            hearingDetails = hearingApiClient.getHearingDetails(userToken, s2sToken, caseReference);
            integrateVenueDetails(hearingDetails);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return hearingDetails;
    }

    /**
     * This method will fetch all the hearings which belongs to a particular caseRefNumber.
     *
     * @param caseIds CaseRefNumber to take all the hearings belongs to this case.
     * @param authorization authorization header.
     * @param serviceAuthorization serviceAuthorization header
     * @return caseHearingsResponse, all the hearings which belongs to a particular caseRefNumber.
     */
    @Override
    public List<Hearings> getHearingsByListOfCaseIds(
            List<String> caseIds, String authorization, String serviceAuthorization) {

        List<Hearings> casesWithHearings = new ArrayList<>();
        if (!caseIds.isEmpty()) {
            final String userToken = idamTokenGenerator.generateIdamTokenForHearingCftData();
            final String s2sToken = authTokenGenerator.generate();

            for (String caseId : caseIds) {
                try {
                    hearingDetails =
                            hearingApiClient.getHearingDetails(userToken, s2sToken, caseId);
                    casesWithHearings.add(hearingDetails);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }

        return casesWithHearings;
    }
}
