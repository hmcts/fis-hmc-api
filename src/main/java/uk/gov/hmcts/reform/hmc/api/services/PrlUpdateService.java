package uk.gov.hmcts.reform.hmc.api.services;

import uk.gov.hmcts.reform.hmc.api.enums.State;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingDTO;
import uk.gov.hmcts.reform.hmc.api.model.request.NextHearingDetailsDTO;

public interface PrlUpdateService {

    Boolean updatePrlServiceWithHearing(HearingDTO hearingDto, State caseState);

    Boolean updatePrlServiceWithNextHearingDate(
            String authorization, NextHearingDetailsDTO nextHearingDetailsDto);
}
