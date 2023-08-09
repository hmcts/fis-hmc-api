package uk.gov.hmcts.reform.hmc.api.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.hmc.api.config.HearingCftDataConfiguration;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(
        name = "hearing-cft-api",
        primary = false,
        url = "${hearing_component.api.url}",
        configuration = HearingCftDataConfiguration.class)
public interface HearingCftApi {

    String AUTHORIZATION = "Authorization";
    String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/hearings",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Hearings getHearings(
            @RequestHeader(AUTHORIZATION) String authorization,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
            @PathVariable("caseRefNo") String caseRefNo);
}
