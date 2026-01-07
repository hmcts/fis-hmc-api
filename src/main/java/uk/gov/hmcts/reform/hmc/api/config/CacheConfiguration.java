package uk.gov.hmcts.reform.hmc.api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;

@Configuration
public class CacheConfiguration {

    public static final String LOCAL_CACHE_MANAGER = "localCacheManager";
    public static final String REF_DATA_USER_CACHE = "refDataUserCache";

    public static final int IDAM_TOKEN_CACHE_EXPIRY = 120;

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_APPLICATION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public CacheManager localCacheManager() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(REF_DATA_USER_CACHE);
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
                                             .initialCapacity(10)
                                             .maximumSize(100)
                                             .expireAfterWrite(Duration.ofMinutes(IDAM_TOKEN_CACHE_EXPIRY)));
        return caffeineCacheManager;
    }

}
