package uk.gov.hmcts.reform.hmc.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingLocation;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingWindow;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(builderMethodName = "automatedHearingDetailsWith")
public class HearingDetails {

    @JsonProperty("autolistFlag")
    private Boolean autolistFlag;

    @JsonProperty("listingAutoChangeReasonCode")
    private String listingAutoChangeReasonCode;

    @JsonProperty("hearingType")
    private String hearingType;

    @JsonProperty("hearingWindow")
    private HearingWindow hearingWindow;

    @JsonProperty("duration")
    private Integer duration;

    @JsonProperty("nonStandardHearingDurationReasons")
    private List<String> nonStandardHearingDurationReasons;

    @JsonProperty("hearingPriorityType")
    private String hearingPriorityType;

    @JsonProperty("numberOfPhysicalAttendees")
    private Integer numberOfPhysicalAttendees;

    @JsonProperty("hearingInWelshFlag")
    private Boolean hearingInWelshFlag;

    @JsonProperty("hearingLocations")
    private List<HearingLocation> hearingLocations;

    @JsonProperty("facilitiesRequired")
    private List<String> facilitiesRequired;

    @JsonProperty("listingComments")
    private String listingComments;

    @JsonProperty("hearingRequester")
    private String hearingRequester;

    @JsonProperty("privateHearingRequiredFlag")
    private Boolean privateHearingRequiredFlag;

    @JsonProperty("leadJudgeContractType")
    private String leadJudgeContractType;

    @JsonProperty("panelRequirements")
    private PanelRequirements panelRequirements;

    @JsonProperty("hearingIsLinkedFlag")
    private Boolean hearingIsLinkedFlag;

    @JsonProperty("amendReasonCodes")
    private List<String> amendReasonCodes;

    @JsonProperty("hearingChannels")
    private List<String> hearingChannels;
}
