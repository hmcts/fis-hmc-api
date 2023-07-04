package uk.gov.hmcts.reform.hmc.api.services;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.restclient.RoleAssignmentServiceApi;
import uk.gov.hmcts.reform.hmc.api.restclient.ServiceAuthorisationTokenApi;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@SpringBootTest
@ExtendWith({MockitoExtension.class})
public class RoleAssignmentServiceTest {

    @InjectMocks RoleAssignmentServiceImpl roleAssignmentService;

    @Mock IdamAuthService idamAuthService;

    @Mock IdamTokenGenerator idamTokenGenerator;

    @Mock ServiceAuthorisationTokenApi serviceAuthorisationTokenApi;

    @Mock RoleAssignmentServiceApi roleAssignmentServiceApi;

    @Test
    public void assignRoleBasedToSysUserTest() {

        ReflectionTestUtils.setField(roleAssignmentService, "microservice", "fis-hmc-api");

        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("sahdjks");
        when(idamAuthService.getUserDetails(anyString()))
                .thenReturn(new UserDetails("id", null, null, null, null));
        when(idamTokenGenerator.getSysUserToken()).thenReturn("IDAM_TOKEN");
        ResponseEntity response = roleAssignmentService.assignHearingRoleToSysUser();
        Assertions.assertEquals(null, response);
    }
}
