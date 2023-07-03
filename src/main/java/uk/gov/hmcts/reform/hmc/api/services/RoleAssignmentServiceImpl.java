package uk.gov.hmcts.reform.hmc.api.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.model.request.MicroserviceInfo;
import uk.gov.hmcts.reform.hmc.api.model.request.RoleAssignment;
import uk.gov.hmcts.reform.hmc.api.model.request.RoleAssignmentAttributesResource;
import uk.gov.hmcts.reform.hmc.api.model.request.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.hmc.api.model.request.RoleRequest;
import uk.gov.hmcts.reform.hmc.api.restclient.RoleAssignmentServiceApi;
import uk.gov.hmcts.reform.hmc.api.restclient.ServiceAuthorisationTokenApi;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.AUTH_TOKEN_AAT;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ROLE_ASSIGNMENT_ACTOR_ID_TYPE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ROLE_ASSIGNMENT_ATTRIBUTE_CASE_TYPE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ROLE_ASSIGNMENT_ATTRIBUTE_JURISDICTION;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ROLE_ASSIGNMENT_CLASSIFICATION;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ROLE_ASSIGNMENT_GRANT_TYPE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ROLE_ASSIGNMENT_HEARING_MANAGER;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ROLE_ASSIGNMENT_HEARING_VIEWER;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ROLE_ASSIGNMENT_ROLE_CATEGORY;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ROLE_ASSIGNMENT_ROLE_REQUEST_PROCESS;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ROLE_ASSIGNMENT_ROLE_REQUEST_REFERENCE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ROLE_ASSIGNMENT_ROLE_TYPE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.S2S_TOKEN_AAT;

@Service
@Slf4j
public class RoleAssignmentServiceImpl implements RoleAssignmentService {

    @Autowired
    IdamAuthService idamAuthService;

    @Autowired
    IdamTokenGenerator idamTokenGenerator;

    @Value("${idam.s2s-auth.microservice}")
    private String microservice;

    private final RoleAssignmentServiceApi roleAssignmentServiceApi;

    private final ServiceAuthorisationTokenApi serviceAuthorisationTokenApi;

    public RoleAssignmentServiceImpl(RoleAssignmentServiceApi roleAssignmentServiceApi, ServiceAuthorisationTokenApi serviceAuthorisationTokenApi) {
        this.roleAssignmentServiceApi = roleAssignmentServiceApi;
        this.serviceAuthorisationTokenApi = serviceAuthorisationTokenApi;
    }


    @Override
    public ResponseEntity<Object> assignRoleBasedOnAuthToken() {
        String systemUserIdamID = idamAuthService.getUserDetails(idamTokenGenerator.generateIdamTokenForRefData()).getId();

        List<RoleAssignment> roleAssignmentList = Arrays.asList(buildRoleAssignment(systemUserIdamID, ROLE_ASSIGNMENT_HEARING_MANAGER)
            , buildRoleAssignment(systemUserIdamID, ROLE_ASSIGNMENT_HEARING_VIEWER));
        RoleAssignmentRequestResource roleAssignmentRequestResource = RoleAssignmentRequestResource.builder()
            .roleRequest(RoleRequest.builder()
                             .assignerId(systemUserIdamID)
                             .process(ROLE_ASSIGNMENT_ROLE_REQUEST_PROCESS)
                             .reference(ROLE_ASSIGNMENT_ROLE_REQUEST_REFERENCE)
                             .replaceExisting(true)
                             .build())
            .requestedRoles(roleAssignmentList)
            .build();

        /*return roleAssignmentServiceApi.createRoleAssignment(roleAssignmentRequestResource, getCorrelationId()
            , "Bearer "+serviceAuthorisationTokenApi.serviceToken(MicroserviceInfo.builder().microservice(microservice).build()), idamTokenGenerator.getSysUserToken());*/
        return roleAssignmentServiceApi.createRoleAssignment(roleAssignmentRequestResource, getCorrelationId()
        , S2S_TOKEN_AAT, AUTH_TOKEN_AAT);
    }

    private RoleAssignment buildRoleAssignment(String id, String roleName) {
        return RoleAssignment.builder()
            .actorId(id)
            .roleType(ROLE_ASSIGNMENT_ROLE_TYPE)
            .classification(ROLE_ASSIGNMENT_CLASSIFICATION)
            .roleName(roleName)
            .roleCategory(ROLE_ASSIGNMENT_ROLE_CATEGORY)
            .grantType(ROLE_ASSIGNMENT_GRANT_TYPE)
            .attributes(RoleAssignmentAttributesResource.builder()
                            .jurisdiction(Optional.of(ROLE_ASSIGNMENT_ATTRIBUTE_JURISDICTION))
                            .caseType(Optional.of(ROLE_ASSIGNMENT_ATTRIBUTE_CASE_TYPE))
                            .build())
            .actorIdType(ROLE_ASSIGNMENT_ACTOR_ID_TYPE)
            .build();
    }

    private String getCorrelationId() {
        try {
            var httpServletRequest = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes())
                .getRequest();
            //return httpServletRequest.getHeader(Constants.CORRELATION_ID_HEADER_NAME);
            return "d1ec4525-87f6-458b-9131-ee24d71cdd14";
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
