package uk.gov.hmcts.reform.hmc.api.services;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.LISTED;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.api.model.ccd.NextHearingDetails;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseHearing;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

@Service
@RequiredArgsConstructor
public class NextHearingDetailsServiceImpl implements NextHearingDetailsService {

    @Autowired HearingsService hearingsService;
    private static Logger log = LoggerFactory.getLogger(HearingsServiceImpl.class);

    @Override
    public NextHearingDetails getNextHearingDateByCaseRefNo(String caseReference) {

        Hearings hearings = hearingsService.getHearingsByCaseRefNo(caseReference);

        List<CaseHearing> listedHearings =
                hearings.getCaseHearings().stream()
                        .filter(eachHearing -> eachHearing.getHmcStatus().equals(LISTED))
                        .collect(Collectors.toList());

        List<NextHearingDetails> nextHearingDateDetailsList = new ArrayList<>();
        LocalDateTime timeNow = LocalDateTime.now();
        for (CaseHearing listHearing : listedHearings) {

            Long currHearingID = listHearing.getHearingID();

            LocalDateTime nearFuture = null;

            Optional<LocalDateTime> minDate =
                    listHearing.getHearingDaySchedule().stream()
                            .map(u -> u.getHearingStartDateTime())
                            .min(LocalDateTime::compareTo);

            nearFuture = minDate.get();

            boolean isFutureDate = nearFuture.isAfter(timeNow);

            if (isFutureDate) {
                NextHearingDetails hearingDetails =
                        NextHearingDetails.builder()
                                .hearingID(currHearingID)
                                .nextHearingDate(nearFuture)
                                .build();

                nextHearingDateDetailsList.add(hearingDetails);
            }
        }
        NextHearingDetails finalHearingDetails = null;
        if (!nextHearingDateDetailsList.isEmpty()) {
            finalHearingDetails =
                    Collections.min(
                            nextHearingDateDetailsList,
                            Comparator.comparing(c -> c.getNextHearingDate()));
            log.info("Final next hearing date details {}", finalHearingDetails);
        }
        return finalHearingDetails;
    }
}
