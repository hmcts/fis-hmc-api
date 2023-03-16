package uk.gov.hmcts.reform.hmc.api.services;

import feign.FeignException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
@Slf4j
@SuppressWarnings("unchecked")
public class RefDataServiceImpl implements RefDataService {

    @Autowired AuthTokenGenerator authTokenGenerator;

    @Autowired IdamTokenGenerator idamTokenGenerator;
    @Autowired RefDataApi refDataApi;

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
        log.info("calling getCourtDetails service " + epimmsId);
        try {
            List<CourtDetail> courtDetailList =
                    refDataApi.getCourtDetails(
                            idamTokenGenerator.generateIdamTokenForRefData(),
                            authTokenGenerator.generate(),
                            epimmsId);
            log.info("RefData call completed successfully" + courtDetailList);
            List<CourtDetail> filteredCourtDetail =
                    courtDetailList.stream()
                            .filter(e -> "18".equals(e.getCourtTypeId()))
                            .collect(Collectors.toList());
            if (!filteredCourtDetail.isEmpty()) {
                courtDetail = filteredCourtDetail.get(0);
                log.info("Court details filtered" + courtDetail);
            }
            return courtDetail;
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
        log.info("calling getCourtDetails service with serviceCode {} " + serviceCode);
        try {
            VenuesDetail venueDetail =
                    refDataApi.getCourtDetailsByServiceCode(
                            idamTokenGenerator.generateIdamTokenForRefData(),
                            authTokenGenerator.generate(),
                            serviceCode);
            log.info("RefData call for allVenues completed successfully ");
            if (venueDetail != null
                    && !venueDetail.getCourtVenues().isEmpty()
                    && venueDetail.getCourtTypeId().equals("18")) {
                return venueDetail.getCourtVenues();
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
