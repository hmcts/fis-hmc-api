package uk.gov.hmcts.reform.hmc.api.controllers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingsRequest;
import uk.gov.hmcts.reform.hmc.api.model.response.*;
import uk.gov.hmcts.reform.hmc.api.services.HearingsDataService;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.services.HearingsService;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class HearingsControllerTest {

    @Mock
    private HearingsDataService hearingsDataService;

    @Mock
    private HearingsService hearingsServiceTest;


    @InjectMocks
    private HearingsController hearingsController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void hearingsDataTest() throws JsonProcessingException {
        HearingsRequest hearingsRequest = HearingsRequest.hearingRequestWith().hearingId("123")
            .caseReference("123").build();
        HearingsData hearingsData = HearingsData.hearingsDataWith()
            .hmctsServiceID("BBA3")
            .hmctsInternalCaseName(hearingsRequest.getCaseReference())
            .publicCaseName("John Smith")
            .caseAdditionalSecurityFlag(false)
            .caseCategories(CaseCategories.caseCategoriesWith()
                                .categoryType("NA")
                                .categoryValue("NA")
                                .categoryParent("NA").build()).build();

        String authorisation = "xyz";

        ResponseEntity<HearingsData> hearingsData1 =
            hearingsController.getHearingsData("Authorization","caseReference","hearingId");
        Assertions.assertEquals(HttpStatus.OK, hearingsData1.getStatusCode());
    }

//    @Test
//    void shouldReturnHearingsTest() {
//        Hearings caseHearingsData =
//                Hearings.HearingsWith().caseRef("123").hmctsServiceCode("BBA3").build();
//        when(hearingsServiceTest.getHearingsByCaseRefNo(anyString(), anyString(), anyString()))
//                .thenReturn(caseHearingsData);
//        Hearings caseHearings =
//                hearingsController.getHearingsByCaseRefNo("authorisation", "serviceAuth", "123");
//        Assertions.assertEquals("BBA3", caseHearings.getHmctsServiceCode());
//    }

}
