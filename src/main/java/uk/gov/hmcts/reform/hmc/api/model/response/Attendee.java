package uk.gov.hmcts.reform.hmc.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder(builderMethodName = "attendeeWith")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Attendee {

    private String partyID;

    private String hearingSubChannel;
}
