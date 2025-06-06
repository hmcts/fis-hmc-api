package uk.gov.hmcts.reform.hmc.api.services;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.mapper.AutomatedHearingTransformer;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseData;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseHearing;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingResponse;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.model.response.JudgeDetail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.AWAITING_HEARING_DETAILS;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CANCELLED;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.COMPLETED;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.LISTED;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.OPEN;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ROLE_ASSIGNMENT_ATTRIBUTE_CASE_TYPE;

@Service
@RequiredArgsConstructor
@Slf4j
public class HearingsServiceImpl implements HearingsService {

    @Value("${ccd.ui.url}")
    private String ccdBaseUrl;

    @Value("${hearing.specialCharacters}")
    private String specialCharacters;

    @Value("${hearing_component.api.url}")
    private String basePath;

    @Value("#{'${cafcass.excludeHearingStates}'.split(',')}")
    private List<String> hearingStatesToBeExcluded;

    @Value("#{'${hearing_component.futureHearingStatus}'.split(',')}")
    private List<String> futureHearingStatusList;

    private final AuthTokenGenerator authTokenGenerator;
    private final IdamTokenGenerator idamTokenGenerator;
    private final RefDataService refDataService;
    private final RefDataJudicialService refDataJudicialService;
    private final HearingApiClient hearingApiClient;

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

        Hearings caseHearingsResponse = null;

