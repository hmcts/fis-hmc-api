package uk.gov.hmcts.reform.hmc.api.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseData;


@Slf4j
public class CaseUtils {

    private CaseUtils() {

    }

    public static String getCaseTypeOfApplication(CaseData caseData) {
        log.info("CaseTypeOfApplication ==> " + caseData.getCaseTypeOfApplication());
        return caseData.getCaseTypeOfApplication() != null
            ? caseData.getCaseTypeOfApplication() : caseData.getSelectedCaseTypeID();
    }
}
