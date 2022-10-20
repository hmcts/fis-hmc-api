package uk.gov.hmcts.reform.hmc.api.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.Disposable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
public class TopicMessageListenerConfiguration {

    private static Logger log = LoggerFactory.getLogger(TopicMessageListenerConfiguration.class);

    @Bean
    public void run() throws InterruptedException {
        AtomicBoolean sampleSuccessful = new AtomicBoolean(true);
        CountDownLatch countdownLatch = new CountDownLatch(1);
        String connectionString = "dummy";

        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .disableAutoComplete()
            .topicName("hmc-to-cft-demo")
            .subscriptionName("hmc-subs-to-cft-demo")
            .buildAsyncClient();

        Disposable subscription = receiver.receiveMessages()
            .flatMap(message -> {
                boolean messageProcessed = processMessage(message);
                if (messageProcessed) {
                    return receiver.complete(message);
                } else {
                    return receiver.abandon(message);
                }
            }).subscribe(
                (ignore) -> log.info("Message processed."),
                error -> sampleSuccessful.set(false)
            );

        countdownLatch.await(10, TimeUnit.SECONDS);
        receiver.close();

    }

    private static boolean processMessage(ServiceBusReceivedMessage message) {
        log.info("Sequence #: %s. Contents: %s%n", message.getSequenceNumber(),
                          message.getBody());

        return true;
    }
}
