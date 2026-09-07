package uk.gov.hmcts.reform.hmc.api.config;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    ServiceBusProcessorClient serviceBusProcessorClient() {
        return mock(ServiceBusProcessorClient.class);
    }
}
