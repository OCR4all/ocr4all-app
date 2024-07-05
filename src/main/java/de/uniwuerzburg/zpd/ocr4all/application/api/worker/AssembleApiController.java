/**
 * File:     AssembleApiController.java
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * 
 * Author:   Herbert Baier
 * Date:     05.07.2024
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

import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.AssembleService;
import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.ModelService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Defines assemble controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@Profile("api")
@Tag(name = "assemble", description = "the assemble API")
@RestController
@RequestMapping(path = AssembleApiController.contextPath, produces = CoreApiController.applicationJson)
public class AssembleApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = apiContextPathVersion_1_0 + "/assemble";

	/**
	 * The assemble service.
	 */
	private final AssembleService service;

	/**
	 * Creates an assemble controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param collectionService    The collection service.
	 * @param modelService         The model service.
	 * @param service              The assemble service.
	 * @since 17
	 */
	public AssembleApiController(ConfigurationService configurationService, SecurityService securityService,
			CollectionService collectionService, ModelService modelService, AssembleService service) {
		super(AssembleApiController.class, configurationService, securityService, collectionService, modelService);

		this.service = service;
	}

	/**
	 * Returns the assemble overview in the response body.
	 * 
	 * @return The assemble overview in the response body.
	 * @since 17
	 */
	@Operation(summary = "returns the assemble overview in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Assemble Overview", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = AssembleResponse.class))) }),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(overviewRequestMapping)
	public ResponseEntity<AssembleResponse> overview() {
		try {
			return ResponseEntity.ok().body(new AssembleResponse(service.isAdministrator(), service.isCreateModel()));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines assemble responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class AssembleResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * True if assemble administrator security permission is achievable by the
		 * session user.
		 */
		@JsonProperty("administrator")
		private boolean isAdministrator;

		/**
		 * True if a model can be created.
		 */
		@JsonProperty("create-model")
		private boolean isCreateModel;

		/**
		 * Creates an assemble responses for the api.
		 * 
		 * @param isAdministrator True if assemble administrator security permission is
		 *                        achievable by the session user.
		 * @param isCreateModel   True if a model can be created.
		 * @since 17
		 */
		public AssembleResponse(boolean isAdministrator, boolean isCreateModel) {
			super();

			this.isAdministrator = isAdministrator;
			this.isCreateModel = isCreateModel;
		}

		/**
		 * Returns true if assemble administrator security permission is achievable by
		 * the session user.
		 *
		 * @return True if assemble administrator security permission is achievable by
		 *         the session user.
		 * @since 17
		 */
		@JsonGetter("administrator")
		public boolean isAdministrator() {
			return isAdministrator;
		}

		/**
		 * Set to true if assemble administrator security permission is achievable by
		 * the session user.
		 *
		 * @param isAdministrator The administrator flag to set.
		 * @since 17
		 */
		public void setAdministrator(boolean isAdministrator) {
			this.isAdministrator = isAdministrator;
		}

		/**
		 * Returns true if a model can be created.
		 *
		 * @return True if a model can be created.
		 * @since 17
		 */
		@JsonGetter("create-model")
		public boolean isCreateModel() {
			return isCreateModel;
		}

		/**
		 * Set to true if a model can be created.
		 *
		 * @param isCreateModel The model create flag to set.
		 * @since 17
		 */
		public void setCreateModel(boolean isCreateModel) {
			this.isCreateModel = isCreateModel;
		}

	}

}
