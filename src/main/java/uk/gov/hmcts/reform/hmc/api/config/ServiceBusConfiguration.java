package uk.gov.hmcts.reform.hmc.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingsRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
public class ServiceBusConfiguration {

    @Value("${amqp.host}")
    private String host;

    @Value("${amqp.sharedAccessKeyName}")
    private String sharedAccessKeyName;

    @Value("${amqp.jrd.topic}")
    private String topic;

    @Value("${amqp.jrd.sharedAccessKeyValue}")
    private String sharedAccessKeyValue;

    @Value("${amqp.jrd.subscription}")
    private String subscription;

    @Value("${thread.count}")
    private int threadCount;

    private static Logger log = LoggerFactory.getLogger(ServiceBusConfiguration.class);

    @Bean
    public SubscriptionClient receiveClient() throws URISyntaxException, ServiceBusException,
        InterruptedException {
        log.info(" host {}",host);
        log.info(" sharedAccessKeyName {}",sharedAccessKeyName);
        log.info(" topic {}",topic);
        log.info(" sharedAccessKeyValue {}",sharedAccessKeyValue);
        log.info(" subscription {}",subscription);
        URI endpoint = new URI("sb://" + host);

        String destination = topic.concat("/subscriptions/").concat(subscription);

        ConnectionStringBuilder connectionStringBuilder = new ConnectionStringBuilder(
            endpoint,
            destination,
            sharedAccessKeyName,
            sharedAccessKeyValue);
        connectionStringBuilder.setOperationTimeout(Duration.ofMinutes(10));
        return new SubscriptionClient(connectionStringBuilder, ReceiveMode.PEEKLOCK);
    }

    @Bean
    CompletableFuture<Void> registerMessageHandlerOnClient(@Autowired SubscriptionClient receiveClient)
        throws ServiceBusException, InterruptedException {

        IMessageHandler messageHandler = new IMessageHandler() {

            @SneakyThrows
            @Override
            public CompletableFuture<Void> onMessageAsync(IMessage message) {
                log.info("RECEIVED");
                List<byte[]> body = message.getMessageBody().getBinaryData();
                log.info("Received message" + body);
                AtomicBoolean result = new AtomicBoolean();
                ObjectMapper mapper = new ObjectMapper();
                String message1 = mapper.writeValueAsString(mapper.readValue(body.get(0), HearingsRequest.class));
                log.info(message1);
                result.set(true);
                if (result.get()) {
                    return receiveClient.completeAsync(message.getLockToken());
                }
                else
                    return null;
            }

            @Override
            public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
                log.error("Exception occurred.");
                log.error(exceptionPhase + "-" + throwable.getMessage());
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        receiveClient.registerMessageHandler(
            messageHandler, new MessageHandlerOptions(threadCount,
                                                      false, Duration.ofHours(1), Duration.ofMinutes(5)),
            executorService);
        return null;
    }
}
