package uk.gov.hmcts.reform.hmc.api.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

import java.util.List;
import java.util.Map;

@Service
public interface HearingsService {
    Hearings getHearingsByCaseRefNo(
            String caseReference, String authorization, String serviceAuthorization);

    List<Hearings> getHearingsByListOfCaseIds(
            Map<String, String> caseIdWithRegionId,
            String authorization,
            String serviceAuthorization);

    Hearings getFutureHearings(String caseReference);
}
