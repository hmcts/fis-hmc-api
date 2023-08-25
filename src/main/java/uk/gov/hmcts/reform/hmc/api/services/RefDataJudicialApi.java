package uk.gov.hmcts.reform.hmc.api.services;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.hmc.api.config.RefDataConfiguration;
import uk.gov.hmcts.reform.hmc.api.model.request.JudgeRequestDTO;
import uk.gov.hmcts.reform.hmc.api.model.response.JudgeDetail;

@FeignClient(
    name = "ref-data-judicial-api",
    primary = false,
    url = "${ref_data_judicial.api.url}",
    configuration = RefDataConfiguration.class)
public interface RefDataJudicialApi {

    String AUTHORIZATION = "Authorization";
    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String CONTENT_TYPE_V2 = "application/vnd.jrd.v2+json";

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/refdata/judicial/users",
        headers = CONTENT_TYPE + "=" + CONTENT_TYPE_V2)
    List<JudgeDetail> getJudgeDetails(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody JudgeRequestDTO refreshRoleRequest);
}
