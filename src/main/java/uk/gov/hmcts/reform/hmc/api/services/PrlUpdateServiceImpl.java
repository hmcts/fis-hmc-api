package uk.gov.hmcts.reform.hmc.api.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.exceptions.PrlUpdateException;
import uk.gov.hmcts.reform.hmc.api.model.request.Hearing;
import uk.gov.hmcts.reform.hmc.api.model.response.IdamTokenResponse;
import java.util.Arrays;

@Service
public class PrlUpdateServiceImpl implements PrlUpdateService {

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    RestTemplate restTemplate = new RestTemplate();

    @Value("${prl.baseUrl}")
    private String prlBaseUrl;

    @Value("${prl.path}")
    private String prlPath;

    private static final int DELAY_COUNT = 30_000;

    private static final Logger LOG = LoggerFactory.getLogger(PrlUpdateServiceImpl.class);

    @Override
    public Boolean updatePrlServiceWithHearing(Hearing hearing) {
        Boolean isPrlRespSuccess = false;
        LOG.info("updatePrlServiceWithHearing");
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
            .fromUriString(prlBaseUrl + prlPath);
        LOG.info("PRL URL {}",builder.toUriString());
        try {
            restTemplate.exchange(builder.toUriString(), HttpMethod.POST,
                                     new HttpEntity<>(hearing, getHttpHeaders()), String.class);
            LOG.info("PRL call completed successfully");
            isPrlRespSuccess = true;

        } catch (HttpClientErrorException | HttpServerErrorException exception) {
            LOG.info("PRL call exception {}",exception.getMessage());
            throw new PrlUpdateException("PRL", exception.getStatusCode(), exception);
        } catch (ResourceAccessException exception) {
            LOG.info("PRL call exception {}",exception.getMessage());
            throw new PrlUpdateException("PRL", HttpStatus.SERVICE_UNAVAILABLE, exception);
        }
        return isPrlRespSuccess;
    }

    private MultiValueMap<String,String> getHttpHeaders() {
        MultiValueMap<String, String> inputHeaders = new LinkedMultiValueMap<>();
        inputHeaders.put("Content-Type", Arrays.asList("application/json"));
        inputHeaders.put("Authorization", Arrays.asList("Bearer " + getAccessToken()));
        inputHeaders.put("ServiceAuthorization", Arrays.asList(getServiceAuthorisationToken()));
        LOG.info("HttpHeader {}", inputHeaders);
        return inputHeaders;
    }

    private String getServiceAuthorisationToken() {
        try {
            String serviceAuthToken = authTokenGenerator.generate();
            LOG.info("authTokenGenerator.generate() {}",serviceAuthToken);
            return serviceAuthToken;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new PrlUpdateException("S2S", e.getStatusCode(), e);
        } catch (Exception e) {
            throw new PrlUpdateException("S2S", HttpStatus.SERVICE_UNAVAILABLE, e);
        }
    }

    private String getAccessToken() {
        IdamTokenResponse idamTokenResponse = idamService.getSecurityTokens();
        LOG.info("idamTokenResponse {}",idamTokenResponse.getAccessToken());
        return idamTokenResponse.getAccessToken();
    }
}
