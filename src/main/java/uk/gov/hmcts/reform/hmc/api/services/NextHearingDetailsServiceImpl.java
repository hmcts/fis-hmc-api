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

    /**
     * This method will update the Prl with next hearing details.
     *
     * @param hearings data to update the next hearing details .
     * @return isNextHearingDetailsUpdated, Boolean - prlNextHearingDetails update.
     */
    @Override
    public Boolean updateNextHearingDetails(Hearings hearings) {
        log.info("inside  updateNextHearingDateInCcd .... ");
        NextHearingDetails nextHearingDetails = getNextHearingDate(hearings);
        Boolean isNextHearingDetailsUpdated = false;
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
            isNextHearingDetailsUpdated =
                    prlUpdateService.updatePrlServiceWithNextHearingDate(nextHearingDateDetailsDTO);
        }
        return isNextHearingDetailsUpdated;
    }

    /**
     * This method will find the final Case Ctate based on the existing hearingStatuses.
     *
     * @param hearings data is used to get the hmcStatus of all the hearings .
     * @return caseState, State - finalCaseState derived based on the given hearings.
     */
    @Override
    public State fetchStateForUpdate(Hearings hearings, String currHearingHmcStatus) {
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

    /**
     * This method will find out the hearings which are in the future for a particular caseRefNo.
     *
     * @param hearings data is used to get the all the hearings which are in the future.
     * @return Boolean, Boolean - return boolean value, based on the future hearing's existence .
     */
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

    /**
     * This method will find one next hearing date out of all the hearings given.
     *
     * @param hearings data is used to find the next hearing date.
     * @return nextHearingDetails, NextHearingDetails - to update prlNextHearingDetails.
     */
    public NextHearingDetails getNextHearingDate(Hearings hearings) {

        List<CaseHearing> listedHearings =
                hearings.getCaseHearings().stream()
                        .filter(eachHearing -> eachHearing.getHmcStatus().equals(LISTED))
                        .collect(Collectors.toList());

        LocalDateTime tempNextDateListed = null;
        NextHearingDetails nextHearingDetails = new NextHearingDetails();

        for (CaseHearing listHearing : listedHearings) {
            Optional<LocalDateTime> minDateOfHearingDaySche =
                    listHearing.getHearingDaySchedule().stream()
                            .filter(u -> u.getHearingStartDateTime().isAfter(LocalDateTime.now()))
                            .map(u -> u.getHearingStartDateTime())
                            .min(LocalDateTime::compareTo);

            if (minDateOfHearingDaySche.isPresent()) {
                if (tempNextDateListed == null) {
                    tempNextDateListed = minDateOfHearingDaySche.get();
                    nextHearingDetails.setHearingId(listHearing.getHearingID());
                    nextHearingDetails.setNextHearingDate(tempNextDateListed);
                } else if (tempNextDateListed.isAfter(minDateOfHearingDaySche.get())) {
                    tempNextDateListed = minDateOfHearingDaySche.get();
                    nextHearingDetails.setHearingId(listHearing.getHearingID());
                    nextHearingDetails.setNextHearingDate(tempNextDateListed);
                }
            }
        }
        return nextHearingDetails.getNextHearingDate() != null ? nextHearingDetails : null;
    }
}
