package uk.gov.hmcts.reform.hmc.api.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class CaseData {

    private String familymanCaseNumber;
    private String dateSubmitted;
    private String caseTypeOfApplication;

    private YesOrNo isInterpreterNeeded;

    private List<Element<PartyDetails>> applicants;

    private List<Element<PartyDetails>> respondents;

    private String applicantSolicitorEmailAddress;

    private String solicitorName;

    private String courtName;
}
