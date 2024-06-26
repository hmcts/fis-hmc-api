package uk.gov.hmcts.reform.hmc.api.services;

import static uk.gov.hmcts.reform.hmc.api.enums.State.DECISION_OUTCOME;
import static uk.gov.hmcts.reform.hmc.api.enums.State.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ADJOURNED;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CANCELLED;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.COMPLETED;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.LISTED;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.api.enums.State;
import uk.gov.hmcts.reform.hmc.api.model.ccd.NextHearingDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.NextHearingDetailsDTO;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseHearing;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

@Service
@RequiredArgsConstructor
public class NextHearingDetailsServiceImpl implements NextHearingDetailsService {

    private static Logger log = LoggerFactory.getLogger(HearingsServiceImpl.class);

    @Autowired PrlUpdateService prlUpdateService;

    @Value("#{'${hearing_component.futureHearingStatusForNonCancel}'.split(',')}")
    private List<String> futureHearingStatusForNonCancel;

    /**
     * This method will update the Prl with next hearing details.
     *
     * @param hearings data to update the next hearing details .
     * @return isNextHearingDetailsUpdated, Boolean - prlNextHearingDetails update.
     */
    @Override
    public Boolean updateNextHearingDetails(String authorization, Hearings hearings) {
        log.info("inside  updateNextHearingDateInCcd .... ");
        NextHearingDetails nextHearingDetails = getNextHearingDate(hearings);
        Boolean isNextHearingDetailsUpdated = false;
        if (null != nextHearingDetails) {
            log.info(
                    "Next Hearing Date Details - ID {} and Date {} ",
                    nextHearingDetails.getHearingID(),
                    nextHearingDetails.getHearingDateTime());
            NextHearingDetailsDTO nextHearingDateDetailsDTO =
                    NextHearingDetailsDTO.nextHearingDetailsRequestDTOWith()
                            .nextHearingDetails(nextHearingDetails)
                            .caseRef(hearings.getCaseRef())
                            .build();
            isNextHearingDetailsUpdated =
                    prlUpdateService.updatePrlServiceWithNextHearingDate(
                            authorization, nextHearingDateDetailsDTO);
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
        Boolean isAllCompleted = isAllCompletedHearingCheck(hearings);

        if (isAllCompleted || isAllFutureHearingsAreCancelled(hearings)) {
            return DECISION_OUTCOME;
        } else {
            if (currHearingHmcStatus != null
                    && (currHearingHmcStatus.equals(COMPLETED)
                            || currHearingHmcStatus.equals(ADJOURNED))) {
                return Boolean.TRUE.equals(anyFutureHearings(hearings))
                        ? PREPARE_FOR_HEARING_CONDUCT_HEARING
                        : DECISION_OUTCOME;
            } else {
                return PREPARE_FOR_HEARING_CONDUCT_HEARING;
            }
        }
    }

    @NotNull
    private static Boolean isAllCompletedHearingCheck(Hearings hearings) {

        int allHearingSize =  hearings.getCaseHearings().stream().filter(eachHearing ->
                        eachHearing.getHmcStatus().equals(COMPLETED)
                            || eachHearing
                            .getHmcStatus()
                            .equals(CANCELLED)
                            || eachHearing
                            .getHmcStatus()
                            .equals(ADJOURNED))
            .toList()
            .size();
        return allHearingSize == hearings.getCaseHearings().size();
    }

    /**
     * This method will get all the future hearingDaySchedules for a particular case hearing.
     *
     * @param hearing data is used to get all the future hearingDaySchedules.
     * @return HearingDaySchedule, List - return List value, list of all the hearingDayScehdule
     *     which are in future for a hearing .
     */
    private List<HearingDaySchedule> getFutureHearingDaySchedule(CaseHearing hearing) {
        return hearing.getHearingDaySchedule().stream()
                .filter(u -> u.getHearingStartDateTime().isAfter(LocalDateTime.now()))
                .toList();
    }

    /**
     * This method will find out the hearings which are in the future for a particular caseRefNo.
     *
     * @param hearings data is used to get the all the hearings which are in the future.
     * @return Boolean, Boolean - return boolean value, based on the future hearing's existence .
     */
    private Boolean anyFutureHearings(Hearings hearings) {
        for (CaseHearing hearing : hearings.getCaseHearings()) {
            if (hearing.getHearingDaySchedule() != null) {
                List<HearingDaySchedule> futureHearingDaySches =
                        getFutureHearingDaySchedule(hearing);
                if (!futureHearingDaySches.isEmpty()) {
                    return true;
                }
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
    @Override
    public NextHearingDetails getNextHearingDate(Hearings hearings) {

        List<CaseHearing> listedHearings =
                hearings.getCaseHearings().stream()
                        .filter(eachHearing -> eachHearing.getHmcStatus().equals(LISTED))
                        .toList();

        LocalDateTime tempNextDateListed = null;
        NextHearingDetails nextHearingDetails = new NextHearingDetails();

        for (CaseHearing listHearing : listedHearings) {
            Optional<LocalDateTime> minDateOfHearingDaySche =
                    listHearing.getHearingDaySchedule().stream()
                            .filter(u -> u.getHearingStartDateTime().isAfter(LocalDateTime.now()))
                            .map(HearingDaySchedule::getHearingStartDateTime)
                            .min(LocalDateTime::compareTo);

            if (minDateOfHearingDaySche.isPresent()
                    && (tempNextDateListed == null
                            || tempNextDateListed.isAfter(minDateOfHearingDaySche.get()))) {
                tempNextDateListed = minDateOfHearingDaySche.get();
                nextHearingDetails.setHearingID(listHearing.getHearingID());
                nextHearingDetails.setHearingDateTime(tempNextDateListed);
            }
        }
        return nextHearingDetails.getHearingDateTime() != null ? nextHearingDetails : null;
    }

    /**
     * This method will find out the whether all future hearings are cancelled or not for a
     * particular caseRefNo.
     *
     * @param hearings data is used to check whether all the future hearings are cancelled.
     * @return Boolean, Boolean - return boolean value, if all the future hearings are cancelled .
     */
    private Boolean isAllFutureHearingsAreCancelled(Hearings hearings) {
        for (CaseHearing hearing : hearings.getCaseHearings()) {
            if (hearing.getHearingDaySchedule() != null) {

                List<HearingDaySchedule> futureHearingDaySches =
                        getFutureHearingDaySchedule(hearing);
                if (!futureHearingDaySches.isEmpty() && !hearing.getHmcStatus().equals(CANCELLED)) {
                    return false;
                } else if (futureHearingDaySches.isEmpty()
                        && !(hearing.getHmcStatus().equals(CANCELLED)
                                || hearing.getHmcStatus().equals(ADJOURNED)
                                || hearing.getHmcStatus().equals(COMPLETED))) {
                    return false;
                }
            } else if (!futureHearingStatusForNonCancel.stream()
                    .map(String::trim)
                    .filter(status -> status.equals(hearing.getHmcStatus()))
                    .collect(Collectors.toList())
                    .isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
