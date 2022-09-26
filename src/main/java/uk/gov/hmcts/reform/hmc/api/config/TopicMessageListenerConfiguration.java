package uk.gov.hmcts.reform.hmc.api.config;

import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

@Slf4j
@Configuration
public class TopicMessageListenerConfiguration {
    @Bean
    public Consumer<Message<String>> consume() {
        return message -> {
            String messagePayload = message.getPayload();
            log.info("Message:" + messagePayload);
        };
    }
}
