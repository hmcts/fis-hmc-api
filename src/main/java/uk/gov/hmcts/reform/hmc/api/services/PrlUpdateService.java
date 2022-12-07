package uk.gov.hmcts.reform.hmc.api.services;

import uk.gov.hmcts.reform.hmc.api.model.request.HearingDTO;

public interface PrlUpdateService {

    Boolean updatePrlServiceWithHearing(HearingDTO hearingDto);
}
