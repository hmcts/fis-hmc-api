package uk.gov.hmcts.reform.hmc.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

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
@SuppressWarnings(
        "HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableAsync
public class Application {
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
