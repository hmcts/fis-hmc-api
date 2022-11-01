package uk.gov.hmcts.reform.hmc.api.services;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@ExtendWith({MockitoExtension.class})
@SpringBootTest
@ActiveProfiles("test")
class CaseApiTest {

    @Autowired CaseApiServiceImpl caseApiServiceImpl;

    @MockBean CoreCaseDataApi coreCaseDataApi;

    @Test
    public void shouldReturnCaseApiDetailsTest() throws JsonProcessingException {

        CaseDetails caseDetails1 = CaseDetails.builder().id(123L).caseTypeId("PrivateLaw").build();
        when(coreCaseDataApi.getCase(anyString(), anyString(), anyString()))
            .thenReturn(caseDetails1);
        CaseDetails caseDetails =
            caseApiServiceImpl.getCaseDetails(anyString(), anyString(), anyString());
        Assertions.assertEquals(123L, caseDetails.getId());
    }
}
