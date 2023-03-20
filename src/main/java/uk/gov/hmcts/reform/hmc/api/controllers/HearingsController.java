package uk.gov.hmcts.reform.hmc.api.controllers;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.ResponseEntity.status;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.AUTHORIZATION;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.PROCESSING_REQUEST_AFTER_AUTHORIZATION;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.SERVICE_AUTHORIZATION;

import feign.FeignException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.List;
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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.exceptions.AuthorizationException;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.model.response.error.ApiError;
import uk.gov.hmcts.reform.hmc.api.services.HearingsDataService;
import uk.gov.hmcts.reform.hmc.api.services.HearingsService;
import uk.gov.hmcts.reform.hmc.api.services.IdamAuthService;
import uk.gov.hmcts.reform.hmc.api.services.NextHearingDetailsService;

/** Hearings controller to get data hearings data. */
@Slf4j
@RequestMapping(path = "/")
@RestController
@Api(value = "/", description = "get hearings Values")
public class HearingsController {

    @Autowired private IdamAuthService idamAuthService;

    @Autowired private HearingsDataService hearingsDataService;

    @Autowired private HearingsService hearingsService;

    @Autowired private NextHearingDetailsService nextHearingDetailsService;

    @Autowired private AuthTokenGenerator authTokenGenerator;

