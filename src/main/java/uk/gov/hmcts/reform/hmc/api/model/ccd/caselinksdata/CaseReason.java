package uk.gov.hmcts.reform.hmc.api.model.ccd.caselinksdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@lombok.Data
@Builder(toBuilder = true)
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class CaseReason {

    @JsonProperty("Reason")
    public String reason;
}
