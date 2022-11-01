package uk.gov.hmcts.reform.hmc.api.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

@Service
public interface HearingsService {
    Hearings getHearingsByCaseRefNo(
            String authorization, String serviceAuthorization, String caseReference);
}
