package uk.gov.hmcts.reform.hmc.api.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Builder(builderMethodName = "attendeeWith")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Attendee {

    private String partyID;

    private String hearingSubChannel;
}
