package uk.gov.hmcts.reform.hmc.api.utils;

import java.util.Locale;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.EMPTY;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.EMPTY_STRING;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.VERSUS;

public final class PublicCaseNameUtils {

    private PublicCaseNameUtils() {
    }

    public static String buildFl401PublicCaseName(
        String caseReference,
        String applicantSurname,
        String respondentSurname
    ) {
        String applicantInitial = getSurnameInitial(applicantSurname);
        String respondentInitial = getSurnameInitial(respondentSurname);

        if (caseReference == null || caseReference.isBlank()
            || applicantInitial.isEmpty() || respondentInitial.isEmpty()) {
            return EMPTY;
        }

        return caseReference + EMPTY_STRING + applicantInitial + VERSUS + respondentInitial;
    }

    private static String getSurnameInitial(String surname) {
        if (surname == null || surname.isBlank()) {
            return EMPTY;
        }

        String trimmedSurname = surname.strip();
        int firstCodePoint = trimmedSurname.codePointAt(0);
        if (!Character.isLetter(firstCodePoint)) {
            return EMPTY;
        }

        return new String(Character.toChars(firstCodePoint)).toUpperCase(Locale.ROOT);
    }
}
