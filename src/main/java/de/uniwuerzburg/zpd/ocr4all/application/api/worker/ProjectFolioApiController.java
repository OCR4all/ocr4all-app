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
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.FolioResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
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
	 * Creates a folio controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param projectService       The project service.
	 * @since 1.8
	 */
	public ProjectFolioApiController(ConfigurationService configurationService, SecurityService securityService,
			ProjectService projectService) {
		super(ProjectApiController.class, configurationService, securityService, projectService);
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
			List<Folio> folio = authorization.project.getFolios(Set.of(id));

			return folio.isEmpty() ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
					: ResponseEntity.ok().body(new FolioResponse(folio.get(0)));
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

}
