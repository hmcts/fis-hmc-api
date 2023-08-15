package uk.gov.hmcts.reform.hmc.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder(builderMethodName = "hearingsWith")
@NoArgsConstructor
@AllArgsConstructor
public class Hearings {
    private String hmctsServiceCode;

    private String caseRef;

    private List<CaseHearing> caseHearings;

    private String courtTypeId;

    private String courtName;
}
