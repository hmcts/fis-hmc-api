package uk.gov.hmcts.reform.hmc.api.services;

import feign.FeignException;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.exceptions.RefDataException;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingDTO;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingUpdateDTO;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.VenuesDetail;

import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefDataServiceImpl implements RefDataService {

    final AuthTokenGenerator authTokenGenerator;

    final IdamTokenGenerator idamTokenGenerator;
    final RefDataApi refDataApi;
    final RefDataClient refDataClient;

    @Value("#{'${hearing_component.familyCourtIds}'.split(',')}")
    private List<String> familyCourtIds;

    // Counters to monitor usage of the new contract vs legacy fallback. Not final so Lombok
    // doesn't include them in the generated constructor.
    private AtomicLong newContractUsedCounter = new AtomicLong(0);
    private AtomicLong legacyFallbackUsedCounter = new AtomicLong(0);

    /**
     * This method will get all the court details of a particular venueId(epimmsId).
     *
     * @param epimmsId data to get court details from refData.
     * @return courtDetail, particular Court detail.
     */
    @Override
    @SuppressWarnings("unused")
    public CourtDetail getCourtDetails(String epimmsId) {
        CourtDetail courtDetail = null;
        log.info("Johnny: calling getCourtDetails service {}", epimmsId);
        try {
            final List<String> courtIds =
                    familyCourtIds.stream().map(String::trim).toList();
            // First try the new API contract which returns a single pre-filtered CourtDetail.
            CourtDetail returnedCourtDetail = callRefDataNewContract(epimmsId);

            // If new-contract returned a valid matching court, use it. Otherwise fall back to legacy behaviour.
            final String returnedCourtTypeId = returnedCourtDetail != null ? returnedCourtDetail.getCourtTypeId() : null;
            if (returnedCourtDetail != null && returnedCourtTypeId != null
                    && courtIds.stream().anyMatch(courtId -> courtId.equals(returnedCourtTypeId))) {
                courtDetail = returnedCourtDetail;
                if (courtDetail.getHearingVenueAddress() != null) {
                    courtDetail.setHearingVenueAddress(courtDetail.getHearingVenueAddress());
                }
                newContractUsedCounter.incrementAndGet();
                log.info("Using new-contract CourtDetail {} (newContractUsedCount={})",
                         courtDetail, newContractUsedCounter.get());
            } else {
                // Legacy fallback: call API that returns a list and apply the existing filtering logic
                List<CourtDetail> courtDetailList = callRefDataLegacyList(epimmsId);
                log.info("RefData legacy call completed successfully {}", courtDetailList);

                List<CourtDetail> filteredCourtDetail = courtDetailList.stream()
                        .filter(courtDetail1 -> courtIds.stream()
                                .anyMatch(courtId -> courtId.equals(courtDetail1.getCourtTypeId())))
                        .toList();
                if (!filteredCourtDetail.isEmpty()) {
                    courtDetail = filteredCourtDetail.get(0);
                    if (courtDetail.getHearingVenueAddress() != null) {
                        courtDetail.setHearingVenueAddress(courtDetail.getHearingVenueAddress());
                    }
                    legacyFallbackUsedCounter.incrementAndGet();
                    log.info("Using legacy-filtered CourtDetail {} (legacyFallbackUsedCount={})",
                             courtDetail, legacyFallbackUsedCounter.get());
                }
            }
        } catch (HttpClientErrorException | HttpServerErrorException exception) {
            log.info("RefData call HttpClientError exception {}", exception.getMessage());
            throw new RefDataException("RefData", exception.getStatusCode(), exception);
        } catch (FeignException exception) {
            log.info("RefData call Feign exception {}", exception.getMessage());
        }
        return courtDetail;
    }

    private CourtDetail callRefDataNewContract(String epimmsId) {
        // Delegate to RefDataClient which handles token generation and error handling
        return refDataClient.fetchCourtDetail(epimmsId);
    }

    private List<CourtDetail> callRefDataLegacyList(String epimmsId) {
        return refDataClient.fetchCourtDetailList(epimmsId);
    }

    /**
     * This method will update the hearing with court details.
     *
     * @param hearingDto data to be updated with court details.
     * @return hearing, updated hearing with Court detail received from refData.
     */
    @Override
    @SuppressWarnings("unused")
    public HearingDTO getHearingWithCourtDetails(HearingDTO hearingDto) {
        log.info(
                "calling getHearingWithCourtDetails service "
                        + hearingDto.getHearingUpdate().getHearingVenueId());
        CourtDetail courtDetail =
                getCourtDetails(hearingDto.getHearingUpdate().getHearingVenueId());
        log.info("courtDetails " + courtDetail);
        if (courtDetail != null) {
            HearingUpdateDTO hearingUpdateDto = hearingDto.getHearingUpdate();
            hearingUpdateDto.setHearingVenueName(courtDetail.getHearingVenueName());
            hearingUpdateDto.setHearingVenueAddress(courtDetail.getHearingVenueAddress());
            hearingUpdateDto.setHearingVenueLocationCode(courtDetail.getHearingVenueLocationCode());
            hearingUpdateDto.setCourtTypeId(courtDetail.getCourtTypeId());
            hearingDto.hearingRequestDTOWith().hearingUpdate(hearingUpdateDto).build();
        }
        return hearingDto;
    }

    /**
     * Expose counters for monitoring and tests.
     */
    public long getNewContractUsedCount() {
        return newContractUsedCounter.get();
    }

    public long getLegacyFallbackUsedCount() {
        return legacyFallbackUsedCounter.get();
    }

    /**
     * This method will get all the court details of a particular venueId(serviceCode).
     *
     * @param serviceCode data to get court details from refData.
     * @return courtDetail, particular Court detail.
     */
    @Override
    @SuppressWarnings("unused")
    public List<CourtDetail> getCourtDetailsByServiceCode(String serviceCode) {
        List<CourtDetail> courtVenues = new ArrayList<>();
        log.info("calling getCourtDetails service with serviceCode {} ", serviceCode);
        try {
            VenuesDetail venueDetail = refDataClient.fetchByServiceCode(serviceCode);
            log.info("RefData call for allVenues completed successfully ");

            final boolean match = familyCourtIds.stream().map(String::trim).toList().stream()
                            .anyMatch(courtId -> courtId.equals(venueDetail.getCourtTypeId()));
            if (venueDetail != null && !venueDetail.getCourtVenues().isEmpty() && match) {
                courtVenues = venueDetail.getCourtVenues();
            }
        } catch (HttpClientErrorException | HttpServerErrorException exception) {
            log.info(
                    "RefData call for allVenues  HttpClientError exception {}",
                    exception.getMessage());
            throw new RefDataException("RefData", exception.getStatusCode(), exception);
        } catch (FeignException exception) {
            log.info("RefData call for allVenues  Feign exception {}", exception.getMessage());
        }
        return courtVenues;
    }
}
