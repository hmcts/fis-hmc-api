package uk.gov.hmcts.reform.demo.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class HearingsRequest {

    @JsonProperty("caseReference")
    private String caseReference;

    @JsonProperty("hearingId")
    private String hearingId;
}
