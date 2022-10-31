package uk.gov.hmcts.reform.hmc.api.model.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(builderMethodName = "caseHearingsWith")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Hearings {

    private String hmctsServiceCode;

    private String caseRef;

    private List<CaseHearing> caseHearings;
}
