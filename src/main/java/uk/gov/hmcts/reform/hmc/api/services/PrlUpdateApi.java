package uk.gov.hmcts.reform.hmc.api.services;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.hmc.api.config.PrlUpdateConfiguration;
import uk.gov.hmcts.reform.hmc.api.model.request.Hearing;

@FeignClient(
        name = "prl-update-api",
        primary = false,
        url = "${prl.baseUrl}",
        configuration = PrlUpdateConfiguration.class)
public interface PrlUpdateApi {
    String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/hearing-management-state-update/",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    ResponseEntity prlUpdate(
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
            @RequestBody final Hearing hearing);
}
