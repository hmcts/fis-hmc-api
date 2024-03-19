package uk.gov.hmcts.reform.hmc.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.time.DateTime;

import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hearing {

    @Size(max = 30)
    private String hearingRequestID;

    @Size(max = 100)
    private String status;

    private DateTime dateTime;

    private Number versionNumber;

}
