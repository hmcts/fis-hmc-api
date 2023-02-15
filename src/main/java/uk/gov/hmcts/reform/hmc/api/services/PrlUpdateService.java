package uk.gov.hmcts.reform.hmc.api.services;

import uk.gov.hmcts.reform.hmc.api.model.request.HearingDTO;
import uk.gov.hmcts.reform.hmc.api.model.request.NextHearingDetailsDTO;

public interface PrlUpdateService {

    Boolean updatePrlServiceWithHearing(HearingDTO hearingDto);

    Boolean updatePrlServiceWithNextHearingDate(NextHearingDetailsDTO nextHearingDetailsDto);
}
