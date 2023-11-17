package uk.gov.hmcts.reform.hmc.api.services;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.hmc.api.exceptions.RefDataException;
import uk.gov.hmcts.reform.hmc.api.model.request.JudgeRequestDTO;
import uk.gov.hmcts.reform.hmc.api.model.response.JudgeDetail;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@SuppressWarnings("unchecked")
public class RefDataJudicialServiceImpl implements RefDataJudicialService {

    @Autowired
    AuthTokenGenerator authTokenGenerator;

    @Autowired
    IdamTokenGenerator idamTokenGenerator;

    @Autowired
    RefDataJudicialApi refDataJudicialApi;

    @Autowired
    LaunchDarklyClient launchDarklyClient;

    /**
     * This method will get all the judge details of a particular judge(judgeId).
     *
     * @param judgeId data to get Judge details from refData-judicial.
     * @return courtDetail, particular Judge detail.
     */
    @Override
    @SuppressWarnings("unused")
    public JudgeDetail getJudgeDetails(String judgeId) {
        JudgeDetail judgeDetail = null;
        log.info("calling getJudgeDetails service " + judgeId);
        List<String> personalCodeList = new ArrayList<>();
        personalCodeList.add(judgeId);
        JudgeRequestDTO judgeRequestDto =
                JudgeRequestDTO.judgeRequestWith().personalCode(personalCodeList).build();
        try {
            List<JudgeDetail> judgeDetailList = null;
            if (launchDarklyClient.isFeatureEnabled("judicial-v2-change")) {
                log.info("Refdata Judicial API V2 called and LD flag is ON");
                judgeDetailList = refDataJudicialApi.getJudgeDetailsV2(
                    idamTokenGenerator.generateIdamTokenForRefData(),
                    authTokenGenerator.generate(),
                    judgeRequestDto
                );
            } else {
                log.info("Refdata Judicial API V1 called and LD flag is OFF");
                judgeDetailList = refDataJudicialApi.getJudgeDetails(
                    idamTokenGenerator.generateIdamTokenForRefData(),
                    authTokenGenerator.generate(),
                    judgeRequestDto
                );
            }

            log.info("RefData Judicial call completed successfully" + judgeDetailList);

            if (!judgeDetailList.isEmpty()) {
                judgeDetail = judgeDetailList.get(0);
                log.info("Judge details filtered" + judgeDetail.getHearingJudgeName());
            }
            return judgeDetail;
        } catch (HttpClientErrorException | HttpServerErrorException exception) {
            log.info("RefData Judicial call HttpClientError exception {}", exception.getMessage());
            throw new RefDataException("RefData", exception.getStatusCode(), exception);
        } catch (FeignException exception) {
            log.info("RefData Judicial call Feign exception {}", exception.getMessage());
        }
        return judgeDetail;
    }
}
