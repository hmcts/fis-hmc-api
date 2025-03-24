package uk.gov.hmcts.reform.hmc.api.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseData;

import java.util.Arrays;


@Slf4j
public class CaseUtils {

    private CaseUtils() {

    }

    public static String getCaseTypeOfApplication(CaseData caseData) {
        log.info("CaseTypeOfApplication ==> " + caseData.getCaseTypeOfApplication());
        return caseData.getCaseTypeOfApplication() != null
            ? caseData.getCaseTypeOfApplication() : caseData.getSelectedCaseTypeID();
    }

    public static String formatPhoneNumber(String phoneNumber,
                                           String specialCharacters) {
        String[] specialCharacterList = null != specialCharacters ? specialCharacters.split("") : new String[]{};
        String formattedPhoneNumber = phoneNumber;
        log.info("1: Replace special characters from list from application yaml with empty string");
        log.info("special character list {}", Arrays.toString(specialCharacterList));
        try {
            for (String specialChar : specialCharacterList) {
                formattedPhoneNumber = formattedPhoneNumber.replace(specialChar, "");
            }
        } catch (Exception e) {
            log.error("Error while formatting phone number", e);
        }
        return formattedPhoneNumber;
    }
}
