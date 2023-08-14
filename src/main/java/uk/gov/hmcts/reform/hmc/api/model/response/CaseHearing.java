package uk.gov.hmcts.reform.hmc.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "caseHearingWith")
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseHearing {

    private Long hearingID;

    private LocalDateTime hearingRequestDateTime;

    private String hearingType;

    private String hearingTypeValue;

    private String hmcStatus;

    private LocalDateTime lastResponseReceivedDateTime;

    private Integer requestVersion;

    private String hearingListingStatus;

    private String listAssistCaseStatus;

    private List<HearingDaySchedule> hearingDaySchedule;

    private String hearingGroupRequestId;

    private Boolean hearingIsLinkedFlag;

    //    private List<String> hearingChannels;

}
