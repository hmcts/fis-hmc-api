package uk.gov.hmcts.reform.hmc.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.restclient.RoleAssignmentServiceApi;
import uk.gov.hmcts.reform.hmc.api.restclient.ServiceAuthorisationTokenApi;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static org.mockito.Mockito.*;

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
    public void assignHearingRoleToSysUserTest(){
        UserDetails user = UserDetails.builder()
            .id("12345")
            .email("test@hmcts.net")
            .roles(List.of("hearing-manager", "hearing-viewer"))
            .forename("ABC")
            .surname("XYZ").build();
        when(idamAuthService.getUserDetails(any())).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn("authToken");
        when(idamTokenGenerator.getSysUserToken()).thenReturn("sysUserToken");
        roleAssignmentService.assignHearingRoleToSysUser();
        verify(idamAuthService, times(1)).getUserDetails(any());
    }

}
