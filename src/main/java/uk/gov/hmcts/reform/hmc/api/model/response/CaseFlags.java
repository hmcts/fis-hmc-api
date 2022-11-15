package uk.gov.hmcts.reform.hmc.api.model.response;

import java.util.List;
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
public class CaseFlags {

    private List<String> flags;

    private String flagAmendUrl;

    private String categoryParent;

    private PartyFlagsModel partyFlagsModel;
}
