package uk.gov.hmcts.reform.hmc.api.model.response.linkdata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder(builderMethodName = "hearingLinkDataWith")
@NoArgsConstructor
@AllArgsConstructor
public class HearingLinkData {
    public String caseReference;
    public String caseName;
    public List<String> reasonsForLink;
}
