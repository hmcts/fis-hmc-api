package uk.gov.hmcts.reform.hmc.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.mapper.FisHmcObjectMapper;


@Service
@Slf4j
@SuppressWarnings("PMD")
public class CaseApiServiceImpl implements CaseApiService {

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    public CaseDetails getCaseDetails(String caseId, String authorization, String serviceToken)
        throws JsonProcessingException {
        ObjectMapper objectMapper = FisHmcObjectMapper.getObjectMapper();
        CaseDetails caseDetails = coreCaseDataApi.getCase(authorization, serviceToken, caseId);
        objectMapper.writeValueAsString(caseDetails);
        System.out.println(caseDetails);
        return caseDetails; }
}
