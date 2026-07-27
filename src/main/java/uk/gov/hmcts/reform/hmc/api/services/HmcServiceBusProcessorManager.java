package uk.gov.hmcts.reform.hmc.api.services;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.api.config.launchdarkly.LaunchDarklyClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class HmcServiceBusProcessorManager {
    private final LaunchDarklyClient launchDarklyClient;
    private final ServiceBusProcessorClient processorClient;

    @Scheduled(fixedDelayString = "${hmc.servicebus.flag-check-delay:30000}")
    public void refreshProcessorState() {
        boolean enabled = launchDarklyClient.isFeatureEnabled("hmc-servicebus-enabled");

        if (enabled && !processorClient.isRunning()) {
            log.info("Starting HMC Service Bus processor");
            processorClient.start();
        } else if (!enabled && processorClient.isRunning()) {
            log.info("Stopping HMC Service Bus processor");
            processorClient.stop();
        }
    }
}
