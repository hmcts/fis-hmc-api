package uk.gov.hmcts.reform.hmc.api.services;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class RefDataServiceTest {

    //    @InjectMocks private RefDataServiceImpl refDataService;
    //
    //    @Mock private RefDataApi refDataApi;
    //
    //    @Mock private AuthTokenGenerator authTokenGenerator;
    //
    //    @Mock private UserAuthTokenGenerator userAuthTokenGenerator;

    //    @Test
    //    public void shouldFetchVenueDetailsRefDataTest() throws IOException, ParseException {
    //        CourtDetail courtDetail =
    //
    // CourtDetail.courtDetailWith().courtTypeId("18").hearingVenueId("231596").build();
    //        List<CourtDetail> courtDetailsList = new ArrayList<>();
    //        courtDetailsList.add(courtDetail);
    //
    //        when(userAuthTokenGenerator.getSecurityTokens()).thenReturn("MOCK_AUTH_TOKEN");
    //        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
    //        when(refDataApi.getCourtDetails(anyString(), any(),
    // any())).thenReturn(courtDetailsList);
    //
    //        String epimmsId = "231596";
    //        CourtDetail courtDetailResp = refDataService.getCourtDetails(epimmsId);
    //
    //        assertEquals("231596", courtDetailResp.getHearingVenueId());
    //    }
    //
    //    @Test
    //    public void shouldFetchVenueDetailsRefDataS2sExceptionTest()
    //            throws IOException, ParseException {
    //        CourtDetail courtDetail =
    //
    // CourtDetail.courtDetailWith().courtTypeId("18").hearingVenueId("231596").build();
    //        List<CourtDetail> courtDetailsList = new ArrayList<>();
    //        courtDetailsList.add(courtDetail);
    //
    //        when(userAuthTokenGenerator.getSecurityTokens()).thenReturn("MOCK_AUTH_TOKEN");
    //        when(authTokenGenerator.generate())
    //                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));
    //        when(refDataApi.getCourtDetails(anyString(), any(),
    // any())).thenReturn(courtDetailsList);
    //
    //        String epimmsId = "231596";
    //        assertThrows(RefDataException.class, () -> refDataService.getCourtDetails(epimmsId));
    //    }
}
