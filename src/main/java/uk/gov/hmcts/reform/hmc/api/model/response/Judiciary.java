package uk.gov.hmcts.reform.hmc.api.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "judiciaryWith")
@NoArgsConstructor
@AllArgsConstructor
public class Judiciary {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String categoryType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String categoryValue;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String categoryParent;
}
