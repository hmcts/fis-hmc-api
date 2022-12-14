package uk.gov.hmcts.reform.hmc.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(builderMethodName = "hearingValuesWith")
@Schema(description = "The response object to hearing management")
public class HearingValues {

    @JsonProperty("caseReference")
    private String caseReference;

    @JsonProperty("hearingId")
    private String hearingId;
}
