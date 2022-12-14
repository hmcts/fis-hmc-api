package uk.gov.hmcts.reform.hmc.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;

@Configuration
public class ServiceAuthTokenConfiguration {
    @Bean
    public AuthTokenGenerator authTokenGenerator(
            @Value("${idam.s2s-auth.totp_secret}") final String secret,
            @Value("${idam.s2s-auth.microservice}") final String microservice,
            ServiceAuthorisationApi serviceAuthorisationApi) {
        return AuthTokenGeneratorFactory.createDefaultGenerator(
                secret, microservice, serviceAuthorisationApi);
    }
}
