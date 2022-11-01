package uk.gov.hmcts.reform.hmc.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingsRequest;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingsData;

@Service
public interface HearingsDataService {

    HearingsData getCaseData(HearingsRequest hearingsRequest, String authorisation)
            throws JsonProcessingException;
}
