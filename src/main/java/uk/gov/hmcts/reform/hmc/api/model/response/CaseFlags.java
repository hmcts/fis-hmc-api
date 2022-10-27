package uk.gov.hmcts.reform.hmc.api.model.response;

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

public class CaseFlags  {

    private PartyFlags  flags;

    private String flagAmendURL;

    private String categoryParent;

}



