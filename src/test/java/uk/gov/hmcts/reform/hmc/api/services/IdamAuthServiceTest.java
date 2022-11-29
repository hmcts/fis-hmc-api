package uk.gov.hmcts.reform.hmc.api.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@ExtendWith(SpringExtension.class)
public class IdamAuthServiceTest {

    @InjectMocks IdamAuthService idamAuthService;

    @Mock ServiceAuthorisationApi serviceAuthorisationApi;

    @Mock IdamClient idamClient;

    @Test
    public void authoriseWhenTheServiceIsCalledFromPayment() {
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("payment_api");
        assertFalse(idamAuthService.authoriseService("Bearer abcasda"));
    }

    @Test
    public void doNotAuthoriseWhenTheServiceIsCalledFromUnknownApi() {
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("unknown_api");
        assertFalse(idamAuthService.authoriseService("Bearer abc"));
    }

    @Test
    public void throwUnAuthorisedExceptionWhenS2sTokenIsMalformed() {
        assertFalse(idamAuthService.authoriseService("Bearer malformed"));
    }

    @Test
    public void throwNullPointerAuthServiceException() {
        when(idamAuthService.authoriseService(null)).thenThrow(NullPointerException.class);
        assertFalse(idamAuthService.authoriseService(null));
    }

    @Test
    public void throwNullPointerAuthUserException() {
        when(idamAuthService.authoriseUser(null)).thenThrow(NullPointerException.class);
        assertFalse(idamAuthService.authoriseUser(null));
    }

    @Test
    public void authoriseUserTheServiceIsCalledWithValidToken() {
        when(idamClient.getUserInfo(any()))
                .thenReturn(UserInfo.builder().uid(UUID.randomUUID().toString()).build());
        assertTrue(idamAuthService.authoriseUser("Bearer abcasda"));
    }

    @Test
    public void doNotAuthoriseUserWhenCalledWithInvalidToken() {
        assertFalse(idamAuthService.authoriseUser("Bearer malformed"));
    }
}
