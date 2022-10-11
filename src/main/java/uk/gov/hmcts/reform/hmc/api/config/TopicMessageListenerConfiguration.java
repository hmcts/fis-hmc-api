package uk.gov.hmcts.reform.hmc.api.config;

import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

@Slf4j
@Configuration
public class TopicMessageListenerConfiguration {
    @Value("${spring.cloud.azure.servicebus.connection-string}")
    private String connectionString;
    @Value("${spring.cloud.stream.bindings.consume-in.destination}")
    private String topicName;
    @Value("${spring.cloud.stream.bindings.consume-in.group}")
    private String subscriptionName;
    @Bean
    public Consumer<Message<String>> consume() {
        return message -> {
            String messagePayload = message.getPayload();
            log.info("Message:" + messagePayload);
            log.info("connection string : " + connectionString);
            log.info("topic name : " + connectionString);
            log.info("topic name : " + subscriptionName);
        };
    }
}
