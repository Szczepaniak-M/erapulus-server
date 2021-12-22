package com.erapulus.server.web.controller;

import com.erapulus.server.dto.ProgramRequestDto;
import com.erapulus.server.dto.ProgramResponseDto;
import com.erapulus.server.service.ProgramService;
import com.erapulus.server.web.common.ServerResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolationException;
import java.util.NoSuchElementException;

import static com.erapulus.server.web.common.CommonRequestVariable.*;
import static com.erapulus.server.web.common.OpenApiConstants.*;
import static com.erapulus.server.web.controller.ControllerUtils.*;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;

@RestController

@AllArgsConstructor
public class ProgramController {

    private final ProgramService programService;

    @NonNull
    @Operation(
            operationId = "list-programs",
            tags = "Program",
            description = "List programs",
            summary = "List programs by university ID and faculty ID",
            parameters = {
                    @Parameter(in = PATH, name = UNIVERSITY_PATH_PARAM, schema = @Schema(type = "integer"), required = true),
                    @Parameter(in = PATH, name = FACULTY_PATH_PARAM, schema = @Schema(type = "integer"), required = true),
                    @Parameter(in = QUERY, name = NAME_QUERY_PARAM, schema = @Schema(type = "string")),
                    @Parameter(in = QUERY, name = PAGE_QUERY_PARAM, schema = @Schema(type = "integer")),
                    @Parameter(in = QUERY, name = PAGE_SIZE_QUERY_PARAM, schema = @Schema(type = "integer"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = OK, content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProgramResponseDto.class)))),
                    @ApiResponse(responseCode = "400", description = BAD_REQUEST),
                    @ApiResponse(responseCode = "401", description = UNAUTHORIZED),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN),
                    @ApiResponse(responseCode = "500", description = INTERNAL_SERVER_ERROR)
            }
    )
    public Mono<ServerResponse> listPrograms(ServerRequest request) {
        return withPathParam(request, UNIVERSITY_PATH_PARAM,
                universityId -> withPathParam(request, FACULTY_PATH_PARAM,
                        facultyId -> withQueryParam(request, NAME_QUERY_PARAM,
                                name -> withPageParams(request,
                                        pageRequest -> programService.listEntities(universityId, facultyId, name, pageRequest)
                                                                     .flatMap(ServerResponseFactory::createHttpSuccessResponse)
                                                                     .onErrorResume(e -> ServerResponseFactory.createHttpInternalServerErrorResponse())))));
    }

    @NonNull
    @Operation(
            operationId = "create-program",
            tags = "Program",
            description = "Create program",
            summary = "Create program",
            parameters = {
                    @Parameter(in = PATH, name = UNIVERSITY_PATH_PARAM, schema = @Schema(type = "integer"), required = true),
                    @Parameter(in = PATH, name = FACULTY_PATH_PARAM, schema = @Schema(type = "integer"), required = true)
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ProgramRequestDto.class)), required = true),
            responses = {
                    @ApiResponse(responseCode = "201", description = OK, content = @Content(schema = @Schema(implementation = ProgramResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = BAD_REQUEST),
                    @ApiResponse(responseCode = "401", description = UNAUTHORIZED),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN),
                    @ApiResponse(responseCode = "500", description = INTERNAL_SERVER_ERROR)
            }
    )
    public Mono<ServerResponse> createProgram(ServerRequest request) {
        return withPathParam(request, UNIVERSITY_PATH_PARAM,
                universityId -> withPathParam(request, FACULTY_PATH_PARAM,
                        facultyId -> request.bodyToMono(ProgramRequestDto.class)
                                            .flatMap(program -> programService.createEntity(program, universityId, facultyId))
                                            .flatMap(ServerResponseFactory::createHttpCreatedResponse)
                                            .onErrorResume(ConstraintViolationException.class, ServerResponseFactory::createHttpBadRequestConstraintViolationErrorResponse)
                                            .onErrorResume(e -> ServerResponseFactory.createHttpInternalServerErrorResponse())
                                            .switchIfEmpty(ServerResponseFactory.createHttpBadRequestNoBodyFoundErrorResponse())));
    }

    @NonNull
    @Operation(
            operationId = "get-program",
            tags = "Program",
            description = "Get program by ID",
            summary = "Get program by ID",
            parameters = {
                    @Parameter(in = PATH, name = UNIVERSITY_PATH_PARAM, schema = @Schema(type = "integer"), required = true),
                    @Parameter(in = PATH, name = FACULTY_PATH_PARAM, schema = @Schema(type = "integer"), required = true),
                    @Parameter(in = PATH, name = PROGRAM_PATH_PARAM, schema = @Schema(type = "integer"), required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = OK, content = @Content(schema = @Schema(implementation = ProgramResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = UNAUTHORIZED),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN),
                    @ApiResponse(responseCode = "404", description = NOT_FOUND),
                    @ApiResponse(responseCode = "500", description = INTERNAL_SERVER_ERROR)
            }
    )
    public Mono<ServerResponse> getProgramById(ServerRequest request) {
        return withPathParam(request, UNIVERSITY_PATH_PARAM,
                universityId -> withPathParam(request, FACULTY_PATH_PARAM,
                        facultyId -> withPathParam(request, PROGRAM_PATH_PARAM,
                                programId -> programService.getEntityById(programId, universityId, facultyId)
                                                           .flatMap(ServerResponseFactory::createHttpSuccessResponse)
                                                           .onErrorResume(NoSuchElementException.class, e -> ServerResponseFactory.createHttpNotFoundResponse("post"))
                                                           .onErrorResume(e -> ServerResponseFactory.createHttpInternalServerErrorResponse()))));
    }

    @NonNull
    @Operation(
            operationId = "update-program",
            tags = "Program",
            description = "Update program",
            summary = "Update program",
            parameters = {
                    @Parameter(in = PATH, name = UNIVERSITY_PATH_PARAM, schema = @Schema(type = "integer"), required = true),
                    @Parameter(in = PATH, name = FACULTY_PATH_PARAM, schema = @Schema(type = "integer"), required = true),
                    @Parameter(in = PATH, name = PROGRAM_PATH_PARAM, schema = @Schema(type = "integer"), required = true)
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ProgramRequestDto.class)), required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = OK, content = @Content(schema = @Schema(implementation = ProgramResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = BAD_REQUEST),
                    @ApiResponse(responseCode = "401", description = UNAUTHORIZED),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN),
                    @ApiResponse(responseCode = "404", description = NOT_FOUND),
                    @ApiResponse(responseCode = "500", description = INTERNAL_SERVER_ERROR)
            }
    )
    public Mono<ServerResponse> updateProgram(ServerRequest request) {
        return withPathParam(request, UNIVERSITY_PATH_PARAM,
                universityId -> withPathParam(request, FACULTY_PATH_PARAM,
                        facultyId -> withPathParam(request, PROGRAM_PATH_PARAM,
                                programId -> request.bodyToMono(ProgramRequestDto.class)
                                                    .flatMap(program -> programService.updateEntity(program, programId, universityId, facultyId))
                                                    .flatMap(ServerResponseFactory::createHttpSuccessResponse)
                                                    .onErrorResume(ConstraintViolationException.class, ServerResponseFactory::createHttpBadRequestConstraintViolationErrorResponse)
                                                    .onErrorResume(NoSuchElementException.class, e -> ServerResponseFactory.createHttpNotFoundResponse("building"))
                                                    .onErrorResume(e -> ServerResponseFactory.createHttpInternalServerErrorResponse())
                                                    .switchIfEmpty(ServerResponseFactory.createHttpBadRequestNoBodyFoundErrorResponse()))));
    }

    @NonNull
    @Operation(
            operationId = "delete-program",
            tags = "Program",
            description = "Delete program",
            summary = "Delete program by ID",
            parameters = {
                    @Parameter(in = PATH, name = UNIVERSITY_PATH_PARAM, schema = @Schema(type = "integer"), required = true),
                    @Parameter(in = PATH, name = FACULTY_PATH_PARAM, schema = @Schema(type = "integer"), required = true),
                    @Parameter(in = PATH, name = PROGRAM_PATH_PARAM, schema = @Schema(type = "integer"), required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = NO_CONTENT),
                    @ApiResponse(responseCode = "400", description = BAD_REQUEST),
                    @ApiResponse(responseCode = "401", description = UNAUTHORIZED),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN),
                    @ApiResponse(responseCode = "404", description = NOT_FOUND),
                    @ApiResponse(responseCode = "500", description = INTERNAL_SERVER_ERROR)
            }
    )
    public Mono<ServerResponse> deleteProgram(ServerRequest request) {
        return withPathParam(request, PROGRAM_PATH_PARAM,
                programId -> programService.deleteEntity(programId)
                                           .flatMap(r -> ServerResponseFactory.createHttpNoContentResponse())
                                           .onErrorResume(NoSuchElementException.class, e -> ServerResponseFactory.createHttpNotFoundResponse("building"))
                                           .onErrorResume(e -> ServerResponseFactory.createHttpInternalServerErrorResponse()));
    }
}

