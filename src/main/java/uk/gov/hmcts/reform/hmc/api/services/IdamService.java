package uk.gov.hmcts.reform.hmc.api.services;

import uk.gov.hmcts.reform.hmc.api.model.response.IdamTokenResponse;

public interface IdamService {
    IdamTokenResponse getSecurityTokens();
}
