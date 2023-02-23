package uk.gov.hmcts.reform.hmc.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@Service
@Slf4j
public class IdamTokenGenerator {

    @Value("${idam.refDataUserAuth.username}")
    private String refDataUserName;

    @Value("${idam.refDataUserAuth.password}")
    private String refDataPassword;

    @Value("${idam.system-update.username}")
    private String sysUsername;

    @Value("${idam.system-update.password}")
    private String sysPassword;

    @Autowired IdamClient idamClient;

    public String generateIdamTokenForRefData() {
        return idamClient.getAccessToken(refDataUserName, refDataPassword);
    }

    public String getSysUserToken() {
        return idamClient.getAccessToken(sysUsername, sysPassword);
    }
}
