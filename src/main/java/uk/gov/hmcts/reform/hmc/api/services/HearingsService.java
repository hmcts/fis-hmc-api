package uk.gov.hmcts.reform.hmc.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.api.restclient.ServiceAuthorisationTokenApi;
import uk.gov.hmcts.reform.hmc.api.restclient.HmcHearingApi;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingsRequest;
import uk.gov.hmcts.reform.hmc.api.model.request.MicroserviceInfo;
import uk.gov.hmcts.reform.hmc.api.model.response.Categories;

@Service
@RequiredArgsConstructor
public class HearingsService {
    @Value("${idam.s2s-auth.microservice}")
    private String microserviceName;
    public static final String HEARING_SUB_CHANNEL = "HearingSubChannel";
    private final HmcHearingApi hmcHearingApi;
    private final ServiceAuthorisationTokenApi serviceAuthorisationTokenApi;

    public Categories getRefData(HearingsRequest hearingsRequest, String authorisation) {
        String serviceToken = serviceAuthorisationTokenApi.serviceToken(MicroserviceInfo.builder().microservice(microserviceName.trim()).build());
        return hmcHearingApi.retrieveListOfValuesByCategoryId(authorisation, serviceToken,
                                                              HEARING_SUB_CHANNEL,
                                                              null,
                                                              null,
                                                              null,
                                                              null,
                                                              null);
    }
}
