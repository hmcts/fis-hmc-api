package uk.gov.hmcts.reform.hmc.api.services;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class PrlUpdateServiceImpl implements PrlUpdateService {

    @Autowired private AuthTokenGenerator authTokenGenerator;

    // private static final Logger LOG = LoggerFactory.getLogger(PrlUpdateServiceImpl.class);

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
        log.info("calling updatePrlServiceWithHearing service " + hearing.getHearingID());

        if (Constants.ABA5.equals(hearing.getHmctsServiceCode())) {
            try {
                ResponseEntity responseEntity =
                        prlUpdateApi.prlUpdate(authTokenGenerator.generate(), hearing);
                log.info("PRL call completed successfully" + responseEntity.getStatusCode());
                isPrlRespSuccess = true;
            } catch (HttpClientErrorException | HttpServerErrorException exception) {
                log.info("PRL call HttpClientError exception {}", exception.getMessage());
                throw new PrlUpdateException("PRL", exception.getStatusCode(), exception);
            } catch (FeignException exception) {
                log.info("PRL call Feign exception {}", exception.getMessage());
            } catch (Exception exception) {
                log.info("PRL call exception {}", exception.getMessage());
            }
        } else {
            log.info(
                    "Not related PRL HmctsServiceCode{} Hence it is ignored ",
                    hearing.getHmctsServiceCode());

            isPrlRespSuccess = true;
        }
        return isPrlRespSuccess;
    }
}
