package uk.gov.hmcts.reform.hmc.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingsRequest;
import uk.gov.hmcts.reform.hmc.api.model.request.MicroserviceInfo;
import uk.gov.hmcts.reform.hmc.api.model.response.*;
import uk.gov.hmcts.reform.hmc.api.restclient.HmcHearingApi;
import uk.gov.hmcts.reform.hmc.api.restclient.ServiceAuthorisationTokenApi;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class HearingsService {
    @Value("${idam.s2s-auth.microservice}")
    private String microserviceName;

    @Value("${hearing_component.api.url}")
    private String BasePath;


    public static final String HEARING_SUB_CHANNEL = "HearingSubChannel";
    private final HmcHearingApi hmcHearingApi;
    private final ServiceAuthorisationTokenApi serviceAuthorisationTokenApi;
    private final CaseApiService caseApiService;


    public Categories getRefData(HearingsRequest hearingsRequest, String authorisation)
            throws JsonProcessingException {
        String serviceToken =
                serviceAuthorisationTokenApi.serviceToken(
                        MicroserviceInfo.builder().microservice(microserviceName.trim()).build());

        caseApiService.getCaseDetails(
                hearingsRequest.getCaseReference(), authorisation, serviceToken);

        return hmcHearingApi.retrieveListOfValuesByCategoryId(
                authorisation, serviceToken, HEARING_SUB_CHANNEL);
    }

    public CaseHearings getHearingsByCaseRefNo(String authorization, String serviceAuthorization, String caseReference)
        throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
            .fromUriString(BasePath + caseReference);
        CaseHearings caseHearings = null;

        try {
            caseHearings = restTemplate.exchange(builder.toUriString(),
                                                 HttpMethod.GET,
                                                 new HttpEntity<>(getHttpHeaders(authorization, serviceAuthorization)),
                                                 CaseHearings.class
            ).getBody();
            System.out.println("Fetch hearings call completed successfully"+ caseHearings);
        } catch (Exception e) {
            System.out.println("Fetch hearings call exception {}" + e.getMessage());
        }
        return caseHearings;
    }
    private MultiValueMap<String,String> getHttpHeaders(String authorization,String serviceAuthorization) {
        MultiValueMap<String, String> inputHeaders = new LinkedMultiValueMap<>();
        inputHeaders.put("Content-Type", Arrays.asList("application/json"));
        inputHeaders.put("Authorization", Arrays.asList(authorization));
        inputHeaders.put("ServiceAuthorization", Arrays.asList(serviceAuthorization));
        return inputHeaders;
    }

}
