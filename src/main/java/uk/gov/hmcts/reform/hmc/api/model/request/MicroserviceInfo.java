package uk.gov.hmcts.reform.hmc.api.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MicroserviceInfo {
    @JsonProperty("microservice")
    private String microservice;
}
