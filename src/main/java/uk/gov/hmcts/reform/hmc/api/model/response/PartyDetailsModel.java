package uk.gov.hmcts.reform.hmc.api.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "partyDetailsWith")
@NoArgsConstructor
@AllArgsConstructor
public class PartyDetailsModel {
    String partyID;
    PartyType partyType;
    String partyName;
    String partyRole;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String hearingSubChannel;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    IndividualDetailsModel individualDetails;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    OrganisationDetailsModel organisationDetailsModel;
}
