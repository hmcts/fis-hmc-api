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

    @Value("${idam.hearingCftUserAuth.username}")
    private String hearingCftUserName;

    @Value("${idam.hearingCftUserAuth.password}")
    private String hearingCftPassword;

    @Autowired IdamClient idamClient;

    public String generateIdamTokenForRefData() {
        return idamClient.getAccessToken(refDataUserName, refDataPassword);
    }

    public String generateIdamTokenForHearingCftData() {
        return idamClient.getAccessToken(hearingCftUserName, hearingCftPassword);
    }
}
