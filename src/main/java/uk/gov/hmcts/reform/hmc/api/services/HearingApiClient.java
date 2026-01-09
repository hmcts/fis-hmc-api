package uk.gov.hmcts.reform.hmc.api.services;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.retry.annotation.Retryable;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingResponse;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

import java.util.List;
import java.util.concurrent.TimeoutException;

@FeignClient(
        name = "hearing-feign-api",
        url = "${hearing_component.api.feign-url}",
        configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface HearingApiClient {
    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String ROLE_ASSIGNMENT_URL = "Role-Assignment-Url";
    String DATA_STORE_URL = "Data-Store-Url";
    String HMCTS_DEPLOYMENT_ID = "hmctsDeploymentId";

    @GetMapping(path = "/hearings/{caseReference}")
    Hearings getHearingDetails(
            @RequestHeader("Authorization") String authorisation,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @PathVariable("caseReference") String caseReference);


    @GetMapping(path = "/hearings")
    List<Hearings> getListOfHearingDetails(
            @RequestHeader("Authorization") String authorisation,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestParam("ccdCaseRefs") List<String> ccdCaseRefs,
            @RequestParam("caseTypeId") String caseTypeId);


    @PostMapping(path = "/hearing")
    @Retryable({RuntimeException.class, TimeoutException.class})
    HearingResponse createHearingDetails(
            @RequestHeader("Authorization") String authorisation,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestHeader(value = HMCTS_DEPLOYMENT_ID, required = false) String hmctsDeploymentId,
            @RequestHeader(value = DATA_STORE_URL, required = false) String dataStoreUrl,
            @RequestHeader(value = ROLE_ASSIGNMENT_URL, required = false) String roleAssignmentUrl,
            @RequestBody AutomatedHearingRequest hearingRequest);

}
