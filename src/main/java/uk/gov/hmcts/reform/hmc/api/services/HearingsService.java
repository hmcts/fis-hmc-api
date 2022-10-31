package uk.gov.hmcts.reform.hmc.api.services;

import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

public interface HearingsService {
    Hearings getHearingsByCaseRefNo(
            String authorization, String serviceAuthorization, String caseReference);
}
