package uk.gov.hmcts.reform.hmc.api.services;



import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.hmc.api.config.RefDataConfiguration;
import uk.gov.hmcts.reform.hmc.api.model.request.JudgeRequestDTO;
import uk.gov.hmcts.reform.hmc.api.model.response.JudgeDetail;

import java.util.List;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(
        name = "ref-data-judicial-api",
        primary = false,
        url = "${ref_data_judicial.api.url}",
        configuration = RefDataConfiguration.class)
public interface RefDataJudicialApi {

    String AUTHORIZATION = "Authorization";
    String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/refdata/judicial/users",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    List<JudgeDetail> getJudgeDetails(
            @RequestHeader(AUTHORIZATION) String authorization,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
            @RequestBody JudgeRequestDTO refreshRoleRequest);
}
