package uk.gov.hmcts.reform.hmc.api.enums.caseflags;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CaseFlag {

    // Party flags
    VULNERABLE_USER("PF0002"),
    UNACCEPTABLE_DISRUPTIVE_CUSTOMER_BEHAVIOUR("PF0007"),
    UNACCOMPANIED_MINOR("PF0013"),
    LANGUAGE_INTERPRETER("PF0015"),
    LACKING_CAPACITY("PF0018"),
    QUALIFIED_LEGAL_REPRESENTATIVE("PF0020"),
    POTENTIALLY_VIOLENT_PERSON("PF0021"),

    // Reasonable Adjustments
    DOCUMENTS_IN_LARGE_PRINT("RA0013"),
    SIGN_LANGUAGE_INTERPRETER("RA0042"),

    // Special Measures
    SCREENING_WITNESS_FROM_ACCUSED("SM0002");

    private final String flagCode;

}
