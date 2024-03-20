package uk.gov.hmcts.reform.hmc.api.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class HearingTransactionRequestMapper {
    public HearingRequest mappingHearingTransactionRequest(CaseDetails caseDetails) {
        return new HearingRequest();
    }
}
