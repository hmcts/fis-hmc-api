package uk.gov.hmcts.reform.hmc.api.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.MicroserviceInfo;
import uk.gov.hmcts.reform.hmc.api.restclient.ServiceAuthorisationTokenApi;

@Service
@Slf4j
@SuppressWarnings("PMD")
public class CaseApiService {
    @Value("${idam.s2s-auth.microservice}")
    private String microserviceName;

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    ServiceAuthorisationTokenApi serviceAuthorisationTokenApi;

    public CaseDetails getCaseDetails(Long caseId, String authorization) {
        String serviceToken = serviceAuthorisationTokenApi.serviceToken(MicroserviceInfo.builder().microservice(microserviceName.trim()).build());
        return coreCaseDataApi.getCase(
            authorization,
            serviceToken,
            String.valueOf(caseId));
    }
}
