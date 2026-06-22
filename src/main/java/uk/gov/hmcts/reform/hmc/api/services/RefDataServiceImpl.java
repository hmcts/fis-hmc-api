package uk.gov.hmcts.reform.hmc.api.services;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.hmc.api.exceptions.RefDataException;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingDTO;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingUpdateDTO;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.VenuesDetail;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.HMCTS_SERVICE_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefDataServiceImpl implements RefDataService {

    final RefDataClient refDataClient;

    @Value("#{'${hearing_component.familyCourtIds}'.split(',')}")
    private List<String> familyCourtIds;

    /**
     * This method will get all the court details of a particular venueId(epimmsId).
     *
     * @param epimmsId data to get court details from refData.
     * @return courtDetail, particular Court detail.
     */
    @Override
    public CourtDetail getCourtDetails(String epimmsId) {
        CourtDetail courtDetail = null;
        log.info("Calling getCourtDetails service {}", epimmsId);
        try {
            final List<String> courtIds =
                    familyCourtIds.stream().map(String::trim).toList();
            List<CourtDetail> returnedCourtDetailList = refDataClient.fetchCourtDetail(epimmsId);
            if (returnedCourtDetailList != null) {
                log.info("returnedCourtDetailList: {}", returnedCourtDetailList);
            }

            if (returnedCourtDetailList != null) {
                courtDetail = returnedCourtDetailList.stream()
                        .filter(detail -> {
                            if (detail.getServiceCode() == null) {
                                return detail.getCourtTypeId() != null
                                        && courtIds.stream().anyMatch(courtId -> courtId.equals(detail.getCourtTypeId()));
                            } else {
                                return HMCTS_SERVICE_ID.equals(detail.getServiceCode());
                            }
                        })
                        .findFirst()
                        .orElse(null);
            }

            if (courtDetail != null) {
                if (courtDetail.getHearingVenueAddress() != null) {
                    courtDetail.setHearingVenueAddress(courtDetail.getHearingVenueAddress());
                }
                log.info("Found CourtDetail {}", courtDetail);
            } else {
                log.warn("Failed to find CourtDetail with court ID {}", epimmsId);
            }
        } catch (HttpClientErrorException | HttpServerErrorException exception) {
            log.info("RefData call HttpClientError exception {}", exception.getMessage());
            throw new RefDataException("RefData", exception.getStatusCode(), exception);
        } catch (FeignException exception) {
            log.info("RefData call Feign exception {}", exception.getMessage());
        }
        return courtDetail;
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
