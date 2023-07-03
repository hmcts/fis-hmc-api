package uk.gov.hmcts.reform.hmc.api.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface RoleAssignmentService {

    ResponseEntity<Object> assignRoleBasedOnAuthToken();
}
