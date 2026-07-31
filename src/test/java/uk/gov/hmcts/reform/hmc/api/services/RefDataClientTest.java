package uk.gov.hmcts.reform.hmc.api.services;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.VenuesDetail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.HMCTS_SERVICE_ID;

@ExtendWith(SpringExtension.class)
class RefDataClientTest {

    @InjectMocks private RefDataClient refDataClient;

    @Mock private RefDataApi refDataApi;

    @Mock private AuthTokenGenerator authTokenGenerator;

    @Mock private IdamTokenGenerator idamTokenGenerator;

    // --- fetchCourtDetail tests ---

    @Test
    void fetchCourtDetailSuccessTest() {
        CourtDetail expected = CourtDetail.courtDetailWith()
            .courtTypeId("18")
            .hearingVenueId("231596")
            .serviceCode("ABA5")
            .build();

        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_IDAM_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataApi.getCourtDetails("MOCK_IDAM_TOKEN", "MOCK_S2S_TOKEN", "231596", HMCTS_SERVICE_ID))
            .thenReturn(List.of(expected));

        List<CourtDetail> result = refDataClient.fetchCourtDetail("231596");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("18", result.get(0).getCourtTypeId());
        assertEquals("231596", result.get(0).getHearingVenueId());
        assertEquals("ABA5", result.get(0).getServiceCode());
    }

    @Test
    void fetchCourtDetailReturnsEmptyListOnNonHttpException() {
        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_IDAM_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataApi.getCourtDetails(anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Network timeout"));

        List<CourtDetail> result = refDataClient.fetchCourtDetail("231596");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchCourtDetailThrowsHttpClientErrorException() {
        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_IDAM_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataApi.getCourtDetails(anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not found"));

        assertThrows(HttpClientErrorException.class, () -> refDataClient.fetchCourtDetail("231596"));
    }

    @Test
    void fetchCourtDetailThrowsHttpServerErrorException() {
        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_IDAM_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataApi.getCourtDetails(anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error"));

        assertThrows(HttpServerErrorException.class, () -> refDataClient.fetchCourtDetail("231596"));
    }

    @Test
    void fetchCourtDetailReturnsEmptyListOnTokenGenerationFailure() {
        when(idamTokenGenerator.generateIdamTokenForRefData())
            .thenThrow(new RuntimeException("Token generation failed"));

        List<CourtDetail> result = refDataClient.fetchCourtDetail("231596");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }



    // --- fetchByServiceCode tests ---

    @Test
    void fetchByServiceCodeSuccessTest() {
        VenuesDetail expected = new VenuesDetail();
        expected.setServiceCode("BBA3");
        expected.setCourtTypeId("18");

        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_IDAM_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataApi.getCourtDetailsByServiceCode("MOCK_IDAM_TOKEN", "MOCK_S2S_TOKEN", "BBA3"))
            .thenReturn(expected);

        VenuesDetail result = refDataClient.fetchByServiceCode("BBA3");
        assertNotNull(result);
        assertEquals("BBA3", result.getServiceCode());
        assertEquals("18", result.getCourtTypeId());
    }

    @Test
    void fetchByServiceCodeReturnsNullOnNonHttpException() {
        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_IDAM_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataApi.getCourtDetailsByServiceCode(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Service unavailable"));

        VenuesDetail result = refDataClient.fetchByServiceCode("BBA3");
        assertNull(result);
    }

    @Test
    void fetchByServiceCodeThrowsHttpClientErrorException() {
        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_IDAM_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataApi.getCourtDetailsByServiceCode(anyString(), anyString(), anyString()))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid service code"));

        assertThrows(HttpClientErrorException.class, () -> refDataClient.fetchByServiceCode("BBA3"));
    }

    @Test
    void fetchByServiceCodeThrowsHttpServerErrorException() {
        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_IDAM_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataApi.getCourtDetailsByServiceCode(anyString(), anyString(), anyString()))
            .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable"));

        assertThrows(HttpServerErrorException.class, () -> refDataClient.fetchByServiceCode("BBA3"));
    }

    @Test
    void fetchByServiceCodeReturnsNullOnS2sTokenGenerationFailure() {
        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_IDAM_TOKEN");
        when(authTokenGenerator.generate()).thenThrow(new RuntimeException("S2S token generation failed"));

        VenuesDetail result = refDataClient.fetchByServiceCode("BBA3");
        assertNull(result);
    }
}

