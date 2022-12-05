package uk.gov.hmcts.reform.hmc.api.services;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.hmc.api.config.RefDataConfiguration;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;

@FeignClient(
        name = "ref-data-api",
        primary = false,
        url = "${ref_data.baseUrl}",
        configuration = RefDataConfiguration.class)
public interface RefDataApi {

    String AUTHORIZATION = "Authorization";
    String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/refdata/location/court-venues",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    List<CourtDetail> getCourtDetails(
            @RequestHeader(AUTHORIZATION) String authorization,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
            @RequestParam("epimms_id") final String epimmsId);
}