        try {
            log.info("Fetching hearings for casereference received from cos api - {}", caseReference);
            final String s2sToken = authTokenGenerator.generate();
            caseHearingsResponse = hearingApiClient.getHearingDetails(idamTokenGenerator.generateIdamTokenForHearingCftData(),
                            s2sToken,
                            caseReference);
            log.info("Fetch hearings call completed successfully");

            integrateVenueDetails(caseHearingsResponse);
            log.info(
                "Number of hearings fetched for casereference - {} is {}",
                caseReference,
                isCaseHearing(caseHearingsResponse)
                            ? caseHearingsResponse.getCaseHearings().size()
                            : 0);

        } catch (HttpClientErrorException | HttpServerErrorException exception) {
            log.error(
                    "HttpClientErrorException {} during getHearingsByCaseRefNo for case {}",
                    exception,
                    caseReference);
        } catch (Exception exception) {
            log.error(
                    "Exception {} during getHearingsByCaseRefNo for case {}",
                    exception,
                    caseReference);
        }
        return caseHearingsResponse;
    }

    private static boolean isCaseHearing(Hearings caseHearingsResponse) {
        return caseHearingsResponse != null && caseHearingsResponse.getCaseHearings() != null;
    }

    private void integrateVenueDetails(Hearings caseHearingsResponse) {

        if (isCaseHearing(caseHearingsResponse)) {
            List<CaseHearing> caseHearings = caseHearingsResponse.getCaseHearings();
            for (CaseHearing caseHearing : caseHearings) {

                if (List.of(LISTED, AWAITING_HEARING_DETAILS, COMPLETED).contains(caseHearing.getHmcStatus())
                        && caseHearing.getHearingDaySchedule() != null) {
                    for (HearingDaySchedule hearingSchedule : caseHearing.getHearingDaySchedule()) {
                        String venueId = hearingSchedule.getHearingVenueId();

                        String judgeId = hearingSchedule.getHearingJudgeId();
                        log.info("judgeId {}", judgeId);
                        setHearingSchedule(hearingSchedule, venueId);

                        setHearingJudgeName(hearingSchedule, judgeId);
                    }
                }
            }
        }
    }

    private void setHearingSchedule(HearingDaySchedule hearingSchedule, String venueId) {
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
    }

    private void setHearingJudgeName(HearingDaySchedule hearingSchedule, String judgeId) {
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

    /**
     * This method will fetch all the hearings which belongs to a particular caseRefNumber.
     *
     * @param caseIdWithRegionIdMap caseIdWithRegionId map to take all the hearings belongs to each
     *     case.
     * @param authorization authorization header.
     * @param serviceAuthorization serviceAuthorization header
     * @return casesWithHearings, List of cases with all the hearings which belongs to all caseIds
     *     passed.
     */
    @Override
    public List<Hearings> getHearingsByListOfCaseIds(
            Map<String, String> caseIdWithRegionIdMap,
            String authorization,
            String serviceAuthorization) {

        List<Hearings> casesWithHearings = new ArrayList<>();
        if (!caseIdWithRegionIdMap.isEmpty()) {
            final String userToken = idamTokenGenerator.generateIdamTokenForHearingCftData();
            final String s2sToken = authTokenGenerator.generate();
            List<Hearings>  hearingDetailsList =
                    hearingApiClient.getListOfHearingDetails(
                            userToken, s2sToken, new ArrayList<>(caseIdWithRegionIdMap.keySet()),
                            ROLE_ASSIGNMENT_ATTRIBUTE_CASE_TYPE);
            for (var hearing : hearingDetailsList) {
                try {
                    List<CaseHearing> filteredHearings = hearing.getCaseHearings();
                    log.info("Excluded hearing statuses {}", hearingStatesToBeExcluded);
                    if (CollectionUtils.isNotEmpty(hearingStatesToBeExcluded)) {
                        filteredHearings = filteredHearings.stream()
                                .filter(
                                    eachHearing ->
                                        !hearingStatesToBeExcluded.contains(eachHearing.getHmcStatus()))
                                .toList();
                    }
                    Hearings filteredCaseHearingsWithCount =
                            Hearings.hearingsWith()
                                    .caseHearings(filteredHearings)
                                    .caseRef(hearing.getCaseRef())
                                    .hmctsServiceCode(hearing.getHmctsServiceCode())
                                    .build();
                    casesWithHearings.add(filteredCaseHearingsWithCount);
                } catch (HttpClientErrorException | HttpServerErrorException exception) {
                    log.info(
                            "Hearing api call HttpClientError exception {}",
                            exception.getMessage());
                } catch (FeignException exception) {
                    log.info("Hearing api call Feign exception {}", exception.getMessage());
                } catch (Exception exception) {
                    log.info("Hearing api call Exception exception {}", exception.getMessage());
                }
            }
            if (!casesWithHearings.isEmpty()) {
                List<CourtDetail> allVenues =
                        refDataService.getCourtDetailsByServiceCode(
                            hearingDetailsList.get(0).getHmctsServiceCode());

                integrateVenueDetailsForCaseId(allVenues, casesWithHearings, caseIdWithRegionIdMap);
            }
        }

        return casesWithHearings;
    }

    @Override
    public List<Hearings> getHearingsByListOfCaseIdsWithoutCourtVenueDetails(
        List<String> listOfCaseIds,
        String authorization, String serviceAuthorization) {

        List<Hearings> casesWithHearings = new ArrayList<>();
        final String userToken = idamTokenGenerator.generateIdamTokenForHearingCftData();
        final String s2sToken = authTokenGenerator.generate();
        List<Hearings> hearingDetailsList =
            hearingApiClient.getListOfHearingDetails(
                userToken, s2sToken, listOfCaseIds, ROLE_ASSIGNMENT_ATTRIBUTE_CASE_TYPE);
        log.info("returning total count of hearings: {}", hearingDetailsList.size());
        if (CollectionUtils.isNotEmpty(hearingDetailsList)) {
            log.info("Hearing list not empty");
            List<String> returnedCaseIds = new ArrayList<>();
            hearingDetailsList.forEach(hearingList -> returnedCaseIds.add(hearingList.getCaseRef()));
            log.info("returned case ids: {}", returnedCaseIds);
            log.info("is listOfCaseIds equal to returned case ids: {}", listOfCaseIds.containsAll(returnedCaseIds));
            for (var hearing : hearingDetailsList) {
                log.info("fis processing {}",hearing.getCaseRef());
                try {
                    log.info("try block hit");
                    List<CaseHearing> filteredHearings =
                        hearing.getCaseHearings().stream()
                            .filter(
                                eachHearing ->
                                    eachHearing.getHmcStatus().equals(LISTED)
                                        || eachHearing
                                        .getHmcStatus()
                                        .equals(CANCELLED)
                                        || eachHearing
                                        .getHmcStatus()
                                        .equals(COMPLETED))
                            .toList();
                    log.info("checking hearings for case id {}", hearing.getCaseRef());
                    Hearings filteredCaseHearingsWithCount =
                        Hearings.hearingsWith()
                            .caseHearings(filteredHearings)
                            .caseRef(hearing.getCaseRef())
                            .hmctsServiceCode(hearing.getHmctsServiceCode())
                            .build();
                    log.info("adding hearing with case id {}", filteredCaseHearingsWithCount.getCaseRef());
                    casesWithHearings.add(filteredCaseHearingsWithCount);
                } catch (HttpClientErrorException | HttpServerErrorException exception) {
                    log.info(
                        "Hearing api call HttpClientError exception {}",
                        exception.getMessage()
                    );
                } catch (FeignException exception) {
                    log.info("Hearing api call Feign exception {}", exception.getMessage());
                } catch (Exception exception) {
                    log.info("Hearing api call Exception exception {}", exception.getMessage());
                }
            }
        }
        return casesWithHearings;
    }

    @Override
    public Map<String, List<String>> getHearingsListedForCurrentDateByListOfCaseIdsWithoutCourtVenueDetails(
        List<String> listOfCaseIds,
        String authorization, String serviceAuthorization) {
        final String userToken = idamTokenGenerator.generateIdamTokenForHearingCftData();
        final String s2sToken = authTokenGenerator.generate();
        List<Hearings> hearingDetailsList = new ArrayList<>();
        try {
            hearingDetailsList.addAll(hearingApiClient.getListOfHearingDetails(userToken, s2sToken, listOfCaseIds,
                                                                                     ROLE_ASSIGNMENT_ATTRIBUTE_CASE_TYPE));
        } catch (Exception exception) {
            log.error("Hearing api call Exception exception {}", exception.getMessage());
        }
        Map<String, List<String>> caseIdHearingIdMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(hearingDetailsList)) {
            log.info("Hearing details retrieved from hmc");
            List<String> validHearingIds = new ArrayList<>();
            for (var caseWithHearings : hearingDetailsList) {
                log.info("checking hearing details for Case id {}", caseWithHearings.getCaseRef());
                validHearingIds.clear();
                caseWithHearings.getCaseHearings().stream()
                    .filter(eachHearing -> eachHearing.getHmcStatus().equals(AWAITING_HEARING_DETAILS))
                    .forEach(hearing -> {
                        if (isHearingScheduledToday(hearing)) {
                            log.info("Hearing is scheduled for today");
                            validHearingIds.add(hearing.getHearingID().toString());
                        }
                    });
                if (!validHearingIds.isEmpty()) {
                    log.info("There are hearings listed for today");
                    caseIdHearingIdMap.put(caseWithHearings.getCaseRef(), validHearingIds);
                }
            }
        }
        return caseIdHearingIdMap;
    }

    private boolean isHearingScheduledToday(CaseHearing hearing) {
        return CollectionUtils.isNotEmpty(hearing.getHearingDaySchedule())
            && hearing.getHearingDaySchedule().stream().anyMatch(hearingDaySchedule -> LocalDate.now()
            .equals(hearingDaySchedule.getHearingStartDateTime().toLocalDate()));
    }

    private void integrateVenueDetailsForCaseId(
            List<CourtDetail> allVenues,
            List<Hearings> casesWithHearings,
            Map<String, String> caseIdWithRegionIdMap) {

        for (Hearings hearings : casesWithHearings) {
            if (null != hearings && !hearings.getCaseHearings().isEmpty()) {
                CourtDetail caseCourt = getCourtDetail(allVenues, caseIdWithRegionIdMap, hearings);
                if (caseCourt != null) {
                    hearings.setCourtTypeId(caseCourt.getCourtTypeId());
                    hearings.setCourtName(caseCourt.getHearingVenueName());
                }

                for (CaseHearing caseHearing : hearings.getCaseHearings()) {
                    for (HearingDaySchedule hearingSchedule : caseHearing.getHearingDaySchedule()) {
                        CourtDetail matchedCourt = getMatchedCourtDetail(
                            allVenues,
                            caseIdWithRegionIdMap,
                            hearings,
                            hearingSchedule
                        );
                        setMatchedCourtHearingSchedule(hearingSchedule, matchedCourt);
                    }
                }
            }
        }
    }

    private static CourtDetail getMatchedCourtDetail(List<CourtDetail> allVenues, Map<String,
        String> caseIdWithRegionIdMap, Hearings hearings, HearingDaySchedule hearingSchedule) {
        CourtDetail matchedCourt = null;
        if (hearingSchedule.getHearingVenueId() != null) {
            matchedCourt = getCourtDetailByVenueIdAndCourtStatus(allVenues, hearingSchedule);
        } else {
            String regionId = null;
            regionId =
                    caseIdWithRegionIdMap.get(hearings.getCaseRef()).split("-")[0];

            if (regionId != null) {
                matchedCourt = getCourtDetailByRegionIdAndCourtStatus(allVenues, regionId);
            }
        }
        return matchedCourt;
    }

    private static void setMatchedCourtHearingSchedule(HearingDaySchedule hearingSchedule, CourtDetail matchedCourt) {
        if (matchedCourt != null) {
            hearingSchedule.setHearingVenueName(matchedCourt.getHearingVenueName());
            hearingSchedule.setHearingVenueAddress(
                matchedCourt.getHearingVenueAddress() != null
                    ? matchedCourt.getHearingVenueAddress()
                    .concat(" " + matchedCourt.getHearingVenuePostCode()) : null);
            hearingSchedule.setHearingVenueLocationCode(
                matchedCourt.getHearingVenueLocationCode());
            hearingSchedule.setCourtTypeId(matchedCourt.getCourtTypeId());
        }
    }

    private static CourtDetail getCourtDetailByRegionIdAndCourtStatus(List<CourtDetail> allVenues, String regionId) {
        CourtDetail matchedCourt;
        String finalRegionId = regionId;
        matchedCourt =
                allVenues.stream()
                        .filter(
                                e ->
                                        finalRegionId.equals(
                                                        e.getRegionId())
                                                && OPEN.equals(
                                                        e.getCourtStatus()))
                        .findFirst()
                        .orElse(null);
        return matchedCourt;
    }

    private static CourtDetail getCourtDetailByVenueIdAndCourtStatus(List<CourtDetail> allVenues,
                                                                     HearingDaySchedule hearingSchedule) {
        CourtDetail matchedCourt;
        String venueId = hearingSchedule.getHearingVenueId();
        matchedCourt =
                allVenues.stream()
                        .filter(
                                e ->
                                        venueId.equals(e.getHearingVenueId())
                                                && OPEN.equals(
                                                        e.getCourtStatus()))
                        .findFirst()
                        .orElse(null);
        return matchedCourt;
    }

    private static CourtDetail getCourtDetail(List<CourtDetail> allVenues,
                                              Map<String, String> caseIdWithRegionIdMap, Hearings hearings) {
        return allVenues.stream()
                        .filter(
                                e ->
                                        caseIdWithRegionIdMap
                                                        .get(hearings.getCaseRef())
                                                        .split("-")[0]
                                                        .equals(e.getRegionId())
                                                && caseIdWithRegionIdMap
                                                        .get(hearings.getCaseRef())
                                                        .split("-")[1]
                                                        .equals(e.getHearingVenueId())
                                                && OPEN.equals(e.getCourtStatus()))
                        .findFirst()
                        .orElse(null);
    }

    @Override
    public Hearings getFutureHearings(String caseReference) {

        final String userToken = idamTokenGenerator.generateIdamTokenForHearingCftData();
        final String s2sToken = authTokenGenerator.generate();
        Hearings futureHearingsResponse = null;
        try {
            Hearings hearingDetails = hearingApiClient.getHearingDetails(userToken, s2sToken, caseReference);

            final List<String> hearingStatuses =
                    futureHearingStatusList.stream().map(String::trim).toList();

            final List<CaseHearing> filteredHearingsByStatus =
                    hearingDetails.getCaseHearings().stream()
                            .filter(
                                    hearing ->
                                            hearingStatuses.stream()
                                                    .anyMatch(
                                                            hearingStatus ->
                                                                    hearingStatus.equals(
                                                                            hearing
                                                                                    .getHmcStatus())))
                            .toList();

            final List<CaseHearing> allFutureHearings =
                    filteredHearingsByStatus.stream()
                            .filter(
                                    hearing ->
                                            hearing.getHearingDaySchedule() != null
                                                    && !hearing.getHearingDaySchedule().stream()
                                                                    .filter(
                                                                            hearDaySche ->
                                                                                    hearDaySche
                                                                                            .getHearingStartDateTime()
                                                                                            .isAfter(
                                                                                                    LocalDateTime
                                                                                                            .now()))
                                                                    .toList()
                                                                    .isEmpty()).toList();

            futureHearingsResponse =
                    Hearings.hearingsWith()
                            .caseHearings(allFutureHearings)
                            .caseRef(hearingDetails.getCaseRef())
                            .hmctsServiceCode(hearingDetails.getHmctsServiceCode())
                            .build();
        } catch (HttpClientErrorException | HttpServerErrorException exception) {
            log.info("Hearing api call HttpClientError exception {}", exception.getMessage());
        } catch (FeignException exception) {
            log.info("Hearing api call Feign exception {}", exception.getMessage());
        } catch (Exception exception) {
            log.info("Hearing api call Exception exception {}", exception.getMessage());
        }

        return futureHearingsResponse;
    }

    @Override
    public HearingResponse createAutomatedHearings(CaseData caseData) {

        final String userToken = idamTokenGenerator.generateIdamTokenForHearingCftData();
        final String s2sToken = authTokenGenerator.generate();
        AutomatedHearingRequest hearingRequest = AutomatedHearingTransformer.mappingHearingTransactionRequest(
            caseData, ccdBaseUrl, specialCharacters);
        HearingResponse hearingResponse = hearingApiClient.createHearingDetails(
            userToken,
            s2sToken,
            hearingRequest
        );
        return HearingResponse.builder()
            .status(hearingResponse.getStatus())
            .versionNumber(hearingResponse.getVersionNumber())
            .hearingRequestID(hearingResponse.getHearingRequestID())
            .timeStamp(hearingResponse.getTimeStamp())
            .build();
    }
}
