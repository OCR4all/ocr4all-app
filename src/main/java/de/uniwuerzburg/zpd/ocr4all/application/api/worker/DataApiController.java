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

import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.ModelService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
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
 * @since 17
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
	 * @param collectionService    The collection service.
	 * @param modelService         The model service.
	 * @param service              The data service.
	 * @since 17
	 */
	public DataApiController(ConfigurationService configurationService, SecurityService securityService,
			CollectionService collectionService, ModelService modelService, DataService service) {
		super(DataApiController.class, configurationService, securityService, collectionService, modelService);

		this.service = service;
	}

	/**
	 * Returns the data overview in the response body.
	 * 
	 * @return The data overview in the response body.
	 * @since 17
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
	 * @since 17
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
		 * True if a collection can be created.
		 */
		@JsonProperty("create-collection")
		private boolean isCreateCollection;

		/**
		 * Creates a data responses for the api.
		 * 
		 * @param isAdministrator    True if data administrator security permission is
		 *                           achievable by the session user.
		 * @param isCreateCollection True if a collection can be created.
		 * @since 17
		 */
		public DataResponse(boolean isAdministrator, boolean isCreateCollection) {
			super();

			this.isAdministrator = isAdministrator;
			this.isCreateCollection = isCreateCollection;
		}

		/**
		 * Returns true if data administrator security permission is achievable by the
		 * session user.
		 *
		 * @return True if data administrator security permission is achievable by the
		 *         session user.
		 * @since 17
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
		 * @since 17
		 */
		public void setAdministrator(boolean isAdministrator) {
			this.isAdministrator = isAdministrator;
		}

		/**
		 * Returns true if a collection can be created.
		 *
		 * @return True if a collection can be created.
		 * @since 17
		 */
		@JsonGetter("create-collection")
		public boolean isCreateCollection() {
			return isCreateCollection;
		}

		/**
		 * Set to true if a collection can be created.
		 *
		 * @param isCreateCollection The collection create flag to set.
		 * @since 17
		 */
		public void setCreateCollection(boolean isCreateCollection) {
			this.isCreateCollection = isCreateCollection;
		}

	}

}
