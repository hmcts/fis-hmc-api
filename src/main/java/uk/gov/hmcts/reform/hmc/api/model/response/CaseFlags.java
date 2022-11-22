package uk.gov.hmcts.reform.hmc.api.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "caseFlagsWith")
@NoArgsConstructor
@AllArgsConstructor
public class CaseFlags {

    private List<PartyFlagsModel> flags;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("flagAmendURL")
    private String flagAmendUrl;
}
