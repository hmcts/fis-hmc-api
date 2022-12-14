package uk.gov.hmcts.reform.hmc.api.model.ccd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtherPersonRelationshipToChild {

    private String personRelationshipToChild;
}
