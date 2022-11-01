package uk.gov.hmcts.reform.hmc.api.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

@ExtendWith({MockitoExtension.class})
@ActiveProfiles("test")
class HearingCftServiceTest {

    @InjectMocks private final HearingsServiceImpl hearingsService = new HearingsServiceImpl();

    @Mock RestTemplate restTemplate;

    @Test
    void shouldReturnCtfHearingsTest() {

        Hearings caseHearings =
                Hearings.hearingsWith().caseRef("123").hmctsServiceCode("BBA3").build();
        ResponseEntity<Hearings> response = ResponseEntity.ok(caseHearings);
        Mockito.when(
                        restTemplate.exchange(
                                ArgumentMatchers.anyString(),
                                ArgumentMatchers.any(HttpMethod.class),
                                ArgumentMatchers.<HttpEntity<?>>any(),
                                ArgumentMatchers.<Class<Hearings>>any()))
                .thenReturn(response);
        hearingsService.getHearingsByCaseRefNo("authorization", "serviceAuthorization", "123");
    }
}
