package uk.gov.hmcts.reform.hmc.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

public interface CaseApiService {

    CaseDetails getCaseDetails(String caseId, String authorization, String serviceToken)
            throws JsonProcessingException;
}
