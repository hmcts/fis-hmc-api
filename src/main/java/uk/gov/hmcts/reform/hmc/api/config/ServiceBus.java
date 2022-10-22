package uk.gov.hmcts.reform.hmc.api.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
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
    }
}
