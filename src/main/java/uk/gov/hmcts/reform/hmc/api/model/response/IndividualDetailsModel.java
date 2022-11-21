package uk.gov.hmcts.reform.hmc.api.model.response;

import java.util.List;

public class IndividualDetailsModel {
    String title;
    String firstName;
    String lastName;
    String preferredHearingChannel;
    String interpreterLanguage;
    String reasonableAdjustments;
    String vulnerableFlag;
    String vulnerabilityDetails;
    String hearingChannelEmail;
    String hearingChannelPhone;
    String custodyStatus;

    List<RelatedPartiesModel> relatedParties;
}
