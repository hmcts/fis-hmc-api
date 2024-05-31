package uk.gov.hmcts.reform.hmc.api.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.hmc.api.services.IdamAuthService;
import uk.gov.hmcts.reform.hmc.api.services.RoleAssignmentService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("test")
public class HearingControllerRoleAssignmentTest {

    @InjectMocks
    private HearingsController hearingsController;

    @Mock private IdamAuthService idamAuthService;

    @Mock
    private RoleAssignmentService roleAssignmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void assignRoleTest() {
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.TRUE);
        Mockito.when(roleAssignmentService.assignHearingRoleToSysUser())
            .thenReturn(new ResponseEntity<>(any(), HttpStatus.OK));
        ResponseEntity<Object> response = hearingsController.assignRole("");
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void assignRoleThrowUnauthorizedExceptionTest() {
        Mockito.when(idamAuthService.authoriseUser(any())).thenReturn(Boolean.FALSE);
        Throwable exception = assertThrows(ResponseStatusException.class, () -> hearingsController.assignRole("Auth"));
        Assertions.assertEquals("401 UNAUTHORIZED", exception.getMessage());
    }
}
