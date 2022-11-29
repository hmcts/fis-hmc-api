package uk.gov.hmcts.reform.hmc.api.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@RunWith(MockitoJUnitRunner.class)
public class IdamAuthServiceTest {

    @InjectMocks AuthorisationService authorisationService;

    @Mock ServiceAuthorisationApi serviceAuthorisationApi;

    @Mock IdamClient idamClient;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(authorisationService, "s2sAuthorisedServices", "payment_api");
    }

    @Test
    public void authoriseWhenTheServiceIsCalledFromPayment() {
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("payment_api");
        assertTrue(authorisationService.authoriseService("Bearer abcasda"));
    }

    @Test
    public void doNotAuthoriseWhenTheServiceIsCalledFromUnknownApi() {
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("unknown_api");
        assertFalse(authorisationService.authoriseService("Bearer abc"));
    }

    @Test
    public void throwUnAuthorisedExceptionWhenS2sTokenIsMalformed() {
        assertFalse(authorisationService.authoriseService("Bearer malformed"));
    }

    @Test
    public void throwNullPointerAuthServiceException() {
        when(authorisationService.authoriseService(null)).thenThrow(NullPointerException.class);
        assertFalse(authorisationService.authoriseService(null));
    }

    @Test
    public void throwNullPointerAuthUserException() {
        when(authorisationService.authoriseUser(null)).thenThrow(NullPointerException.class);
        assertFalse(authorisationService.authoriseUser(null));
}

    @Test
    public void authoriseUserTheServiceIsCalledWithValidToken() {
        when(idamClient.getUserInfo(any()))
                .thenReturn(UserInfo.builder().uid(UUID.randomUUID().toString()).build());
        assertTrue(authorisationService.authoriseUser("Bearer abcasda"));
    }

    @Test
    public void doNotAuthoriseUserWhenCalledWithInvalidToken() {
        assertFalse(authorisationService.authoriseUser("Bearer malformed"));
    }

}