    /**
     * End point to fetch the hearingsData info based on the hearingValues passed.
     *
     * @return hearingsData, response data for the input hearingValues.
     * @header authorization, User authorization token.
     * @header serviceAuthorization, S2S authorization token.
     * @responseBody hearingValues, combination of caseRefNo and hearingId to fetch hearingsData.
     */
    @PostMapping(path = "/serviceHearingValues")
    @ApiOperation("get hearings Values")
    @ApiResponses(
            value = {
                @ApiResponse(code = 200, message = "get hearings Values successfully"),
                @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<Object> getHearingsData(
            @RequestHeader(AUTHORIZATION) String authorization,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
            @RequestBody final HearingValues hearingValues)
            throws IOException, ParseException {
        try {
            if (Boolean.TRUE.equals(idamAuthService.authoriseService(serviceAuthorization))) {
                log.info(PROCESSING_REQUEST_AFTER_AUTHORIZATION);
                return ResponseEntity.ok(
                        hearingsDataService.getCaseData(
                                hearingValues, authorization, serviceAuthorization));
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
     * @return caseHearingsResponse, all the hearings which belongs to a particular caseRefNumber.
     * @header serviceAuthorization, S2S authorization token.
     * @header caseReference, CaseRefNumber to take all the hearings belongs to this case.
     */
    @GetMapping(path = "/hearings")
    @ApiOperation("get hearings by case reference number")
    @ApiResponses(
            value = {
                @ApiResponse(code = 200, message = "get hearings by caseRefNo successfully"),
                @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<Object> getHearingsByCaseRefNo(
            @RequestHeader(AUTHORIZATION) String authorization,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
            @RequestHeader("caseReference") String caseReference) {
        try {
            if (Boolean.TRUE.equals(idamAuthService.authoriseService(serviceAuthorization))
                    && Boolean.TRUE.equals(idamAuthService.authoriseUser(authorization))) {
                log.info(PROCESSING_REQUEST_AFTER_AUTHORIZATION);
                return ResponseEntity.ok(
                        hearingsService.getHearingsByCaseRefNo(
                                caseReference, authorization, serviceAuthorization));
            } else {
                throw new ResponseStatusException(UNAUTHORIZED);
            }
        } catch (AuthorizationException | ResponseStatusException e) {
            return status(UNAUTHORIZED).body(new ApiError(e.getMessage()));
        } catch (FeignException feignException) {
            return status(feignException.status()).body(new ApiError(feignException.getMessage()));
        } catch (Exception e) {
            return status(INTERNAL_SERVER_ERROR).body(new ApiError(e.getMessage()));
        }
    }


    /**
     * End point to fetch all the hearings which belongs to all the caseIds passed.
     *
     * @return casesWithHearings, List of cases with all the hearings which belongs to all caseIds
     *     passed.
     * @header authorization, user authorization token.
     * @header serviceAuthorization, S2S authorization token.
     * @header caseIds, caseId list to take all the hearings belongs to each case.
     */
    @GetMapping(path = "/hearings-by-list-of-case-ids")
    @ApiOperation("get hearings by case reference number")
    @ApiResponses(
            value = {
                @ApiResponse(code = 200, message = "get hearings by caseRefNo successfully"),
                @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<Object> getHearingsByListOfCaseIds(
            @RequestHeader(AUTHORIZATION) String authorization,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
            @RequestBody List<String> caseIds) {
        try {
            if (Boolean.TRUE.equals(idamAuthService.authoriseService(serviceAuthorization))
                    && Boolean.TRUE.equals(idamAuthService.authoriseUser(authorization))) {
                log.info(PROCESSING_REQUEST_AFTER_AUTHORIZATION);
                return ResponseEntity.ok(
                        hearingsService.getHearingsByListOfCaseIds(
                                caseIds, authorization, serviceAuthorization));
            } else {
                throw new ResponseStatusException(UNAUTHORIZED);
            }
        } catch (AuthorizationException | ResponseStatusException e) {
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
     * @return hearingsLinkData, response data for the hearing request Values.
     * @header authorization, User authorization token.
     * @header serviceAuthorization, S2S authorization token.
     * @responseBody hearingValues, combination of caseRefNo and hearingId to fetch
     *     hearingsLinkData.
     */
    @PostMapping(path = "/serviceLinkedCases")
    @ApiOperation("get service hearings Linked Cases")
    @ApiResponses(
            value = {
                @ApiResponse(code = 200, message = "get Hearings Linked case Data successfully"),
                @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<Object> getHearingsLinkData(
            @RequestHeader(AUTHORIZATION) String authorization,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
            @RequestBody final HearingValues hearingValues)
            throws IOException, ParseException {
        try {
            if (Boolean.TRUE.equals(idamAuthService.authoriseService(serviceAuthorization))) {
                log.info(PROCESSING_REQUEST_AFTER_AUTHORIZATION);
                return ResponseEntity.ok(
                        hearingsDataService.getHearingLinkData(
                                hearingValues, authorization, serviceAuthorization));
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
     * End point to fetch the immediate or next future hearingDate for a particular caseRefNumber.
     *
     * @return nextHearingsDetailsResponse, near future hearingDate and hearingId for a particular
     *     caseRefNumber.
     * @header authorization, User authorization token.
     * @header serviceAuthorization, S2S authorization token.
     * @header caseReference, CaseRefNumber to take all the hearings belongs to this case.
     */
    @GetMapping(path = "/updateNextHearingDetails")
    @ApiOperation("get next hearing details for a case reference number")
    @ApiResponses(
            value = {
                @ApiResponse(
                        code = 200,
                        message = "get next hearing details by caseRefNo successfully"),
                @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<Object> updateNextHearingDetails(
            @RequestHeader(AUTHORIZATION) String authorization,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
            @RequestHeader("caseReference") String caseReference) {
        try {
            if (Boolean.TRUE.equals(idamAuthService.authoriseUser(authorization))
                    && Boolean.TRUE.equals(
                            idamAuthService.authoriseService(serviceAuthorization))) {
                log.info(PROCESSING_REQUEST_AFTER_AUTHORIZATION);
                Hearings hearings =
                        hearingsService.getHearingsByCaseRefNo(
                                caseReference, authorization, serviceAuthorization);
                return ResponseEntity.ok(
                        nextHearingDetailsService.updateNextHearingDetails(
                                authorization, hearings));
            } else {
                throw new ResponseStatusException(UNAUTHORIZED);
            }
        } catch (AuthorizationException | ResponseStatusException e) {
            return status(UNAUTHORIZED).body(new ApiError(e.getMessage()));
        } catch (FeignException feignException) {
            return status(feignException.status()).body(new ApiError(feignException.getMessage()));
        } catch (Exception e) {
            return status(INTERNAL_SERVER_ERROR).body(new ApiError(e.getMessage()));
        }
    }

    @GetMapping(path = "/getNextHearingDate")
    @ApiOperation("get next hearing details for a case reference number")
    @ApiResponses(
            value = {
                @ApiResponse(
                        code = 200,
                        message = "get next hearing details by caseRefNo  successfully"),
                @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<Object> getNextHearingDate(
            @RequestHeader(AUTHORIZATION) String authorization,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
            @RequestHeader("caseReference") String caseReference) {
        try {

            if (Boolean.TRUE.equals(idamAuthService.authoriseUser(authorization))
                    && Boolean.TRUE.equals(
                            idamAuthService.authoriseService(serviceAuthorization))) {
                Hearings hearings =
                        hearingsService.getHearingsByCaseRefNo(
                                caseReference, authorization, authTokenGenerator.generate());
                return ResponseEntity.ok(nextHearingDetailsService.getNextHearingDate(hearings));
            } else {
                throw new ResponseStatusException(UNAUTHORIZED);
            }
        } catch (AuthorizationException | ResponseStatusException e) {
            return status(UNAUTHORIZED).body(new ApiError(e.getMessage()));
        } catch (FeignException feignException) {
            return status(feignException.status()).body(new ApiError(feignException.getMessage()));
        } catch (Exception e) {
            return status(INTERNAL_SERVER_ERROR).body(new ApiError(e.getMessage()));
        }
    }
}
