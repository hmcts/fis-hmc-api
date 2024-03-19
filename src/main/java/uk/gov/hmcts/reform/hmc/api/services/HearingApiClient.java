package uk.gov.hmcts.reform.hmc.api.services;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingRequest;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

import java.util.List;

@FeignClient(
        name = "hearing-feign-api",
        url = "${hearing_component.api.feign-url}",
        configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface HearingApiClient {

    @GetMapping(path = "/hearings/{caseReference}")
    Hearings getHearingDetails(
            @RequestHeader("Authorization") String authorisation,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @PathVariable("caseReference") String caseReference);


    @GetMapping(path = "/hearings")
    List<Hearings> getListOfHearingDetails(
            @RequestHeader("Authorization") String authorisation,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestParam("ccdCaseRefs") List<String> ccdCaseRefs);


    @PostMapping(path = "/hearing")
    List<Hearings> createHearingDetails(
            @RequestHeader("Authorization") String authorisation,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestBody HearingRequest hearingRequest);

}
