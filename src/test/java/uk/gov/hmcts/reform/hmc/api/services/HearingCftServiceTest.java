package uk.gov.hmcts.reform.hmc.api.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.exceptions.AuthorizationException;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

@ExtendWith({MockitoExtension.class})
@ActiveProfiles("test")
class HearingCftServiceTest {

    @InjectMocks HearingsServiceImpl hearingsService;

    @Mock RestTemplate restTemplate;

    @Mock private AuthTokenGenerator authTokenGenerator;

    @Test
    void shouldReturnCtfHearingsTest() {
        Hearings caseHearings =
                Hearings.hearingsWith().caseRef("123").hmctsServiceCode("ABA5").build();
        ResponseEntity<Hearings> response = ResponseEntity.ok(caseHearings);
        when(restTemplate.exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),
                        ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<Hearings>>any()))
                .thenReturn(response);
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        Hearings hearings = hearingsService.getHearingsByCaseRefNo("authoriation", "123");
        Assertions.assertEquals("ABA5", hearings.getHmctsServiceCode());
    }

    @Test
    void shouldReturnCtfHearingsAuthExceptionTest() throws IOException, ParseException {
        when(restTemplate.exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),
                        ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<Hearings>>any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_GATEWAY));

        assertThrows(
                AuthorizationException.class,
                () -> hearingsService.getHearingsByCaseRefNo("authorization", "123"));
    }

    @Test
    void shouldReturnCtfHearingsExceptionTest() throws IOException, ParseException {

        when(restTemplate.exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(HttpMethod.class),
                        ArgumentMatchers.<HttpEntity<?>>any(),
                        ArgumentMatchers.<Class<Hearings>>any()))
                .thenThrow(new NullPointerException("Null Point Exception"));
        Assertions.assertEquals(
                null, hearingsService.getHearingsByCaseRefNo("authorization", "123"));
    }
}
