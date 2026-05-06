package uk.gov.hmcts.reform.hmc.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(builderMethodName = "hearingWindowWith")
@NoArgsConstructor
@AllArgsConstructor
public class HearingWindow {

    private String dateRangeStart;

    private String dateRangeEnd;

    private String firstDateTimeMustBe;
}
