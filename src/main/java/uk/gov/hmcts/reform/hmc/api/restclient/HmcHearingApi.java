package uk.gov.hmcts.reform.hmc.api.restclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.hmc.api.model.response.Categories;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "hmc-hearing", url = "${hearing.api.url}", configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface HmcHearingApi {
    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String HEARING_ENDPOINT = "/refdata/commondata/lov/categories";

   @GetMapping(value = HEARING_ENDPOINT + "/{categoryId}", consumes = MediaType.APPLICATION_JSON_VALUE)
   Categories retrieveListOfValuesByCategoryId(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable("categoryId") String categoryId,
        @RequestParam(name = "isChildRequired", required = false) String isChildRequired,
        @RequestParam(name = "key", required = false) String key,
        @RequestParam(name = "parentCategory", required = false) String parentCategory,
        @RequestParam(name = "parentKey", required = false) String parentKey,
        @RequestParam(name = "serviceId", required = false) String serviceId
        );
}
