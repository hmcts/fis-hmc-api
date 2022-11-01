package uk.gov.hmcts.reform.hmc.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseCategories;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingsData;
import uk.gov.hmcts.reform.hmc.api.services.HearingsDataService;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class HearingsControllerTest {

    @InjectMocks private HearingsController hearingsController;

    @Mock private HearingsDataService hearingsDataService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void hearingsDataTest() throws JsonProcessingException {

        HearingsData hearingsData =
                HearingsData.hearingsDataWith()
                        .hmctsServiceID("BBA3")
                        .hmctsInternalCaseName("123")
                        .publicCaseName("John Smith")
                        .caseAdditionalSecurityFlag(false)
                        .caseCategories(
                                CaseCategories.caseCategoriesWith()
                                        .categoryType("NA")
                                        .categoryValue("NA")
                                        .categoryParent("NA")
                                        .build())
                        .build();

        Mockito.when(hearingsDataService.getCaseData(any(), anyString())).thenReturn(hearingsData);
        ResponseEntity<HearingsData> hearingsData1 =
                hearingsController.getHearingsData("Authorization", "caseReference", "hearingId");
        Assertions.assertEquals(HttpStatus.OK, hearingsData1.getStatusCode());
    }
}
