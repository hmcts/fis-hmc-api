package uk.gov.hmcts.reform.hmc.api.model.ccd;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Flags {

    String partyName;
    String roleOnCase;

    List<Element<String>> details;
}
