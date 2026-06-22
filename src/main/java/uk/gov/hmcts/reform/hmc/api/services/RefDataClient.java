package uk.gov.hmcts.reform.hmc.api.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.model.response.CourtDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.VenuesDetail;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.HMCTS_SERVICE_ID;

/**
 * Thin wrapper around the Feign `RefDataApi` that centralises token generation and error handling.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RefDataClient {

    private final RefDataApi refDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamTokenGenerator idamTokenGenerator;

    public List<CourtDetail> fetchCourtDetail(String epimmsId) {
        try {
            List<CourtDetail> courtDetailList = refDataApi.getCourtDetails(
                    idamTokenGenerator.generateIdamTokenForRefData(),
                    authTokenGenerator.generate(),
                    epimmsId,
                    HMCTS_SERVICE_ID);
            log.info("RefDataClient: call succeeded for {}: returned {} items", epimmsId,
                     courtDetailList == null ? 0 : courtDetailList.size());
            return courtDetailList != null ? courtDetailList : List.of();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw ex;
        } catch (Exception ex) {
            log.info("RefDataClient: call failed for {}: {}", epimmsId, ex.getMessage());
            return List.of();
        }
    }

    public VenuesDetail fetchByServiceCode(String serviceCode) {
        try {
            return refDataApi.getCourtDetailsByServiceCode(
                    idamTokenGenerator.generateIdamTokenForRefData(),
                    authTokenGenerator.generate(),
                    serviceCode);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw ex;
        } catch (Exception ex) {
            log.info("RefDataClient: fetchByServiceCode failed for {}: {}", serviceCode, ex.getMessage());
            return null;
        }
    }
}

