package uk.gov.hmcts.reform.hmc.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.IOException;
import java.util.List;
import org.json.simple.parser.ParseException;
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
import uk.gov.hmcts.reform.hmc.api.model.request.HearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.model.response.ServiceHearingValues;
import uk.gov.hmcts.reform.hmc.api.services.HearingsDataService;
import uk.gov.hmcts.reform.hmc.api.services.HearingsService;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@SuppressWarnings("unchecked")
class HearingsControllerTest {

    @InjectMocks private HearingsController hearingsController;

    @Mock private HearingsDataService hearingsDataService;

    @Mock private HearingsService hearingsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void hearingsDataControllerTest() throws IOException, ParseException {

        ServiceHearingValues hearingsData =
                ServiceHearingValues.hearingsDataWith()
                        .hmctsServiceID("BBA3")
                        .hmctsInternalCaseName("123")
                        .publicCaseName("John Smith")
                        .caseAdditionalSecurityFlag(false)
                        .build();

        Mockito.when(hearingsDataService.getCaseData(any(), anyString(), anyString()))
                .thenReturn(hearingsData);

        HearingValues hearingValues =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();

        ResponseEntity<ServiceHearingValues> hearingsData1 =
                hearingsController.getHearingsData("Auth", "sauth", hearingValues);
        Assertions.assertEquals(HttpStatus.OK, hearingsData1.getStatusCode());
    }

    @Test
    public void hearingsControllerTest() throws IOException, ParseException {
        Hearings hearings = Hearings.hearingsWith().caseRef("123").hmctsServiceCode("BBA3").build();
        Mockito.when(hearingsService.getHearingsByCaseRefNo(any(), anyString(), anyString()))
                .thenReturn(hearings);
        Hearings hearingsResponse =
                hearingsController.getHearingsByCaseRefNo("Auth", "sauth", "caseRef");
        Assertions.assertEquals("123", hearingsResponse.getCaseRef());
    }

    @Test
    public void hearingsLinkCaseDataControllerTest() throws IOException, ParseException {

        HearingValues hearingsRequestData =
                HearingValues.hearingValuesWith().hearingId("123").caseReference("123").build();

        ResponseEntity<List> hearingsLinkCaseDataLst =
                hearingsController.getHearingsLinkData("Auth", "sauth", hearingsRequestData);
        Assertions.assertEquals(HttpStatus.OK, hearingsLinkCaseDataLst.getStatusCode());
    }
}
