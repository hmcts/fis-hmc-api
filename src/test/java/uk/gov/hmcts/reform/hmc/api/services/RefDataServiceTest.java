package uk.gov.hmcts.reform.hmc.api.services;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.exceptions.RefDataException;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingDTO;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingUpdateDTO;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.VenuesDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class RefDataServiceTest {

    @InjectMocks private RefDataServiceImpl refDataService;

    @Mock private RefDataClient refDataClient;

    @Mock private AuthTokenGenerator authTokenGenerator;

    @Mock private IdamTokenGenerator idamTokenGenerator;

    private static final String COURT_TYPE_ID = "18";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refDataService, "familyCourtIds", List.of("10", COURT_TYPE_ID, "25"));
    }

    @Test
    void shouldFetchVenueDetailsRefDataTest() {
        CourtDetail courtDetail =
                CourtDetail.courtDetailWith().courtTypeId(COURT_TYPE_ID).hearingVenueId("231596").serviceCode("ABA5").build();

        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataClient.fetchCourtDetail(anyString())).thenReturn(List.of(courtDetail));

        String epimmsId = "231596";
        CourtDetail courtDetailResp = refDataService.getCourtDetails(epimmsId);
        assertNotNull(courtDetailResp);
        assertEquals("231596", courtDetailResp.getHearingVenueId());
    }

    @Test
     void shouldFetchFirstVenueDetailsWhenMultipleCourtsReturnedFromNewContractTest() {
        CourtDetail courtDetail1 =
            CourtDetail.courtDetailWith().courtTypeId(COURT_TYPE_ID).hearingVenueId("231596").serviceCode("ABA5").build();
        CourtDetail courtDetail2 =
            CourtDetail.courtDetailWith().courtTypeId(COURT_TYPE_ID).hearingVenueId("231596").serviceCode("ABA5").build();
        CourtDetail courtDetail3 =
            CourtDetail.courtDetailWith().courtTypeId("10").hearingVenueId("231596").serviceCode("ABA5").build();

        List<CourtDetail> courtList = new ArrayList<>();
        courtList.add(courtDetail1);
        courtList.add(courtDetail2);
        courtList.add(courtDetail3);

        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataClient.fetchCourtDetail(anyString())).thenReturn(courtList);

        String epimmsId = "231596";
        CourtDetail courtDetailResp = refDataService.getCourtDetails(epimmsId);
        assertNotNull(courtDetailResp);
        assertEquals("231596", courtDetailResp.getHearingVenueId());
        assertEquals(COURT_TYPE_ID, courtDetailResp.getCourtTypeId());
        assertEquals(courtDetail1, courtDetailResp);
    }

    @Test
    void shouldReturnNullWhenNoCourtMatches() {
        // This detail has a courtTypeId "99" which is not in your [10, 18, 25] list
        // AND a serviceCode that doesn't match HMCTS_SERVICE_ID
        CourtDetail nonMatchingDetail =
            CourtDetail.courtDetailWith().courtTypeId("99").serviceCode("WRONG_CODE").build();

        when(refDataClient.fetchCourtDetail(anyString())).thenReturn(List.of(nonMatchingDetail));

        String epimmsId = "231596";
        CourtDetail courtDetailResp = refDataService.getCourtDetails(epimmsId);

        assertNull(courtDetailResp);
    }

    @Test
    void shouldFetchVenueDetailsRefDataS2sExceptionTest() {
        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataClient.fetchCourtDetail(anyString()))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));
        String epimmsId = "231596";
        assertThrows(RefDataException.class, () -> refDataService.getCourtDetails(epimmsId));
    }

    @Test
     void shouldFetchVenueDetailsRefDataHttpClientExceptionTest() {
        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataClient.fetchCourtDetail(anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        String epimmsId = "231596";
        assertThrows(RefDataException.class, () -> refDataService.getCourtDetails(epimmsId));
    }

    @Test
    void shouldUpdateHearingWithCourtDetailsRefDataTest() {

        CourtDetail courtDetail =
                CourtDetail.courtDetailWith()
                        .courtTypeId(COURT_TYPE_ID)
                        .hearingVenueId("231596")
                        .hearingVenueName("courtDetailVenueTest")
                        .hearingVenueAddress("courtDetailAddressTest")
                    .hearingVenueLocationCode("courtDetailLocationCodTest")
                    .hearingVenuePostCode("courtPostCodeTest")
                    .serviceCode("ABA5")
                        .build();

        // single CourtDetail is used directly by mocks; no list required

        HearingUpdateDTO hearingupdateDto =
                HearingUpdateDTO.hearingUpdateRequestDTOWith()
                        .hearingVenueId("231596")
                        .hearingVenueName("hearingVenueTest")
                    .hearingVenueAddress("hearingAddressTest")
                    .hearingVenueLocationCode("hearingLocationCodeTest")
                        .build();

        HearingDTO hearingDto =
                HearingDTO.hearingRequestDTOWith()
                        .hearingId("testHearinID")
                        .caseRef("testCaseRef")
                        .hmctsServiceCode("BBA3")
                        .hearingUpdate(hearingupdateDto)
                        .build();

        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataClient.fetchCourtDetail(anyString())).thenReturn(List.of(courtDetail));
        HearingDTO hearingResp = refDataService.getHearingWithCourtDetails(hearingDto);
        assertNotNull(hearingResp);
        assertEquals("courtDetailVenueTest", hearingResp.getHearingUpdate().getHearingVenueName());
        assertEquals("courtDetailAddressTest, courtPostCodeTest", hearingResp.getHearingUpdate().getHearingVenueAddress());
        assertEquals("courtDetailLocationCodTest", hearingResp.getHearingUpdate().getHearingVenueLocationCode());
        assertEquals(COURT_TYPE_ID, hearingResp.getHearingUpdate().getCourtTypeId());
    }

    @Test
    void shouldMatchWhenServiceCodeIsNullAndCourtTypeIsInAllowedList() {
        CourtDetail matchingDetail = CourtDetail.courtDetailWith()
            .serviceCode(null)
            .courtTypeId("10") // Exists in familyCourtIds
            .build();

        when(refDataClient.fetchCourtDetail(anyString())).thenReturn(List.of(matchingDetail));

        CourtDetail result = refDataService.getCourtDetails("123");

        assertNotNull(result);
        assertEquals("10", result.getCourtTypeId());
    }

    @Test
    void shouldMatchWhenServiceCodeMatches() {
        CourtDetail matchingDetail = CourtDetail.courtDetailWith()
            .serviceCode("ABA5")
            .courtTypeId("99")
            .build();

        when(refDataClient.fetchCourtDetail(anyString())).thenReturn(List.of(matchingDetail));

        CourtDetail result = refDataService.getCourtDetails("123");

        assertNotNull(result);
        assertEquals("ABA5", result.getServiceCode());
    }

    @Test
    void shouldReturnNullWhenNoCriteriaMatch() {
        CourtDetail nonMatchingDetail = CourtDetail.courtDetailWith()
            .serviceCode("WRONG_CODE")
            .courtTypeId("99")
            .build();

        when(refDataClient.fetchCourtDetail(anyString())).thenReturn(List.of(nonMatchingDetail));

        CourtDetail result = refDataService.getCourtDetails("123");

        assertNull(result);
    }

    @Test
    void shouldUpdateHearingWithCourtDetailsRefDataTest1() {
        HearingUpdateDTO hearingupdateDto =
            HearingUpdateDTO.hearingUpdateRequestDTOWith()
                .hearingVenueId("231596")
                .hearingVenueName("hearingVenuetest")
                .build();

        HearingDTO hearingDto =
            HearingDTO.hearingRequestDTOWith()
                .hearingId("testHearinID")
                .caseRef("testCaseRef")
                .hmctsServiceCode("BBA3")
                .hearingUpdate(hearingupdateDto)
                .build();

        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataClient.fetchCourtDetail(anyString()))
            .thenThrow(feignException(HttpStatus.BAD_REQUEST.value(), "Not found"));

        HearingDTO hearingResp = refDataService.getHearingWithCourtDetails(hearingDto);

        assertEquals("hearingVenuetest", hearingResp.getHearingUpdate().getHearingVenueName());
    }

    @Test
    void getCourtDetailsByServiceCodeTest() {
        CourtDetail courtDetail =
            CourtDetail.courtDetailWith()
                .courtTypeId(COURT_TYPE_ID)
                .hearingVenueId("231596")
                .hearingVenueName("test")
                .build();

        List<CourtDetail> courtDetailsList = new ArrayList<>();
        courtDetailsList.add(courtDetail);

        VenuesDetail venue = new VenuesDetail();
        venue.setServiceCode("mock_service_code");
        venue.setCourtTypeId(COURT_TYPE_ID);
        venue.setCourtVenues(courtDetailsList);

        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataClient.fetchByServiceCode(anyString())).thenReturn(venue);
        List<CourtDetail> courtList = refDataService.getCourtDetailsByServiceCode("mock_serviceCode");
        assertNotNull(courtList);
        assertFalse(courtList.isEmpty());
        assertEquals("231596", courtList.get(0).getHearingVenueId());
    }

    @Test
    void getCourtDetailListByServiceCodeForEmptyTest() {
        CourtDetail courtDetail =
            CourtDetail.courtDetailWith()
                .courtTypeId(COURT_TYPE_ID)
                .hearingVenueId("231596")
                .hearingVenueName("test")
                .build();
        List<CourtDetail> courtDetailsList = new ArrayList<>();
        courtDetailsList.add(courtDetail);

        VenuesDetail venue = new VenuesDetail();
        venue.setServiceCode("mock_service_code");
        venue.setCourtTypeId("mock_courttype_id");
        venue.setCourtVenues(courtDetailsList);

        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataClient.fetchByServiceCode(anyString())).thenReturn(venue);
        List<CourtDetail> courtList = refDataService.getCourtDetailsByServiceCode("mock_serviceCode");
        assertTrue(courtList.isEmpty());
    }

    @Test
    void newContractReturnsEmptyListReturnsNullTest() {
        when(refDataClient.fetchCourtDetail(anyString())).thenReturn(List.of());

        CourtDetail result = refDataService.getCourtDetails("231596");
        assertNull(result);
    }

    public static FeignException feignException(int status, String message) {
        return FeignException.errorStatus(
            message,
            Response.builder()
                .status(status)
                .request(Request.create(GET, EMPTY, Map.of(), new byte[] {}, UTF_8, null))
                .build());
    }
}

