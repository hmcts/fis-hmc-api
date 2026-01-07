package uk.gov.hmcts.reform.hmc.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static uk.gov.hmcts.reform.hmc.api.config.CacheConfiguration.LOCAL_CACHE_MANAGER;
import static uk.gov.hmcts.reform.hmc.api.config.CacheConfiguration.REF_DATA_USER_CACHE;

@Service
@Slf4j
@EnableCaching
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

    @Cacheable(cacheManager = LOCAL_CACHE_MANAGER, cacheNames = REF_DATA_USER_CACHE, unless = "#result == null",
        key = "#root.target.REF_DATA_USER_TOKEN_CACHE_KEY")
    public String generateIdamTokenForRefData() {
        return idamClient.getAccessToken(refDataUserName, refDataPassword);
    }

    public String getSysUserToken() {
        return idamClient.getAccessToken(sysUsername, sysPassword);
    }

    public String generateIdamTokenForHearingCftData() {
        return idamClient.getAccessToken(hearingCftUserName, hearingCftPassword);
    }
}
