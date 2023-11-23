/**
 * File:     FolioApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     11.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Keyword;
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
import jakarta.validation.constraints.NotNull;

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
	 * The order request mapping.
	 */
	public static final String orderRequestMapping = "/order";

	/**
	 * The order download request mapping.
	 */
	public static final String orderDownloadRequestMapping = orderRequestMapping + downloadRequestMapping;

	/**
	 * The order upload request mapping.
	 */
	public static final String orderUploadRequestMapping = orderRequestMapping + uploadRequestMapping;

	/**
	 * The derivative request mapping.
	 */
	public static final String derivativeRequestMapping = "/derivative";

	/**
	 * The thumbnail image derivative request mapping.
	 */
	public static final String derivativeThumbnailRequestMapping = derivativeRequestMapping + "/thumbnail";

	/**
	 * The detail image derivative request mapping.
	 */
	public static final String derivativeDetailRequestMapping = derivativeRequestMapping + "/detail";

	/**
	 * The best image derivative request mapping.
	 */
	public static final String derivativeBestRequestMapping = derivativeRequestMapping + "/best";

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
	 * @param request   The folio list request. An empty list returns all folios.
	 * @return The list of folios of given project in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the list of folios of given project in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Folio", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = FolioResponse.class))) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(listRequestMapping + projectPathVariable)
	public ResponseEntity<List<FolioResponse>> list(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@RequestBody @Valid FolioListRequest request) {
		Authorization authorization = authorizationFactory.authorize(projectId);
		try {
			final List<FolioResponse> folios = new ArrayList<>();
			for (Folio folio : authorization.project
					.getFolios(request.getIdentifiers().isEmpty() ? null : request.getIdentifiers()))
				folios.add(new FolioResponse(folio));

			return ResponseEntity.ok().body(folios);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Downloads the folio order.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param response  The HTTP-specific functionality in sending a response to the
	 *                  client.
	 * @throws IOException Signals that an I/O exception of some sort has occurred.
	 * @since 1.8
	 */
	@Operation(summary = "downloads the folio order")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Downloaded Folio"),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(value = orderDownloadRequestMapping + projectPathVariable, produces = CoreApiController.applicationText)
	public void orderDownload(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			HttpServletResponse response) throws IOException {
		Authorization authorization = authorizationFactory.authorize(projectId, ProjectRight.special);
		try {
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=\"" + projectId + "-folios.txt\"");

			authorization.project.foliosOrder(response.getOutputStream());
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
		}
	}

	/**
	 * Updates folios order.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param file      The uploaded file received in a multipart request with the
	 *                  new order.
	 * @param response  The HTTP-specific functionality in sending a response to the
	 *                  client.
	 * @since 1.8
	 */
	@Operation(summary = "updates folios order")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request Succeeded Normally"),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(orderUploadRequestMapping + projectPathVariable)
	public void orderUpload(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@RequestParam MultipartFile file, HttpServletResponse response) {
		Authorization authorization = authorizationFactory.authorize(projectId, ProjectRight.special);
		try {
			List<String> order = new ArrayList<>();
			Set<String> addedFolioId = new HashSet<>();
			for (String line : (new String(file.getBytes(), StandardCharsets.UTF_8)).split("\\r?\\n"))
				try {
					String folioId = line.split("\t", 2)[0].trim();
					if (!folioId.isEmpty() && addedFolioId.add(folioId))
						order.add(folioId);
				} catch (Exception e) {
					// Ignore wrong line
				}

			if (order.isEmpty() || authorization.project.getFolios().isEmpty())
				return;

			List<Folio> folios = authorization.project.getFolios();

			Hashtable<String, Folio> idFolios = new Hashtable<String, Folio>();
			for (Folio folio : folios)
				idFolios.put(folio.getId(), folio);

			// Set the new order
			List<Folio> newOrder = new ArrayList<>();
			for (String id : order)
				if (idFolios.containsKey(id))
					newOrder.add(idFolios.get(id));

			/*
			 * Adds the remainder folios at the end of the new order list preserving the
			 * original order
			 */
			Set<String> orderSet = new HashSet<>(order);
			for (Folio folio : folios)
				if (!orderSet.contains(folio.getId()))
					newOrder.add(folio);

			if (authorization.project.persist(newOrder) < 0)
				throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
			else
				response.setStatus(HttpServletResponse.SC_OK);
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates the folios.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param request   The folios request.
	 * @return The updated folios in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the folios and returns then in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Updated Folios", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = FolioResponse.class))) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(updateRequestMapping + projectPathVariable)
	public ResponseEntity<List<FolioResponse>> update(@PathVariable String projectId,
			@RequestBody @Valid FolioRequest request) {
		Authorization authorization = authorizationFactory.authorize(projectId, ProjectRight.special);
		try {
			if (request.getIdentifiers().isEmpty())
				return ResponseEntity.status(HttpStatus.OK).build();

			List<Folio> folios = authorization.project.getFolios();
			if (folios.isEmpty())
				return ResponseEntity.status(HttpStatus.OK).build();

			Set<String> keywords = null;
			if (!FolioRequest.Action.pageXMLType_set.equals(request.getAction()))
				keywords = request.getKeywords() == null ? new HashSet<>()
						: Keyword.normalizeKeywords(request.getKeywords());

			Set<String> updated = new HashSet<>();

			for (Folio folio : folios)
				if (request.getIdentifiers().contains(folio.getId())) {
					updated.add(folio.getId());

					switch (request.getAction()) {
					case pageXMLType_set:
						folio.setPageXMLType(request.getPageXMLType());

						break;
					case keywords_add:
						if (!keywords.isEmpty()) {
							if (folio.getKeywords() == null)
								folio.setKeywords(keywords);
							else
								folio.getKeywords().addAll(keywords);
						}

						break;
					case keywords_set:
						folio.setKeywords(keywords);

						break;
					case keywords_remove:
						if (!keywords.isEmpty() && folio.getKeywords() != null)
							folio.getKeywords().removeAll(keywords);

						break;
					case keywords_remove_all:
						folio.setKeywords(null);

						break;
					}
				}

			if (authorization.project.persist(folios) < 0)
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
			else {
				final List<FolioResponse> updatedFolios = new ArrayList<>();
				for (Folio folio : authorization.project.getFolios(updated))
					updatedFolios.add(new FolioResponse(folio));

				return ResponseEntity.ok().body(updatedFolios);
			}
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
		Authorization authorization = authorizationFactory.authorize(projectId, ProjectRight.special);

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
		Authorization authorization = authorizationFactory.authorize(projectId, ProjectRight.special);

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
		Authorization authorization = authorizationFactory.authorize(projectId, ProjectRight.special);

		getDerivative(authorization.project,
				authorization.project.getConfiguration().getImages().getDerivatives().getBest(), id, response);
	}

	/**
	 * Defines folio list requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class FolioListRequest implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The identifiers.
		 */
		@NotNull
		private Set<String> identifiers;

		/**
		 * Returns the identifiers.
		 *
		 * @return The identifiers.
		 * @since 1.8
		 */
		public Set<String> getIdentifiers() {
			return identifiers;
		}

		/**
		 * Set the identifiers.
		 *
		 * @param identifiers The identifiers to set.
		 * @since 1.8
		 */
		public void setIdentifiers(Set<String> identifiers) {
			this.identifiers = identifiers;
		}

	}

	/**
	 * Defines folio responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class FolioResponse extends Folio {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a folio response for the api.
		 * 
		 * @param folio The folio.
		 * @since 1.8
		 */
		public FolioResponse(Folio folio) {
			super(folio.getDate(), folio.getUser(), folio.getKeywords(), folio.getId(), folio.getName(),
					folio.getFormat(), folio.getSize(), folio.getDerivatives());
		}

	}

	/**
	 * Defines folio requests for the api. It includes project identification, name,
	 * state, description and keywords.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class FolioRequest implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Defines folio actions.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public enum Action {
			pageXMLType_set, keywords_add, keywords_set, keywords_remove, keywords_remove_all
		}

		/**
		 * The action to perform.
		 */
		@NotNull
		private Action action;

		/**
		 * The PAGE XML type.
		 */
		private Folio.PageXMLType pageXMLType;

		/**
		 * The keywords.
		 */
		private Set<String> keywords;

		/**
		 * The image identifiers to perform the action
		 */
		@NotNull
		private Set<String> identifiers;

		/**
		 * Returns the action to perform.
		 *
		 * @return The action.
		 * @since 1.8
		 */
		public Action getAction() {
			return action;
		}

		/**
		 * Set the action to perform. Allowed actions are: type_set, keywords_add,
		 * keywords_set, keywords_remove, keywords_remove_all.
		 *
		 * @param action The action to set.
		 * @since 1.8
		 */
		public void setAction(Action action) {
			this.action = action;
		}

		/**
		 * Returns the PAGE XML type.
		 *
		 * @return The PAGE XML type.
		 * @since 1.8
		 */
		public Folio.PageXMLType getPageXMLType() {
			return pageXMLType;
		}

		/**
		 * Set the PAGE XML type.
		 *
		 * @param pageXMLType The PAGE XML type to set.
		 * @since 1.8
		 */
		public void setPageXMLType(Folio.PageXMLType pageXMLType) {
			this.pageXMLType = pageXMLType;
		}

		/**
		 * Returns the keywords.
		 *
		 * @return The keywords.
		 * @since 1.8
		 */
		public Set<String> getKeywords() {
			return keywords;
		}

		/**
		 * Set the keywords.
		 *
		 * @param keywords The keywords to set.
		 * @since 1.8
		 */
		public void setKeywords(Set<String> keywords) {
			this.keywords = keywords;
		}

		/**
		 * Returns the image identifiers to perform the action.
		 *
		 * @return The identifiers.
		 * @since 1.8
		 */
		public Set<String> getIdentifiers() {
			return identifiers;
		}

		/**
		 * Set the image identifiers to perform the action.
		 *
		 * @param images The identifiers to set.
		 * @since 1.8
		 */
		public void setIdentifiers(Set<String> images) {
			this.identifiers = images;
		}

	}

}
