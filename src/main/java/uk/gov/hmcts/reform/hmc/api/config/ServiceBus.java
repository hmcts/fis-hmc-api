package uk.gov.hmcts.reform.hmc.api.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.Disposable;

@Configuration
public class ServiceBus {

    private static final Logger log = LoggerFactory.getLogger(ServiceBus.class);

    @Bean
    public void run() throws InterruptedException {

        String connectionString = "dummy";
        String topicName = "hmc-to-cft-aat";
        String subscriptionName = "hmc-subs-to-cft-aat";

        log.info("Connection String  : ", connectionString);
        log.info("Topic name   : ", topicName);
        log.info("subscription  : ", subscriptionName);

        AtomicBoolean sampleSuccessful = new AtomicBoolean(true);
        CountDownLatch countdownLatch = new CountDownLatch(1);
        ServiceBusReceiverAsyncClient receiver =
                new ServiceBusClientBuilder()
                        .connectionString(connectionString)
                        .receiver()
                        .disableAutoComplete()
                        .topicName(topicName)
                        .subscriptionName(subscriptionName)
                        .buildAsyncClient();

        Disposable subscription =
                receiver.receiveMessages()
                        .flatMap(
                                message -> {
                                    boolean messageProcessed = processMessage(message);
                                    if (messageProcessed) {
                                        return receiver.complete(message);
                                    } else {
                                        return receiver.abandon(message);
                                    }
                                })
                        .subscribe(
                                (ignore) -> System.out.println("Message processed."),
                                error -> sampleSuccessful.set(false));

        countdownLatch.await(10, TimeUnit.SECONDS);
    }

    private static boolean processMessage(ServiceBusReceivedMessage message) {
        System.out.printf(
                "Sequence #: %s. Contents: %s%n", message.getSequenceNumber(), message.getBody());

        return true;
    }
}
