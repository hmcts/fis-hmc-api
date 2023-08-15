package uk.gov.hmcts.reform.hmc.api.services;

import org.junit.Ignore;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.restclient.RoleAssignmentServiceApi;
import uk.gov.hmcts.reform.hmc.api.restclient.ServiceAuthorisationTokenApi;

@SpringBootTest
@ExtendWith({MockitoExtension.class})
@Ignore
public class RoleAssignmentServiceTest {

    @InjectMocks RoleAssignmentServiceImpl roleAssignmentService;

    @Mock IdamAuthService idamAuthService;

    @Mock IdamTokenGenerator idamTokenGenerator;

    @Mock ServiceAuthorisationTokenApi serviceAuthorisationTokenApi;

    @Mock RoleAssignmentServiceApi roleAssignmentServiceApi;
}
