package uk.gov.hmcts.reform.hmc.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "partyFlagsModelWith")
@NoArgsConstructor
@AllArgsConstructor
public class PartyFlagsModel {
    String partyId;
    String partyName;
    String flagParentId;
    String flagId;
    String flagDescription;
    String flagStatus;
}
