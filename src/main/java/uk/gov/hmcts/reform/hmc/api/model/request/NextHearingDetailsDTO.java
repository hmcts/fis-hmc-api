package uk.gov.hmcts.reform.hmc.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.api.model.ccd.NextHearingDetails;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(builderMethodName = "nextHearingDetailsRequestDTOWith")
@Schema(description = "The response object to hearing management")
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class NextHearingDetailsDTO {

    private String caseRef;

    private NextHearingDetails nextHearingDetails;
}
