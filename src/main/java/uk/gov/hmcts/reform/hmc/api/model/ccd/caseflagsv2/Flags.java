package uk.gov.hmcts.reform.hmc.api.model.ccd.caseflagsv2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
public class Flags {
    String partyName;
    String roleOnCase;
    String groupId;
    String visibility;

    List<Element<FlagDetail>> details;
}
