package uk.gov.hmcts.reform.hmc.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HearingDetails {

    @JsonProperty("autolistFlag")
    private Boolean autolistFlag;

    @JsonProperty("listingAutoChangeReasonCode")
    private String listingAutoChangeReasonCode;

    @JsonProperty("hearingType")
    private String hearingType;

    @JsonProperty("hearingWindow")
    private Object hearingWindow;

    @JsonProperty("duration")
    private Integer duration;

    @JsonProperty("nonStandardHearingDurationReasons")
    private String nonStandardHearingDurationReasons;

    @JsonProperty("hearingPriorityType")
    private String hearingPriorityType;

    @JsonProperty("numberOfPhysicalAttendees")
    private Integer numberOfPhysicalAttendees;

    @JsonProperty("hearingInWelshFlag")
    private Boolean hearingInWelshFlag;

    @JsonProperty("hearingLocations")
    private Object hearingLocations;

    @JsonProperty("facilitiesRequired")
    private Object facilitiesRequired;

    @JsonProperty("listingComments")
    private Object listingComments;

    @JsonProperty("hearingRequester")
    private String hearingRequester;

    @JsonProperty("privateHearingRequiredFlag")
    private Boolean privateHearingRequiredFlag;

    @JsonProperty("leadJudgeContractType")
    private String leadJudgeContractType;

    @JsonProperty("panelRequirements")
    private Object panelRequirements;

    @JsonProperty("hearingIsLinkedFlag")
    private Boolean hearingIsLinkedFlag;

    @JsonProperty("amendReasonCodes")
    private String amendReasonCodes;

    @JsonProperty("hearingChannels")
    private String hearingChannels;
}
