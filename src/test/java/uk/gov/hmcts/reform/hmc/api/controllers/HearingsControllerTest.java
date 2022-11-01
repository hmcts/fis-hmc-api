package uk.gov.hmcts.reform.hmc.api.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingsData;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class HearingsControllerTest {

    @InjectMocks private HearingsController hearingsController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void hearingsDataTest() throws JsonProcessingException {
        ResponseEntity<HearingsData> hearingsData1 =
                hearingsController.getHearingsData("Authorization", "caseReference", "hearingId");
        Assertions.assertEquals(HttpStatus.OK, hearingsData1.getStatusCode());
    }
}
