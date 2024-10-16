/**
 * File:     RepositorySecurityApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     23.11.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.TrackingResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.ModelService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.repository.RepositoryService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.security.SecurityOwner;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Defines repository security controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api & server")
@Tag(name = "repository security", description = "the repository security API")
@RestController
@RequestMapping(path = RepositorySecurityApiController.contextPath, produces = CoreApiController.applicationJson)
public class RepositorySecurityApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = RepositoryApiController.contextPath + securityRequestMapping;

	/**
	 * The repository service.
	 */
	private final RepositoryService service;

	/**
	 * Creates a repository security controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param collectionService    The collection service.
	 * @param modelService         The model service.
	 * @param service              The repository service.
	 * @since 1.8
	 */
	public RepositorySecurityApiController(ConfigurationService configurationService, SecurityService securityService,
			CollectionService collectionService, ModelService modelService, RepositoryService service) {
		super(RepositorySecurityApiController.class, configurationService, securityService, collectionService,
				modelService);

		this.service = service;
	}

	/**
	 * Authorizes the session user for administrator security operations.
	 * 
	 * @throws ResponseStatusException Throw with http status 401 (Unauthorized) if
	 *                                 the administrator security permission is not
	 *                                 achievable by the session user.
	 * @since 1.8
	 */
	private void authorize() throws ResponseStatusException {
		if (!service.isAdministrator())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Returns the repository security in the response body.
	 * 
	 * @return The repository security in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the repository security in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Repository Security", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = RepositorySecurityResponse.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(informationRequestMapping)
	public ResponseEntity<RepositorySecurityResponse> information() {
		authorize();

		try {
			return ResponseEntity.ok().body(new RepositorySecurityResponse(service));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates the repository security and returns it in the response body.
	 * 
	 * @param request The repository security request.
	 * @return The updated repository security in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the repository security and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Updated Repository Security", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = RepositorySecurityResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(updateRequestMapping)
	public ResponseEntity<RepositorySecurityResponse> update(@RequestBody RepositorySecurityRequest request) {
		authorize();

		if (request == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		else
			try {
				service.update(request);

				return ResponseEntity.ok().body(new RepositorySecurityResponse(service));
			} catch (Exception ex) {
				log(ex);

				throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
			}

	}

	/**
	 * Secures the repository.
	 * 
	 * @return The repository security in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "secures the repository")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Secures Repository", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = RepositorySecurityResponse.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(secureRequestMapping)
	public ResponseEntity<RepositorySecurityResponse> secure() {
		authorize();

		try {
			service.secure(true);

			return ResponseEntity.ok().body(new RepositorySecurityResponse(service));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Unsecures the repository.
	 * 
	 * @return The repository security in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "unsecures the repository")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Unsecures Repository", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = RepositorySecurityResponse.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(unsecureRequestMapping)
	public ResponseEntity<RepositorySecurityResponse> unsecure() {
		authorize();

		try {
			service.secure(false);

			return ResponseEntity.ok().body(new RepositorySecurityResponse(service));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines repository security responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class RepositorySecurityResponse extends SecurityOwner {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The tracking.
		 */
		private TrackingResponse tracking;

		/**
		 * Creates a repository security response for the api.
		 * 
		 * @param service The repository service.
		 * @since 1.8
		 */
		public RepositorySecurityResponse(RepositoryService service) {
			super(service.getSecurity().isSecured(), service.getSecurity().getUsers(),
					service.getSecurity().getGroups());

			tracking = new TrackingResponse(service.getConfiguration());
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
	 * Defines repository security requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class RepositorySecurityRequest extends SecurityOwner {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

	}

}
