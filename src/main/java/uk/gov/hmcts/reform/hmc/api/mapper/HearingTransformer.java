package uk.gov.hmcts.reform.hmc.api.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingRequest;

@Service
@RequiredArgsConstructor
public class HearingTransformer {

    private final HearingTransactionRequestMapper hearingTransactionRequestMapper;

    public HearingRequest mappingHearingTransactionRequest(CaseDetails caseDetails) {
        return hearingTransactionRequestMapper.mappingHearingTransactionRequest(caseDetails);
    }

}
