package uk.gov.hmcts.reform.hmc.api.controllers;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.hmc.api.model.request.HearingsRequest;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseHearings;
import uk.gov.hmcts.reform.hmc.api.model.response.Categories;
import uk.gov.hmcts.reform.hmc.api.model.response.Category;
import uk.gov.hmcts.reform.hmc.api.services.HearingsService;

import java.util.ArrayList;

/** Hearings controller to get data hearings data. */
@Slf4j
@RestController
public class HearingsController {
    @Autowired private HearingsService hearingsService;

    @GetMapping(path = "/hearingsdata", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "get hearings data")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "get hearings data successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = Categories.class))),
                @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)
            })
    public ResponseEntity<Categories> getHearingsData(
            @RequestHeader("authorisation") String authorisation,
            @RequestBody HearingsRequest hearingsRequest)
            throws JsonProcessingException {
        return ResponseEntity.ok(hearingsService.getRefData(hearingsRequest, authorisation));
    }

    @GetMapping(path = "/hearings", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "get hearings by caseRefNo")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "get hearings by caseRefNo successfully",
                content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Categories.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)
        })
    public CaseHearings getHearingsByCaseRefNo(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestHeader("caseReference") String caseReference)
        throws JsonProcessingException {

        CaseHearings caseHearings = hearingsService.getHearingsByCaseRefNo(authorization, serviceAuthorization, caseReference);

        return  caseHearings;
    }
}
