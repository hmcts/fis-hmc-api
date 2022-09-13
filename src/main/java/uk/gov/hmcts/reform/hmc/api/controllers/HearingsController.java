package uk.gov.hmcts.reform.hmc.api.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.hmcts.reform.hmc.api.model.request.HearingsRequest;
import uk.gov.hmcts.reform.hmc.api.model.response.Categories;
import uk.gov.hmcts.reform.hmc.api.services.HearingsService;

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
                schema = @Schema(implementation = Categories.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public ResponseEntity<Categories> getHearingsData(
        @RequestHeader("authorisation") String authorisation,
        @RequestBody HearingsRequest hearingsRequest
    ) throws JsonProcessingException {
        return ResponseEntity.ok(hearingsService.getRefData(hearingsRequest, authorisation));
    }
}

