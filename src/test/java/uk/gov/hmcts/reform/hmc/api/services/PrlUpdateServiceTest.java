package uk.gov.hmcts.reform.hmc.api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.exceptions.PrlUpdateException;
import uk.gov.hmcts.reform.hmc.api.model.request.Hearing;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class PrlUpdateServiceTest {

    @InjectMocks private PrlUpdateServiceImpl prlUpdateService;

    @Mock private PrlUpdateApi prlUpdateApi;

    @Mock private AuthTokenGenerator authTokenGenerator;

    @Test
    public void shouldUpdatePrivateLawInPrlcosTest() throws IOException, ParseException {

        Hearing hearing =
                Hearing.hearingRequestWith()
                        .hearingId("testHearinID")
                        .caseRef("testCaseRef")
                        .hmctsServiceCode("ABA5")
                        .build();
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(prlUpdateApi.prlUpdate(anyString(), any())).thenReturn(ResponseEntity.ok("OK"));

        Boolean isOK = prlUpdateService.updatePrlServiceWithHearing(hearing);
        assertEquals(true, isOK);
    }

    @Test
    public void shouldNotUpdatePrivateLawInPrlcosTest() throws IOException, ParseException {

        Hearing hearing =
                Hearing.hearingRequestWith()
                        .hearingId("testHearinID")
                        .caseRef("testCaseRef")
                        .hmctsServiceCode("NonBBA3")
                        .build();
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(prlUpdateApi.prlUpdate(anyString(), any())).thenReturn(ResponseEntity.ok("OK"));

        Boolean isOK = prlUpdateService.updatePrlServiceWithHearing(hearing);
        assertEquals(true, isOK);
    }

    @Test
    public void shouldUpdatePrivateLawInPrlcosReturn401Test() throws IOException, ParseException {

        Hearing hearing =
                Hearing.hearingRequestWith()
                        .hearingId("testHearinID")
                        .caseRef("testCaseRef")
                        .hmctsServiceCode("ABA5")
                        .build();
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(prlUpdateApi.prlUpdate(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

        assertThrows(
                PrlUpdateException.class,
                () -> prlUpdateService.updatePrlServiceWithHearing(hearing));
    }

    @Test
    public void shouldUpdatePrivateLawInPrlcosS2sExceptionTest()
            throws IOException, ParseException {

        Hearing hearing =
                Hearing.hearingRequestWith()
                        .hearingId("testHearinID")
                        .caseRef("testCaseRef")
                        .hmctsServiceCode("ABA5")
                        .build();
        when(authTokenGenerator.generate())
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));
        when(prlUpdateApi.prlUpdate(anyString(), any())).thenReturn(ResponseEntity.ok("OK"));

        assertThrows(
                PrlUpdateException.class,
                () -> prlUpdateService.updatePrlServiceWithHearing(hearing));
    }
}
