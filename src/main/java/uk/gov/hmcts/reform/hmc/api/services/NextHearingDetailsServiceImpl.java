package uk.gov.hmcts.reform.hmc.api.services;

import static uk.gov.hmcts.reform.hmc.api.enums.State.DECISION_OUTCOME;
import static uk.gov.hmcts.reform.hmc.api.enums.State.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ADJOURNED;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.COMPLETED;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.LISTED;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.api.enums.State;
import uk.gov.hmcts.reform.hmc.api.model.ccd.NextHearingDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.NextHearingDetailsDTO;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseHearing;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

@Service
@RequiredArgsConstructor
public class NextHearingDetailsServiceImpl implements NextHearingDetailsService {

    private static Logger log = LoggerFactory.getLogger(HearingsServiceImpl.class);

    @Autowired PrlUpdateService prlUpdateService;

    @Override
    public Boolean updateNextHearingDate(Hearings hearings) {
        log.info("inside  updateNextHearingDateInCcd .... ");
        NextHearingDetails nextHearingDetails = getNextHearingDate(hearings);
        Boolean isNextHearingDateUpdated = false;
        if (null != nextHearingDetails) {
            log.info(
                    "Next Hearing Date Details - ID {} and Date {} ",
                    nextHearingDetails.getHearingId(),
                    nextHearingDetails.getNextHearingDate());
            NextHearingDetailsDTO nextHearingDateDetailsDTO =
                    NextHearingDetailsDTO.nextHearingDetailsRequestDTOWith()
                            .nextHearingDetails(nextHearingDetails)
                            .caseRef(hearings.getCaseRef())
                            .build();
            isNextHearingDateUpdated =
                    prlUpdateService.updatePrlServiceWithNextHearingDate(nextHearingDateDetailsDTO);
        }
        return isNextHearingDateUpdated;
    }

    @Override
    public State fetchStateForUpdate(Hearings hearings, String currHearingHmcStatus) {
        log.info("test state .... {}", DECISION_OUTCOME);
        Boolean isAllCompleted =
                hearings.getCaseHearings().stream()
                                .filter(
                                        eachHearing ->
                                                eachHearing.getHmcStatus().equals(COMPLETED)
                                                        || eachHearing
                                                                .getHmcStatus()
                                                                .equals(ADJOURNED))
                                .collect(Collectors.toList())
                                .size()
                        == hearings.getCaseHearings().size();

        if (isAllCompleted) {
            return DECISION_OUTCOME;
        } else {
            if (currHearingHmcStatus.equals(COMPLETED) || currHearingHmcStatus.equals(ADJOURNED)) {
                return anyFutureHearings(hearings)
                        ? PREPARE_FOR_HEARING_CONDUCT_HEARING
                        : DECISION_OUTCOME;
            } else {
                return PREPARE_FOR_HEARING_CONDUCT_HEARING;
            }
        }
    }

    private Boolean anyFutureHearings(Hearings hearings) {
        for (CaseHearing hearing : hearings.getCaseHearings()) {
            Optional<LocalDateTime> minDateOfHearingDaySche =
                    hearing.getHearingDaySchedule().stream()
                            .filter(u -> u.getHearingStartDateTime().isAfter(LocalDateTime.now()))
                            .map(u -> u.getHearingStartDateTime())
                            .min(LocalDateTime::compareTo);

            if (minDateOfHearingDaySche.isPresent()) {
                return true;
            }
        }
        return false;
    }

    public NextHearingDetails getNextHearingDate(Hearings hearings) {

        List<CaseHearing> listedHearings =
                hearings.getCaseHearings().stream()
                        .filter(eachHearing -> eachHearing.getHmcStatus().equals(LISTED))
                        .collect(Collectors.toList());

        LocalDateTime tempNextDateListed = null;
        NextHearingDetails haringDetails = new NextHearingDetails();

        for (CaseHearing listHearing : listedHearings) {
            Optional<LocalDateTime> minDateOfHearingDaySche =
                    listHearing.getHearingDaySchedule().stream()
                            .filter(u -> u.getHearingStartDateTime().isAfter(LocalDateTime.now()))
                            .map(u -> u.getHearingStartDateTime())
                            .min(LocalDateTime::compareTo);

            if (minDateOfHearingDaySche.isPresent()) {
                if (tempNextDateListed == null) {
                    tempNextDateListed = minDateOfHearingDaySche.get();
                    haringDetails.setHearingId(listHearing.getHearingID());
                    haringDetails.setNextHearingDate(tempNextDateListed);
                } else if (tempNextDateListed.isAfter(minDateOfHearingDaySche.get())) {
                    tempNextDateListed = minDateOfHearingDaySche.get();
                    haringDetails.setHearingId(listHearing.getHearingID());
                    haringDetails.setNextHearingDate(tempNextDateListed);
                }
            }
        }
        return haringDetails.getNextHearingDate() != null ? haringDetails : null;
    }
}
