package uk.gov.hmcts.reform.hmc.api.model.ccd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class CaseManagementLocation {
    private String region;
    private String baseLocation;
}
