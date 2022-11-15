package uk.gov.hmcts.reform.hmc.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.hmc.api.Application;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingsData;
import uk.gov.hmcts.reform.hmc.api.services.HearingsDataService;
import uk.gov.hmcts.reform.hmc.api.services.HearingsService;
import uk.gov.hmcts.reform.hmc.api.utils.ResourceLoader;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = {Application.class})
public class HearingsControllerFunctionalTest {

    private static final String HEARING_VALUES_REQUEST_BODY_JSON = "requests/hearing-values.json";
    private MockMvc mockMvc;

    @Autowired private WebApplicationContext webApplicationContext;

    @MockBean RestTemplate restTemplate;

    @MockBean private HearingsService hearingsService;

    @MockBean private HearingsDataService hearingsDataService;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Ignore
    @Test
    public void givenHearingValuesWhenGetHearingsDataThen200Response() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        String requestBody = ResourceLoader.loadJson(HEARING_VALUES_REQUEST_BODY_JSON);

        HearingsData hearingsData =
                HearingsData.hearingsDataWith()
                        .hmctsServiceID("BBA3")
                        .hmctsInternalCaseName("123")
                        .publicCaseName("John Smith")
                        .caseAdditionalSecurityFlag(false)
                        .build();
        Mockito.when(hearingsDataService.getCaseData(any(), anyString(), anyString()))
                .thenReturn(hearingsData);

        mockMvc.perform(
                        post("/serviceHearingValues")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "auth")
                                .header("ServiceAuthorization", "service auth")
                                .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Ignore
    @Test
    public void givenCaseReferenceNoWhenGetRequestToHearingsByCaseReferenceThen200Response()
            throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();

        Hearings hearings = Hearings.hearingsWith().caseRef("123").hmctsServiceCode("BBA3").build();
        Mockito.when(hearingsService.getHearingsByCaseRefNo(any(), anyString(), anyString()))
                .thenReturn(hearings);

        mockMvc.perform(
                        get("/hearings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorisation", "auth")
                                .header("ServiceAuthorization", "service auth")
                                .header("caseReference", "1663170041386456")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }
}
