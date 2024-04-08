package uk.gov.hmcts.reform.hmc.api.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseData;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingResponse;
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


    HearingResponse createAutomatedHearings(CaseData caseData) throws IOException, ParseException;
}
