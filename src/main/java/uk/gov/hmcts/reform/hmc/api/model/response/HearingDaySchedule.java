package uk.gov.hmcts.reform.hmc.api.model.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
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

    private String  hearingRoomId;

    private String hearingJudgeId;

    private List<String> panelMemberIds;

    private List<Attendee> attendees;

}
