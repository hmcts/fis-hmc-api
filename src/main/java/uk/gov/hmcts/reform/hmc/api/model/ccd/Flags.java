package uk.gov.hmcts.reform.hmc.api.model.ccd;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class Flags {

    String partyName;
    String roleOnCase;

    List<Element<String>> details;
}
