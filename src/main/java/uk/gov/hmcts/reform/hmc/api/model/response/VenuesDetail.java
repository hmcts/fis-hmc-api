package uk.gov.hmcts.reform.hmc.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "venuesDetailsWith")
@NoArgsConstructor
@AllArgsConstructor
public class VenuesDetail {

    @JsonProperty("service_code")
    private String serviceCode;

    @JsonProperty("court_type_id")
    private String courtTypeId;

    @JsonProperty("court_venues")
    private List<CourtDetail> courtVenues;
}
