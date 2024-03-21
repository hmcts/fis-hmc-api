package uk.gov.hmcts.reform.hmc.api.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.CaseCategories;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_FILE_VIEW;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomatedHearingTransactionRequestMapper {
    @Value("${ccd.ui.url}")
    private String ccdBaseUrl;

    public AutomatedHearingRequest mappingHearingTransactionRequest(CaseDetails caseDetails) {
        AutomatedHearingRequest hearingRequest = AutomatedHearingRequest.automatedHearingRequestWith().build();
        hearingRequest.setCaseDetails(uk.gov.hmcts.reform.hmc.api.model.request.CaseDetails.automatedCaseDetailsWith()
                                          .hmctsServiceCode("ABA5") //Hardcoded in prl-cos-api
                                          .caseRef(caseDetails.getId().toString())
                                          .requestTimeStamp(LocalDateTime.now())
                                          .externalCaseReference("") //Need to verify
                                          .caseDeepLink(ccdBaseUrl + "caseReference" + CASE_FILE_VIEW) //Need to verify
                                          .hmctsInternalCaseName("")
                                          .publicCaseName("")
                                          .caseAdditionalSecurityFlag(Boolean.TRUE)
                                          .caseInterpreterRequiredFlag(Boolean.TRUE)
                                          .caseCategories(CaseCategories.CaseCategoriesWith().build())
                                          .caseManagementLocationCode("")
                                          .caseRestrictedFlag(Boolean.TRUE)
                                          .caseSlaStartDate("")
                                          .build());
        return hearingRequest;
    }
}
