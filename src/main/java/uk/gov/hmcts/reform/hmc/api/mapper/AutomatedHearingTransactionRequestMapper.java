package uk.gov.hmcts.reform.hmc.api.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingCaseCategories;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingCaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.AND;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.C100;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_FILE_VIEW;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_SUB_TYPE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_TYPE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CATEGORY_VALUE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.EMPTY;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.FALSE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.FL401;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.FL401_APPLICANT_TABLE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.FL401_RESPONDENT_TABLE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.LAST_NAME;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.RE_MINOR;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomatedHearingTransactionRequestMapper {
    @Value("${ccd.ui.url}")
    private String ccdBaseUrl;

    public AutomatedHearingRequest mappingAutomatedHearingTransactionRequest(CaseDetails caseDetails) {
        AutomatedHearingRequest hearingRequest = AutomatedHearingRequest.automatedHearingRequestWith().build();

        String publicCaseNameMapper = EMPTY;
        if (C100.equals(caseDetails.getData().get(CASE_TYPE_OF_APPLICATION))) {
            publicCaseNameMapper = RE_MINOR;
        } else if (FL401.equals(caseDetails.getData().get(CASE_TYPE_OF_APPLICATION))) {
            @SuppressWarnings("unchecked")
            Map<String, String> applicantMap = (LinkedHashMap) caseDetails.getData().get(FL401_APPLICANT_TABLE);
            @SuppressWarnings("unchecked")
            Map<String, String> respondentTableMap = (LinkedHashMap) caseDetails.getData().get(FL401_RESPONDENT_TABLE);
            if (applicantMap != null && respondentTableMap != null) {
                publicCaseNameMapper = applicantMap.get(LAST_NAME) + AND + respondentTableMap.get(LAST_NAME);
            } else {
                publicCaseNameMapper = EMPTY;
            }
        }

        hearingRequest.setCaseDetails(AutomatedHearingCaseDetails.automatedHearingCaseDetailsWith()
                                          .hmctsServiceCode("ABA5") //Hardcoded in prl-cos-api
                                          .caseRef(caseDetails.getId().toString())
                                          .requestTimeStamp(LocalDateTime.now()) //Need to verify
                                          .externalCaseReference("") //Need to verify
                                          .caseDeepLink(ccdBaseUrl + "caseReference" + CASE_FILE_VIEW) //Need to verify
                                          .hmctsInternalCaseName("") //Need to verify
                                          .publicCaseName(publicCaseNameMapper)
                                          .caseAdditionalSecurityFlag(FALSE)
                                          .caseInterpreterRequiredFlag(FALSE)
                                          .caseCategories(getCaseCategories())
                                          .caseManagementLocationCode("")
                                          .caseRestrictedFlag(Boolean.TRUE)
                                          .caseSlaStartDate("")
                                          .build());
        return hearingRequest;
    }

    private List<AutomatedHearingCaseCategories> getCaseCategories() {
        List<AutomatedHearingCaseCategories> caseCategoriesList = new ArrayList<>();
        AutomatedHearingCaseCategories caseCategories =
            AutomatedHearingCaseCategories.AutomatedHearingCaseCategoriesWith()
                .categoryType(CASE_TYPE)
                .categoryValue(CATEGORY_VALUE)
                .build();

        AutomatedHearingCaseCategories caseSubCategories =
            AutomatedHearingCaseCategories.AutomatedHearingCaseCategoriesWith()
                .categoryType(CASE_SUB_TYPE)
                .categoryValue(CATEGORY_VALUE)
                .categoryParent(CATEGORY_VALUE)
                .build();

        caseCategoriesList.add(caseCategories);
        caseCategoriesList.add(caseSubCategories);
        return caseCategoriesList;
    }
}
