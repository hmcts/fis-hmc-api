package uk.gov.hmcts.reform.hmc.api.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.exceptions.PrlUpdateException;
import uk.gov.hmcts.reform.hmc.api.model.request.Hearing;
import uk.gov.hmcts.reform.hmc.api.utils.Constants;

@Service
public class PrlUpdateServiceImpl implements PrlUpdateService {

    @Autowired private AuthTokenGenerator authTokenGenerator;

    private static final Logger LOG = LoggerFactory.getLogger(PrlUpdateServiceImpl.class);

    @Autowired PrlUpdateApi prlUpdateApi;

    /**
     * This method will update the Prl with hearing data.
     *
     * @param hearing data to update prl.
     * @return isPrlRespSuccess, Boolean - prlHearing update.
     */
    @Override
    @SuppressWarnings("unused")
    public Boolean updatePrlServiceWithHearing(Hearing hearing) {
        Boolean isPrlRespSuccess = false;
        LOG.info("updatePrlServiceWithHearing");

        if (Constants.BBA3.equals(hearing.getHmctsServiceCode())) {

            try {
                ResponseEntity responseEntity =
                        prlUpdateApi.prlUpdate(getServiceAuthorisationToken(), hearing);
                LOG.info("PRL call completed successfully" + responseEntity.getStatusCode());
                isPrlRespSuccess = true;
            } catch (HttpClientErrorException | HttpServerErrorException exception) {
                LOG.info("PRL call exception {}", exception.getMessage());
                throw new PrlUpdateException("PRL", exception.getStatusCode(), exception);
            }
        }
        return isPrlRespSuccess;
    }

    /**
     * This method will generate and return ServiceAuthrisationToken.
     *
     * @return ServiceAuthrisationToen, a s2s token to call the prl API.
     */
    private String getServiceAuthorisationToken() {

        try {
            return authTokenGenerator.generate();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new PrlUpdateException("S2S", e.getStatusCode(), e);
        }
    }
}
