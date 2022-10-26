package uk.gov.hmcts.reform.hmc.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.mapper.FisHmcObjectMapper;
import uk.gov.hmcts.reform.hmc.api.restclient.ServiceAuthorisationTokenApi;

@Service
@Slf4j
@SuppressWarnings("PMD")
public class CaseApiService {
    @Value("${idam.s2s-auth.microservice}")
    private String microserviceName;

    @Autowired CoreCaseDataApi coreCaseDataApi;

    @Autowired ServiceAuthorisationTokenApi serviceAuthorisationTokenApi;

    public CaseDetails getCaseDetails(String caseId, String authorization, String serviceToken)
            throws JsonProcessingException {
        ObjectMapper objectMapper = FisHmcObjectMapper.getObjectMapper();
        CaseDetails caseDetails = coreCaseDataApi.getCase(authorization, serviceToken, caseId);
        objectMapper.writeValueAsString(caseDetails);
        System.out.println(caseDetails);
        return caseDetails;
    }
}
