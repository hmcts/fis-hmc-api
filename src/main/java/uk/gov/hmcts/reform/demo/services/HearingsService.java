package uk.gov.hmcts.reform.demo.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.demo.model.request.HearingsRequest;
import uk.gov.hmcts.reform.demo.model.response.HearingsResponseDummy;

@Service
public class HearingsService {
    public HearingsResponseDummy getHearingsData(HearingsRequest hearingsRequest) {
        return HearingsResponseDummy.builder()
            .dummyResponse("Retrieving hearing data, please wait ...").build();
    }
}
