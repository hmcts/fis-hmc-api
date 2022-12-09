package uk.gov.hmcts.reform.hmc.api.services;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.hmc.api.exceptions.AuthorizationException;
import uk.gov.hmcts.reform.hmc.api.exceptions.ServerErrorException;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class HearingsServiceImpl implements HearingsService {

    @Value("${hearing_component.api.url}")
    private String basePath;

    RestTemplate restTemplate = new RestTemplate();
    private static Logger log = LoggerFactory.getLogger(HearingsServiceImpl.class);

    /**
     * This method will fetch all the hearings which belongs to a particular caseRefNumber.
     *
     * @param authorization User authorization token.
     * @param serviceAuthorization S2S authorization token.
     * @param caseReference CaseRefNumber to take all the hearings belongs to this case.
     * @return caseHearingsResponse, all the hearings which belongs to a particular caseRefNumber.
     */
    @Override
    public Hearings getHearingsByCaseRefNo(
            String authorization, String serviceAuthorization, String caseReference) {
        UriComponentsBuilder builder =
                UriComponentsBuilder.newInstance().fromUriString(basePath + caseReference);
        Hearings caseHearings = null;

        ResponseEntity<Hearings> caseHearingsResponse;
        try {
            MultiValueMap<String, String> inputHeaders =
                    getHttpHeaders(authorization, serviceAuthorization);
            HttpEntity<String> httpsHeader = new HttpEntity<>(inputHeaders);
            caseHearingsResponse =
                    restTemplate.exchange(
                            builder.toUriString(), HttpMethod.GET, httpsHeader, Hearings.class);
            log.info("Fetch hearings call completed successfully {}", caseHearings);
            return caseHearingsResponse.getBody();
        } catch (HttpClientErrorException exception) {
            throw new AuthorizationException("Hearing Client Error exception {}", exception.getStatusCode(), exception);
        } catch (HttpServerErrorException exception) {
            throw new ServerErrorException("Hearing Server Error exception {}", exception.getStatusCode(), exception);
        }
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
}
