package uk.gov.hmcts.reform.hmc.api.config;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.hmc.api.config.CacheConfiguration.REF_DATA_USER_CACHE;
import static uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator.REF_DATA_USER_TOKEN_CACHE_KEY;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {IdamTokenGenerator.class, CacheConfiguration.class})
class IdamTokenGeneratorTest {

    private static final String SYS_USER_NAME = "dummy";
    private static final String SYS_USER_PASS = "dummy";

    @MockBean
    private IdamClient idamClient;

    @Autowired
    @Qualifier("localCacheManager")
    private CacheManager localCacheManager;

    @Autowired
    private IdamTokenGenerator idamTokenGenerator;


    @Test
    void shouldGetCachedTokenIfValid() {
        String token = RandomStringUtils.randomAlphanumeric(10);

        given(idamClient.getAccessToken(SYS_USER_NAME, SYS_USER_PASS)).willReturn(token);
        String retrieved = idamTokenGenerator.generateIdamTokenForRefData();
        String retrievedSecond = idamTokenGenerator.generateIdamTokenForRefData();

        // should get first time, then get cached version
        verify(idamClient, times(1)).getAccessToken(SYS_USER_NAME, SYS_USER_PASS);

        // ensure the token is the same all the time
        assertThat(retrieved).isEqualTo(token);
        assertThat(retrievedSecond).isEqualTo(token);
        assertThat(getCachedToken()).isEqualTo(token);
    }

    private String getCachedToken() {
        return localCacheManager.getCache(REF_DATA_USER_CACHE).get(REF_DATA_USER_TOKEN_CACHE_KEY, String.class);
    }

}
