
package uk.gov.hmcts.reform.hmc.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "panelRequirementsWith")
@NoArgsConstructor
@AllArgsConstructor

public class PanelRequirements {

    private String panelRequirements;

}


