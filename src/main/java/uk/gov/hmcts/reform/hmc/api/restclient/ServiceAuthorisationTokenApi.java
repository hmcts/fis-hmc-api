package uk.gov.hmcts.reform.hmc.api.restclient;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.util.MimeTypeUtils.TEXT_PLAIN_VALUE;

import feign.codec.Decoder;
import feign.codec.StringDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.hmc.api.model.request.MicroserviceInfo;

@FeignClient(
        name = "idam-s2s-auth-token",
        url = "${idam.s2s-auth.url}",
        configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface ServiceAuthorisationTokenApi {

    @PostMapping(
            value = "/testing-support/lease",
            consumes = APPLICATION_JSON_VALUE,
            produces = TEXT_PLAIN_VALUE)
    String serviceToken(@RequestBody MicroserviceInfo microserviceInfo);

    @SuppressWarnings({"PMD.UseVarargs", "PMD.UnnecessaryAnnotationValueElement"})
    @GetMapping(value = "/authorisation-check")
    void authorise(
            @RequestHeader(AUTHORIZATION) final String authHeader,
            @RequestParam("role") final String[] roles);

    @SuppressWarnings({"PMD.UseVarargs", "PMD.UnnecessaryAnnotationValueElement"})
    @GetMapping(value = "/details")
    String getServiceName(@RequestHeader(AUTHORIZATION) final String authHeader);
}
