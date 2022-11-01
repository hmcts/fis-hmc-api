package uk.gov.hmcts.reform.hmc.api.controllers;

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
import uk.gov.hmcts.reform.hmc.api.model.response.CaseCategories;

import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.services.HearingsDataService;


@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class HearingsControllerTest {
    @Mock
    private HearingsDataService hearingsDataService;

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
        Hearings hearings = Hearings.hearingsWith()
            .hmctsServiceID("BBA3")
            .hmctsInternalCaseName(hearingsRequest.getCaseReference())
            .publicCaseName("John Smith")
            .caseAdditionalSecurityFlag(false)
            .caseCategories(CaseCategories.caseCategoriesWith()
                                .categoryType("NA")
                                .categoryValue("NA")
                                .categoryParent("NA").build()).build();

        String authorisation = "xyz";

//        Mockito.when(hearingsService.getCaseData(hearingsRequest, authorisation))
//                .thenReturn(hearings);
        ResponseEntity<Hearings> hearingsData =
                hearingsController.getHearingsData("Authorization","caseReference","hearingId");
       // hearingsData.getBody();
        Assertions.assertEquals(HttpStatus.OK, hearingsData.getStatusCode());
     }

}
