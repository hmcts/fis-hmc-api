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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class HearingsServiceImpl implements HearingsService {
    @Value("${idam.s2s-auth.microservice}")
    private String microserviceName;

    @Value("${hearing_component.api.url}")
    private String basePath;

    RestTemplate restTemplate = new RestTemplate();
    private static Logger log = LoggerFactory.getLogger(HearingsServiceImpl.class);

    @Override
    public Hearings getHearingsByCaseRefNo(
            String authorization, String serviceAuthorization, String caseReference) {

        UriComponentsBuilder builder =
                UriComponentsBuilder.newInstance().fromUriString(basePath + caseReference);
        Hearings caseHearings = null;

        ResponseEntity<Hearings> caseHearingsResponse = null;
        try {
            MultiValueMap<String, String> inputHeaders =
                    getHttpHeaders(authorization, serviceAuthorization);
            HttpEntity<String> httpsHeader = new HttpEntity<>(inputHeaders);
            caseHearingsResponse =
                    restTemplate.exchange(
                            builder.toUriString(), HttpMethod.GET, httpsHeader, Hearings.class);
            log.info("Fetch hearings call completed successfully {}", caseHearings);
        } catch (Exception e) {
            log.info("Fetch hearings call exception {}", e.getMessage());
        }
        log.info("Fetch hearings call completed successfully {} finalll", caseHearingsResponse);
        return caseHearingsResponse.getBody();
    }

    private MultiValueMap<String, String> getHttpHeaders(
            String authorization, String serviceAuthorization) {
        MultiValueMap<String, String> inputHeaders = new LinkedMultiValueMap<>();
        inputHeaders.put("Content-Type", Arrays.asList("application/json"));
        inputHeaders.put("Authorization", Arrays.asList(authorization));
        inputHeaders.put("ServiceAuthorization", Arrays.asList(serviceAuthorization));
        return inputHeaders;
    }
}
