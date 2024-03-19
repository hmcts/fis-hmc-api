package uk.gov.hmcts.reform.hmc.api.services;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearing;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

@Service
public interface HearingsService {
    Hearings getHearingsByCaseRefNo(
            String caseReference, String authorization, String serviceAuthorization);

    List<Hearings> getHearingsByListOfCaseIds(
            Map<String, String> caseIdWithRegionId,
            String authorization,
            String serviceAuthorization);

    Hearings getFutureHearings(String caseReference);


    Hearing createHearings(CaseDetails caseDetails);
}
