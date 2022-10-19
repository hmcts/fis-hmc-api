package uk.gov.hmcts.reform.hmc.api.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Hearing {

    private String hmctsServiceID;

    private String hmctsInternalCaseName;

    private String publicCaseName;

    private Boolean caseCategories;

    private String caseDeepLink;

    private Boolean caserestrictedFlag;

    private String externalCaseReference;

    private String caseManagementLocationCode;

    private String caseSLAStartDate;

    private Boolean autoListFlag;

    private String hearingType;

    private int duration;

    private String hearingPriorityType;

    private int numberOfPhysicalAttendees;

    private Boolean hearingInWelshFlag;

    private List<String> facilitiesRequired;

    private String listingComments;

    private String hearingRequester;

    private Boolean privateHearingRequiredFlag;

    private Boolean caseInterpreterRequiredFlag;

    private String leadJudgeContractType;

    private Boolean hearingIsLinkedFlag;

    private List<String> hearingChannels;
}
