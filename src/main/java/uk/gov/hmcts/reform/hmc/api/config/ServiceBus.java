package uk.gov.hmcts.reform.hmc.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceBus {

    @Bean
    public void run() throws InterruptedException {
        System.out.println("test");
        String connectionString = "dummy";
        String topicName = "hmc-to-cft-aat";
        String subscriptionName = "hmc-subs-to-cft-aat";
    }
}
