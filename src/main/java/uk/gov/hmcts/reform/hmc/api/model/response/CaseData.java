package uk.gov.hmcts.reform.hmc.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

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
