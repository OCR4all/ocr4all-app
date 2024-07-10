/**
 * File:     ProjectApiController.java
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * 
 * Author:   Herbert Baier
 * Date:     07.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.BasicRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.HistoryResponse;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.ProjectResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.ModelService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
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
 * Defines project controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Tag(name = "project", description = "the project API")
@RestController
@RequestMapping(path = ProjectApiController.contextPath, produces = CoreApiController.applicationJson)
public class ProjectApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = apiContextPathVersion_1_0 + "/project";

	/**
	 * The project service.
	 */
	private final ProjectService service;

	/**
	 * Creates a project controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param collectionService    The collection service.
	 * @param modelService         The model service.
	 * @param service              The project service.
	 * @since 1.8
	 */
	public ProjectApiController(ConfigurationService configurationService, SecurityService securityService,
			CollectionService collectionService, ModelService modelService, ProjectService service) {
		super(ProjectApiController.class, configurationService, securityService, collectionService, modelService,
				service);

		this.service = service;
	}

	/**
	 * Returns the project in the response body.
	 * 
	 * @param id The project id. This is the folder name.
	 * @return The project in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the project in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Project", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = ProjectResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(entityRequestMapping)
	public ResponseEntity<ProjectResponse> entity(
			@Parameter(description = "the project id - this is the folder name") @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(id);

		try {
			return ResponseEntity.ok().body(new ProjectResponse(authorization.project));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the list of authorized projects sorted by name in the response body.
	 * 
	 * @return The list of authorized projects sorted by name in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the list of authorized projects sorted by name in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Projects", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = ProjectResponse.class))) }),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(listRequestMapping)
	public ResponseEntity<List<ProjectResponse>> list() {
		try {
			List<ProjectResponse> projects = new ArrayList<>();
			for (Project project : service.getProjectsRightExist())
				if (service.authorize(project) != null)
					projects.add(new ProjectResponse(project));

			return ResponseEntity.ok().body(projects);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Creates a project and returns it in the response body.
	 * 
	 * @param id The project id. This is the folder name.
	 * @return The created project in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "creates a project and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Created Project", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = ProjectResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "405", description = "Method Not Allowed", content = @Content),
			@ApiResponse(responseCode = "409", description = "Conflict", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(createRequestMapping)
	public ResponseEntity<ProjectResponse> create(
			@Parameter(description = "the project id - this is the folder name") @RequestParam String id) {
		if (id.isBlank())
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

		try {
			if (service.isAvailable(id)) {
				Project project = service.getProject(id);

				return project == null ? ResponseEntity.status(HttpStatus.CONFLICT).build()
						: ResponseEntity.status(HttpStatus.CONFLICT).body(new ProjectResponse(project));
			}

			Path path = configurationService.getWorkspace().getProjects().createProject(id, getUser());
			if (path == null)
				return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();

			Project project = service.getProject(path.getFileName().toString());
			return project == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
					: ResponseEntity.ok().body(new ProjectResponse(project));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates a project and returns it in the response body.
	 * 
	 * @param request The project request.
	 * @return The updated project in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates a project and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Updated Project", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = ProjectResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "405", description = "Method Not Allowed", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(updateRequestMapping)
	public ResponseEntity<ProjectResponse> update(@RequestBody @Valid ProjectRequest request) {
		if (request.getName() == null || request.getName().isBlank())
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

		de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.State state = null;
		try {
			state = de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.State
					.valueOf(request.getState());
		} catch (Exception e) {
			// unknown form state, ignore it
		}

		Authorization authorization = authorizationFactory.authorize(request.getId(), ProjectRight.none);
		try {
			if (authorization.project.getConfiguration().getConfiguration().updateBasicData(request.getName(),
					request.getDescription(), request.getKeywords(), request.getExchange(), state)) {
				Project updatedProject = service.getProject(request.getId());

				return updatedProject == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
						: ResponseEntity.ok().body(new ProjectResponse(updatedProject));
			} else
				return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the project history in the response body.
	 * 
	 * @param id The project id. This is the folder name.
	 * @return The project history in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the project history in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Project History", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = HistoryResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(historyInformationRequestMapping)
	public ResponseEntity<HistoryResponse> historyInformation(
			@Parameter(description = "the project id - this is the folder name") @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(id, ProjectRight.execute);
		try {
			return ResponseEntity.ok().body(new HistoryResponse(authorization.project.getHistory()));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Downloads the project history in zip format.
	 * 
	 * @param id       The project id. This is the folder name.
	 * @param response The HTTP-specific functionality in sending a response to the
	 *                 client.
	 * @throws IOException Signals that an I/O exception of some sort has occurred.
	 * @since 1.8
	 */
	@Operation(summary = "downloads the project history in zip format")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Project History ZIP"),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "405", description = "Method Not Allowed", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(value = historyDownloadRequestMapping, produces = CoreApiController.applicationZip)
	public void historyDownload(
			@Parameter(description = "the project id - this is the folder name") @RequestParam String id,
			HttpServletResponse response) throws IOException {
		Authorization authorization = authorizationFactory.authorize(id, ProjectRight.execute);
		try {
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + id + ".zip\"");

			authorization.project.zipHistory(response.getOutputStream());
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
		}
	}

	/**
	 * Initializes/reset the project and returns it in the response body.
	 * 
	 * @param id           The project id. This is the folder name.
	 * @param isInitialize True if the project should be initialized. Otherwise it
	 *                     will be reseted.
	 * @return The initialized/reseted project in the response body.
	 * @throws ResponseStatusException Throws on authorization troubles.
	 * @since 1.8
	 */
	private ResponseEntity<ProjectResponse> initializeReset(String id, boolean isInitialize)
			throws ResponseStatusException {
		Authorization authorization = authorizationFactory.authorize(id, ProjectRight.special);
		try {
			if (!authorization.project.getConfiguration().getConfiguration().isStateBlocked())
				return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
						.body(new ProjectResponse(authorization.project));

			if ((isInitialize && authorization.project.initialize())
					|| (!isInitialize && authorization.project.reset())) {
				Project project = service.getProject(id);

				return project == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
						: ResponseEntity.ok().body(new ProjectResponse(project));
			} else
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Initializes the project and returns it in the response body.
	 * 
	 * @param id The project id. This is the folder name.
	 * @return The initialized project in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "initializes the project and returns in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Initialized Project", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = ProjectResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "405", description = "Method Not Allowed", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(initializeRequestMapping)
	public ResponseEntity<ProjectResponse> initialize(
			@Parameter(description = "the project id - this is the folder name") @RequestParam String id) {
		return initializeReset(id, true);
	}

	/**
	 * Reset the project and returns it in the response body.
	 * 
	 * @param id The project id. This is the folder name.
	 * @return The reset project in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "reset the project and returns in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Reset Project", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = ProjectResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "405", description = "Method Not Allowed", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(resetRequestMapping)
	public ResponseEntity<ProjectResponse> reset(
			@Parameter(description = "the project id - this is the folder name") @RequestParam String id) {
		return initializeReset(id, false);
	}

	/**
	 * Removes the project and returns it in the response body.
	 * 
	 * @param id The project id. This is the folder name.
	 * @return The removed project in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "removes the project and returns in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Removed Project", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = ProjectResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(removeRequestMapping)
	public ResponseEntity<ProjectResponse> remove(
			@Parameter(description = "the project id - this is the folder name") @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(id, ProjectRight.none);
		try {
			return configurationService.getWorkspace().getProjects()
					.removeProject(authorization.project.getConfiguration().getFolder(), getUser())
							? ResponseEntity.ok().body(new ProjectResponse(authorization.project))
							: ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines project requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ProjectRequest extends BasicRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The exchange.
		 */
		private String exchange = null;

		/**
		 * Returns the exchange.
		 *
		 * @return The exchange.
		 * @since 1.8
		 */
		public String getExchange() {
			return exchange;
		}

		/**
		 * Set the exchange.
		 *
		 * @param exchange The exchange to set.
		 * @since 1.8
		 */
		public void setExchange(String exchange) {
			this.exchange = exchange;
		}

	}

}
