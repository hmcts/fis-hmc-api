package uk.gov.hmcts.reform.hmc.api.services;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

@FeignClient(
        name = "hearing-feign-api",
        url = "${hearing_component.api.url}",
        configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface HearingApiClient {

    @GetMapping(path = "/hearings/{caseReference}")
    Hearings getHearingDetails(
            @RequestHeader("Authorization") String authorisation,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @PathVariable("caseReference") String caseReference);
}
