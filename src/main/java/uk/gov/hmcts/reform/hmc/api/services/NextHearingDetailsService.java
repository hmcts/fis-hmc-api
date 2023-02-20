package uk.gov.hmcts.reform.hmc.api.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.api.model.ccd.NextHearingDetails;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

@Service
public interface NextHearingDetailsService {

    NextHearingDetails getNextHearingDate(Hearings hearings);

    String fetchStateForUpdate(Hearings hearings, String currHearingHmcStatus);
}
