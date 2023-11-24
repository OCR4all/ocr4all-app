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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
		 * The uuid.
		 */
		private String uuid = null;

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

			uuid = configuration.getFolder().getFileName().toString();

			final ContainerConfiguration.Configuration.BasicData basicData = configuration.getConfiguration()
					.getBasicData();

			name = basicData.getName();
			description = basicData.getDescription();
			keywords = basicData.getKeywords();

			tracking = new TrackingResponse(configuration.getConfiguration());
		}

		/**
		 * Returns the uuid.
		 *
		 * @return The uuid.
		 * @since 1.8
		 */
		public String getUuid() {
			return uuid;
		}

		/**
		 * Set the uuid.
		 *
		 * @param uuid The uuid to set.
		 * @since 1.8
		 */
		public void setUuid(String uuid) {
			this.uuid = uuid;
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
}
