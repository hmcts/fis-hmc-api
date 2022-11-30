package uk.gov.hmcts.reform.hmc.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@Service
public class IdamTokenGenerator {

    @Value("${idam.refDataUserAuth.username}")
    private String refDataUserName;

    @Value("${idam.refDataUserAuth.password}")
    private String refDataPassword;

    @Autowired IdamClient idamClient;

    public String generateIdamTokenForRefData() {
        return idamClient.getAccessToken(refDataUserName, refDataPassword);
    }
}