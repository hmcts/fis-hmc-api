package uk.gov.hmcts.reform.hmc.api.services;

import feign.FeignException;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.UserAuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.exceptions.RefDataException;
import uk.gov.hmcts.reform.hmc.api.model.request.Hearing;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingUpdate;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;

@Service
@SuppressWarnings("unchecked")
public class RefDataServiceImpl implements RefDataService {

    @Autowired AuthTokenGenerator authTokenGenerator;
    @Autowired UserAuthTokenGenerator userAuthTokenGenerator;
    @Autowired RefDataApi refDataApi;
    private static final Logger LOG = LoggerFactory.getLogger(RefDataServiceImpl.class);

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
        LOG.info("calling getCourtDetails service " + epimmsId);
        try {
            List<CourtDetail> courtDetailList =
                    refDataApi.getCourtDetails(
                            userAuthTokenGenerator.getSecurityTokens(),
                            authTokenGenerator.generate(),
                            epimmsId);
            LOG.info("RefData call completed successfully" + courtDetailList);
            List<CourtDetail> filteredCourtDetail =
                    courtDetailList.stream()
                            .filter(e -> "18".equals(e.getCourtTypeId()))
                            .collect(Collectors.toList());
            if (!filteredCourtDetail.isEmpty()) {
                courtDetail = filteredCourtDetail.get(0);
                LOG.info("Court details filtered" + courtDetail);
            }
            return courtDetail;
        } catch (HttpClientErrorException | HttpServerErrorException exception) {
            LOG.info("RefData call HttpClientError exception {}", exception.getMessage());
            throw new RefDataException("RefData", exception.getStatusCode(), exception);
        } catch (FeignException exception) {
            LOG.info("RefData call Feign exception {}", exception.getMessage());
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
        LOG.info(
                "calling getHearingWithCourtDetails service "
                        + hearing.getHearingUpdate().getHearingVenueId());
        CourtDetail courtDetail = getCourtDetails(hearing.getHearingUpdate().getHearingVenueId());
        LOG.info("courtDetails " + courtDetail);
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
