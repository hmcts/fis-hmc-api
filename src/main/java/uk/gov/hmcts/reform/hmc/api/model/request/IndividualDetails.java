package uk.gov.hmcts.reform.hmc.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.api.model.response.IndividualDetailsModel;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class IndividualDetails {

    private String title;

    private String firstName;

    private String lastName;

    private String preferredHearingChannel;

    private String interpreterLanguage;

    private List<String> reasonableAdjustments;

    private Boolean vulnerableFlag;

    private String vulnerabilityDetails;

    private List<String> hearingChannelEmail;

    private List<String> hearingChannelPhone;

    private List<RelatedParty> relatedParties;

    private String custodyStatus;

    private String otherReasonableAdjustmentDetails;

    public static IndividualDetails fromIndividualDetailsModel(IndividualDetailsModel individualDetailsModel) {
        return IndividualDetails.builder()
            // no title used in IndividualDetailsModel
            .firstName(individualDetailsModel.getFirstName())
            .lastName(individualDetailsModel.getLastName())
            .preferredHearingChannel(individualDetailsModel.getPreferredHearingChannel())
            .interpreterLanguage(individualDetailsModel.getInterpreterLanguage())
            .reasonableAdjustments(individualDetailsModel.getReasonableAdjustments())
            .vulnerableFlag(individualDetailsModel.getVulnerableFlag())
            .vulnerabilityDetails(individualDetailsModel.getVulnerabilityDetails())
            .hearingChannelEmail(individualDetailsModel.getHearingChannelEmail())
            .hearingChannelPhone(individualDetailsModel.getHearingChannelPhone())
            .custodyStatus(individualDetailsModel.getCustodyStatus())
            // no related parties ever set to be other than an empty list
            // no other reasonable adjustment details used in IndividualDetailsModel
            .build();
    }
}
