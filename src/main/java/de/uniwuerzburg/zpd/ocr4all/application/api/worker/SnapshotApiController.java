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
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.SnapshotRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.SetResponse;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.SnapshotResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.ModelService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Sandbox;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.SandboxService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Snapshot;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.postcorrection.provider.LAREXLauncher;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.folio.Folio;
import de.uniwuerzburg.zpd.ocr4all.application.spi.util.mets.MetsParser;
import de.uniwuerzburg.zpd.ocr4all.application.spi.util.mets.MetsUtils;
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
import jakarta.validation.constraints.NotNull;

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
	 * @param collectionService    The collection service.
	 * @param modelService         The model service.
	 * @param projectService       The project service.
	 * @param sandboxService       The sandbox service.
	 * @since 1.8
	 */
	public SnapshotApiController(ConfigurationService configurationService, SecurityService securityService,
			CollectionService collectionService, ModelService modelService, ProjectService projectService,
			SandboxService sandboxService) {
		super(ProjectApiController.class, configurationService, securityService, collectionService, modelService,
				projectService, sandboxService);
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
	 * Adds the snapshot files to the collection as set.
	 * 
	 * @param isAllSets True if all all snapshot sets. Otherwise, adds the desired
	 *                  sets.
	 * @param sandbox   The sandbox.
	 * @param request   The snapshot collection request.
	 * @return The added collection sets.
	 * @throws ResponseStatusException Throw with http status:
	 *                                 <ul>
	 *                                 <li>400 (Bad Request): if the collection is
	 *                                 not available or the sandbox is not for a
	 *                                 LAREX processor.</li>
	 *                                 <li>401 (Unauthorized): if the read security
	 *                                 permission is not achievable by the session
	 *                                 user.</li>
	 *                                 <li>404 (Not Found): if the snapshot track is
	 *                                 invalid for the sandbox.</li>
	 *                                 <li>412 (Precondition Failed): if the mets
	 *                                 file group is unknown.</li>
	 *                                 <li>503 (Service Unavailable): in case of
	 *                                 unexpected exceptions.</li>
	 *                                 </ul>
	 * @since 17
	 */
	private List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> addSnapshot2collection(boolean isAllSets,
			Sandbox sandbox, SnapshotCollectionRequest request) throws ResponseStatusException {
		CollectionService.Collection collection = authorizeCollectionRead(request.getCollectionId());

		Snapshot snapshot;
		try {
			snapshot = sandbox.getSnapshot(request.getTrack());
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		if (!snapshot.getConfiguration().getConfiguration().getMainConfiguration().getServiceProvider().getId()
				.equals(LAREXLauncher.class.getName()))
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

		List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> sets = null;

		Path temporaryFolder = null;
		final String snapshotsFolder = sandbox.getConfiguration().getSnapshots().getRoot().getFolder().toString();
		Path mets = Paths.get(snapshotsFolder, sandbox.getConfiguration().getMetsFileName());
		if (Files.exists(mets))
			try {
				final MetsParser.Root root = (new MetsParser()).deserialise(mets.toFile());
				final String metsGroup = sandbox.getConfiguration().getMetsGroup();

				// search mets file group
				final String fileGroupID = MetsUtils.getFileGroup(metsGroup)
						.getFileGroup(snapshot.getConfiguration().getTrack());
				MetsParser.Root.FileGroup fileGroup = null;

				for (MetsParser.Root.FileGroup group : root.getFileGroups())
					if (fileGroupID.equals(group.getId()))
						fileGroup = group;

				if (fileGroup == null)
					throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED);

				// mets pages
				final Hashtable<String, String> metsFieldId2ocr4allId = new Hashtable<>();

				final MetsUtils.Page metsPageUtils = MetsUtils.getPage(metsGroup);
				for (MetsParser.Root.StructureMap.PhysicalSequence.Page page : root.getStructureMap()
						.getPhysicalSequence().getPages())
					try {
						String ocr4allId = metsPageUtils.getGroupId(page.getId());
						for (MetsParser.Root.StructureMap.PhysicalSequence.Page.FileId fieldId : page.getFileIds())
							metsFieldId2ocr4allId.put(fieldId.getId(), ocr4allId);
					} catch (Exception e) {
						// Ignore malformed mets page
					}

				// The sets to import. The value is the ocr4all id. Null to import all.
				final Set<String> importSets = isAllSets ? null
						: new HashSet<>(((SnapshotCollectionSetRequest) request).getSets());

				// The folio names map
				final Set<String> availableFolios = new HashSet<>();
				final List<Folio> folios = sandbox.getProject().getFolios();
				for (Folio folio : folios)
					availableFolios.add(folio.getId());

				temporaryFolder = configurationService.getTemporary().getTemporaryDirectory();

				final Set<String> availableSets = new HashSet<>();
				for (MetsParser.Root.FileGroup.File file : fileGroup.getFiles()) {
					String ocr4allId = metsFieldId2ocr4allId.get(file.getId());

					if (ocr4allId != null && (isAllSets || importSets.contains(ocr4allId))
							&& availableFolios.contains(ocr4allId)) {
						Path data = Paths.get(snapshotsFolder, file.getLocation().getPath());

						if (Files.isRegularFile(data)) {
							// Build target name
							String target = data.getFileName().toString();

							int index = target.indexOf(ocr4allId);
							if (index < 0)
								target = ocr4allId + "." + target;
							else {
								String suffix = target.substring(index + ocr4allId.length());
								target = ocr4allId + (suffix.startsWith(".") ? suffix : "." + suffix);
							}

							// copy the file to the temporary directory
							Files.copy(data, Paths.get(temporaryFolder.toString(), target),
									StandardCopyOption.REPLACE_EXISTING);

							availableSets.add(ocr4allId);
						}
					}
				}

				if (!availableSets.isEmpty()) {
					final Date date = new Date();
					final String user = securityService.getUser();

					final List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> collectionSets = new ArrayList<>();
					for (Folio folio : folios)
						if (availableSets.contains(folio.getId()))
							collectionSets.add(new de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set(date,
									user, request.isKeywords() ? folio.getKeywords() : null, folio.getId(),
									folio.getName()));

					sets = collectionService.add(collection, collectionSets, temporaryFolder, true);
				}

			} catch (ResponseStatusException ex) {
				throw ex;
			} catch (Exception ex) {
				log(ex);

				throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
			} finally {
				if (temporaryFolder != null)
					try {
						FileSystemUtils.deleteRecursively(temporaryFolder);
					} catch (IOException e) {
						logger.warn("cannot delete directory " + temporaryFolder + " - " + e.getMessage() + ".");
					}
			}

		return sets == null ? new ArrayList<>() : sets;
	}

	/**
	 * Returns the files for the sets.
	 * 
	 * @param collectionId The collection id.
	 * @return The files for the sets. The key is the set id.
	 * @throws IOException
	 * @since 17
	 */
	private Hashtable<String, List<Path>> getSetFiles(String collectionId) throws IOException {
		Hashtable<String, List<Path>> set = new Hashtable<>();

		CollectionService.Collection collection = collectionService.getCollection(collectionId);

		if (collection == null)
			return set;

		List<Path> files;
		try (Stream<Path> stream = Files.list(collection.getConfiguration().getFolder())) {
			files = stream.filter(file -> !Files.isDirectory(file)).collect(Collectors.toList());
		}

		for (Path name : files) {
			String[] split = name.getFileName().toString().split("\\.", 2);
			if (split.length == 2 && !split[0].isEmpty()) {
				List<Path> list = set.get(split[0]);
				if (list == null) {
					list = new ArrayList<Path>();
					set.put(split[0], list);
				}

				list.add(name);
			}
		}

		return set;
	}

	/**
	 * Adds all snapshot sets to a collection and returns the list of added sets of
	 * the collection in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param sandboxId The sandbox id. This is the folder name.
	 * @param request   The snapshot collection set request.
	 * @return The list of added sets to the collection.
	 * @since 1.8
	 */
	@Operation(summary = "adds all snapshot sets to a collection and returns the list of added sets of given collection in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Collection Sets", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = SetResponse.class))) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "412", description = "Precondition Failed", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(collectionRequestMapping + allRequestMapping + projectPathVariable + sandboxPathVariable)
	public ResponseEntity<List<SetResponse>> addAllSnapshot2collection(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @PathVariable String sandboxId,
			@RequestBody @Valid SnapshotCollectionRequest request) {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, sandboxId);
		try {
			List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> addedSets = addSnapshot2collection(true,
					authorization.sandbox, request);

			Hashtable<String, List<Path>> setFiles = getSetFiles(request.getCollectionId());
			final List<SetResponse> sets = new ArrayList<>();

			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : addedSets)
				sets.add(new SetResponse(set, setFiles));

			return ResponseEntity.ok().body(sets);
		} catch (ResponseStatusException ex) {
			return ResponseEntity.status(ex.getStatusCode()).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Adds the desired snapshot sets to a collection and returns the list of added
	 * sets of the collection in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param sandboxId The sandbox id. This is the folder name.
	 * @param request   The snapshot collection set request.
	 * @return The list of sets of the collection.
	 * @since 1.8
	 */
	@Operation(summary = "adds the desired snapshot sets to a collection and returns the list of added sets of given collection in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Collection Sets", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = SetResponse.class))) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "412", description = "Precondition Failed", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(collectionRequestMapping + setRequestMapping + projectPathVariable + sandboxPathVariable)
	public ResponseEntity<List<SetResponse>> addSetSnapshot2collection(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @PathVariable String sandboxId,
			@RequestBody @Valid SnapshotCollectionSetRequest request) {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, sandboxId);
		try {
			List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> addedSets = addSnapshot2collection(false,
					authorization.sandbox, request);

			final List<SetResponse> sets = new ArrayList<>();
			Hashtable<String, List<Path>> setFiles = getSetFiles(request.getCollectionId());

			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : addedSets)
				sets.add(new SetResponse(set, setFiles));

			return ResponseEntity.ok().body(sets);
		} catch (ResponseStatusException ex) {
			return ResponseEntity.status(ex.getStatusCode()).build();
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
	 * Returns the file descriptions in the snapshot track.
	 * 
	 * @param project The project.
	 * @param sandbox The sandbox.
	 * @param track   The track.
	 * @return The file descriptions in the snapshot track.
	 * @since 17
	 */
	private List<FileDescription> getFileDescriptions(Project project, Sandbox sandbox, List<Integer> track) {
		final List<FileDescription> fileDescriptions = new ArrayList<>();

		final Path workspaceSnapshots = sandbox.getConfiguration().getSnapshots().getFolder();
		final Path metsPath = workspaceSnapshots
				.resolve(project.getConfiguration().getSandboxesConfiguration().getMetsFileName());
		final MetsParser.Root root;

		if (Files.exists(metsPath))
			try {
				root = (new MetsParser()).deserialise(metsPath.toFile());
			} catch (Exception e) {
				logger.warn("could not parse the mets file '" + metsPath.toString() + "' - " + e.getMessage());

				return fileDescriptions;
			}
		else {
			logger.warn("the mets file '" + metsPath.toString() + "' is not available.");

			return fileDescriptions;
		}

		// search for snapshot file group
		MetsUtils.FileGroup metsFileGroup = MetsUtils
				.getFileGroup(project.getConfiguration().getSandboxesConfiguration().getMetsGroup());
		String snapshotFileGroup = metsFileGroup.getFileGroup(track);

		MetsParser.Root.FileGroup fileGroup = null;
		for (MetsParser.Root.FileGroup group : root.getFileGroups())
			if (snapshotFileGroup.equals(group.getId())) {
				fileGroup = group;

				break;
			}

		if (fileGroup == null) {
			logger.warn("the required mets file group '" + snapshotFileGroup + "' of mets file '" + metsPath.toString()
					+ "' is not available.");

			return fileDescriptions;
		}

		// mets file ids to folios ids
		final Hashtable<String, String> fileFolioMappings = new Hashtable<>();
		final MetsUtils.Page metsPageUtils = MetsUtils.getPage(sandbox.getConfiguration().getMetsGroup());
		for (MetsParser.Root.StructureMap.PhysicalSequence.Page page : root.getStructureMap().getPhysicalSequence()
				.getPages())
			try {
				final String folioId = metsPageUtils.getGroupId(page.getId());
				for (MetsParser.Root.StructureMap.PhysicalSequence.Page.FileId fieldId : page.getFileIds())
					fileFolioMappings.put(fieldId.getId(), folioId);
			} catch (Exception e) {
				// Ignore malformed mets page
			}

		// load the track
		for (MetsParser.Root.FileGroup.File file : fileGroup.getFiles()) {
			final String id = fileFolioMappings.get(file.getId());

			if (id == null) {
				logger.warn("unknown folio id mapping for file id '" + file.getId() + "' of mets file '"
						+ metsPath.toString() + "'.");

				continue;
			}

			final Path path = workspaceSnapshots.resolve(file.getLocation().getPath());

			// Build file suffix
			final String target = path.getFileName().toString();
			int index = target.indexOf(id);

			fileDescriptions
					.add(new FileDescription(id, index < 0 ? null : target.substring(index + id.length()), path));
		}

		return fileDescriptions;
	}

	/**
	 * Exports the files in the sandbox of the leaf snapshot in the track of the
	 * request in a zip file. Normalizes and returns the source images if desired.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param sandboxId The sandbox id. This is the folder name.
	 * @param request   The snapshot export request.
	 * @param response  The HTTP-specific functionality in sending a response to the
	 *                  client.
	 * @throws IOException Signals that an I/O exception of some sort has occurred.
	 * @since 1.8
	 */
	@Operation(summary = "exports the files in the sandbox of the leaf snapshot in the track of the request in a zip file - normalizes and returns the source images if desired")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Exports Leaf Track Snapshot"),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(exportRequestMapping + projectPathVariable + sandboxPathVariable)
	public void export(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @PathVariable String sandboxId,
			@RequestBody @Valid SnapshotExportRequest request, HttpServletResponse response) throws IOException {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, sandboxId);
		try {
			// Get the normalized and unique file names
			Set<String> filenames = new HashSet<String>();
			Hashtable<String, String> mappings = new Hashtable<>();
			Hashtable<String, Folio> folios = new Hashtable<>();
			for (Folio folio : authorization.project.getFolios()) {
				String filename = request.isNormalizeFilenames() ? folio.getName().replaceAll("\\W+", "_")
						: folio.getName();

				// rename the file name if it is not unique
				if (!filenames.add(filename.toLowerCase())) {
					int index = 0;
					String unique;

					do {
						unique = filename + "_" + ++index;
					} while (!filenames.add(unique.toLowerCase()));

					filename = unique;
				}

				mappings.put(folio.getId(), filename);
				folios.put(folio.getId(), folio);
			}

			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
					+ authorization.project.getName() + "_" + authorization.sandbox.getName() + "_snapshot.zip\"");

			try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());) {
				final List<FileDescription> fileDescriptions = getFileDescriptions(authorization.project,
						authorization.sandbox, request.getTrack());

				final Set<String> zipFilenames = new HashSet<String>();
				for (FileDescription fileDescription : fileDescriptions)
					if (Files.isReadable(fileDescription.getPath())) {
						String filename = mappings.get(fileDescription.getId());
						if (filename == null)
							filename = fileDescription.getId();

						String unique = filename + (fileDescription.isSuffixSet() ? fileDescription.getSuffix() : "");
						if (!zipFilenames.add(unique)) {
							int index = 0;

							do {
								unique = filename + "_" + ++index;
							} while (!filenames.add(unique.toLowerCase()));

							filename = unique;
						}

						zipOutputStream.putNextEntry(new ZipEntry(
								filename + (fileDescription.isSuffixSet() ? fileDescription.getSuffix() : "")));

						Files.copy(fileDescription.getPath(), zipOutputStream);

						zipOutputStream.closeEntry();
					}

				// adds the source images
				if (request.isSourceImages()) {
					final String sourceFolder = "source/";
					zipOutputStream.putNextEntry(new ZipEntry(sourceFolder));
					zipOutputStream.closeEntry();

					final Path foliosPath = authorization.project.getConfiguration().getImages().getFolios();
					Set<String> folioIds = new HashSet<>();
					for (FileDescription fileDescription : fileDescriptions)
						if (folioIds.add(fileDescription.getId()) && folios.containsKey(fileDescription.getId())) {
							final String suffix = folios.get(fileDescription.getId()).getFormat().name();
							final Path folio = foliosPath.resolve(fileDescription.getId() + "." + suffix);
							if (Files.isReadable(folio)) {
								String filename = mappings.get(fileDescription.getId());
								if (filename == null)
									filename = fileDescription.getId();

								zipOutputStream.putNextEntry(new ZipEntry(sourceFolder + filename + "." + suffix));

								Files.copy(folio, zipOutputStream);

								zipOutputStream.closeEntry();
							}
						}
				}

				response.getOutputStream().flush();
			}
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
	 * FileDescription is an immutable class that defines file descriptions.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	private static class FileDescription {
		/**
		 * The id.
		 */
		private final String id;

		/**
		 * The suffix.
		 */
		private final String suffix;

		/**
		 * The path.
		 */
		private final Path path;

		/**
		 * Creates a file description.
		 * 
		 * @param id     The id.
		 * @param suffix The suffix.
		 * @param path   The path.
		 * @since 17
		 */
		public FileDescription(String id, String suffix, Path path) {
			super();

			this.id = id;
			this.suffix = suffix == null || suffix.isBlank() ? null : suffix.trim();
			this.path = path;

			System.out.println("HB: filename " + this.path.getFileName().toString() + " -> suffix " + this.suffix);
		}

		/**
		 * Returns the id.
		 *
		 * @return The id.
		 * @since 17
		 */
		public String getId() {
			return id;
		}

		/**
		 * Returns true if the suffix is set.
		 *
		 * @return True if the suffix is set.
		 * @since 17
		 */
		public boolean isSuffixSet() {
			return suffix != null;
		}

		/**
		 * Returns the suffix.
		 *
		 * @return The suffix.
		 * @since 17
		 */
		public String getSuffix() {
			return suffix;
		}

		/**
		 * Returns the path.
		 *
		 * @return The path.
		 * @since 17
		 */
		public Path getPath() {
			return path;
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
	 * Defines snapshot collection requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class SnapshotCollectionRequest extends SnapshotRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The collection id.
		 */
		@NotBlank
		@JsonProperty("collection-id")
		private String collectionId;

		/**
		 * True if the keywords available in the folio configurations are to be added to
		 * the collection sets.
		 */
		@JsonProperty("keywords")
		private boolean isKeywords;

		/**
		 * Returns the collectionId.
		 *
		 * @return The collectionId.
		 * @since 17
		 */
		public String getCollectionId() {
			return collectionId;
		}

		/**
		 * Set the collectionId.
		 *
		 * @param collectionId The collectionId to set.
		 * @since 17
		 */
		public void setCollectionId(String collectionId) {
			this.collectionId = collectionId;
		}

		/**
		 * Returns true if the keywords available in the folio configurations are to be
		 * added to the collection sets.
		 *
		 * @return True if the keywords available in the folio configurations are to be
		 *         added to the collection sets.
		 * @since 17
		 */
		@JsonGetter("keywords")
		public boolean isKeywords() {
			return isKeywords;
		}

		/**
		 * Set to true if the keywords available in the folio configurations are to be
		 * added to the collection sets.
		 *
		 * @param isKeywords The keywords flag to set.
		 * @since 17
		 */
		public void setKeywords(boolean isKeywords) {
			this.isKeywords = isKeywords;
		}

	}

	/**
	 * Defines snapshot collection set requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class SnapshotCollectionSetRequest extends SnapshotCollectionRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The sets.
		 */
		@NotNull
		private List<String> sets;

		/**
		 * Returns the sets.
		 *
		 * @return The sets.
		 * @since 17
		 */
		public List<String> getSets() {
			return sets;
		}

		/**
		 * Set the sets.
		 *
		 * @param sets The sets to set.
		 * @since 17
		 */
		public void setSets(List<String> sets) {
			this.sets = sets;
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

	/**
	 * Defines snapshot export requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class SnapshotExportRequest extends SnapshotRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * True if normalize file names.
		 */
		@JsonProperty("normalize-filenames")
		private boolean isNormalizeFilenames;

		/**
		 * True if download source images.
		 */
		@JsonProperty("source-images")
		private boolean isSourceImages;

		/**
		 * Returns true if normalize file names.
		 *
		 * @return True if normalize file names.
		 * @since 17
		 */
		@JsonGetter("normalize-filenames")
		public boolean isNormalizeFilenames() {
			return isNormalizeFilenames;
		}

		/**
		 * Set to true if normalize file names.
		 *
		 * @param isNormalizeFilenames The normalize flag to set.
		 * @since 17
		 */
		public void setNormalizeFilenames(boolean isNormalizeFilenames) {
			this.isNormalizeFilenames = isNormalizeFilenames;
		}

		/**
		 * Returns true if download source images.
		 *
		 * @return True if download source images.
		 * @since 17
		 */
		@JsonGetter("source-images")
		public boolean isSourceImages() {
			return isSourceImages;
		}

		/**
		 * Set to true if download source images.
		 *
		 * @param isSourceImages The download flag to set.
		 * @since 17
		 */
		public void setSourceImages(boolean isSourceImages) {
			this.isSourceImages = isSourceImages;
		}

	}
}
