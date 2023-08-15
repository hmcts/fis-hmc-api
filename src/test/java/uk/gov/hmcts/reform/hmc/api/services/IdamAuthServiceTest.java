package uk.gov.hmcts.reform.hmc.api.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class IdamAuthServiceTest {

    @InjectMocks IdamAuthService idamAuthService;

    @Mock ServiceAuthorisationApi serviceAuthorisationApi;

    @Mock IdamClient idamClient;

    @Test
    void authoriseWhenTheServiceIsCalledFromPayment() {
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("payment_api");
        assertFalse(idamAuthService.authoriseService("Bearer abcasda"));
    }

    @Test
    void doNotAuthoriseWhenTheServiceIsCalledFromUnknownApi() {
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("unknown_api");
        assertFalse(idamAuthService.authoriseService("Bearer abc"));
    }

    @Test
    void throwUnAuthorisedExceptionWhenS2sTokenIsMalformed() {
        assertFalse(idamAuthService.authoriseService("Bearer malformed"));
    }

    @Test
    void throwNullPointerAuthServiceException() {
        when(idamAuthService.authoriseService(null)).thenThrow(NullPointerException.class);
        assertFalse(idamAuthService.authoriseService(null));
    }

    @Test
    void throwNullPointerAuthUserException() {
        when(idamAuthService.authoriseUser(null)).thenThrow(NullPointerException.class);
        assertFalse(idamAuthService.authoriseUser(null));
    }

    @Test
    void authoriseUserTheServiceIsCalledWithValidToken() {
        when(idamClient.getUserInfo(any()))
                .thenReturn(UserInfo.builder().uid(UUID.randomUUID().toString()).build());
        assertTrue(idamAuthService.authoriseUser("Bearer abcasda"));
    }

    @Test
    void doNotAuthoriseUserWhenCalledWithInvalidToken() {
        assertFalse(idamAuthService.authoriseUser("Bearer malformed"));
    }
}
