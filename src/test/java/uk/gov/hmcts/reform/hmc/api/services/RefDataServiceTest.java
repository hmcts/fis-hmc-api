package uk.gov.hmcts.reform.hmc.api.services;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.exceptions.RefDataException;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingDTO;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingUpdateDTO;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.VenuesDetail;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class RefDataServiceTest {

    @InjectMocks private RefDataServiceImpl refDataService;

    @Mock private RefDataApi refDataApi;

    @Mock private AuthTokenGenerator authTokenGenerator;

    @Mock private IdamTokenGenerator idamTokenGenerator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refDataService, "familyCourtIds", List.of("10", "18", "25"));
    }

    @Test
    public void shouldFetchVenueDetailsRefDataTest() throws IOException, ParseException {
        CourtDetail courtDetail =
                CourtDetail.courtDetailWith().courtTypeId("18").hearingVenueId("231596").build();
        List<CourtDetail> courtDetailsList = new ArrayList<>();
        courtDetailsList.add(courtDetail);

        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataApi.getCourtDetails(anyString(), any(), any())).thenReturn(courtDetailsList);

        String epimmsId = "231596";
        CourtDetail courtDetailResp = refDataService.getCourtDetails(epimmsId);
        assertNotNull(courtDetailResp);
        assertEquals("231596", courtDetailResp.getHearingVenueId());
    }

    @Test
    public void shouldFetchVenueDetailsRefDataWhenCourtTypeNotThereTest() throws IOException, ParseException {
        CourtDetail courtDetail =
            CourtDetail.courtDetailWith().courtTypeId("30").hearingVenueId("231596").build();
        List<CourtDetail> courtDetailsList = new ArrayList<>();
        courtDetailsList.add(courtDetail);

        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataApi.getCourtDetails(anyString(), any(), any())).thenReturn(courtDetailsList);
        String epimmsId = "231596";
        CourtDetail courtDetailResp = refDataService.getCourtDetails(epimmsId);
        assertNull(courtDetailResp);

    }

    @Test
    public void shouldFetchVenueDetailsRefDataS2sExceptionTest()
            throws IOException, ParseException {
        CourtDetail courtDetail =
                CourtDetail.courtDetailWith().courtTypeId("18").hearingVenueId("231596").build();
        List<CourtDetail> courtDetailsList = new ArrayList<>();
        courtDetailsList.add(courtDetail);

        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate())
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));
        when(refDataApi.getCourtDetails(anyString(), any(), any())).thenReturn(courtDetailsList);
        String epimmsId = "231596";
        assertThrows(RefDataException.class, () -> refDataService.getCourtDetails(epimmsId));
    }

    @Test
    public void shouldUpdateHearingWithCourtDetailsRefDataTest()
            throws IOException, ParseException {

        CourtDetail courtDetail =
                CourtDetail.courtDetailWith()
                        .courtTypeId("18")
                        .hearingVenueId("231596")
                        .hearingVenueName("courtDetailVenueTest")
                        .hearingVenueAddress("courtDetailAddressTest")
                    .hearingVenueLocationCode("courtDetailLocationCodTest")
                    .hearingVenuePostCode("courtPostCodeTest")
                        .build();

        List<CourtDetail> courtDetailsList = new ArrayList<>();
        courtDetailsList.add(courtDetail);

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
        when(refDataApi.getCourtDetails(anyString(), any(), any())).thenReturn(courtDetailsList);
        HearingDTO hearingResp = refDataService.getHearingWithCourtDetails(hearingDto);
        assertNotNull(hearingResp);
        assertEquals("courtDetailVenueTest", hearingResp.getHearingUpdate().getHearingVenueName());
        assertEquals("courtDetailAddressTest, courtPostCodeTest", hearingResp.getHearingUpdate().getHearingVenueAddress());
        assertEquals("courtDetailLocationCodTest", hearingResp.getHearingUpdate().getHearingVenueLocationCode());
        assertEquals("18", hearingResp.getHearingUpdate().getCourtTypeId());
    }

    @Test
    public void shouldUpdateHearingWithCourtDetailsRefDataTest1()
        throws IOException, ParseException {
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
        when(refDataApi.getCourtDetails(anyString(), any(), any()))
            .thenThrow(feignException(HttpStatus.BAD_REQUEST.value(), "Not found"));

        HearingDTO hearingResp = refDataService.getHearingWithCourtDetails(hearingDto);

        assertEquals("hearingVenuetest", hearingResp.getHearingUpdate().getHearingVenueName());
    }

    @Test
    void getCourtDetailsByServiceCodeTest() {
        CourtDetail courtDetail =
            CourtDetail.courtDetailWith()
                .courtTypeId("18")
                .hearingVenueId("231596")
                .hearingVenueName("test")
                .build();

        List<CourtDetail> courtDetailsList = new ArrayList<>();
        courtDetailsList.add(courtDetail);

        VenuesDetail venue = new VenuesDetail();
        venue.setServiceCode("mock_service_code");
        venue.setCourtTypeId("18");
        venue.setCourtVenues(courtDetailsList);

        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(refDataApi.getCourtDetailsByServiceCode(anyString(), any(), any())).thenReturn(venue);
        List<CourtDetail> courtList = refDataService.getCourtDetailsByServiceCode("mock_serviceCode");
        assertNotNull(courtList);
        assertFalse(courtList.isEmpty());
        assertEquals("231596", courtList.get(0).getHearingVenueId());
    }

    @Test
    void getCourtDetailListByServiceCodeForEmptyTest() {
        CourtDetail courtDetail =
            CourtDetail.courtDetailWith()
                .courtTypeId("18")
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
        when(refDataApi.getCourtDetailsByServiceCode(anyString(), any(), any())).thenReturn(venue);
        List<CourtDetail> courtList = refDataService.getCourtDetailsByServiceCode("mock_serviceCode");
        assertTrue(courtList.isEmpty());
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

