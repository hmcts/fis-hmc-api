package uk.gov.hmcts.reform.hmc.api;

import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import uk.gov.hmcts.reform.hmc.api.config.OpenAPIConfiguration;

@Slf4j
@EnableFeignClients(
        basePackages = {
            "uk.gov.hmcts.reform.hmc.api",
            "uk.gov.hmcts.reform.idam.client",
        })
@SpringBootApplication(
        scanBasePackages = {
            "uk.gov.hmcts.ccd.sdk",
            "uk.gov.hmcts.reform.hmc.api",
            "uk.gov.hmcts.reform.idam"
        })
@Import(OpenAPIConfiguration.class)
@SuppressWarnings(
        "HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public Consumer<Message<String>> consume() {
        return message -> {
            log.info("New message received: " + message.getPayload());
        };
    }
}