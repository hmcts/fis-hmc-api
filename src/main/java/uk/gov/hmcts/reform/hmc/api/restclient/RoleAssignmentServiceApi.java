package uk.gov.hmcts.reform.hmc.api.restclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.hmc.api.model.request.RoleAssignmentRequestResource;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.AUTHORIZATION;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CORRELATION_ID_HEADER_NAME;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "am-role-assignment-service-api",
    url = "${role-assignment-service.api.url}",
    configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface RoleAssignmentServiceApi {

    @PostMapping(value = "/am/role-assignments", headers = "x-correlation-id")
    public ResponseEntity<Object> createRoleAssignment(@RequestBody RoleAssignmentRequestResource roleAssignmentRequestResource,
                                                       @RequestHeader(name = CORRELATION_ID_HEADER_NAME) String correlationId,
                                                       @RequestHeader(name = SERVICE_AUTHORIZATION) String serviceAuthorization,
                                                       @RequestHeader(name = AUTHORIZATION) String authorization);
}
