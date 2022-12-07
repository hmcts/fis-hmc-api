package uk.gov.hmcts.reform.hmc.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(builderMethodName = "hearingUpdateRequestDTOWith")
@Schema(description = "The response object to hearing management")
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class HearingUpdateDTO {

    private String hearingResponseReceivedDateTime;

    private String hearingEventBroadcastDateTime;

    private String hearingListingStatus;

    private String nextHearingDate;

    private String hearingVenueId;

    private String hearingVenueName;

    private String hearingVenueAddress;

    private String hearingVenueLocationCode;

    private String courtTypeId;

    private String hearingJudgeId;

    private String hearingRoomId;

    @JsonProperty("HMCStatus")
    private String hmcStatus;

    @JsonProperty("ListAssistCaseStatus")
    private String listAssistCaseStatus;
}
