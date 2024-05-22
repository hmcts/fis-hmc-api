package uk.gov.hmcts.reform.hmc.api.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

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
    void authoriseWhenTheServiceIsCalledFromPaymentAndS2sAuthorisedServices() {
        ReflectionTestUtils.setField(idamAuthService, "s2sAuthorisedServices", "test,payment_api");
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("payment_api");
        assertTrue(idamAuthService.authoriseService("Bearer abcasda"));
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

    @Test
    void getUserDetailsTest() {
        when(idamClient.getUserDetails(any()))
            .thenReturn(UserDetails.builder().id(UUID.randomUUID().toString()).forename("forenameTest").surname("surnameTest").build());
        UserDetails userDetails = idamAuthService.getUserDetails("Bearer abcasda");
        assertNotNull(userDetails);
        assertEquals("forenameTest", userDetails.getForename());
        assertEquals("surnameTest", userDetails.getSurname().get());

    }
}
