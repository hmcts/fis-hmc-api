package uk.gov.hmcts.reform.hmc.api.model.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
/*@NoArgsConstructor
@AllArgsConstructor*/
public class HearingResponse {

    @Size(max = 30)
    private String hearingRequestID;

    @Size(max = 100)
    private String status;

    private LocalDateTime timeStamp;

    private Number versionNumber;

}
