package uk.gov.hmcts.reform.hmc.api.model.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder(builderMethodName = "courtDetailWith")
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class CourtDetail {

    @JsonProperty("epimms_id")
    @JsonAlias("epimms_id")
    private String hearingVenueId;

    @JsonProperty("court_name")
    @JsonAlias("court_name")
    private String hearingVenueName;

    @JsonProperty("court_address")
    @JsonAlias("court_address")
    private String hearingVenueAddress;

    @JsonProperty("court_location_code")
    @JsonAlias("court_location_code")
    private String hearingVenueLocationCode;

    @JsonProperty("court_type_id")
    private String courtTypeId;
}
