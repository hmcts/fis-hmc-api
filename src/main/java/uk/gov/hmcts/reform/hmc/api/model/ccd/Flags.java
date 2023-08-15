package uk.gov.hmcts.reform.hmc.api.model.ccd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.api.model.ccd.flagdata.FlagDetail;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class Flags {

    String partyName;
    String roleOnCase;

    List<Element<FlagDetail>> details;
}
