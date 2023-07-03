package uk.gov.hmcts.reform.hmc.api.services;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class IdamAuthService {

    private final ServiceAuthorisationApi serviceAuthorisationApi;

    @Value("${private-law.hmc-authorised-services}")
    private String s2sAuthorisedServices;

    private final IdamClient idamClient;

    private UserInfo userInfo;

    public Boolean authoriseService(String serviceAuthHeader) {
        log.info("calling authoriseService..");

        String callingService;
        try {
            callingService = serviceAuthorisationApi.getServiceName(serviceAuthHeader);
            if (callingService != null
                    && Arrays.asList(s2sAuthorisedServices.split(",")).contains(callingService)) {
                return true;
            }
        } catch (Exception ex) {
            log.error("S2S token is not authorised"); // do nothing
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
            log.error("User token is invalid"); // do nothing
        }
        return false;
    }

    public UserDetails getUserDetails(String authorisation) {
        return idamClient.getUserDetails(authorisation);
    }
}
