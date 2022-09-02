package uk.gov.hmcts.reform.demo.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.demo.model.request.HearingsRequest;
import uk.gov.hmcts.reform.demo.model.response.HearingsResponseDummy;
import uk.gov.hmcts.reform.demo.services.HearingsService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
/**
 * Hearings controller to get data hearings data.
 */

@Slf4j
@RestController
@RequiredArgsConstructor
public class HearingsController {
    private final HearingsService hearingsService;

    @GetMapping(path = "/hearingsdata", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "get hearings data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "get hearings data successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = HearingsResponseDummy.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public ResponseEntity<HearingsResponseDummy> getHearingsData(
        @RequestHeader("authorisation") String authorisation,
        @RequestHeader("serviceAuthorisation") String serviceAuthorisation,
        @RequestBody HearingsRequest hearingsRequest
    ) {
        return ResponseEntity.ok(hearingsService.getHearingsData(hearingsRequest));
    }
}

