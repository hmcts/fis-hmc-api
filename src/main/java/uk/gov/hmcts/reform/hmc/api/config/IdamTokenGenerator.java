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

    @Value("${idam.hearingCftUserAuth.username}")
    private String hearingCftUserName;

    @Value("${idam.hearingCftUserAuth.password}")
    private String hearingCftPassword;

    @Autowired IdamClient idamClient;

    public String generateIdamTokenForRefData() {

        log.info("hmc 1111 {}", refDataUserName);
        log.info("hmc 2222 {}", refDataPassword);

        return idamClient.getAccessToken(refDataUserName, refDataPassword);
    }

    public String getSysUserToken() {
        log.info("hmc 3333 {}", sysUsername);
        log.info("hmc 4444 {}", sysPassword);
        return idamClient.getAccessToken(sysUsername, sysPassword);
    }

    public String generateIdamTokenForHearingCftData() {
        log.info("hmc 5555 {}", hearingCftUserName);
        log.info("hmc 6666 {}", hearingCftPassword);
        return idamClient.getAccessToken(hearingCftUserName, hearingCftPassword);
    }
}
