package uk.gov.hmcts.reform.hmc.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(builderMethodName = "hearingLocationWith")
@NoArgsConstructor
@AllArgsConstructor
public class HearingLocation {

    private String locationType;

    private String locationId;
}
