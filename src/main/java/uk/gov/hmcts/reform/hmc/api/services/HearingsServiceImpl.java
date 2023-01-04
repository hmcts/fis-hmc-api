package uk.gov.hmcts.reform.hmc.api.services;

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
import uk.gov.hmcts.reform.hmc.api.exceptions.AuthorizationException;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseHearing;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class HearingsServiceImpl implements HearingsService {

    @Value("${hearing_component.api.url}")
    private String basePath;

    @Autowired AuthTokenGenerator authTokenGenerator;

    @Autowired IdamTokenGenerator idamTokenGenerator;

    @Autowired RefDataService refDataService;

    RestTemplate restTemplate = new RestTemplate();
    private static Logger log = LoggerFactory.getLogger(HearingsServiceImpl.class);

    /**
     * This method will fetch all the hearings which belongs to a particular caseRefNumber.
     *
     * @param caseReference CaseRefNumber to take all the hearings belongs to this case.
     * @return caseHearingsResponse, all the hearings which belongs to a particular caseRefNumber.
     */
    @Override
    public Hearings getHearingsByCaseRefNo(String caseReference) {

        UriComponentsBuilder builder =
                UriComponentsBuilder.newInstance().fromUriString(basePath + caseReference);
        Hearings caseHearingsResponse = null;

        try {
            MultiValueMap<String, String> inputHeaders =
                    getHttpHeaders(
                            idamTokenGenerator.generateIdamTokenForHearingCftData(),
                            authTokenGenerator.generate());
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

            caseHearingsResponse = integrateVenueDetails(caseHearingsResponse);

            return caseHearingsResponse;
        } catch (HttpClientErrorException | HttpServerErrorException exception) {
            log.info(
                    "HttpClientErrorException exception during getHearingsByCaseRefNo ",
                    exception.getStatusCode());
            throw new AuthorizationException(
                    "Hearing Client Error exception {}", exception.getStatusCode(), exception);
        } catch (Exception exception) {
            log.info("Exception exception during getHearingsByCaseRefNo ", exception);
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

    private Hearings integrateVenueDetails(Hearings caseHearingsResponse) {
        if (caseHearingsResponse != null && caseHearingsResponse.getCaseHearings() != null) {
            List<CaseHearing> caseHearings = caseHearingsResponse.getCaseHearings();
            for (CaseHearing caseHearing : caseHearings) {
                if (caseHearing.getHmcStatus().equals("LISTED")
                        && caseHearing.getHearingDaySchedule() != null) {
                    for (HearingDaySchedule hearingSchedule : caseHearing.getHearingDaySchedule()) {
                        String venueId = hearingSchedule.getHearingVenueId();
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
                    }
                }
            }

            caseHearingsResponse.setCaseHearings(caseHearings);
            return caseHearingsResponse;
        }
        return caseHearingsResponse;
    }
}
