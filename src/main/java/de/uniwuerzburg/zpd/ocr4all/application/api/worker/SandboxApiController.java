/**
 * File:     SandboxApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     09.02.2022
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.BasicRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.HistoryResponse;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.MetsResponse;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.SandboxResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.ModelService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Sandbox;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.SandboxService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
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
 * Defines sandbox controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Tag(name = "sandbox", description = "the sandbox API")
@RestController
@RequestMapping(path = SandboxApiController.contextPath, produces = CoreApiController.applicationJson)
public class SandboxApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = apiContextPathVersion_1_0 + "/sandbox";

	/**
	 * The mets request mapping.
	 */
	public static final String metsRequestMapping = "/mets";

	/**
	 * The file to be ignored in the zip export.
	 */
	public static final String zipIgnoreFile = "ocrd.log";

	/**
	 * The sandbox service.
	 */
	private final SandboxService service;

	/**
	 * Creates a sandbox controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param collectionService    The collection service.
	 * @param modelService         The model service.
	 * @param service              The sandbox service.
	 * @param projectService       The project service.
	 * @since 1.8
	 */
	public SandboxApiController(ConfigurationService configurationService, SecurityService securityService,
			CollectionService collectionService, ModelService modelService, SandboxService service,
			ProjectService projectService) {
		super(ProjectApiController.class, configurationService, securityService, collectionService, modelService,
				projectService, service);

		this.service = service;
	}

	/**
	 * Returns the sandbox in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The sandbox id. This is the folder name.
	 * @return The sandbox in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the sandbox in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sandbox", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = SandboxResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(entityRequestMapping + projectPathVariable)
	public ResponseEntity<SandboxResponse> entity(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId, id);
		try {
			return ResponseEntity.ok().body(new SandboxResponse(authorization.sandbox, true));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the list of sandboxes of given project sorted by name in the response
	 * body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @return The list of sandbox sorted by name in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the list of sandboxes of given project sorted by name in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sandboxes", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = SandboxResponse.class))) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(listRequestMapping + projectPathVariable)
	public ResponseEntity<List<SandboxResponse>> list(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId) {
		Authorization authorization = authorizationFactory.authorize(projectId);
		try {
			List<SandboxResponse> sandboxes = new ArrayList<>();
			for (Sandbox sandbox : service.getSandboxes(authorization.project))
				sandboxes.add(new SandboxResponse(sandbox));

			return ResponseEntity.ok().body(sandboxes);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Creates a sandbox and returns it in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The sandbox id. This is the folder name.
	 * @return The created sandbox in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "creates a sandbox and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Created Sandbox", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = SandboxResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "405", description = "Method Not Allowed", content = @Content),
			@ApiResponse(responseCode = "409", description = "Conflict", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(createRequestMapping + projectPathVariable)
	public ResponseEntity<SandboxResponse> create(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId, ProjectRight.special);
		try {
			if (authorization.project.getConfiguration().getSandboxesConfiguration().isAvailable(id)) {
				Sandbox sandbox = service.authorize(authorization.project, id);

				return sandbox == null ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
						: ResponseEntity.status(HttpStatus.CONFLICT).body(new SandboxResponse(sandbox, true));
			}

			Path path = authorization.project.getConfiguration().getSandboxesConfiguration().create(id, getUser());
			if (path == null)
				return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();

			Sandbox sandbox = service.authorize(authorization.project, id);
			return sandbox == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
					: ResponseEntity.ok().body(new SandboxResponse(sandbox));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates a sandbox and returns it in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param request   The sandbox request.
	 * @return The updated sandbox in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates a sandbox and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Updated Sandbox", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = SandboxResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "405", description = "Method Not Allowed", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(updateRequestMapping + projectPathVariable)
	public ResponseEntity<SandboxResponse> update(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@RequestBody @Valid SandboxRequest request) {
		if (request.getName() == null || request.getName().isBlank())
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

		de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Sandbox.State state = null;
		try {
			state = de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Sandbox.State
					.valueOf(request.getState());
		} catch (Exception e) {
			// unknown form state, ignore it
		}

		Authorization authorization = authorizationFactory.authorize(projectId, request.getId(), ProjectRight.special);
		try {
			if (authorization.sandbox.getConfiguration().getConfiguration().updateBasicData(request.getName(),
					request.getDescription(), request.getKeywords(), state)) {
				Sandbox updatedSandbox = service.authorize(authorization.project, request.getId());

				return updatedSandbox == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
						: ResponseEntity.ok().body(new SandboxResponse(updatedSandbox, true));
			} else
				return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the sandbox history in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The sandbox id. This is the folder name.
	 * @return The sandbox history in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the sandbox history in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sandbox History", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = HistoryResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(historyInformationRequestMapping + projectPathVariable)
	public ResponseEntity<HistoryResponse> historyInformation(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId, id, ProjectRight.execute);
		try {
			return ResponseEntity.ok().body(new HistoryResponse(authorization.sandbox.getHistory()));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Downloads the sandbox history in zip format.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The sandbox id. This is the folder name.
	 * @param response  The HTTP-specific functionality in sending a response to the
	 *                  client.
	 * @throws IOException Signals that an I/O exception of some sort has occurred.
	 * @since 1.8
	 */
	@Operation(summary = "downloads the sandbox history in zip format")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sandbox History ZIP"),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "405", description = "Method Not Allowed", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(value = historyDownloadRequestMapping
			+ projectPathVariable, produces = CoreApiController.applicationZip)
	public void historyDownload(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @RequestParam String id,
			HttpServletResponse response) throws IOException {
		Authorization authorization = authorizationFactory.authorize(projectId, id, ProjectRight.execute);
		try {
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + id + ".zip\"");

			authorization.sandbox.zipHistory(response.getOutputStream());
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
		}
	}

	/**
	 * Reset the sandbox and returns it in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The sandbox id. This is the folder name.
	 * @return The reseted sandbox in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "reset the sandbox and returns in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Reset Sandbox", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = SandboxResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(resetRequestMapping + projectPathVariable)
	public ResponseEntity<SandboxResponse> reset(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId, id, ProjectRight.special);
		try {
			if (authorization.sandbox.reset()) {
				Sandbox sandbox = service.authorize(authorization.project, id);

				return sandbox == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
						: ResponseEntity.ok().body(new SandboxResponse(sandbox, false));
			} else
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Removes the sandbox and returns it in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The sandbox id. This is the folder name.
	 * @return The removed sandbox in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "removes the sandbox and returns in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Removed Sandbox", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = SandboxResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(removeRequestMapping + projectPathVariable)
	public ResponseEntity<SandboxResponse> remove(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId, id, ProjectRight.special);
		try {
			return authorization.project.getConfiguration().getSandboxesConfiguration()
					.remove(authorization.sandbox.getConfiguration().getFolder(), getUser())
							? ResponseEntity.ok().body(new SandboxResponse(authorization.sandbox))
							: ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the information contained in the mets (Metadata Encoding and
	 * Transmission Standard) XML file in the specified sandbox in the response
	 * body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The sandbox id. This is the folder name.
	 * @return The mets (Metadata Encoding and Transmission Standard) information in
	 *         the response body.
	 * @since 1.8
	 */
	@Operation(summary = "Returns the information contained in the mets (Metadata Encoding and Transmission Standard) XML file in the specified sandbox in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "METS", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = MetsResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(metsRequestMapping + projectPathVariable)
	public ResponseEntity<MetsResponse> mets(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId, id);
		try {
			return ResponseEntity.ok().body(new MetsResponse(authorization.sandbox));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
		}
	}

	/**
	 * Zips the files in the sandbox. The metadata is not included.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The sandbox id. This is the folder name.
	 * @param response  The HTTP-specific functionality in sending a response to the
	 *                  client.
	 * @throws IOException Signals that an I/O exception of some sort has occurred.
	 * @since 1.8
	 */
	@Operation(summary = "zips the files in the sandbox")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Zip Leaf Track Snapshot"),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(zipRequestMapping + projectPathVariable)
	public void zip(@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @RequestParam String id,
			HttpServletResponse response) throws IOException {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, id);
		try {
			Path sandbox = authorization.sandbox.getSnapshot().getConfiguration().getFolder();

			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
					+ authorization.project.getName() + "_" + authorization.sandbox.getName() + ".zip\"");

			OCR4allUtils.zip(sandbox, true, response.getOutputStream(),
					entry -> !entry.isHidden() && (entry.isDirectory() || !zipIgnoreFile.equals(entry.getName())),
					getZipMetadataFilenameMappingTSV(authorization.project));
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines sandbox requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class SandboxRequest extends BasicRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

	}

}
