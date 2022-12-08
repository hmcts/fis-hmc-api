package uk.gov.hmcts.reform.hmc.api.model.ccd.caselinksdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Element;

@lombok.Data
@Builder(toBuilder = true)
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class CaseLinkData {
    @JsonProperty("CaseType")
    public String caseType;

    @JsonProperty("CaseReference")
    public String caseReference;

    @JsonProperty("ReasonForLink")
    public List<Element<CaseReason>> reasonForLink;

    @JsonProperty("CreatedDateTime")
    public Date createdDateTime;
}
