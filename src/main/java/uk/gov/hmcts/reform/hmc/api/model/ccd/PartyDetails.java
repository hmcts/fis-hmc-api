package uk.gov.hmcts.reform.hmc.api.model.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

    private String otherGender;
    private String placeOfBirth;
    private DontKnow isAddressUnknown;

    private String addressLivedLessThan5YearsDetails;
    private String landline;

    private String relationshipToChildren;
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

    private Flags partyLevelFlag;

    private UUID partyId;
    private UUID solicitorOrgUuid;
    private UUID solicitorPartyId;
}
