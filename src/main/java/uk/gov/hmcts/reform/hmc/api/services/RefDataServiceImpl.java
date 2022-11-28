package uk.gov.hmcts.reform.hmc.api.services;

import feign.FeignException;
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
import uk.gov.hmcts.reform.hmc.api.model.request.Hearing;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingUpdate;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;

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
     * @param hearing data to be updated with court details.
     * @return hearing, updated hearing with Court detail received from refData.
     */
    @Override
    @SuppressWarnings("unused")
    public Hearing getHearingWithCourtDetails(Hearing hearing) {
        log.info(
                "calling getHearingWithCourtDetails service "
                        + hearing.getHearingUpdate().getHearingVenueId());
        CourtDetail courtDetail = getCourtDetails(hearing.getHearingUpdate().getHearingVenueId());
        log.info("courtDetails " + courtDetail);
        if (courtDetail != null) {
            HearingUpdate hearingUpdate = hearing.getHearingUpdate();
            hearingUpdate.setHearingVenueName(courtDetail.getHearingVenueName());
            hearingUpdate.setHearingVenueAddress(courtDetail.getHearingVenueAddress());
            hearingUpdate.setHearingVenueLocationCode(courtDetail.getHearingVenueLocationCode());
            hearingUpdate.setCourtTypeId(courtDetail.getCourtTypeId());
            hearing.hearingRequestWith().hearingUpdate(hearingUpdate).build();
        }
        return hearing;
    }
}
