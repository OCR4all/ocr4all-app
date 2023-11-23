/**
 * File:     RepositorySecurityApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     23.11.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.Serializable;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.ProjectSecurity;
import de.uniwuerzburg.zpd.ocr4all.application.api.worker.CoreApiController.Authorization;
import de.uniwuerzburg.zpd.ocr4all.application.api.worker.CoreApiController.ProjectRight;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.repository.RepositoryService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
	 * @param service              The repository service.
	 * @since 1.8
	 */
	public RepositorySecurityApiController(ConfigurationService configurationService, SecurityService securityService,
			RepositoryService service) {
		super(RepositorySecurityApiController.class, configurationService, securityService);

		this.service = service;
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
		if (service.isAdministrator())
			try {
				return ResponseEntity.ok().body(new RepositorySecurityResponse(service.getSecurity()));
			} catch (Exception ex) {
				log(ex);

				throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
			}
		else
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Defines repository security responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class RepositorySecurityResponse
			extends de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Repository.Security {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a sandbox responses for the api.
		 * 
		 * @param files The files.
		 * @since 1.8
		 */
		public RepositorySecurityResponse(
				de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Repository.Security security) {
			super(security.isSecured(), security.getUsers(), security.getGroups());
		}

	}

}
