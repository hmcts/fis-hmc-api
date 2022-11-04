package uk.gov.hmcts.reform.hmc.api.util;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum HearingDataEnum {
    CASE_TYPE_OF_APPLICATION("caseTypeOfApplication"),
    FL401_APPLICATION_TABLE("fl401ApplicantTable"),
    FL401_RESPONDENT_TABLE("fl401RespondentTable"),

    FL401("FL401"),

    C100("C100"),
    RE_MINOR("Re-Minor"),
    APPLICANT_CASE_NAME("applicantCaseName"),
    ISSUE_DATE("issueDate"),

    BBA3("BBA3");

    private final String hearingDataDesc;

    public String getHearingDataDesc() {
        return hearingDataDesc;
    }

    public static HearingDataEnum getValue(String key) {
        return HearingDataEnum.valueOf(key);
    }
}
