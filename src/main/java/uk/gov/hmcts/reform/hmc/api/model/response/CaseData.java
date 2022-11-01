package uk.gov.hmcts.reform.hmc.api.model.response;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "caseDataWith")
@NoArgsConstructor
@AllArgsConstructor
public class CaseData {

    private String jurisdiction;

    private String caseType;

    private Date createdOn;
}
