package uk.gov.hmcts.reform.hmc.api.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RelatedParty {

    private String relatedPartyID;

    private String relationshipType;

}
