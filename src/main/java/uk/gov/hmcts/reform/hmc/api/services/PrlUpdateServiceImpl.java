package uk.gov.hmcts.reform.hmc.api.services;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.enums.State;
import uk.gov.hmcts.reform.hmc.api.exceptions.PrlUpdateException;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingDTO;
import uk.gov.hmcts.reform.hmc.api.model.request.NextHearingDetailsDTO;
import uk.gov.hmcts.reform.hmc.api.utils.Constants;

@Service
@Slf4j
public class PrlUpdateServiceImpl implements PrlUpdateService {

    @Autowired private AuthTokenGenerator authTokenGenerator;

    @Autowired PrlUpdateApi prlUpdateApi;

    @Autowired HearingsService hearingsService;

    /**
     * This method will update the Prl with hearing data.
     *
     * @param hearingDto data to update prl.
     * @return isPrlRespSuccess, Boolean - prlHearing update.
     */
    @Override
    @SuppressWarnings("unused")
    public Boolean updatePrlServiceWithHearing(HearingDTO hearingDto, State caseState) {

        Boolean isPrlRespSuccess = false;
        log.info("calling updatePrlServiceWithHearing service " + hearingDto.getHearingId());

        if (Constants.ABA5.equals(hearingDto.getHmctsServiceCode())) {
            try {
                prlUpdateApi.prlUpdate(authTokenGenerator.generate(), hearingDto, caseState);
                log.info("PRL call completed successfully");
                isPrlRespSuccess = true;
            } catch (HttpClientErrorException | HttpServerErrorException exception) {
                log.info("PRL call HttpClientError exception {}", exception.getMessage());
                throw new PrlUpdateException("PRL", exception.getStatusCode(), exception);
            } catch (FeignException exception) {
                log.info("PRL call Feign exception {}", exception.getMessage());
            }
        } else {
            log.info(
                    "Not related PRL HmctsServiceCode{} Hence it is ignored ",
                    hearingDto.getHmctsServiceCode());

            isPrlRespSuccess = true;
        }
        return isPrlRespSuccess;
    }

    @Override
    @SuppressWarnings("unused")
    public Boolean updatePrlServiceWithNextHearingDate(
            String authorization, NextHearingDetailsDTO nextHearingDetailsDto) {

        Boolean isPrlRespSuccess = false;
        log.info(
                "calling updatePrlServiceWithNextHearingDate service "
                        + nextHearingDetailsDto.getNextHearingDetails().getHearingId());
        try {
            prlUpdateApi.prlNextHearingDateUpdate(
                    authorization, authTokenGenerator.generate(), nextHearingDetailsDto);
            log.info("PRL next hearing date update call completed successfully");
            isPrlRespSuccess = true;
        } catch (HttpClientErrorException | HttpServerErrorException exception) {
            log.info(
                    "PRL next hearing date update call HttpClientError exception {}",
                    exception.getMessage());
            throw new PrlUpdateException("PRL", exception.getStatusCode(), exception);
        } catch (FeignException exception) {
            log.info(
                    "PRL next hearing date update call Feign exception {}", exception.getMessage());
        }

        return isPrlRespSuccess;
    }
}
