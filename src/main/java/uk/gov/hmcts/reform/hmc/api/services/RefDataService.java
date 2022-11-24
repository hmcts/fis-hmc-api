package uk.gov.hmcts.reform.hmc.api.services;

import uk.gov.hmcts.reform.hmc.api.model.request.Hearing;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;

public interface RefDataService {

    CourtDetail getCourtDetails(String epimmsId);

    Hearing getHearingWithCourtDetails(Hearing hearing);
}
