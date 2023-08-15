package uk.gov.hmcts.reform.hmc.api.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder(builderMethodName = "individualDetailsWith")
@NoArgsConstructor
@AllArgsConstructor
public class IndividualDetailsModel {

    String firstName;
    String lastName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String preferredHearingChannel;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String interpreterLanguage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<String> reasonableAdjustments;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Boolean vulnerableFlag;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String vulnerabilityDetails;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<String> hearingChannelEmail;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<String> hearingChannelPhone;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String custodyStatus;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<RelatedPartiesModel> relatedParties;
}
