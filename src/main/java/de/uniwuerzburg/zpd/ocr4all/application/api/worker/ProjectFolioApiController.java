/**
 * File:     FolioApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     11.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Profile;
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

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.FolioSortRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.FolioUpdateRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.IdentifiersRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.FolioResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.ModelService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.repository.ContainerService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.ImageUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.folio.Folio;
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
 * Defines project folio controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Tag(name = "project folio", description = "the project folio API")
@RestController
@RequestMapping(path = ProjectFolioApiController.contextPath, produces = CoreApiController.applicationJson)
public class ProjectFolioApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = ProjectApiController.contextPath + folioRequestMapping;

	/**
	 * The container service.
	 */
	private final ContainerService containerService;

	/**
	 * Creates a folio controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param collectionService    The collection service.
	 * @param modelService         The model service.
	 * @param projectService       The project service.
	 * @param containerService     The container service.
	 * @since 1.8
	 */
	public ProjectFolioApiController(ConfigurationService configurationService, SecurityService securityService,
			CollectionService collectionService, ModelService modelService, ProjectService projectService,
			ContainerService containerService) {
		super(ProjectApiController.class, configurationService, securityService, collectionService, modelService,
				projectService);

		this.containerService = containerService;
	}

	/**
	 * Returns the folio of given project in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The folio id.
	 * @return The folio in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the folio of given project in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Folio", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = FolioResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(entityRequestMapping + projectPathVariable)
	public ResponseEntity<FolioResponse> entity(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the folio id") @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId);

		try {
			List<Folio> folios = authorization.project.getFolios(Set.of(id));

			return folios.isEmpty() ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
					: ResponseEntity.ok().body(new FolioResponse(folios.get(0)));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the list of folios of given project in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @return The list of folios of given project in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the list of folios of given project in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Folios", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = FolioResponse.class))) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(listRequestMapping + projectPathVariable)
	public ResponseEntity<List<FolioResponse>> list(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId) {
		Authorization authorization = authorizationFactory.authorize(projectId);

		try {
			final List<FolioResponse> folios = new ArrayList<>();
			for (Folio folio : authorization.project.getFolios())
				folios.add(new FolioResponse(folio));

			return ResponseEntity.ok().body(folios);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Sorts the folios.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param request   The folios sort request.
	 * @return The sorted folios in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "sort folios")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sorted Folios"),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(sortRequestMapping + projectPathVariable)
	public ResponseEntity<List<FolioResponse>> sort(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@RequestBody @Valid FolioSortRequest request) {
		Authorization authorization = authorizationFactory.authorize(projectId, ProjectRight.special);
		try {
			final List<FolioResponse> folios = new ArrayList<>();
			for (Folio folio : authorization.project.sortFolios(request.getIds(), request.isAfter()))
				folios.add(new FolioResponse(folio));

			return ResponseEntity.ok().body(folios);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates the folios.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param request   The folios update request.
	 * @return The folios in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the required folios and returns all folios in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Folios", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = FolioResponse.class))) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(updateRequestMapping + projectPathVariable)
	public ResponseEntity<List<FolioResponse>> update(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@RequestBody @Valid FolioUpdateRequest request) {
		Authorization authorization = authorizationFactory.authorize(projectId, ProjectRight.special);
		try {
			List<ImageUtils.Metadata> metadata = new ArrayList<>();
			for (FolioUpdateRequest.Metadata update : request.getMetadata())
				if (update != null)
					metadata.add(new ImageUtils.Metadata(update.getId(), update.getName(), update.getKeywords(),
							update.getPageXMLType()));

			final List<FolioResponse> folios = new ArrayList<>();
			for (Folio folio : authorization.project.updateFolios(metadata))
				folios.add(new FolioResponse(folio));

			return ResponseEntity.ok().body(folios);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the derivative with given id.
	 * 
	 * @param project  The project.
	 * @param folder   The image derivative folder.
	 * @param imageId  The image id.
	 * @param response The HTTP response.
	 * @throws ResponseStatusException Throws if the image does not exists with http
	 *                                 status not found (404).
	 * @since 1.8
	 */
	private void getDerivative(Project project, Path folder, String imageId, HttpServletResponse response)
			throws ResponseStatusException {
		getImage(folder, imageId, project.getConfiguration().getImages().getDerivatives().getFormat().name(), response);
	}

	/**
	 * Returns the thumbnail derivative with given id.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The image id.
	 * @param response  The HTTP-specific functionality in sending a response to the
	 *                  client.
	 * @since 1.8
	 */
	@Operation(summary = "returns the thumbnail derivative with given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Downloaded Thumbnail Derivative"),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(derivativeThumbnailRequestMapping + projectPathVariable)
	public void getDerivativeThumbnail(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the image id") @RequestParam String id, HttpServletResponse response) {
		Authorization authorization = authorizationFactory.authorize(projectId, ProjectRight.read);

		getDerivative(authorization.project,
				authorization.project.getConfiguration().getImages().getDerivatives().getThumbnail(), id, response);
	}

	/**
	 * Returns the detail derivative with given id.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The image id.
	 * @param response  The HTTP-specific functionality in sending a response to the
	 *                  client.
	 * @since 1.8
	 */
	@Operation(summary = "returns the detail derivative with given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Downloaded Detail Derivative"),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(derivativeDetailRequestMapping + projectPathVariable)
	public void getDerivativeDetail(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the image id") @RequestParam String id, HttpServletResponse response) {
		Authorization authorization = authorizationFactory.authorize(projectId, ProjectRight.read);

		getDerivative(authorization.project,
				authorization.project.getConfiguration().getImages().getDerivatives().getDetail(), id, response);
	}

	/**
	 * Returns the best derivative with given id.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The image id.
	 * @param response  The HTTP-specific functionality in sending a response to the
	 *                  client.
	 * @since 1.8
	 */
	@Operation(summary = "returns the best derivative with given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Downloaded Best Derivative"),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(derivativeBestRequestMapping + projectPathVariable)
	public void getDerivativeBest(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the image id") @RequestParam String id, HttpServletResponse response) {
		Authorization authorization = authorizationFactory.authorize(projectId, ProjectRight.read);

		getDerivative(authorization.project,
				authorization.project.getConfiguration().getImages().getDerivatives().getBest(), id, response);
	}

	/**
	 * Authorizes the session user for read security operations on container.
	 * 
	 * @param id The container id.
	 * @return The authorized container.
	 * @throws ResponseStatusException Throw with http status:
	 *                                 <ul>
	 *                                 <li>400 (Bad Request): if the container is
	 *                                 not available.</li>
	 *                                 <li>401 (Unauthorized): if the read security
	 *                                 permission is not achievable by the session
	 *                                 user.</li>
	 *                                 </ul>
	 * @since 1.8
	 */
	private ContainerService.Container authorizeContainerRead(String id) throws ResponseStatusException {
		ContainerService.Container container = containerService.getContainer(id);

		if (container == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		else if (!container.getRight().isReadFulfilled())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		else
			return container;
	}

	/**
	 * Import a folio from container.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The container id. This is the folder name.
	 * @param folio     The folio id to import.
	 * @return The imported folio in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "import a folio from a container and returns it")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Imported Folio", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = FolioResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(importRequestMapping + entityRequestMapping + projectPathVariable)
	public ResponseEntity<FolioResponse> importFolioEntity(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the container id - this is the folder name") @RequestParam String id,
			@Parameter(description = "the folio id") @RequestParam String folio) {
		Authorization authorization = authorizationFactory.authorize(projectId, ProjectRight.special);
		ContainerService.Container container = authorizeContainerRead(id);

		try {
			if (folio.isBlank())
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

			final List<Folio> folios = authorization.project.importFolios(container, Set.of(folio));

			return folios.isEmpty() ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
					: ResponseEntity.ok().body(new FolioResponse(folios.get(0)));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Import folios from container.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The container id. This is the folder name.
	 * @param request   The folios import request.
	 * @return The imported project folios in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "import folios from a container and returns them")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Imported Folios", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = FolioResponse.class))) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(importRequestMapping + listRequestMapping + projectPathVariable)
	public ResponseEntity<List<FolioResponse>> importFolioList(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the container id - this is the folder name") @RequestParam String id,
			@RequestBody @Valid IdentifiersRequest request) {
		Authorization authorization = authorizationFactory.authorize(projectId, ProjectRight.special);
		ContainerService.Container container = authorizeContainerRead(id);

		try {
			final List<FolioResponse> folios = new ArrayList<>();
			for (Folio folio : authorization.project.importFolios(container, request.getIds()))
				folios.add(new FolioResponse(folio));

			return ResponseEntity.ok().body(folios);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Import all folios from container.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The container id. This is the folder name.
	 * @return The list of project folios in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "import all folios from a container and returns thzem")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Imported Folios", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = FolioResponse.class))) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(importRequestMapping + allRequestMapping + projectPathVariable)
	public ResponseEntity<List<FolioResponse>> importFolioAll(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the container id - this is the folder name") @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId, ProjectRight.special);
		ContainerService.Container container = authorizeContainerRead(id);

		try {
			final List<FolioResponse> folios = new ArrayList<>();
			for (Folio folio : authorization.project.importFolios(container, null))
				folios.add(new FolioResponse(folio));

			return ResponseEntity.ok().body(folios);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

}
