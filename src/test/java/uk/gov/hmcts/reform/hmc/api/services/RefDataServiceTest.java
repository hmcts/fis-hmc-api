package uk.gov.hmcts.reform.hmc.api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.exceptions.RefDataException;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingDTO;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingUpdateDTO;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class RefDataServiceTest {

    @InjectMocks private RefDataServiceImpl refDataService;

    @Mock private RefDataApi refDataApi;

    @Mock private AuthTokenGenerator authTokenGenerator;

    @Mock private IdamTokenGenerator idamTokenGenerator;

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

        assertEquals("231596", courtDetailResp.getHearingVenueId());
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
                        .hearingVenueName("test")
                        .build();

        List<CourtDetail> courtDetailsList = new ArrayList<>();
        courtDetailsList.add(courtDetail);

        HearingUpdateDTO hearingupdateDto =
                HearingUpdateDTO.hearingUpdateRequestDTOWith()
                        .hearingVenueId("231596")
                        .hearingVenueName("test")
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

        assertEquals("test", hearingResp.getHearingUpdate().getHearingVenueName());
    }
}
