package uk.gov.hmcts.reform.hmc.api.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ADJOURNED;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CANCELLED;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.COMPLETED;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.HMCTS_SERVICE_ID;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.LISTED;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.POSTPONED;

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
    private ServiceBusProcessorClient processorClient;

    @Bean
    @Profile("!test")
    public ServiceBusProcessorClient serviceBusProcessorClient() throws URISyntaxException{
        URI endpoint = new URI("sb://" + host);
        String connectionString = "Endpoint=" + endpoint + "/"
            + ";SharedAccessKeyName=" + sharedAccessKeyName
            + ";SharedAccessKey=" + sharedAccessKeyValue;

        processorClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .processor()
            .topicName(topic)
            .subscriptionName(subscription)
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .processMessage(this::processMessage)
            .processError(this::processError)
            .buildProcessorClient();

        processorClient.start();
        log.info("HMC ServiceBusProcessorClient started successfully.");

        return processorClient;
    }

    private void processMessage(ServiceBusReceivedMessageContext context) {
        try {
            ServiceBusReceivedMessage message = context.getMessage();

            byte[] body = message.getBody().toBytes();
            ObjectMapper mapper = new ObjectMapper();

            Hearing hearing = mapper.readValue(body, Hearing.class);

            if (HMCTS_SERVICE_ID.equals(hearing.getHmctsServiceCode())
                && isHearingStateConsumptionRequired(hearing.getHearingUpdate().getHmcStatus())) {
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
                        "VenueId "
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
                        context.complete();
                    }
                }
                context.abandon();
            }
            context.complete();
        } catch (Exception e) {
            log.error("There was a problem processing the message: {}", e.getMessage(), e);
            context.abandon();
        }
    }

    private void processError(ServiceBusErrorContext context) {
        log.error("Exception occurred while processing message");
        log.error("{} - {}", context.getErrorSource(), context.getException().getMessage(), context.getException());
    }

    private boolean isHearingStateConsumptionRequired(String hearingStatus) {
        List<String> allowedHmcStatus = List.of(
            LISTED,
            COMPLETED,
            POSTPONED,
            ADJOURNED,
            CANCELLED
        );
        if (allowedHmcStatus.contains(hearingStatus)) {
            return true;
        }
        return false;
    }
}
