/**
 * File:     DataSecurityApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     29.05.2024
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
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.DataService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.security.SecurityOwner;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Defines data security controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api & server")
@Tag(name = "data security", description = "the data security API")
@RestController
@RequestMapping(path = DataSecurityApiController.contextPath, produces = CoreApiController.applicationJson)
public class DataSecurityApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = DataApiController.contextPath + securityRequestMapping;

	/**
	 * The data service.
	 */
	private final DataService service;

	/**
	 * Creates a data security controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param service              The data service.
	 * @since 1.8
	 */
	public DataSecurityApiController(ConfigurationService configurationService, SecurityService securityService,
			DataService service) {
		super(DataSecurityApiController.class, configurationService, securityService);

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
	 * Returns the data security in the response body.
	 * 
	 * @return The data security in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the data security in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Data Security", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = DataSecurityResponse.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(informationRequestMapping)
	public ResponseEntity<DataSecurityResponse> information() {
		authorize();

		try {
			return ResponseEntity.ok().body(new DataSecurityResponse(service));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates the data security and returns it in the response body.
	 * 
	 * @param request The data security request.
	 * @return The updated data security in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the data security and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Updated Data Security", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = DataSecurityResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(updateRequestMapping)
	public ResponseEntity<DataSecurityResponse> update(@RequestBody DataSecurityRequest request) {
		authorize();

		if (request == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		else
			try {
				service.update(request);

				return ResponseEntity.ok().body(new DataSecurityResponse(service));
			} catch (Exception ex) {
				log(ex);

				throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
			}

	}

	/**
	 * Secures the data.
	 * 
	 * @return The data security in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "secures the data")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Secures Data", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = DataSecurityResponse.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(secureRequestMapping)
	public ResponseEntity<DataSecurityResponse> secure() {
		authorize();

		try {
			service.secure(true);

			return ResponseEntity.ok().body(new DataSecurityResponse(service));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Unsecures the data.
	 * 
	 * @return The data security in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "unsecures the data")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Unsecures Data", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = DataSecurityResponse.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(unsecureRequestMapping)
	public ResponseEntity<DataSecurityResponse> unsecure() {
		authorize();

		try {
			service.secure(false);

			return ResponseEntity.ok().body(new DataSecurityResponse(service));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines data security responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class DataSecurityResponse extends SecurityOwner {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The tracking.
		 */
		private TrackingResponse tracking;

		/**
		 * Creates a data security response for the api.
		 * 
		 * @param service The data service.
		 * @since 1.8
		 */
		public DataSecurityResponse(DataService service) {
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
	 * Defines data security requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class DataSecurityRequest extends SecurityOwner {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

	}

}
