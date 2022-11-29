package uk.gov.hmcts.reform.hmc.api.controllers;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.ResponseEntity.status;

import feign.FeignException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.error.ApiError;
import uk.gov.hmcts.reform.hmc.api.services.HearingsDataService;
import uk.gov.hmcts.reform.hmc.api.services.HearingsService;
import uk.gov.hmcts.reform.hmc.api.services.IdamAuthService;

/** Hearings controller to get data hearings data. */
@Slf4j
@RequestMapping(path = "/")
@RestController
@Api(value = "/", description = "get hearings Values")
public class HearingsController {

    public static final String PROCESSING_REQUEST_AFTER_AUTHORIZATION =
            "processing request after authorization";

    @Autowired private IdamAuthService idamAuthService;

    @Autowired private HearingsDataService hearingsDataService;

    @Autowired private HearingsService hearingsService;

    /**
     * End point to fetch the hearingsData info based on the hearingValues passed.
     *
     * @header authorisation, User authorisation token.
     * @header serviceAuthorization, S2S authorization token.
     * @responseBody hearingValues, combination of caseRefNo and hearingId to fetch hearingsData.
     * @return hearingsData, response data for the input hearingValues.
     */
    @PostMapping(path = "/serviceHearingValues")
    @ApiOperation("get hearings Values")
    @ApiResponses(
            value = {
                @ApiResponse(code = 200, message = "get hearings Values successfully"),
                @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<? extends Object> getHearingsData(
            @RequestHeader("Authorization") String authorisation,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestBody final HearingValues hearingValues)
            throws IOException, ParseException {
        try {
            if (Boolean.TRUE.equals(idamAuthService.authoriseService(serviceAuthorization))) {
                log.info(PROCESSING_REQUEST_AFTER_AUTHORIZATION);

                return ResponseEntity.ok(
                        hearingsDataService.getCaseData(
                                hearingValues, authorisation, serviceAuthorization));
            } else {
                throw new ResponseStatusException(UNAUTHORIZED);
            }
        } catch (ResponseStatusException e) {
            return status(UNAUTHORIZED).body(new ApiError(e.getMessage()));
        } catch (FeignException feignException) {
            return status(feignException.status()).body(new ApiError(feignException.getMessage()));
        } catch (Exception e) {
            return status(INTERNAL_SERVER_ERROR).body(new ApiError(e.getMessage()));
        }
    }

    /**
     * End point to fetch all the hearings which belongs to a particular caseRefNumber.
     *
     * @header authorization, User authorization token.
     * @header serviceAuthorization, S2S authorization token.
     * @header caseReference, CaseRefNumber to take all the hearings belongs to this case.
     * @return caseHearingsResponse, all the hearings which belongs to a particular caseRefNumber.
     */
    @GetMapping(path = "/hearings")
    @ApiOperation("get hearings by case reference number")
    @ApiResponses(
            value = {
                @ApiResponse(code = 200, message = "get hearings by caseRefNo successfully"),
                @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<? extends Object> getHearingsByCaseRefNo(
            @RequestHeader("Authorisation") String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestHeader("caseReference") String caseReference) {
        try {
            if (Boolean.TRUE.equals(idamAuthService.authoriseService(serviceAuthorization))) {
                log.info(PROCESSING_REQUEST_AFTER_AUTHORIZATION);
                return ResponseEntity.ok(
                        hearingsService.getHearingsByCaseRefNo(
                                authorization, serviceAuthorization, caseReference));
            } else {
                throw new ResponseStatusException(UNAUTHORIZED);
            }
        } catch (ResponseStatusException e) {
            return status(UNAUTHORIZED).body(new ApiError(e.getMessage()));
        } catch (FeignException feignException) {
            return status(feignException.status()).body(new ApiError(feignException.getMessage()));
        } catch (Exception e) {
            return status(INTERNAL_SERVER_ERROR).body(new ApiError(e.getMessage()));
        }
    }

    /**
     * End point to fetch the Hearings Link Data info based on the hearing request Values passed.
     *
     * @header authorisation, User authorisation token.
     * @header serviceAuthorization, S2S authorization token.
     * @responseBody hearingValues, combination of caseRefNo and hearingId to fetch
     *     hearingsLinkData.
     * @return hearingsLinkData, response data for the hearing request Values.
     */
    @PostMapping(path = "/serviceLinkedCases")
    @ApiOperation("get service hearings Linked Cases")
    @ApiResponses(
            value = {
                @ApiResponse(code = 200, message = "get Hearings Linked case Data successfully"),
                @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<? extends Object> getHearingsLinkData(
            @RequestHeader("Authorization") String authorisation,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestBody final HearingValues hearingValues)
            throws IOException, ParseException {
        try {
            if (Boolean.TRUE.equals(idamAuthService.authoriseService(serviceAuthorization))) {
                log.info(PROCESSING_REQUEST_AFTER_AUTHORIZATION);
                return ResponseEntity.ok(
                        hearingsDataService.getHearingLinkData(
                                hearingValues, authorisation, serviceAuthorization));
            } else {
                throw new ResponseStatusException(UNAUTHORIZED);
            }
        } catch (ResponseStatusException e) {
            return status(UNAUTHORIZED).body(new ApiError(e.getMessage()));
        } catch (FeignException feignException) {
            return status(feignException.status()).body(new ApiError(feignException.getMessage()));
        } catch (Exception e) {
            return status(INTERNAL_SERVER_ERROR).body(new ApiError(e.getMessage()));
        }
    }
}
