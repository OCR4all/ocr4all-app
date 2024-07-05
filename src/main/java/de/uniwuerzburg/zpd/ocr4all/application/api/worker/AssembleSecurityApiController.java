/**
 * File:     AssembleSecurityApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     05.07.2024
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
import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.AssembleService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.security.SecurityOwner;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Defines assemble security controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@Profile("api & server")
@Tag(name = "assemble security", description = "the assemble security API")
@RestController
@RequestMapping(path = AssembleSecurityApiController.contextPath, produces = CoreApiController.applicationJson)
public class AssembleSecurityApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = AssembleApiController.contextPath + securityRequestMapping;

	/**
	 * The assemble service.
	 */
	private final AssembleService service;

	/**
	 * Creates an assemble security controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param collectionService    The collection service.
	 * @param modelService         The model service.
	 * @param service              The assemble service.
	 * @since 17
	 */
	public AssembleSecurityApiController(ConfigurationService configurationService, SecurityService securityService,
			CollectionService collectionService, ModelService modelService, AssembleService service) {
		super(AssembleSecurityApiController.class, configurationService, securityService, collectionService,
				modelService);

		this.service = service;
	}

	/**
	 * Authorizes the session user for administrator security operations.
	 * 
	 * @throws ResponseStatusException Throw with http status 401 (Unauthorized) if
	 *                                 the administrator security permission is not
	 *                                 achievable by the session user.
	 * @since 17
	 */
	private void authorize() throws ResponseStatusException {
		if (!service.isAdministrator())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Returns the assemble security in the response body.
	 * 
	 * @return The assemble security in the response body.
	 * @since 17
	 */
	@Operation(summary = "returns the assemble security in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Assemble Security", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = AssembleSecurityResponse.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(informationRequestMapping)
	public ResponseEntity<AssembleSecurityResponse> information() {
		authorize();

		try {
			return ResponseEntity.ok().body(new AssembleSecurityResponse(service));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates the assemble security and returns it in the response body.
	 * 
	 * @param request The assemble security request.
	 * @return The updated assemble security in the response body.
	 * @since 17
	 */
	@Operation(summary = "updates the assemble security and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Updated Assemble Security", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = AssembleSecurityResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(updateRequestMapping)
	public ResponseEntity<AssembleSecurityResponse> update(@RequestBody AssembleSecurityRequest request) {
		authorize();

		if (request == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		else
			try {
				service.update(request);

				return ResponseEntity.ok().body(new AssembleSecurityResponse(service));
			} catch (Exception ex) {
				log(ex);

				throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
			}

	}

	/**
	 * Secures the assemble.
	 * 
	 * @return The assemble security in the response body.
	 * @since 17
	 */
	@Operation(summary = "secures the assemble")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Secures Assemble", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = AssembleSecurityResponse.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(secureRequestMapping)
	public ResponseEntity<AssembleSecurityResponse> secure() {
		authorize();

		try {
			service.secure(true);

			return ResponseEntity.ok().body(new AssembleSecurityResponse(service));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Unsecures the assemble.
	 * 
	 * @return The assemble security in the response body.
	 * @since 17
	 */
	@Operation(summary = "unsecures the assemble")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Unsecures Assemble", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = AssembleSecurityResponse.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(unsecureRequestMapping)
	public ResponseEntity<AssembleSecurityResponse> unsecure() {
		authorize();

		try {
			service.secure(false);

			return ResponseEntity.ok().body(new AssembleSecurityResponse(service));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines assemble security responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class AssembleSecurityResponse extends SecurityOwner {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The tracking.
		 */
		private TrackingResponse tracking;

		/**
		 * Creates a assemble security response for the api.
		 * 
		 * @param service The assemble service.
		 * @since 17
		 */
		public AssembleSecurityResponse(AssembleService service) {
			super(service.getSecurity().isSecured(), service.getSecurity().getUsers(),
					service.getSecurity().getGroups());

			tracking = new TrackingResponse(service.getConfiguration());
		}

		/**
		 * Returns the tracking.
		 *
		 * @return The tracking.
		 * @since 17
		 */
		public TrackingResponse getTracking() {
			return tracking;
		}

		/**
		 * Set the tracking.
		 *
		 * @param tracking The tracking to set.
		 * @since 17
		 */
		public void setTracking(TrackingResponse tracking) {
			this.tracking = tracking;
		}

	}

	/**
	 * Defines assemble security requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class AssembleSecurityRequest extends SecurityOwner {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

	}

}
