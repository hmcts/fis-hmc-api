package uk.gov.hmcts.reform.hmc.api.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;

@Service
@RequiredArgsConstructor
public class AutomatedHearingTransformer {

    private final AutomatedHearingTransactionRequestMapper automatedHearingTransactionRequestMapper;

    public AutomatedHearingRequest mappingAutomatedHearingTransactionRequest(CaseDetails caseDetails) {
        return automatedHearingTransactionRequestMapper.mappingAutomatedHearingTransactionRequest(caseDetails);
    }

}
