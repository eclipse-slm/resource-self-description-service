package org.eclipse.slm.self_description_service.aas;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.eclipse.slm.self_description_service.http.Base64UrlEncodedIdentifier;
import org.eclipse.slm.self_description_service.http.Base64UrlEncodedIdentifierSize;
import org.eclipse.slm.self_description_service.http.pagination.Base64UrlEncodedCursor;
import org.eclipse.slm.self_description_service.http.pagination.GetSubmodelsResult;
import org.eclipse.slm.self_description_service.http.pagination.PagedResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.eclipse.digitaltwin.aas4j.v3.model.Result;

import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface SubmodelRepositoryHTTPApi {

    @Operation(summary = "Returns all Submodels", description = "", tags = { "Submodel Repository API" })
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Requested Submodels", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetSubmodelsResult.class))),

            @ApiResponse(responseCode = "400", description = "Bad Request, e.g. the request parameters of the format of the request body is wrong.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),

            @ApiResponse(responseCode = "401", description = "Unauthorized, e.g. the server refused the authorization attempt.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),

            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),

            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),

            @ApiResponse(responseCode = "200", description = "Default error handling for unmentioned status codes", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))) })
    @RequestMapping(value = "/submodels", produces = { "application/json" }, method = RequestMethod.GET)
    ResponseEntity<PagedResult> getAllSubmodels(
            @Base64UrlEncodedIdentifierSize(min = 1, max = 3072) @Parameter(in = ParameterIn.QUERY, description = "The value of the semantic id reference (UTF8-BASE64-URL-encoded)", schema = @Schema()) @Valid @RequestParam(value = "semanticId", required = false) Base64UrlEncodedIdentifier semanticId,
            @Parameter(in = ParameterIn.QUERY, description = "The Asset Administration Shell’s IdShort", schema = @Schema()) @Valid @RequestParam(value = "idShort", required = false) String idShort,
            @Min(1) @Parameter(in = ParameterIn.QUERY, description = "The maximum number of elements in the response array", schema = @Schema(allowableValues = {
                    "1" }, minimum = "1")) @Valid @RequestParam(value = "limit", required = false) Integer limit,
            @Parameter(in = ParameterIn.QUERY, description = "A server-generated identifier retrieved from pagingMetadata that specifies from which position the result listing should continue", schema = @Schema()) @Valid @RequestParam(value = "cursor", required = false) Base64UrlEncodedCursor cursor,
            @Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
                    "core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level,
            @Parameter(in = ParameterIn.QUERY, description = "Determines to which extent the resource is being serialized", schema = @Schema(allowableValues = { "withBlobValue",
                    "withoutBlobValue" }, defaultValue = "withoutBlobValue")) @Valid @RequestParam(value = "extent", required = false, defaultValue = "withoutBlobValue") String extent);


}
