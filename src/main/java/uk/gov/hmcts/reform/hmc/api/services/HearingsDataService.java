package uk.gov.hmcts.reform.hmc.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingsRequest;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

public interface HearingsDataService {

    Hearings getCaseData(HearingsRequest hearingsRequest, String authorisation) throws JsonProcessingException;
}
