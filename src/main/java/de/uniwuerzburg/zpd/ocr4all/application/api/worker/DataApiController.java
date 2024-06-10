/**
 * File:     DataApiController.java
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * 
 * Author:   Herbert Baier
 * Date:     29.05.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.Serializable;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.DataService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Defines data controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Tag(name = "data", description = "the data API")
@RestController
@RequestMapping(path = DataApiController.contextPath, produces = CoreApiController.applicationJson)
public class DataApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = apiContextPathVersion_1_0 + "/data";

	/**
	 * The data service.
	 */
	private final DataService service;

	/**
	 * Creates a data controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param service              The data service.
	 * @since 1.8
	 */
	public DataApiController(ConfigurationService configurationService, SecurityService securityService,
			DataService service) {
		super(DataApiController.class, configurationService, securityService);

		this.service = service;
	}

	/**
	 * Returns the data overview in the response body.
	 * 
	 * @return The data overview in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the data overview in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Data Overview", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = DataResponse.class))) }),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(overviewRequestMapping)
	public ResponseEntity<DataResponse> overview() {
		try {
			return ResponseEntity.ok().body(new DataResponse(service.isAdministrator(), service.isCreateCollection()));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines data responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class DataResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * True if data administrator security permission is achievable by the session
		 * user.
		 */
		@JsonProperty("administrator")
		private boolean isAdministrator;

		/**
		 * True if a container can be created.
		 */
		@JsonProperty("create-container")
		private boolean isCreateContainer;

		/**
		 * Creates a data responses for the api.
		 * 
		 * @param isAdministrator   True if data administrator security permission is
		 *                          achievable by the session user.
		 * @param isCreateContainer True if a container can be created.
		 * @since 1.8
		 */
		public DataResponse(boolean isAdministrator, boolean isCreateContainer) {
			super();

			this.isAdministrator = isAdministrator;
			this.isCreateContainer = isCreateContainer;
		}

		/**
		 * Returns true if data administrator security permission is achievable by the
		 * session user.
		 *
		 * @return True if data administrator security permission is achievable by the
		 *         session user.
		 * @since 1.8
		 */
		@JsonGetter("administrator")
		public boolean isAdministrator() {
			return isAdministrator;
		}

		/**
		 * Set to true if data administrator security permission is achievable by the
		 * session user.
		 *
		 * @param isAdministrator The administrator flag to set.
		 * @since 1.8
		 */
		public void setAdministrator(boolean isAdministrator) {
			this.isAdministrator = isAdministrator;
		}

		/**
		 * Returns true if a container can be created.
		 *
		 * @return True if a container can be created.
		 * @since 1.8
		 */
		@JsonGetter("create-container")
		public boolean isCreateContainer() {
			return isCreateContainer;
		}

		/**
		 * Set to true if a container can be created.
		 *
		 * @param isCreateContainer The container create flag to set.
		 * @since 1.8
		 */
		public void setCreateContainer(boolean isCreateContainer) {
			this.isCreateContainer = isCreateContainer;
		}

	}

}
