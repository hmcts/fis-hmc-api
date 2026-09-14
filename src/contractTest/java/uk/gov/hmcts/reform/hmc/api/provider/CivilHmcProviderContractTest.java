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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Provider contract check for the Civil-to-HMC pact using FIS test fixtures.
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
class CivilHmcProviderContractTest {

    @BeforeEach
    void setUp(PactVerificationContext context) {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CivilHmcFixtureController()).build();
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
        // Response is supplied by the test-only controller.
    }

    @State("Parties notified exist for supplied hearing id")
    void partiesNotifiedExist() {
        // Response is supplied by the test-only controller.
    }

    @State("Parties notified payload can be updated")
    void partiesNotifiedCanBeUpdated() {
        // Response is supplied by the test-only controller.
    }

    @State("Unnotified hearings exist for service code")
    void unnotifiedHearingsExist() {
        // Response is supplied by the test-only controller.
    }

    @State("Hearings exist for case id")
    void hearingsExist() {
        // Response is supplied by the test-only controller.
    }

    @RestController
    static class CivilHmcFixtureController {

        private static final String HEARING_ID = "2000000000000000";
        private static final String CASE_REFERENCE = "1671000000000018";
        private static final String SERVICE_CODE = "AAA7";
        private static final String HEARING_START = "2024-10-20T09:30";
        private static final String HEARING_END = "2024-10-20T11:30";

        @GetMapping(path = "/hearing/{hearingId}", produces = APPLICATION_JSON_VALUE)
        Map<String, Object> getHearing(@PathVariable String hearingId) {
            return Map.of(
                "requestDetails", Map.of(
                    "hearingRequestID", hearingId,
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

        @GetMapping(path = "/partiesNotified/{hearingId}", produces = APPLICATION_JSON_VALUE)
        Map<String, Object> getPartiesNotified(@PathVariable String hearingId) {
            return Map.of(
                "hearingID", hearingId,
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

        @PutMapping(path = "/partiesNotified/{hearingId}", consumes = APPLICATION_JSON_VALUE)
        ResponseEntity<Void> updatePartiesNotified(
            @PathVariable String hearingId,
            @RequestParam int version,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime received,
            @RequestBody Map<String, Object> payload
        ) {
            return ResponseEntity.ok().build();
        }

        @GetMapping(path = "/unNotifiedHearings/{serviceCode}", produces = APPLICATION_JSON_VALUE)
        Map<String, Object> getUnnotifiedHearings(
            @PathVariable String serviceCode,
            @RequestParam(name = "hearing_start_date_from") String hearingStartDateFrom
        ) {
            return Map.of("hearingIds", List.of(HEARING_ID), "totalFound", 1);
        }

        @GetMapping(path = "/hearings/{caseId}", produces = APPLICATION_JSON_VALUE)
        Map<String, Object> getHearings(@PathVariable String caseId, @RequestParam String status) {
            return Map.of(
                "caseRef", caseId,
                "hmctsServiceCode", SERVICE_CODE,
                "caseHearings", List.of(Map.of(
                    "hearingID", 1000000000000000L,
                    "hearingRequestDateTime", "2024-10-02T08:30:00",
                    "hmcStatus", status,
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
    }
}
