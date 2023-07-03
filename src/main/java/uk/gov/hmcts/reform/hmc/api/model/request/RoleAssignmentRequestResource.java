package uk.gov.hmcts.reform.hmc.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Builder
@Value
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleAssignmentRequestResource {

    RoleRequest roleRequest;

    List<RoleAssignment> requestedRoles;
}
