package uk.gov.hmcts.reform.hmc.api.mapper;


import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseData;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;


public final class AutomatedHearingTransformer {

    private AutomatedHearingTransformer() {
        throw new IllegalStateException("Utility class");
    }


    public static AutomatedHearingRequest mappingHearingTransactionRequest(CaseData caseData,
                                                                           String ccdBaseUrl,
                                                                           String phoneNoSpecialChars) {
        return AutomatedHearingTransactionRequestMapper.mappingHearingTransactionRequest(caseData,ccdBaseUrl,phoneNoSpecialChars);
    }

}
