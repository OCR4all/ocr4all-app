/**
 * File:     ContainerApiController.java
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * 
 * Author:   Herbert Baier
 * Date:     24.11.2023
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.TrackingResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.repository.ContainerConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.repository.ContainerService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
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
 * Defines container repository controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Tag(name = "container", description = "the repository container API")
@RestController
@RequestMapping(path = ContainerApiController.contextPath, produces = CoreApiController.applicationJson)
public class ContainerApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = RepositoryApiController.contextPath + "/container";

	/**
	 * The container service.
	 */
	private final ContainerService service;

	/**
	 * Creates a container repository controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param service              The container service.
	 * @since 1.8
	 */
	public ContainerApiController(ConfigurationService configurationService, SecurityService securityService,
			ContainerService service) {
		super(ContainerApiController.class, configurationService, securityService);

		this.service = service;
	}

	/**
	 * Returns the container in the response body.
	 * 
	 * @param id The container id.
	 * @return The container in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the container in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Container", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = ContainerRightResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(entityRequestMapping + idPathVariable)
	public ResponseEntity<ContainerRightResponse> entity(
			@Parameter(description = "the container id") @PathVariable String id) {
		try {
			ContainerService.Container container = service.getContainer(id);

			return container == null ? ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
					: ResponseEntity.ok().body(new ContainerRightResponse(container));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the list of containers sorted by name with rights in the response
	 * body.
	 * 
	 * @return The list of containers sorted by name with rights in the response
	 *         body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the list of containers sorted by name in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Containers", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = ContainerRightResponse.class))) }),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(listRequestMapping)
	public ResponseEntity<List<ContainerRightResponse>> list() {
		try {
			List<ContainerRightResponse> containers = new ArrayList<>();
			for (ContainerService.Container container : service.getContainers())
				containers.add(new ContainerRightResponse(container));

			return ResponseEntity.ok().body(containers);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Creates the container and returns it in the response body.
	 * 
	 * @param request The container request.
	 * @return The container in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "creates the the container and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Created Container", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = ContainerResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(createRequestMapping)
	public ResponseEntity<ContainerResponse> create(@RequestBody @Valid ContainerRequest request) {
		if (!service.isCreate())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		else if (request == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		else
			try {
				ContainerConfiguration configuration = service.create(request.getName(), request.getDescription(),
						request.getKeywords());

				return configuration == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
						: ResponseEntity.ok().body(new ContainerResponse(configuration));
			} catch (Exception ex) {
				log(ex);

				throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
			}
	}

	/**
	 * Authorizes the session user for special security operations.
	 * 
	 * @param id The container id.
	 * @return The authorized container.
	 * @throws ResponseStatusException Throw with http status:
	 *                                 <ul>
	 *                                 <li>400 (Bad Request): if the container is
	 *                                 not available.</li>
	 *                                 <li>401 (Unauthorized): if the administrator
	 *                                 security permission is not achievable by the
	 *                                 session user.</li>
	 *                                 </ul>
	 * @since 1.8
	 */
	private ContainerService.Container authorize(String id) throws ResponseStatusException {
		ContainerService.Container container = service.getContainer(id);

		if (container == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		else if (!container.getRight().isSpecialFulfilled())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		else
			return container;
	}

	/**
	 * Removes the container.
	 * 
	 * @param id       The container id.
	 * @param response The HTTP-specific functionality in sending a response to the
	 *                 client.
	 * @since 1.8
	 */
	@Operation(summary = "removes the container")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Removed container"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(removeRequestMapping + idPathVariable)
	public void remove(@Parameter(description = "the container id") @PathVariable String id,
			HttpServletResponse response) {
		authorize(id);

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
	 * Updates the container information and returns it in the response body.
	 * 
	 * @param id      The container id.
	 * @param request The container request.
	 * @return The container in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the container information and returns it in the response body")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Updated Container Information", content = {
					@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = ContainerResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(updateRequestMapping + idPathVariable)
	public ResponseEntity<ContainerResponse> update(
			@Parameter(description = "the container id") @PathVariable String id,
			@RequestBody @Valid ContainerRequest request) {
		authorize(id);

		try {
			ContainerConfiguration configuration = service.update(id, request.getName(), request.getDescription(),
					request.getKeywords());

			return configuration == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
					: ResponseEntity.ok().body(new ContainerResponse(configuration));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the container security in the response body.
	 * 
	 * @param id      The container id.
	 * @param request The container security request.
	 * @return The container security in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the container security in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Container Security", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = ContainerSecurityResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content) })
	@GetMapping(securityRequestMapping + entityRequestMapping + idPathVariable)
	public ResponseEntity<ContainerSecurityResponse> security(
			@Parameter(description = "the container id") @PathVariable String id,
			@RequestBody ContainerSecurityRequest request) {
		return ResponseEntity.ok().body(new ContainerSecurityResponse(authorize(id)));
	}

	/**
	 * Defines container requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ContainerRequest implements Serializable {
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
	 * Defines container security requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ContainerSecurityRequest
			extends de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Container.Security {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

	}

	/**
	 * Defines container responses for the api without security.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ContainerResponse implements Serializable {
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
		 * Creates a container response for the api without security.
		 * 
		 * @param configuration The container configuration.
		 * @since 1.8
		 */
		public ContainerResponse(ContainerConfiguration configuration) {

			id = configuration.getFolder().getFileName().toString();

			final ContainerConfiguration.Configuration.Information information = configuration.getConfiguration()
					.getInformation();

			name = information.getName();
			description = information.getDescription();
			keywords = information.getKeywords();

			tracking = new TrackingResponse(configuration.getConfiguration());
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
	 * Defines container with right responses for the api without security.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ContainerRightResponse extends ContainerResponse {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The right.
		 */
		private de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Container.Security.Right right;

		/**
		 * Creates a container with right response for the api without security.
		 * 
		 * @param container The container.
		 * @since 1.8
		 */
		public ContainerRightResponse(ContainerService.Container container) {
			super(container.getConfiguration());

			right = container.getRight();
		}

		/**
		 * Returns the right.
		 *
		 * @return The right.
		 * @since 1.8
		 */
		public de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Container.Security.Right getRight() {
			return right;
		}

		/**
		 * Set the right.
		 *
		 * @param right The right to set.
		 * @since 1.8
		 */
		public void setRight(
				de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Container.Security.Right right) {
			this.right = right;
		}

	}

	/**
	 * Defines container with security responses for the api without security.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ContainerSecurityResponse extends ContainerResponse {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The security.
		 */
		private de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Container.Security security;

		/**
		 * Creates a container with right response for the api without security.
		 * 
		 * @param container The container.
		 * @since 1.8
		 */
		public ContainerSecurityResponse(ContainerService.Container container) {
			super(container.getConfiguration());

			security = container.getConfiguration().getConfiguration().getSecurity();
		}

		/**
		 * Returns the security.
		 *
		 * @return The security.
		 * @since 1.8
		 */
		public de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Container.Security getSecurity() {
			return security;
		}

		/**
		 * Set the security.
		 *
		 * @param security The security to set.
		 * @since 1.8
		 */
		public void setSecurity(
				de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Container.Security security) {
			this.security = security;
		}

	}
}
