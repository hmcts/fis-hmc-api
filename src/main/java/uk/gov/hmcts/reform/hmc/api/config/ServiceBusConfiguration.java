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
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.gov.hmcts.reform.hmc.api.model.request.Hearing;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingUpdate;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;
import uk.gov.hmcts.reform.hmc.api.services.PrlUpdateService;
import uk.gov.hmcts.reform.hmc.api.services.RefDataService;

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

    @Autowired PrlUpdateService prlUpdateService;

    @Autowired RefDataService refDataService;

    private static Logger log = LoggerFactory.getLogger(ServiceBusConfiguration.class);

    @Bean
    @Profile("!test")
    public SubscriptionClient receiveClient()
            throws URISyntaxException, ServiceBusException, InterruptedException {
        log.debug(" host {}", host);
        log.debug(" sharedAccessKeyName {}", sharedAccessKeyName);
        log.debug(" topic {}", topic);
        log.debug(" sharedAccessKeyValue {}", sharedAccessKeyValue);
        log.debug(" subscription {}", subscription);
        URI endpoint = new URI("sb://" + host);

        String destination = topic.concat("/subscriptions/").concat(subscription);

        ConnectionStringBuilder connectionStringBuilder =
                new ConnectionStringBuilder(
                        endpoint, destination, sharedAccessKeyName, sharedAccessKeyValue);
        connectionStringBuilder.setOperationTimeout(Duration.ofMinutes(10));
        return new SubscriptionClient(connectionStringBuilder, ReceiveMode.PEEKLOCK);
    }

    @Bean
    @Profile("!test")
    CompletableFuture<Void> registerMessageHandlerOnClient(
            @Autowired SubscriptionClient receiveClient)
            throws ServiceBusException, InterruptedException {

        IMessageHandler messageHandler =
                new IMessageHandler() {

                    @SneakyThrows
                    @Override
                    public CompletableFuture<Void> onMessageAsync(IMessage message) {
                        List<byte[]> body = message.getMessageBody().getBinaryData();
                        ObjectMapper mapper = new ObjectMapper();
                        String messageReceived =
                                mapper.writeValueAsString(
                                        mapper.readValue(body.get(0), Hearing.class));
                        Hearing hearing = mapper.readValue(body.get(0), Hearing.class);
                        if (hearing.getHearingUpdate().getHearingVenueId() != null) {
                            log.info("VenueId " + hearing.getHearingUpdate().getHearingVenueId());

                            CourtDetail courtDetail =
                                    refDataService.getCourtDetails(
                                            hearing.getHearingUpdate().getHearingVenueId());

                            if (courtDetail != null) {
                                log.info(
                                        "courtDetailllllllls " + courtDetail.getHearingVenueName());
                                HearingUpdate hearingUpdate = hearing.getHearingUpdate();
                                hearingUpdate.setHearingVenueName(
                                        courtDetail.getHearingVenueName());
                                hearingUpdate.setHearingVenueAddress(
                                        courtDetail.getHearingVenueAddress());
                                hearingUpdate.setHearingVenueLocationCode(
                                        courtDetail.getHearingVenueLocationCode());
                                hearingUpdate.setCourtTypeId(courtDetail.getCourtTypeId());
                                hearing.hearingRequestWith().hearingUpdate(hearingUpdate).build();
                            } else {
                                return receiveClient.abandonAsync(message.getLockToken());
                            }
                        } else {
                            return receiveClient.abandonAsync(message.getLockToken());
                        }

                        Boolean isPrlSuccess =
                                prlUpdateService.updatePrlServiceWithHearing(hearing);
                        if (isPrlSuccess) {
                            return receiveClient.completeAsync(message.getLockToken());
                        } else {
                            return receiveClient.abandonAsync(message.getLockToken());
                        }
                    }

                    @Override
                    public void notifyException(
                            Throwable throwable, ExceptionPhase exceptionPhase) {
                        log.error("Exception occurred.");
                        log.error(exceptionPhase + "-" + throwable.getMessage());
                    }
                };

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        receiveClient.registerMessageHandler(
                messageHandler,
                new MessageHandlerOptions(
                        threadCount, false, Duration.ofHours(1), Duration.ofMinutes(5)),
                executorService);
        return null;
    }
}
