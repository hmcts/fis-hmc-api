package uk.gov.hmcts.reform.hmc.api.mapper;


import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseData;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;

import java.io.IOException;
import java.util.List;


public final class AutomatedHearingTransformer {

    private AutomatedHearingTransformer() {
        throw new IllegalStateException("Utility class");
    }


    public static List<AutomatedHearingRequest> mappingHearingTransactionRequest(CaseData caseData) throws IOException {
        return AutomatedHearingTransactionRequestMapper.mappingHearingTransactionRequest(caseData);
    }

}
