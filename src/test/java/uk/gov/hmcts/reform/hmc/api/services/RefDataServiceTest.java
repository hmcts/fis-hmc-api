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
    public void shouldFetchVenueDetailsRefDataTest() {
        CourtDetail courtDetail =
                CourtDetail.courtDetailWith().courtTypeId(COURT_TYPE_ID).hearingVenueId("231596").serviceCode("ABA5").build();

        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataClient.fetchCourtDetail(anyString())).thenReturn(courtDetail);

        String epimmsId = "231596";
        CourtDetail courtDetailResp = refDataService.getCourtDetails(epimmsId);
        assertNotNull(courtDetailResp);
        assertEquals("231596", courtDetailResp.getHearingVenueId());
    }

    @Test
    public void shouldFetchFirstVenueDetailsWhenMultipleCourtsWithSameIdReturnedRefDataTest() {
        CourtDetail courtDetail1 =
            CourtDetail.courtDetailWith().courtTypeId(COURT_TYPE_ID).hearingVenueId("231596").build();
        CourtDetail courtDetail2 =
            CourtDetail.courtDetailWith().courtTypeId(COURT_TYPE_ID).hearingVenueId("231596").build();
        CourtDetail courtDetail3 =
            CourtDetail.courtDetailWith().courtTypeId(COURT_TYPE_ID).hearingVenueId("231596").build();
        CourtDetail courtDetail4 =
            CourtDetail.courtDetailWith().courtTypeId(COURT_TYPE_ID).hearingVenueId("231596").build();
        CourtDetail courtDetail5 =
            CourtDetail.courtDetailWith().courtTypeId("10").hearingVenueId("231596").build();
        CourtDetail courtDetail6 =
            CourtDetail.courtDetailWith().courtTypeId("10").hearingVenueId("231596").build();
        CourtDetail courtDetail7 =
            CourtDetail.courtDetailWith().courtTypeId("31").hearingVenueId("231596").build();
        CourtDetail courtDetail8 =
            CourtDetail.courtDetailWith().courtTypeId("27").hearingVenueId("231596").build();

        List<CourtDetail> legacyList = new ArrayList<>();
        legacyList.add(courtDetail1);
        legacyList.add(courtDetail2);
        legacyList.add(courtDetail3);
        legacyList.add(courtDetail4);
        legacyList.add(courtDetail5);
        legacyList.add(courtDetail6);
        legacyList.add(courtDetail7);
        legacyList.add(courtDetail8);

        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataClient.fetchCourtDetail(anyString())).thenReturn(null);
        when(refDataClient.fetchCourtDetailList(anyString())).thenReturn(legacyList);

        String epimmsId = "231596";
        CourtDetail courtDetailResp = refDataService.getCourtDetails(epimmsId);
        assertNotNull(courtDetailResp);
        assertEquals("231596", courtDetailResp.getHearingVenueId());
        assertEquals(COURT_TYPE_ID, courtDetailResp.getCourtTypeId());
        assertEquals(courtDetail1, courtDetailResp);
    }

    @Test
    public void shouldFetchVenueDetailsRefDataWhenCourtTypeNotThereTest() {
        CourtDetail courtDetail =
            CourtDetail.courtDetailWith().courtTypeId("30").hearingVenueId("231596").serviceCode("ABA5").build();

        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataClient.fetchCourtDetail(anyString())).thenReturn(courtDetail);
        String epimmsId = "231596";
        CourtDetail courtDetailResp = refDataService.getCourtDetails(epimmsId);
        assertNull(courtDetailResp);

    }

    @Test
    public void shouldFetchVenueDetailsRefDataS2sExceptionTest() {
        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataClient.fetchCourtDetail(anyString()))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));
        String epimmsId = "231596";
        assertThrows(RefDataException.class, () -> refDataService.getCourtDetails(epimmsId));
    }

    @Test
    public void shouldFetchVenueDetailsRefDataHttpClientExceptionTest() {
        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataClient.fetchCourtDetail(anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        String epimmsId = "231596";
        assertThrows(RefDataException.class, () -> refDataService.getCourtDetails(epimmsId));
    }

    @Test
    public void shouldUpdateHearingWithCourtDetailsRefDataTest() {

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
        when(refDataClient.fetchCourtDetail(anyString())).thenReturn(courtDetail);
        HearingDTO hearingResp = refDataService.getHearingWithCourtDetails(hearingDto);
        assertNotNull(hearingResp);
        assertEquals("courtDetailVenueTest", hearingResp.getHearingUpdate().getHearingVenueName());
        assertEquals("courtDetailAddressTest, courtPostCodeTest", hearingResp.getHearingUpdate().getHearingVenueAddress());
        assertEquals("courtDetailLocationCodTest", hearingResp.getHearingUpdate().getHearingVenueLocationCode());
        assertEquals(COURT_TYPE_ID, hearingResp.getHearingUpdate().getCourtTypeId());
    }

    @Test
    public void shouldUpdateHearingWithCourtDetailsRefDataTest1() {
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
    void newContractNullLegacyReturnsMatchFallbackTest() {
        CourtDetail legacyMatch =
            CourtDetail.courtDetailWith().courtTypeId(COURT_TYPE_ID).hearingVenueId("231596").build();
        List<CourtDetail> legacyList = new ArrayList<>();
        legacyList.add(legacyMatch);

        when(refDataClient.fetchCourtDetail(anyString())).thenReturn(null);
        when(refDataClient.fetchCourtDetailList(anyString())).thenReturn(legacyList);

        CourtDetail result = refDataService.getCourtDetails("231596");
        assertNotNull(result);
        assertEquals("231596", result.getHearingVenueId());
        // new contract not used, legacy used once
        assertEquals(0, refDataService.getNewContractUsedCount());
        assertEquals(1, refDataService.getLegacyFallbackUsedCount());
    }

    @Test
    void newContractNonMatchingLegacyReturnsMatchFallbackTest() {
        CourtDetail newNonMatch =
            CourtDetail.courtDetailWith().courtTypeId("30").hearingVenueId("231596").build();
        CourtDetail legacyMatch =
            CourtDetail.courtDetailWith().courtTypeId("10").hearingVenueId("231596").build();
        List<CourtDetail> legacyList = new ArrayList<>();
        legacyList.add(legacyMatch);

        when(refDataClient.fetchCourtDetail(anyString())).thenReturn(newNonMatch);
        when(refDataClient.fetchCourtDetailList(anyString())).thenReturn(legacyList);

        CourtDetail result = refDataService.getCourtDetails("231596");
        assertNotNull(result);
        assertEquals("231596", result.getHearingVenueId());
        assertEquals(0, refDataService.getNewContractUsedCount());
        assertEquals(1, refDataService.getLegacyFallbackUsedCount());
    }

    @Test
    void newContractNullServiceCodeLegacyReturnsMatchFallbackTest() {
        CourtDetail newNullServiceCode =
            CourtDetail.courtDetailWith().courtTypeId(COURT_TYPE_ID).hearingVenueId("231596").serviceCode(null).build();
        CourtDetail legacyMatch =
            CourtDetail.courtDetailWith().courtTypeId("10").hearingVenueId("231596").build();
        List<CourtDetail> legacyList = new ArrayList<>();
        legacyList.add(legacyMatch);

        when(refDataClient.fetchCourtDetail(anyString())).thenReturn(newNullServiceCode);
        when(refDataClient.fetchCourtDetailList(anyString())).thenReturn(legacyList);

        CourtDetail result = refDataService.getCourtDetails("231596");
        assertNotNull(result);
        assertEquals("231596", result.getHearingVenueId());
        assertEquals(0, refDataService.getNewContractUsedCount());
        assertEquals(1, refDataService.getLegacyFallbackUsedCount());
    }

    @Test
    void shouldReturnCourtDetailWithServiceCodeWhenNewContractUsed() {
        CourtDetail courtDetail =
                CourtDetail.courtDetailWith()
                        .courtTypeId(COURT_TYPE_ID)
                        .hearingVenueId("231596")
                        .serviceCode("ABA5")
                        .build();

        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataClient.fetchCourtDetail(anyString())).thenReturn(courtDetail);

        CourtDetail courtDetailResp = refDataService.getCourtDetails("231596");
        assertNotNull(courtDetailResp);
        assertEquals("231596", courtDetailResp.getHearingVenueId());
        assertEquals("ABA5", courtDetailResp.getServiceCode());
        assertEquals(1, refDataService.getNewContractUsedCount());
        assertEquals(0, refDataService.getLegacyFallbackUsedCount());
    }

    @Test
    void bothCallsFailNonHttpReturnsNullTest() {
        when(refDataClient.fetchCourtDetail(anyString())).thenReturn(null);
        when(refDataClient.fetchCourtDetailList(anyString())).thenReturn(List.of());

        CourtDetail result = refDataService.getCourtDetails("231596");
        assertNull(result);
        assertEquals(0, refDataService.getNewContractUsedCount());
        assertEquals(0, refDataService.getLegacyFallbackUsedCount());
    }

    @Test
    void legacyThrowsHttpExceptionIsPropagated() {
        when(refDataClient.fetchCourtDetail(anyString())).thenReturn(null);
        when(refDataClient.fetchCourtDetailList(anyString()))
            .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

        assertThrows(RefDataException.class, () -> refDataService.getCourtDetails("231596"));
    }

    @Test
    void legacyThrowsHttpClientErrorExceptionIsPropagated() {
        when(refDataClient.fetchCourtDetail(anyString())).thenReturn(null);
        when(refDataClient.fetchCourtDetailList(anyString()))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(RefDataException.class, () -> refDataService.getCourtDetails("231596"));
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

