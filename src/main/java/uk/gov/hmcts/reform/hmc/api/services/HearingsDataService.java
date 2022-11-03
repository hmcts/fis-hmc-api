package uk.gov.hmcts.reform.hmc.api.services;

import java.io.IOException;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingsRequest;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingsData;

@Service
public interface HearingsDataService {

    HearingsData getCaseData(
            HearingsRequest hearingsRequest, String authorisation, String serviceAuthorization)
            throws IOException, ParseException;
}
