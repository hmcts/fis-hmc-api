package uk.gov.hmcts.reform.hmc.api.services;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.LISTED;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.model.request.Cases;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseHearing;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.model.response.JudgeDetail;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class HearingsServiceImpl implements HearingsService {

    @Autowired AuthTokenGenerator authTokenGenerator;

    @Autowired IdamTokenGenerator idamTokenGenerator;

    @Autowired RefDataService refDataService;

    @Autowired RefDataJudicialService refDataJudicialService;

    @Autowired HearingApiClient hearingApiClient;

    private Hearings hearingDetails;

    RestTemplate restTemplate = new RestTemplate();
    private static Logger log = LoggerFactory.getLogger(HearingsServiceImpl.class);

    /**
     * This method will fetch all the hearings which belongs to a particular caseRefNumber.
     *
     * @param caseReference CaseRefNumber to take all the hearings belongs to this case.
     * @param authorization authorization header.
     * @param serviceAuthorization serviceAuthorization header
     * @return caseHearingsResponse, all the hearings which belongs to a particular caseRefNumber.
     */
    @Override
    public Hearings getHearingsByCaseRefNo(
            String caseReference, String authorization, String serviceAuthorization) {

        final String userToken = idamTokenGenerator.generateIdamTokenForHearingCftData();
        final String s2sToken = authTokenGenerator.generate();

        try {
            hearingDetails = hearingApiClient.getHearingDetails(userToken, s2sToken, caseReference);
            integrateVenueDetails(hearingDetails);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return hearingDetails;
    }

    /**
     * This method will fetch all the hearings which belongs to a particular caseRefNumber.
     *
     * @param cases CaseRefNumber to take all the hearings belongs to this case.
     * @param authorization authorization header.
     * @param serviceAuthorization serviceAuthorization header
     * @return caseHearingsResponse, all the hearings which belongs to a particular caseRefNumber.
     */
    @Override
    public List<Hearings> getHearingsByListOfCaseRefNos(
            Cases cases, String authorization, String serviceAuthorization) {

        List<Hearings> casesWithHearings = new ArrayList<>();
        if (cases != null && !cases.getCaseIds().isEmpty()) {
            final String userToken = idamTokenGenerator.generateIdamTokenForHearingCftData();
            final String s2sToken = authTokenGenerator.generate();

            for (String caseId : cases.getCaseIds()) {
                try {
                    hearingDetails =
                            hearingApiClient.getHearingDetails(userToken, s2sToken, caseId);
                    casesWithHearings.add(hearingDetails);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }

        return casesWithHearings;
    }

    private void integrateVenueDetails(Hearings caseHearingsResponse) {

        if (caseHearingsResponse != null && caseHearingsResponse.getCaseHearings() != null) {
            List<CaseHearing> caseHearings = caseHearingsResponse.getCaseHearings();
            for (CaseHearing caseHearing : caseHearings) {

                if (caseHearing.getHmcStatus().equals(LISTED)
                        && caseHearing.getHearingDaySchedule() != null) {
                    for (HearingDaySchedule hearingSchedule : caseHearing.getHearingDaySchedule()) {
                        String venueId = hearingSchedule.getHearingVenueId();

                        String judgeId = hearingSchedule.getHearingJudgeId();
                        log.info("judgeId {}", judgeId);
                        if (null != venueId) {
                            log.info("VenueId {}", venueId);
                            CourtDetail courtDetail = refDataService.getCourtDetails(venueId);
                            if (courtDetail != null) {
                                hearingSchedule.setHearingVenueName(
                                        courtDetail.getHearingVenueName());
                                hearingSchedule.setHearingVenueAddress(
                                        courtDetail.getHearingVenueAddress());
                                hearingSchedule.setHearingVenueLocationCode(
                                        courtDetail.getHearingVenueLocationCode());
                            }
                        }

                        if (null != judgeId) {
                            log.info("judgeId==> {}", judgeId);
                            JudgeDetail judgeDetail =
                                    refDataJudicialService.getJudgeDetails(judgeId);
                            if (judgeDetail != null) {
                                hearingSchedule.setHearingJudgeName(
                                        judgeDetail.getHearingJudgeName());
                            }
                        }
                    }
                }
            }
            caseHearingsResponse.setCaseHearings(caseHearings);
        }
    }
}
