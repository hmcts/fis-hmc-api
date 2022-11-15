package uk.gov.hmcts.reform.hmc.api.services;

import uk.gov.hmcts.reform.hmc.api.model.request.Hearing;

public interface PrlUpdateService {

    Boolean updatePrlServiceWithHearing(Hearing hearing);
}
