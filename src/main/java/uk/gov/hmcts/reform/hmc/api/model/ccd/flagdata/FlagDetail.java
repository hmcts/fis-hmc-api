package uk.gov.hmcts.reform.hmc.api.model.ccd.flagdata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@lombok.Data
@Builder(toBuilder = true)
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class FlagDetail {
    public String id;
    public Data data;
}
