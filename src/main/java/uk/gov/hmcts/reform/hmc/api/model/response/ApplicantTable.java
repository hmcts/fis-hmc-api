package uk.gov.hmcts.reform.hmc.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(builderMethodName = "applicantTableWith")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApplicantTable {

    private String lastName;
}
