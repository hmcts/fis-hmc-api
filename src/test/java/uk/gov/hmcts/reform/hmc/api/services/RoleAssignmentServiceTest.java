package uk.gov.hmcts.reform.hmc.api.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.model.request.RoleAssignment;
import uk.gov.hmcts.reform.hmc.api.model.request.RoleAssignmentAttributesResource;
import uk.gov.hmcts.reform.hmc.api.model.request.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.hmc.api.model.request.RoleRequest;
import uk.gov.hmcts.reform.hmc.api.restclient.RoleAssignmentServiceApi;
import uk.gov.hmcts.reform.hmc.api.restclient.ServiceAuthorisationTokenApi;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

@SpringBootTest
@ExtendWith({MockitoExtension.class})
@ActiveProfiles("test")
@PropertySource("classpath:application.yaml")
public class RoleAssignmentServiceTest {

    @InjectMocks RoleAssignmentServiceImpl roleAssignmentService;

    @Mock IdamAuthService idamAuthService;

    @Mock IdamTokenGenerator idamTokenGenerator;

    @Mock ServiceAuthorisationTokenApi serviceAuthorisationTokenApi;

    @Mock RoleAssignmentServiceApi roleAssignmentServiceApi;

    @Mock AuthTokenGenerator authTokenGenerator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void assignHearingRoleToSysUserTest() {
        UserDetails user = UserDetails.builder()
            .id("12345")
            .email("test@hmcts.net")
            .roles(List.of("hearing-manager", "hearing-viewer"))
            .forename("ABC")
            .surname("XYZ").build();

        List<RoleAssignment> roleAssignmentList =
            Arrays.asList(buildRoleAssignment(ROLE_ASSIGNMENT_HEARING_MANAGER),
                          buildRoleAssignment(ROLE_ASSIGNMENT_HEARING_VIEWER));

        RoleAssignmentRequestResource roleAssignmentRequestResource =
            RoleAssignmentRequestResource.builder()
                .roleRequest(
                    RoleRequest.builder()
                        .assignerId("12345")
                        .process(ROLE_ASSIGNMENT_ROLE_REQUEST_PROCESS)
                        .reference(ROLE_ASSIGNMENT_ROLE_REQUEST_REFERENCE)
                        .replaceExisting(true)
                        .build())
                .requestedRoles(roleAssignmentList)
                .build();

        when(idamAuthService.getUserDetails(any())).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn("authToken");
        when(idamTokenGenerator.getSysUserToken()).thenReturn("sysUserToken");

        when(roleAssignmentServiceApi.createRoleAssignment(
            eq(roleAssignmentRequestResource),
            anyString(), eq("authToken"), eq("sysUserToken")))
            .thenReturn(ResponseEntity.ok("OK"));
        ResponseEntity<Object> response = roleAssignmentService.assignHearingRoleToSysUser();
        Assertions.assertNotNull(response);
        verify(idamAuthService, times(1)).getUserDetails(any());
    }

    private RoleAssignment buildRoleAssignment(String roleName) {
        return RoleAssignment.builder()
            .actorId("12345")
            .roleType(ROLE_ASSIGNMENT_ROLE_TYPE)
            .classification(ROLE_ASSIGNMENT_CLASSIFICATION)
            .roleName(roleName)
            .roleCategory(ROLE_ASSIGNMENT_ROLE_CATEGORY)
            .grantType(ROLE_ASSIGNMENT_GRANT_TYPE)
            .attributes(
                RoleAssignmentAttributesResource.builder()
                    .jurisdiction(Optional.of(ROLE_ASSIGNMENT_ATTRIBUTE_JURISDICTION))
                    .caseType(Optional.of(ROLE_ASSIGNMENT_ATTRIBUTE_CASE_TYPE))
                    .build())
            .actorIdType(ROLE_ASSIGNMENT_ACTOR_ID_TYPE)
            .build();
    }


}
