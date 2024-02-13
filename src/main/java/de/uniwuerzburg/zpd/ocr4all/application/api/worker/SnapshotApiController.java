/**
 * File:     SnapshotApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     18.03.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.SnapshotRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.SnapshotResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.SandboxService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Snapshot;
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
import jakarta.validation.constraints.NotBlank;

/**
 * Defines snapshot controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Tag(name = "snapshot", description = "the snapshot API")
@RestController
@RequestMapping(path = SnapshotApiController.contextPath, produces = CoreApiController.applicationJson)
public class SnapshotApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = apiContextPathVersion_1_0 + "/snapshot";

	/**
	 * The path request mapping.
	 */
	public static final String derivedRequestMapping = "/derived";

	/**
	 * The path request mapping.
	 */
	public static final String pathRequestMapping = "/path";

	/**
	 * Creates a snapshot controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param projectService       The project service.
	 * @param sandboxService       The sandbox service.
	 * @since 1.8
	 */
	public SnapshotApiController(ConfigurationService configurationService, SecurityService securityService,
			ProjectService projectService, SandboxService sandboxService) {
		super(ProjectApiController.class, configurationService, securityService, projectService, sandboxService);
	}

	/**
	 * Returns the leaf snapshot in the track of the request in the response body.
	 * If the track is empty, then returns the root snapshot.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param sandboxId The sandbox id. This is the folder name.
	 * @param request   The snapshot request.
	 * @return The snapshot in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the leaf snapshot in the track of the request in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Leaf Track Snapshot", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = SnapshotResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(entityRequestMapping + projectPathVariable + sandboxPathVariable)
	public ResponseEntity<SnapshotResponse> entity(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @PathVariable String sandboxId,
			@RequestBody @Valid SnapshotRequest request) {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, sandboxId);
		try {
			return ResponseEntity.ok()
					.body(new SnapshotResponse(authorization.sandbox.getSnapshot(request.getTrack())));
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the derived snapshots of the leaf snapshot in the track of the
	 * request in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param sandboxId The sandbox id. This is the folder name.
	 * @param request   The snapshot request.
	 * @return The snapshots in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the derived snapshots of the leaf snapshot in the track of the request in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Derived Track Snapshots", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = SnapshotResponse.class))) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(derivedRequestMapping + projectPathVariable + sandboxPathVariable)
	public ResponseEntity<List<SnapshotResponse>> derived(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @PathVariable String sandboxId,
			@RequestBody @Valid SnapshotRequest request) {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, sandboxId);
		try {
			List<Snapshot> snapshots = authorization.sandbox.getDerived(request.getTrack());

			List<SnapshotResponse> response = new ArrayList<>();
			for (Snapshot snapshot : snapshots)
				response.add(new SnapshotResponse(snapshot));

			return ResponseEntity.ok().body(response);
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the snapshots in the track of the request in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param sandboxId The sandbox id. This is the folder name.
	 * @param request   The snapshot request.
	 * @return The snapshots in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the snapshots in the track of the request in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Track Snapshots", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = SnapshotResponse.class))) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(pathRequestMapping + projectPathVariable + sandboxPathVariable)
	public ResponseEntity<List<SnapshotResponse>> path(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @PathVariable String sandboxId,
			@RequestBody @Valid SnapshotRequest request) {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, sandboxId);
		try {
			List<Snapshot> snapshots = authorization.sandbox.getSnapshots(request.getTrack());

			List<SnapshotResponse> response = new ArrayList<>();
			for (Snapshot snapshot : snapshots)
				response.add(new SnapshotResponse(snapshot));

			return ResponseEntity.ok().body(response);

		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates the configuration of the leaf snapshot in the track of the request
	 * and returns it in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param sandboxId The sandbox id. This is the folder name.
	 * @param request   The snapshot configuration request.
	 * @return The updated snapshot in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the configuration of the leaf snapshot in the track of the request and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Updated Leaf Track Snapshot", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = SnapshotResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "409", description = "Conflict", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(updateRequestMapping + projectPathVariable + sandboxPathVariable)
	public ResponseEntity<SnapshotResponse> update(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @PathVariable String sandboxId,
			@RequestBody @Valid SnapshotConfigurationRequest request) {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, sandboxId,
				ProjectRight.execute);
		try {
			Snapshot snapshot = authorization.sandbox.getSnapshot(request.getTrack());

			return snapshot.getConfiguration().getConfiguration().updateMainConfiguration(request.getLabel(),
					request.getDescription()) ? ResponseEntity.ok().body(new SnapshotResponse(snapshot))
							: ResponseEntity.status(HttpStatus.CONFLICT).build();
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Locks the leaf snapshot in the track of the request and returns it in the
	 * response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param sandboxId The sandbox id. This is the folder name.
	 * @param request   The snapshot lock request.
	 * @return The updated snapshot in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "locks the leaf snapshot in the track of the request and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Locked Leaf Track Snapshot", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = SnapshotResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "409", description = "Conflict", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(lockRequestMapping + projectPathVariable + sandboxPathVariable)
	public ResponseEntity<SnapshotResponse> lock(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @PathVariable String sandboxId,
			@RequestBody @Valid SnapshotLockRequest request) {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, sandboxId,
				ProjectRight.execute);
		try {
			Snapshot snapshot = authorization.sandbox.getSnapshot(request.getTrack());

			return snapshot.getConfiguration().getConfiguration().lockSnapshot(request.getSource(),
					request.getComment()) ? ResponseEntity.ok().body(new SnapshotResponse(snapshot))
							: ResponseEntity.status(HttpStatus.CONFLICT).build();
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Unlocks the leaf snapshot in the track of the request and returns it in the
	 * response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param sandboxId The sandbox id. This is the folder name.
	 * @param request   The snapshot request.
	 * @return The updated snapshot in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "unlocks the leaf snapshot in the track of the request and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Unlocked Leaf Track Snapshot", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = SnapshotResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "409", description = "Conflict", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(unlockRequestMapping + projectPathVariable + sandboxPathVariable)
	public ResponseEntity<SnapshotResponse> unlock(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @PathVariable String sandboxId,
			@RequestBody @Valid SnapshotRequest request) {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, sandboxId,
				ProjectRight.execute);
		try {
			Snapshot snapshot = authorization.sandbox.getSnapshot(request.getTrack());

			return snapshot.getConfiguration().getConfiguration().unlockSnapshot()
					? ResponseEntity.ok().body(new SnapshotResponse(snapshot))
					: ResponseEntity.status(HttpStatus.CONFLICT).build();
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Removes the leaf snapshot in the track of the request and returns it in the
	 * response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param sandboxId The sandbox id. This is the folder name.
	 * @param request   The snapshot request.
	 * @return The removed snapshot in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "removes the leaf snapshot in the track of the request and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Removed Leaf Track Snapshot", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = SnapshotResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(removeRequestMapping + projectPathVariable + sandboxPathVariable)
	public ResponseEntity<SnapshotResponse> remove(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @PathVariable String sandboxId,
			@RequestBody @Valid SnapshotRequest request) {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, sandboxId,
				ProjectRight.execute);
		try {
			final Snapshot snapshot = authorization.sandbox.getSnapshot(request.getTrack());
			final SnapshotResponse snapshotResponse = new SnapshotResponse(snapshot);

			return (snapshot.getConfiguration().isRoot()
					? authorization.sandbox.getConfiguration().getSnapshots().reset()
					: snapshot.getConfiguration().getParent()
							.removeDerived(request.getTrack().get(request.getTrack().size() - 1)))
									? ResponseEntity.ok().body(snapshotResponse)
									: ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the list of files in the sandbox of the leaf snapshot in the track of
	 * the request in the response body. If the track is null or empty, then the
	 * sandbox belongs to the root snapshot.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param sandboxId The sandbox id. This is the folder name.
	 * @param request   The snapshot request.
	 * @return The files in the sandbox of the snapshot.
	 * @since 1.8
	 */
	@Operation(summary = "returns the list of files in the sandbox of the leaf snapshot in the track of the request in the response body - if the track is null or empty, then the sandbox belongs to the root snapshot")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "File List Leaf Track Snapshot", content = {
					@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = SnapshotResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(fileRequestMapping + projectPathVariable + sandboxPathVariable)
	public ResponseEntity<SandboxResponse> fileList(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @PathVariable String sandboxId,
			@RequestBody @Valid SnapshotRequest request) {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, sandboxId);
		try {
			return ResponseEntity.ok().body(new SandboxResponse(authorization.sandbox.getSnapshot(request.getTrack())
					.getConfiguration().getSandbox().listAllFiles()));
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Downloads the file in the sandbox of the leaf snapshot in the track of the
	 * request.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param sandboxId The sandbox id. This is the folder name.
	 * @param request   The sandbox file request.
	 * @param response  The HTTP-specific functionality in sending a response to the
	 *                  client.
	 * @throws IOException Signals that an I/O exception of some sort has occurred.
	 * @since 1.8
	 */
	@Operation(summary = "downloads the file in the sandbox of the leaf snapshot in the track of the request")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "File Leaf Track Snapshot"),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(downloadRequestMapping + projectPathVariable + sandboxPathVariable)
	public void download(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @PathVariable String sandboxId,
			@RequestBody @Valid SandboxFileRequest request, HttpServletResponse response) throws IOException {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, sandboxId);
		try {
			Snapshot snapshot = authorization.sandbox.getSnapshot(request.getTrack());

			Path path = Paths.get(snapshot.getConfiguration().getSandbox().getFolder().toString(), request.getFile())
					.normalize();
			if (!path.startsWith(snapshot.getConfiguration().getSandbox().getFolder()) || !Files.exists(path)
					|| Files.isDirectory(path))
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

			byte[] content = Files.readAllBytes(path);

			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName() + "\"");
			response.getOutputStream().write(content, 0, content.length);
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
	 * Zips the files in the sandbox of the leaf snapshot in the track of the
	 * request.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param sandboxId The sandbox id. This is the folder name.
	 * @param request   The snapshot request.
	 * @param response  The HTTP-specific functionality in sending a response to the
	 *                  client.
	 * @throws IOException Signals that an I/O exception of some sort has occurred.
	 * @since 1.8
	 */
	@Operation(summary = "zips the files in the sandbox of the leaf snapshot in the track of the request")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Zip Leaf Track Snapshot"),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(zipRequestMapping + projectPathVariable + sandboxPathVariable)
	public void zip(@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @PathVariable String sandboxId,
			@RequestBody @Valid SnapshotRequest request, HttpServletResponse response) throws IOException {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, sandboxId);
		try {
			Path sandbox = authorization.sandbox.getSnapshot(request.getTrack()).getConfiguration().getSandbox()
					.getFolder();

			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
					+ authorization.project.getName() + "_" + authorization.sandbox.getName() + "_snapshot.zip\"");

			OCR4allUtils.zip(sandbox, true, response.getOutputStream(), null,
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
	 * Defines snapshot configuration requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class SnapshotConfigurationRequest extends SnapshotRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The label.
		 */
		@NotBlank
		private String label;

		/**
		 * The description.
		 */
		private String description;

		/**
		 * Returns the label.
		 *
		 * @return The label.
		 * @since 1.8
		 */
		public String getLabel() {
			return label;
		}

		/**
		 * Set the label.
		 *
		 * @param label The label to set.
		 * @since 1.8
		 */
		public void setLabel(String label) {
			this.label = label;
		}

		/**
		 * Returns the description.
		 *
		 * @return The description.
		 * @since 1.8
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * Set the description.
		 *
		 * @param description The description to set.
		 * @since 1.8
		 */
		public void setDescription(String description) {
			this.description = description;
		}

	}

	/**
	 * Defines snapshot lock requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class SnapshotLockRequest extends SnapshotRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The source.
		 */
		@NotBlank
		private String source;

		/**
		 * The comment.
		 */
		private String comment;

		/**
		 * Returns the source.
		 *
		 * @return The source.
		 * @since 1.8
		 */
		public String getSource() {
			return source;
		}

		/**
		 * Set the source.
		 *
		 * @param source The source to set.
		 * @since 1.8
		 */
		public void setSource(String source) {
			this.source = source;
		}

		/**
		 * Returns the comment.
		 *
		 * @return The comment.
		 * @since 1.8
		 */
		public String getComment() {
			return comment;
		}

		/**
		 * Set the comment.
		 *
		 * @param comment The comment to set.
		 * @since 1.8
		 */
		public void setComment(String comment) {
			this.comment = comment;
		}

	}

	/**
	 * Defines sandbox responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class SandboxResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The files.
		 */
		private List<String> files;

		/**
		 * Creates a sandbox responses for the api.
		 * 
		 * @param files The files.
		 * @since 1.8
		 */
		public SandboxResponse(List<String> files) {
			super();

			this.files = files;
		}

		/**
		 * Returns the files.
		 *
		 * @return The files.
		 * @since 1.8
		 */
		public List<String> getFiles() {
			return files;
		}

		/**
		 * Set the files.
		 *
		 * @param files The files to set.
		 * @since 1.8
		 */
		public void setFiles(List<String> files) {
			this.files = files;
		}

	}

	/**
	 * Defines sandbox requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class SandboxFileRequest extends SnapshotRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The file.
		 */
		@NotBlank
		private String file;

		/**
		 * Returns the file.
		 *
		 * @return The file.
		 * @since 1.8
		 */
		public String getFile() {
			return file;
		}

		/**
		 * Set the file.
		 *
		 * @param file The file to set.
		 * @since 1.8
		 */
		public void setFile(String file) {
			this.file = file;
		}

	}

}
