package uk.gov.hmcts.reform.hmc.api.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Provider contract check for HMC API pacts using FIS test fixtures.
 *
 * <p>FIS does not own these production endpoints. This test intentionally verifies the pact against
 * test-only fixtures so that Family teams can see changes to the shared HMC contract without
 * exercising FIS production endpoints.</p>
 */
@Provider("hmcHearingServiceProvider")
@PactBroker(
    scheme = "${PACT_BROKER_SCHEME:http}",
    host = "${PACT_BROKER_URL:localhost}",
    port = "${PACT_BROKER_PORT:}",
    consumers = {"civil_service"},
    consumerVersionSelectors = {@VersionSelector(tag = "master", consumer = "civil_service")}
)
@IgnoreNoPactsToVerify
class HmcProviderContractTest {

    private static final String HEARING_ID = "2000000000000000";
    private static final String CASE_REFERENCE = "1671000000000018";
    private static final String SERVICE_CODE = "AAA7";
    private static final String HEARING_START = "2024-10-20T09:30";
    private static final String HEARING_END = "2024-10-20T11:30";

    private HmcFixtureService fixtureService;

    @BeforeEach
    void setUp(PactVerificationContext context) {
        fixtureService = mock(HmcFixtureService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new HmcFixtureController(fixtureService)).build();
        if (context != null) {
            MockMvcTestTarget target = new MockMvcTestTarget();
            target.setMockMvc(mockMvc);
            context.setTarget(target);
        }
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyCompatibility(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @State("Hearing exists for supplied id")
    void hearingExists() {
        when(fixtureService.getHearing(anyString())).thenReturn(hearingResponse());
    }

    @State("Parties notified exist for supplied hearing id")
    void partiesNotifiedExist() {
        when(fixtureService.getPartiesNotified(anyString())).thenReturn(partiesNotifiedResponse());
    }

    @State("Parties notified payload can be updated")
    void partiesNotifiedCanBeUpdated() {
        doNothing().when(fixtureService)
            .updatePartiesNotified(anyString(), anyInt(), any(LocalDateTime.class), anyMap());
    }

    @State("Unnotified hearings exist for service code")
    void unnotifiedHearingsExist() {
        when(fixtureService.getUnnotifiedHearings(anyString(), anyString()))
            .thenReturn(Map.of("hearingIds", List.of(HEARING_ID), "totalFound", 1));
    }

    @State("Hearings exist for case id")
    void hearingsExist() {
        when(fixtureService.getHearings(anyString(), anyString())).thenReturn(hearingsResponse());
    }

    private Map<String, Object> hearingResponse() {
        return Map.of(
            "requestDetails", Map.of(
                "hearingRequestID", HEARING_ID,
                "versionNumber", 1,
                "status", "LISTED",
                "timestamp", "2024-10-01T10:00:00"
            ),
            "caseDetails", Map.of(
                "caseRef", CASE_REFERENCE,
                "hmctsServiceCode", SERVICE_CODE
            ),
            "hearingResponse", Map.of(
                "listAssistTransactionID", "TRANSACTION-123",
                "receivedDateTime", "2024-10-03T11:15",
                "laCaseStatus", "LISTED",
                "listingStatus", "FIXED",
                "hearingDaySchedule", List.of(hearingDaySchedule(true))
            ),
            "partyDetails", List.of(Map.of(
                "partyID", "P1",
                "partyType", "IND",
                "partyRole", "CLAIMANT"
            ))
        );
    }

    private Map<String, Object> partiesNotifiedResponse() {
        return Map.of(
            "hearingID", HEARING_ID,
            "responses", List.of(Map.of(
                "responseReceivedDateTime", "2024-10-05T14:45",
                "requestVersion", 1,
                "partiesNotified", "2024-10-05T13:45",
                "serviceData", Map.of(
                    "hearingNoticeGenerated", true,
                    "hearingLocation", "0001",
                    "hearingDate", HEARING_START,
                    "days", List.of(hearingDaySchedule(false))
                )
            ))
        );
    }

    private Map<String, Object> hearingsResponse() {
        return Map.of(
            "caseRef", CASE_REFERENCE,
            "hmctsServiceCode", SERVICE_CODE,
            "caseHearings", List.of(Map.of(
                "hearingID", 1000000000000000L,
                "hearingRequestDateTime", "2024-10-02T08:30:00",
                "hmcStatus", "LISTED",
                "requestVersion", 1,
                "hearingDaySchedule", List.of(hearingDaySchedule(false))
            ))
        );
    }

    private static Map<String, String> hearingDaySchedule(boolean includeVenue) {
        if (includeVenue) {
            return Map.of(
                "hearingStartDateTime", HEARING_START,
                "hearingEndDateTime", HEARING_END,
                "hearingVenueId", "0001",
                "hearingRoomId", "Room A"
            );
        }
        return Map.of(
            "hearingStartDateTime", HEARING_START,
            "hearingEndDateTime", HEARING_END
        );
    }

    interface HmcFixtureService {

        Map<String, Object> getHearing(String hearingId);

        Map<String, Object> getPartiesNotified(String hearingId);

        void updatePartiesNotified(
            String hearingId,
            int version,
            LocalDateTime received,
            Map<String, Object> payload
        );

        Map<String, Object> getUnnotifiedHearings(String serviceCode, String hearingStartDateFrom);

        Map<String, Object> getHearings(String caseId, String status);
    }

    @RestController
    static class HmcFixtureController {

        private final HmcFixtureService fixtureService;

        HmcFixtureController(HmcFixtureService fixtureService) {
            this.fixtureService = fixtureService;
        }

        @GetMapping(path = "/hearing/{hearingId}", produces = APPLICATION_JSON_VALUE)
        Map<String, Object> getHearing(@PathVariable String hearingId) {
            return fixtureService.getHearing(hearingId);
        }

        @GetMapping(path = "/partiesNotified/{hearingId}", produces = APPLICATION_JSON_VALUE)
        Map<String, Object> getPartiesNotified(@PathVariable String hearingId) {
            return fixtureService.getPartiesNotified(hearingId);
        }

        @PutMapping(path = "/partiesNotified/{hearingId}", consumes = APPLICATION_JSON_VALUE)
        ResponseEntity<Void> updatePartiesNotified(
            @PathVariable String hearingId,
            @RequestParam int version,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime received,
            @RequestBody Map<String, Object> payload
        ) {
            fixtureService.updatePartiesNotified(hearingId, version, received, payload);
            return ResponseEntity.ok().build();
        }

        @GetMapping(path = "/unNotifiedHearings/{serviceCode}", produces = APPLICATION_JSON_VALUE)
        Map<String, Object> getUnnotifiedHearings(
            @PathVariable String serviceCode,
            @RequestParam(name = "hearing_start_date_from") String hearingStartDateFrom
        ) {
            return fixtureService.getUnnotifiedHearings(serviceCode, hearingStartDateFrom);
        }

        @GetMapping(path = "/hearings/{caseId}", produces = APPLICATION_JSON_VALUE)
        Map<String, Object> getHearings(@PathVariable String caseId, @RequestParam String status) {
            return fixtureService.getHearings(caseId, status);
        }
    }
}
