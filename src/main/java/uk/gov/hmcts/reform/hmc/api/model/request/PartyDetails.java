package uk.gov.hmcts.reform.hmc.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartyDetails {

    @JsonProperty("partyID")
    private String partyID;

    @JsonProperty("partyType")
    private String partyType;

    @JsonProperty("partyRole")
    private String partyRole;

    @JsonProperty("oneOf")
    private OneOf oneOf;

    @JsonProperty("unavailabilityDOW")
    private UnavailabilityDOW unavailabilityDOW;

    @JsonProperty("unavailabilityRange")
    private UnavailabilityRange unavailabilityRange;

}
