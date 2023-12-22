/**
 * File:     RepositoryApiController.java
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * 
 * Author:   Herbert Baier
 * Date:     23.11.2023
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
import de.uniwuerzburg.zpd.ocr4all.application.core.repository.RepositoryService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Defines repository controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Tag(name = "repository", description = "the repository API")
@RestController
@RequestMapping(path = RepositoryApiController.contextPath, produces = CoreApiController.applicationJson)
public class RepositoryApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = apiContextPathVersion_1_0 + "/repository";

	/**
	 * The repository service.
	 */
	private final RepositoryService service;

	/**
	 * Creates a repository controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param service              The repository service.
	 * @since 1.8
	 */
	public RepositoryApiController(ConfigurationService configurationService, SecurityService securityService,
			RepositoryService service) {
		super(RepositoryApiController.class, configurationService, securityService);

		this.service = service;
	}

	/**
	 * Returns the repository overview in the response body.
	 * 
	 * @return The repository overview in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the repository overview in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Repository Overview", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = RepositoryResponse.class))) }),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(overviewRequestMapping)
	public ResponseEntity<RepositoryResponse> overview() {
		try {
			return ResponseEntity.ok()
					.body(new RepositoryResponse(service.isAdministrator(), service.isCreateContainer()));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines repository responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class RepositoryResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * True if repository administrator security permission is achievable by the
		 * session user.
		 */
		@JsonProperty("administrator")
		private boolean isAdministrator;

		/**
		 * True if a container can be created.
		 */
		@JsonProperty("create-container")
		private boolean isCreateContainer;

		/**
		 * Creates a repository responses for the api.
		 * 
		 * @param isAdministrator   True if repository administrator security permission
		 *                          is achievable by the session user.
		 * @param isCreateContainer True if a container can be created.
		 * @since 1.8
		 */
		public RepositoryResponse(boolean isAdministrator, boolean isCreateContainer) {
			super();

			this.isAdministrator = isAdministrator;
			this.isCreateContainer = isCreateContainer;
		}

		/**
		 * Returns true if repository administrator security permission is achievable by
		 * the session user.
		 *
		 * @return True if repository administrator security permission is achievable by
		 *         the session user.
		 * @since 1.8
		 */
		@JsonGetter("administrator")
		public boolean isAdministrator() {
			return isAdministrator;
		}

		/**
		 * Set to true if repository administrator security permission is achievable by
		 * the session user.
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
