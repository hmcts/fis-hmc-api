package uk.gov.hmcts.reform.hmc.api.services;

import java.util.List;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingDTO;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;

public interface RefDataService {

    CourtDetail getCourtDetails(String epimmsId);

    List<CourtDetail> getCourtDetailsByServiceCode(String serviceCode);

    HearingDTO getHearingWithCourtDetails(HearingDTO hearing);
}
