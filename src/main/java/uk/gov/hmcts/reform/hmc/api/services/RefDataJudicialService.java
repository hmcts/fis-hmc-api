package uk.gov.hmcts.reform.hmc.api.services;

import uk.gov.hmcts.reform.hmc.api.model.response.JudgeDetail;

public interface RefDataJudicialService {
    JudgeDetail getJudgeDetails(String judgeId);
}
