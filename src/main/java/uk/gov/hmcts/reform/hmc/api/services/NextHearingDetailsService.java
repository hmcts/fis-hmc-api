package uk.gov.hmcts.reform.hmc.api.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.api.model.ccd.NextHearingDetails;

@Service
public interface NextHearingDetailsService {

    NextHearingDetails getNextHearingDateByCaseRefNo(String caseReference);
}
