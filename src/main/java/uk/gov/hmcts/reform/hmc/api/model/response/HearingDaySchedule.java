package uk.gov.hmcts.reform.hmc.api.model.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "hearingDayScheduleWith")
@NoArgsConstructor
@AllArgsConstructor
public class HearingDaySchedule {

    private LocalDateTime hearingStartDateTime;

    private LocalDateTime hearingEndDateTime;

    private String listAssistSessionId;

    private String hearingVenueId;

    private String courtTypeId;

    private String hearingVenueName;

    private String hearingVenueLocationCode;

    private String hearingVenueAddress;

    private String hearingRoomId;

    private String hearingJudgeId;

    private String hearingJudgeName;

    private List<String> panelMemberIds;

    private List<Attendee> attendees;
}
