package uk.gov.hmcts.reform.hmc.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(builderMethodName = "hearingUpdateRequestWith")
@Schema(description = "The response object to hearing management")
public class HearingUpdate {

    private String hearingResponseReceivedDateTime;

    private String hearingEventBroadcastDateTime;

    private String hearingListingStatus;

    private String nextHearingDate;

    private String hearingVenueId;

    private String hearingJudgeId;

    private String hmcStatus;
}