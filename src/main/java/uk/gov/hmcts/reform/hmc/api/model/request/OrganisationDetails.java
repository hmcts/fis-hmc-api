package uk.gov.hmcts.reform.hmc.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.api.model.response.OrganisationDetailsModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisationDetails {

    @JsonProperty("name")
    private String name;
    private String organisationType;
    private String cftOrganisationID;


    public static OrganisationDetails fromOrganisationDetailsModel(OrganisationDetailsModel organisationDetailsModel) {
        return OrganisationDetails.builder()
            .name(organisationDetailsModel.getName())
            .organisationType(organisationDetailsModel.getOrganisationType())
            .cftOrganisationID(organisationDetailsModel.getCftOrganisationID())
            .build();
    }

}
