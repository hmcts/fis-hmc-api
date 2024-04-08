package uk.gov.hmcts.reform.hmc.api.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseData;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public final class AutomatedHearingTransformer {

    private AutomatedHearingTransformer() {
        throw new IllegalStateException("Utility class");
    }

    private final AutomatedHearingTransactionRequestMapper hearingTransactionRequestMapper;

    public AutomatedHearingRequest mappingHearingTransactionRequest(CaseDetails caseDetails) {
        return hearingTransactionRequestMapper.mappingHearingTransactionRequest(caseDetails);
    }

}
