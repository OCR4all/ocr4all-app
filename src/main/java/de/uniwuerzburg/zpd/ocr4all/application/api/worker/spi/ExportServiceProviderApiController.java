/**
 * File:     ExportServiceProviderApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     11.05.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker.spi;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.SnapshotRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi.ServiceProviderResponse;
import de.uniwuerzburg.zpd.ocr4all.application.api.worker.CoreApiController;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.SchedulerService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.SandboxService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.export.ExportService;
import de.uniwuerzburg.zpd.ocr4all.application.spi.ExportServiceProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * Defines export service provider controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Tag(name = "SPI export", description = "the export service provider API")
@RestController
@RequestMapping(path = ExportServiceProviderApiController.contextPath, produces = CoreApiController.applicationJson)
public class ExportServiceProviderApiController
		extends ProcessServiceProviderApiController<ExportServiceProvider, ExportService> {
	/**
	 * The context path.
	 */
	public static final String contextPath = spiContextPathVersion_1_0 + "/export";

	/**
	 * Creates an export service provider controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param projectService       The project service.
	 * @param sandboxService       The sandbox service.
	 * @param schedulerService     The scheduler service.
	 * @param service              The service.
	 * @since 1.8
	 */
	public ExportServiceProviderApiController(ConfigurationService configurationService,
			SecurityService securityService, ProjectService projectService, SandboxService sandboxService,
			SchedulerService schedulerService, ExportService service) {
		super(ExportServiceProviderApiController.class, configurationService, securityService, projectService,
				sandboxService, schedulerService, Type.export, service, Project.Right.execute);
	}

	/**
	 * Returns the service providers in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param sandboxId The sandbox id. This is the folder name.
	 * @param request   The snapshot request. The track can not be null.
	 * @param lang      The language. if null, then use the application preferred
	 *                  locale.
	 * @return The service providers in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the service providers in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Service Providers", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = ServiceProviderResponse.class))) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "405", description = "Method Not Allowed", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(providersRequestMapping + projectPathVariable + sandboxPathVariable)
	public ResponseEntity<List<ServiceProviderResponse>> serviceProviders(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @PathVariable String sandboxId,
			@RequestBody @Valid SnapshotRequest request,
			@Parameter(description = "the language") @RequestParam(required = false) String lang) {
		return serviceProviders(authorizationFactory.authorizeSnapshot(projectId, sandboxId, ProjectRight.execute),
				request.getTrack(), lang);
	}

	/**
	 * Schedules a process to execute the service provider.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param sandboxId The sandbox id. This is the folder name.
	 * @param request   The service provider snapshot request. The track of the
	 *                  parent snapshot can not be null.
	 * @param lang      The language. if null, then use the application preferred
	 *                  locale.
	 * @param response  The HTTP-specific functionality in sending a response to the
	 *                  client.
	 * @since 1.8
	 */
	@Operation(summary = "schedules a process to execute the service provider")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Job"),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "405", description = "Method Not Allowed", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(scheduleRequestMapping + projectPathVariable + sandboxPathVariable)
	public void schedule(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @PathVariable String sandboxId,
			@RequestBody @Valid ServiceProviderSnapshotRequest request,
			@Parameter(description = "the language") @RequestParam(required = false) String lang,
			HttpServletResponse response) {
		schedule(authorizationFactory.authorizeSnapshot(projectId, sandboxId, ProjectRight.execute), request, lang,
				response);
	}
}
