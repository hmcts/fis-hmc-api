package uk.gov.hmcts.reform.hmc.api.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.api.enums.State;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

@Service
public interface NextHearingDetailsService {

    State fetchStateForUpdate(Hearings hearings, String currHearingHmcStatus);

    Boolean updateNextHearingDate(Hearings hearings);
}
