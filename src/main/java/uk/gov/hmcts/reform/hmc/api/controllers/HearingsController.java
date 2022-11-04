package uk.gov.hmcts.reform.hmc.api.controllers;

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
import uk.gov.hmcts.reform.hmc.api.model.request.HearingValues;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingsData;
import uk.gov.hmcts.reform.hmc.api.services.HearingsDataService;
import uk.gov.hmcts.reform.hmc.api.services.HearingsService;

/** Hearings controller to get data hearings data. */
@Slf4j
@RequestMapping(path = "/")
@RestController
@Api(value = "/", description = "get hearings Values")
public class HearingsController {
    @Autowired private HearingsDataService hearingsDataService;

    @Autowired private HearingsService hearingsService;

    @PostMapping(path = "/serviceHearingValues")
    @ApiOperation("get hearings Values")
    @ApiResponses(
            value = {
                @ApiResponse(code = 200, message = "get hearings Values successfully"),
                @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<HearingsData> getHearingsData(
            @RequestHeader("Authorization") String authorisation,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestBody final HearingValues hearingValues)
            throws IOException, ParseException {
        return ResponseEntity.ok(
                hearingsDataService.getCaseData(
                        hearingValues, authorisation, serviceAuthorization));
    }

    @GetMapping(path = "/hearings")
    @ApiOperation("get hearings by case reference number")
    @ApiResponses(
            value = {
                @ApiResponse(code = 200, message = "get hearings by caseRefNo successfully"),
                @ApiResponse(code = 400, message = "Bad Request")
            })
    public Hearings getHearingsByCaseRefNo(
            @RequestHeader("Authorisation") String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestHeader("caseReference") String caseReference) {
        return hearingsService.getHearingsByCaseRefNo(
                authorization, serviceAuthorization, caseReference);
    }
}
