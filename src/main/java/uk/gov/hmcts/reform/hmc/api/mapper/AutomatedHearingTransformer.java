package uk.gov.hmcts.reform.hmc.api.mapper;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;

public final class AutomatedHearingTransformer {

    private AutomatedHearingTransformer() {
        throw new IllegalStateException("Utility class");
    }

    public static AutomatedHearingRequest mappingAutomatedHearingTransactionRequest(CaseDetails caseDetails) {
        return AutomatedHearingTransactionRequestMapper.mappingAutomatedHearingTransactionRequest(caseDetails);
    }
}
