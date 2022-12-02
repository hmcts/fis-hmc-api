package uk.gov.hmcts.reform.hmc.api.model.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class PartyDetails {

    private String firstName;
    private String lastName;
    private String previousName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private DontKnow isDateOfBirthUnknown;
    private Gender gender;
    private String otherGender;
    private String placeOfBirth;
    private DontKnow isAddressUnknown;
    private YesOrNo isAddressConfidential;
    private YesOrNo isAtAddressLessThan5Years;
    private String addressLivedLessThan5YearsDetails;
    private YesOrNo canYouProvideEmailAddress;
    private YesOrNo isEmailAddressConfidential;
    private String landline;
    private YesOrNo isPhoneNumberConfidential;
    private String relationshipToChildren;
    private YesOrNo isDateOfBirthKnown;
    private YesOrNo isCurrentAddressKnown;
    private YesOrNo canYouProvidePhoneNumber;
    private YesOrNo isPlaceOfBirthKnown;
    private List<Element<OtherPersonRelationshipToChild>> otherPersonRelationshipToChildren;
    private Organisation solicitorOrg;
    private Address solicitorAddress;
    private String dxNumber;
    private String solicitorReference;
    private String representativeFirstName;
    private String representativeLastName;
    private String sendSignUpLink;
    private String solicitorEmail;
    private String phoneNumber;
    private String email;
    private Address address;
    private String solicitorTelephone;
    private String caseTypeOfApplication;
    private YesOrNo respondentLivedWithApplicant;
}
