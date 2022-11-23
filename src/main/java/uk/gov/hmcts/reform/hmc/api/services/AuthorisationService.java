package uk.gov.hmcts.reform.hmc.api.services;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthorisationService {

    private final ServiceAuthorisationApi serviceAuthorisationApi;

    @Value("${private-law.hmc-authorised-services}")
    private String s2sAuthorisedServices;

    private final IdamClient idamClient;

    private UserInfo userInfo;

    public Boolean authoriseService(String serviceAuthHeader) {
        String callingService;
        try {
            callingService = serviceAuthorisationApi.getServiceName(serviceAuthHeader);
            if (callingService != null
                    && Arrays.asList(s2sAuthorisedServices.split(",")).contains(callingService)) {
                return true;
            }
        } catch (Exception ex) {
            // do nothing
            log.error("S2S token is not authorised");
        }
        return false;
    }

    public Boolean authoriseUser(String authorisation) {
        try {
            userInfo = idamClient.getUserInfo(authorisation);
            if (null != userInfo) {
                return true;
            }
        } catch (Exception ex) {
            // do nothing
            log.error("User token is invalid");
        }
        return false;
    }
}
