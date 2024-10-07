package uk.gov.hmcts.reform.hmc.api.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.util.List;

@NoArgsConstructor
@Data
public class PanelRequirements {

    private List<String> roleType;

    private List<String> authorisationTypes;

    private List<String> authorisationSubType;

    @Valid
    private List<PanelPreference> panelPreferences;

    private List<String> panelSpecialisms;

}
