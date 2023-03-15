package uk.gov.hmcts.reform.hmc.api.services;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.api.model.request.Cases;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

@Service
public interface HearingsService {
    Hearings getHearingsByCaseRefNo(
            String caseReference, String authorization, String serviceAuthorization);

    List<Hearings> getHearingsByListOfCaseRefNos(
            Cases cases, String authorization, String serviceAuthorization);
}
