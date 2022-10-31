package uk.gov.hmcts.reform.hmc.api.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingsRequest;
import uk.gov.hmcts.reform.hmc.api.model.response.Categories;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;
import uk.gov.hmcts.reform.hmc.api.services.HearingsService;
import uk.gov.hmcts.reform.hmc.api.services.HearingsService1;

/** Hearings controller to get data hearings data. */
@Slf4j
@RequestMapping(path = "/")
@RestController
@Api(value = "/", description = "Standard API")
public class HearingsController {
    @Autowired private HearingsService hearingsService;

    @Autowired private HearingsService1 hearingsService1;

    @GetMapping(path = "/hearingsdata")
    @ApiOperation("get hearings data")
    @ApiResponses(
            value = {
                @ApiResponse(code = 200, message = "get hearings data successfully"),
                @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<Categories> getHearingsData(
            @RequestHeader("authorisation") String authorisation,
            @RequestBody HearingsRequest hearingsRequest)
            throws JsonProcessingException {
        return ResponseEntity.ok(hearingsService1.getRefData(hearingsRequest, authorisation));
    }

    @GetMapping(path = "/hearings")
    @ApiOperation("get hearings by case reference number")
    @ApiResponses(
            value = {
                @ApiResponse(code = 200, message = "get hearings by caseRefNo successfully"),
                @ApiResponse(code = 400, message = "Bad Request")
            })
    public Hearings getHearingsByCaseRefNo(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestHeader("caseReference") String caseReference) {

        return hearingsService.getHearingsByCaseRefNo(
                authorization, serviceAuthorization, caseReference);
    }
}
