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
@Builder(builderMethodName = "hearingRequestWith")
@Schema(description = "The response object to hearing management")
public class Hearing {

    @JsonProperty("hmctsServiceCode")
    private String hmctsServiceCode;

    @JsonProperty("caseRef")
    private String caseRef;

    @JsonProperty("hearingId")
    private String hearingId;

    private HearingUpdate hearingUpdate;
}
