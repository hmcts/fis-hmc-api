package uk.gov.hmcts.reform.hmc.api.config;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.HMCTS_SERVICE_ID;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.LISTED;

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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.enums.State;
import uk.gov.hmcts.reform.hmc.api.model.ccd.NextHearingDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.Hearing;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingDTO;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingUpdateDTO;
import uk.gov.hmcts.reform.hmc.api.model.request.NextHearingDetailsDTO;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.services.HearingsService;
import uk.gov.hmcts.reform.hmc.api.services.NextHearingDetailsService;
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

    @Autowired NextHearingDetailsService nextHearingDetailsService;

    @Autowired HearingsService hearingsService;

    @Autowired IdamTokenGenerator idamTokenGenerator;

    @Autowired AuthTokenGenerator authTokenGenerator;

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

                        Hearing hearing = mapper.readValue(body.get(0), Hearing.class);

                        if (HMCTS_SERVICE_ID.equals(hearing.getHmctsServiceCode())) {
                            HearingUpdateDTO hearingUpdateDto =
                                    HearingUpdateDTO.hearingUpdateRequestDTOWith()
                                            .hearingResponseReceivedDateTime(
                                                    hearing.getHearingUpdate()
                                                            .getHearingResponseReceivedDateTime())
                                            .hearingEventBroadcastDateTime(
                                                    hearing.getHearingUpdate()
                                                            .getHearingEventBroadcastDateTime())
                                            .hearingListingStatus(
                                                    hearing.getHearingUpdate()
                                                            .getHearingListingStatus())
                                            .nextHearingDate(
                                                    hearing.getHearingUpdate().getNextHearingDate())
                                            .hearingVenueId(
                                                    hearing.getHearingUpdate().getHearingVenueId())
                                            .hearingJudgeId(
                                                    hearing.getHearingUpdate().getHearingJudgeId())
                                            .hearingRoomId(
                                                    hearing.getHearingUpdate().getHearingRoomId())
                                            .hmcStatus(hearing.getHearingUpdate().getHmcStatus())
                                            .listAssistCaseStatus(
                                                    hearing.getHearingUpdate()
                                                            .getListAssistCaseStatus())
                                            .build();

                            HearingDTO hearingDto =
                                    HearingDTO.hearingRequestDTOWith()
                                            .hmctsServiceCode(hearing.getHmctsServiceCode())
                                            .caseRef(hearing.getCaseRef())
                                            .hearingId(hearing.getHearingID())
                                            .hearingUpdate(hearingUpdateDto)
                                            .build();

                            log.info("Service Bus message for PRL Update " + hearingDto);

                            if (hearingDto.getHearingUpdate().getHearingVenueId() != null
                                    && LISTED.equals(
                                            hearingDto.getHearingUpdate().getHmcStatus())) {

                                log.info(
                                        "VenueId --> "
                                                + hearing.getHearingUpdate().getHearingVenueId());
                                hearingDto = refDataService.getHearingWithCourtDetails(hearingDto);
                                log.info(
                                        "Hearing with Full CourtDetails  "
                                                + hearingDto
                                                        .getHearingUpdate()
                                                        .getHearingVenueName());
                            }
                            String userToken = idamTokenGenerator.getSysUserToken();
                            String serviceToken = authTokenGenerator.generate();

                            Hearings hearings =
                                    hearingsService.getHearingsByCaseRefNo(
                                            hearingDto.getCaseRef(), userToken, serviceToken);
                            State caseState = null;
                            if (hearings != null) {
                                NextHearingDetails nextHearingDetails =
                                        nextHearingDetailsService.getNextHearingDate(hearings);
                                if (nextHearingDetails != null) {
                                    log.info("Next Hearing details " + nextHearingDetails);
                                    NextHearingDetailsDTO nextHearingDetailsDTO =
                                            NextHearingDetailsDTO.nextHearingDetailsRequestDTOWith()
                                                    .nextHearingDetails(nextHearingDetails)
                                                    .caseRef(hearings.getCaseRef())
                                                    .build();

                                    hearingDto.setNextHearingDateRequest(nextHearingDetailsDTO);
                                }
                                caseState =
                                        nextHearingDetailsService.fetchStateForUpdate(
                                                hearings,
                                                hearingDto.getHearingUpdate().getHmcStatus());
                                Boolean isPrlSuccess = false;
                                isPrlSuccess =
                                        prlUpdateService.updatePrlServiceWithHearing(
                                                hearingDto, caseState);
                                if (isPrlSuccess) {
                                    return receiveClient.completeAsync(message.getLockToken());
                                }
                            }
                            return receiveClient.abandonAsync(message.getLockToken());
                        }
                        return receiveClient.completeAsync(message.getLockToken());
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
