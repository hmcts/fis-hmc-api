package uk.gov.hmcts.reform.hmc.api.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@NoArgsConstructor

public class CaseHearings {

    private String hmctsServiceCode;

    private String caseRef;

    private List<CaseHearing> caseHearings;
}
