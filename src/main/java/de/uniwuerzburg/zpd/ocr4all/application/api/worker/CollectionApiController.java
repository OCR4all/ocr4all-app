/**
 * File:     CollectionApiController.java
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 *
 * Author:   Herbert Baier
 * Date:     29.05.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.TrackingResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.data.CollectionConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.security.SecurityGrant;
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
 * Defines collection data controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Tag(name = "collection", description = "the data collection API")
@RestController
@RequestMapping(path = CollectionApiController.contextPath, produces = CoreApiController.applicationJson)
public class CollectionApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = DataApiController.contextPath + "/collection";

	/**
	 * The collection service.
	 */
	private final CollectionService service;

	/**
	 * Creates a collection data controller for the api.
	 *
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param service              The collection service.
	 * @since 1.8
	 */
	public CollectionApiController(ConfigurationService configurationService, SecurityService securityService,
			CollectionService service) {
		super(CollectionApiController.class, configurationService, securityService);

		this.service = service;
	}

	/**
	 * Returns the collection in the response body.
	 *
	 * @param id The collection id.
	 * @return The collection in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the collection in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Collection", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = CollectionRightResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(entityRequestMapping)
	public ResponseEntity<CollectionRightResponse> entity(
			@Parameter(description = "the collection id - this is the folder name") @RequestParam String id) {
		try {
			CollectionService.Collection collection = service.getCollection(id);

			return collection == null ? ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
					: ResponseEntity.ok().body(new CollectionRightResponse(collection));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the list of collections sorted by name with rights in the response
	 * body.
	 *
	 * @return The list of collections sorted by name with rights in the response
	 *         body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the list of collections sorted by name in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Collections", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = CollectionRightResponse.class))) }),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(listRequestMapping)
	public ResponseEntity<List<CollectionRightResponse>> list() {
		try {
			List<CollectionRightResponse> collections = new ArrayList<>();
			for (CollectionService.Collection collection : service.getCollections())
				collections.add(new CollectionRightResponse(collection));

			return ResponseEntity.ok().body(collections);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Creates the collection and returns it in the response body.
	 *
	 * @param request The collection request.
	 * @return The collection in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "creates the the collection and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Created Collection", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = CollectionResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(createRequestMapping)
	public ResponseEntity<CollectionResponse> create(@RequestBody @Valid CollectionRequest request) {
		if (!service.isCreate())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		else
			try {
				CollectionService.Collection collection = service.create(request.getName(), request.getDescription(),
						request.getKeywords());

				return collection == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
						: ResponseEntity.ok().body(new CollectionResponse(collection));
			} catch (Exception ex) {
				log(ex);

				throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
			}
	}

	/**
	 * Authorizes the session user for special security operations.
	 *
	 * @param id The collection id.
	 * @return The authorized collection.
	 * @throws ResponseStatusException Throw with http status:
	 *                                 <ul>
	 *                                 <li>400 (Bad Request): if the collection is
	 *                                 not available.</li>
	 *                                 <li>401 (Unauthorized): if the special
	 *                                 security permission is not achievable by the
	 *                                 session user.</li>
	 *                                 </ul>
	 * @since 1.8
	 */
	private CollectionService.Collection authorizeSpecial(String id) throws ResponseStatusException {
		CollectionService.Collection collection = service.getCollection(id);

		if (collection == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		else if (!collection.getRight().isSpecialFulfilled())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		else
			return collection;
	}

	/**
	 * Removes the collection.
	 *
	 * @param id       The collection id.
	 * @param response The HTTP-specific functionality in sending a response to the
	 *                 client.
	 * @since 1.8
	 */
	@Operation(summary = "removes the collection")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Removed collection"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(removeRequestMapping)
	public void remove(@Parameter(description = "the collection id - this is the folder name") @RequestParam String id,
			HttpServletResponse response) {
		authorizeSpecial(id);

		try {
			if (service.remove(id))
				response.setStatus(HttpServletResponse.SC_OK);
			else
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates the collection information and returns it in the response body.
	 *
	 * @param id      The collection id.
	 * @param request The collection request.
	 * @return The collection in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the collection information and returns it in the response body")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Updated Collection Information", content = {
					@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = CollectionResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(updateRequestMapping)
	public ResponseEntity<CollectionResponse> update(
			@Parameter(description = "the collection id - this is the folder name") @RequestParam String id,
			@RequestBody @Valid CollectionRequest request) {
		authorizeSpecial(id);

		try {
			CollectionService.Collection collection = service.update(id, request.getName(), request.getDescription(),
					request.getKeywords());

			return collection == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
					: ResponseEntity.ok().body(new CollectionResponse(collection));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the collection security in the response body.
	 *
	 * @param id The collection id.
	 * @return The collection security in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the collection security in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Collection Security", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = CollectionSecurityResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content) })
	@GetMapping(securityRequestMapping)
	public ResponseEntity<CollectionSecurityResponse> security(
			@Parameter(description = "the collection id - this is the folder name") @RequestParam String id) {
		return ResponseEntity.ok().body(new CollectionSecurityResponse(authorizeSpecial(id)));
	}

	/**
	 * Updates the collection security and returns it in the response body.
	 *
	 * @param id      The collection id.
	 * @param request The collection security request.
	 * @return The collection security in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the collection security and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Updated Collection Security", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = CollectionSecurityResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(securityRequestMapping + updateRequestMapping)
	public ResponseEntity<CollectionSecurityResponse> updateSecurity(
			@Parameter(description = "the collection id - this is the folder name") @RequestParam String id,
			@RequestBody CollectionSecurityRequest request) {
		authorizeSpecial(id);

		try {
			CollectionService.Collection collection = service.update(id, request);

			return collection == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
					: ResponseEntity.ok().body(new CollectionSecurityResponse(collection));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines collection requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class CollectionRequest implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The name.
		 */
		@NotBlank
		private String name = null;

		/**
		 * The description.
		 */
		private String description = null;

		/**
		 * The keywords.
		 */
		private Set<String> keywords = null;

		/**
		 * Returns the name.
		 *
		 * @return The name.
		 * @since 1.8
		 */
		public String getName() {
			return name;
		}

		/**
		 * Set the name.
		 *
		 * @param name The name to set.
		 * @since 1.8
		 */
		public void setName(String name) {
			this.name = name;
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

	}

	/**
	 * Defines collection security requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class CollectionSecurityRequest extends SecurityGrant {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

	}

	/**
	 * Defines collection responses for the api without security.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class CollectionResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The id.
		 */
		private String id = null;

		/**
		 * The name.
		 */
		private String name = null;

		/**
		 * The description.
		 */
		private String description = null;

		/**
		 * The keywords.
		 */
		private Set<String> keywords = null;

		/**
		 * The tracking.
		 */
		private TrackingResponse tracking;

		/**
		 * Creates a collection response for the api without security.
		 *
		 * @param collection The collection configuration.
		 * @since 1.8
		 */
		public CollectionResponse(CollectionService.Collection collection) {

			id = collection.getConfiguration().getFolder().getFileName().toString();

			final CollectionConfiguration.Configuration.Information information = collection.getConfiguration()
					.getConfiguration().getInformation();

			name = information.getName();
			description = information.getDescription();
			keywords = information.getKeywords();

			tracking = new TrackingResponse(collection.getConfiguration().getConfiguration());
		}

		/**
		 * Returns the id.
		 *
		 * @return The id.
		 * @since 1.8
		 */
		public String getId() {
			return id;
		}

		/**
		 * Set the id.
		 *
		 * @param id The id to set.
		 * @since 1.8
		 */
		public void setId(String id) {
			this.id = id;
		}

		/**
		 * Returns the name.
		 *
		 * @return The name.
		 * @since 1.8
		 */
		public String getName() {
			return name;
		}

		/**
		 * Set the name.
		 *
		 * @param name The name to set.
		 * @since 1.8
		 */
		public void setName(String name) {
			this.name = name;
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
		 * Returns the tracking.
		 *
		 * @return The tracking.
		 * @since 1.8
		 */
		public TrackingResponse getTracking() {
			return tracking;
		}

		/**
		 * Set the tracking.
		 *
		 * @param tracking The tracking to set.
		 * @since 1.8
		 */
		public void setTracking(TrackingResponse tracking) {
			this.tracking = tracking;
		}

	}

	/**
	 * Defines collection with right responses for the api without security.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class CollectionRightResponse extends CollectionResponse {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The right.
		 */
		private SecurityGrant.Right right;

		/**
		 * Creates a collection with right response for the api without security.
		 *
		 * @param collection The collection.
		 * @since 1.8
		 */
		public CollectionRightResponse(CollectionService.Collection collection) {
			super(collection);

			right = collection.getRight();
		}

		/**
		 * Returns the right.
		 *
		 * @return The right.
		 * @since 1.8
		 */
		public SecurityGrant.Right getRight() {
			return right;
		}

		/**
		 * Set the right.
		 *
		 * @param right The right to set.
		 * @since 1.8
		 */
		public void setRight(SecurityGrant.Right right) {
			this.right = right;
		}

	}

	/**
	 * Defines collection with security responses for the api without security.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class CollectionSecurityResponse extends CollectionResponse {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The security.
		 */
		private SecurityGrant security;

		/**
		 * Creates a collection with right response for the api without security.
		 *
		 * @param collection The collection.
		 * @since 1.8
		 */
		public CollectionSecurityResponse(CollectionService.Collection collection) {
			super(collection);

			security = collection.getConfiguration().getConfiguration().getSecurity();
		}

		/**
		 * Returns the security.
		 *
		 * @return The security.
		 * @since 1.8
		 */
		public SecurityGrant getSecurity() {
			return security;
		}

		/**
		 * Set the security.
		 *
		 * @param security The security to set.
		 * @since 1.8
		 */
		public void setSecurity(SecurityGrant security) {
			this.security = security;
		}

	}
}
