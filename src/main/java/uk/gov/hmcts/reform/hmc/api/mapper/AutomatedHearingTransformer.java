package uk.gov.hmcts.reform.hmc.api.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AutomatedHearingTransformer {

    private final AutomatedHearingTransactionRequestMapper hearingTransactionRequestMapper;

    public AutomatedHearingRequest mappingHearingTransactionRequest(CaseDetails caseDetails)  throws IOException {
        return hearingTransactionRequestMapper.mappingHearingTransactionRequest(caseDetails);
    }

}
