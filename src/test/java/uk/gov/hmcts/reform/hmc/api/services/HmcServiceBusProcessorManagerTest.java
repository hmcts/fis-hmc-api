package uk.gov.hmcts.reform.hmc.api.services;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.api.config.launchdarkly.LaunchDarklyClient;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HmcServiceBusProcessorManagerTest {
    public static final String HMC_SERVICEBUS_ENABLED = "hmc-servicebus-enabled";
    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Mock
    private ServiceBusProcessorClient processorClient;

    @InjectMocks
    private HmcServiceBusProcessorManager scheduler;

    @Test
    void shouldStartProcessorWhenFeatureEnabledAndProcessorNotRunning() {
        when(launchDarklyClient.isFeatureEnabled(HMC_SERVICEBUS_ENABLED))
            .thenReturn(true);
        when(processorClient.isRunning()).thenReturn(false);

        scheduler.refreshProcessorState();

        verify(processorClient).start();
        verify(processorClient, never()).stop();
    }

    @Test
    void shouldStopProcessorWhenFeatureDisabledAndProcessorRunning() {
        when(launchDarklyClient.isFeatureEnabled(HMC_SERVICEBUS_ENABLED))
            .thenReturn(false);
        when(processorClient.isRunning()).thenReturn(true);

        scheduler.refreshProcessorState();

        verify(processorClient).stop();
        verify(processorClient, never()).start();
    }

    @Test
    void shouldDoNothingWhenFeatureEnabledAndProcessorAlreadyRunning() {
        when(launchDarklyClient.isFeatureEnabled(HMC_SERVICEBUS_ENABLED))
            .thenReturn(true);
        when(processorClient.isRunning()).thenReturn(true);

        scheduler.refreshProcessorState();

        verify(processorClient, never()).start();
        verify(processorClient, never()).stop();
    }

    @Test
    void shouldDoNothingWhenFeatureDisabledAndProcessorAlreadyStopped() {
        when(launchDarklyClient.isFeatureEnabled(HMC_SERVICEBUS_ENABLED))
            .thenReturn(false);
        when(processorClient.isRunning()).thenReturn(false);

        scheduler.refreshProcessorState();

        verify(processorClient, never()).start();
        verify(processorClient, never()).stop();
    }
}
