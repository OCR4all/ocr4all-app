/**
 * File:     PartitionApiController.java
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 *
 * Author:   Herbert Baier
 * Date:     29.05.2024
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.TrackingResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.ModelService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.exchange.PartitionConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.exchange.PartitionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.security.SecurityGrantRW;
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
 * Defines partition exchange controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Tag(name = "partition", description = "the exchange partition API")
@RestController
@RequestMapping(path = PartitionApiController.contextPath, produces = CoreApiController.applicationJson)
public class PartitionApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = apiContextPathVersion_1_0 + exchangeRequestMapping
			+ partitionRequestMapping;

	/**
	 * The partition service.
	 */
	private final PartitionService service;

	/**
	 * Creates a partition exchange controller for the api.
	 *
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param collectionService    The collection service.
	 * @param modelService         The model service.
	 * @param service              The partition service.
	 * @since 1.8
	 */
	public PartitionApiController(ConfigurationService configurationService, SecurityService securityService,
			CollectionService collectionService, ModelService modelService, PartitionService service) {
		super(PartitionApiController.class, configurationService, securityService, collectionService, modelService);

		this.service = service;
	}

	/**
	 * Returns the partition in the response body.
	 *
	 * @param id The partition id.
	 * @return The partition in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the partition in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Partition", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = PartitionRightResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(entityRequestMapping)
	public ResponseEntity<PartitionRightResponse> entity(
			@Parameter(description = "the partition id - this is the folder name") @RequestParam String id) {
		try {
			PartitionService.Partition partition = service.getPartition(id);

			return partition == null ? ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
					: ResponseEntity.ok().body(new PartitionRightResponse(partition));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the list of partitions sorted by name with rights in the response
	 * body.
	 *
	 * @return The list of partitions sorted by name with rights in the response
	 *         body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the list of partitions sorted by name in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Partitions", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = PartitionRightResponse.class))) }),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(listRequestMapping)
	public ResponseEntity<List<PartitionRightResponse>> list() {
		try {
			List<PartitionRightResponse> partitions = new ArrayList<>();
			for (PartitionService.Partition partition : service.getPartitions())
				partitions.add(new PartitionRightResponse(partition));

			return ResponseEntity.ok().body(partitions);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Creates the partition and returns it in the response body.
	 *
	 * @param request The partition request.
	 * @return The partition in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "creates the the partition and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Created Partition", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = PartitionResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(createRequestMapping)
	public ResponseEntity<PartitionResponse> create(@RequestBody @Valid PartitionRequest request) {
		if (!service.isCreate())
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		else
			try {
				PartitionService.Partition partition = service.create(request.getName(), request.getDescription(),
						request.getKeywords());

				return partition == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
						: ResponseEntity.ok().body(new PartitionResponse(partition));
			} catch (Exception ex) {
				log(ex);

				throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
			}
	}

	/**
	 * Removes the partition.
	 *
	 * @param id       The partition id.
	 * @param response The HTTP-specific functionality in sending a response to the
	 *                 client.
	 * @since 1.8
	 */
	@Operation(summary = "removes the partition")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Removed partition"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(removeRequestMapping)
	public void remove(@Parameter(description = "the partition id - this is the folder name") @RequestParam String id,
			HttpServletResponse response) {
		if (!service.isAdministrator())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

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
	 * Updates the partition information and returns it in the response body.
	 *
	 * @param id      The partition id.
	 * @param request The partition request.
	 * @return The partition in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the partition information and returns it in the response body")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Updated Partition Information", content = {
					@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = PartitionResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(updateRequestMapping)
	public ResponseEntity<PartitionResponse> update(
			@Parameter(description = "the partition id - this is the folder name") @RequestParam String id,
			@RequestBody @Valid PartitionRequest request) {
		if (!service.isAdministrator())
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		else
			try {
				PartitionService.Partition partition = service.update(id, request.getName(), request.getDescription(),
						request.getKeywords());

				return partition == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
						: ResponseEntity.ok().body(new PartitionResponse(partition));
			} catch (Exception ex) {
				log(ex);

				throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
			}
	}

	/**
	 * Returns the partition security in the response body.
	 *
	 * @param id The partition id.
	 * @return The partition security in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the partition security in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Partition Security", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = PartitionSecurityResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content) })
	@GetMapping(securityRequestMapping)
	public ResponseEntity<PartitionSecurityResponse> security(
			@Parameter(description = "the partition id - this is the folder name") @RequestParam String id) {
		if (service.isAdministrator()) {
			PartitionService.Partition collection = service.getPartition(id);

			return collection == null ? ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
					: ResponseEntity.ok().body(new PartitionSecurityResponse(collection));
		} else
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}

	/**
	 * Updates the partition security and returns it in the response body.
	 *
	 * @param id      The partition id.
	 * @param request The partition security request.
	 * @return The partition security in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the partition security and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Updated Partition Security", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = PartitionSecurityResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(securityRequestMapping + updateRequestMapping)
	public ResponseEntity<PartitionSecurityResponse> updateSecurity(
			@Parameter(description = "the partition id - this is the folder name") @RequestParam String id,
			@RequestBody PartitionSecurityRequest request) {
		if (!service.isAdministrator())
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		else
			try {
				PartitionService.Partition partition = service.update(id, request);

				return partition == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
						: ResponseEntity.ok().body(new PartitionSecurityResponse(partition));
			} catch (Exception ex) {
				log(ex);

				throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
			}
	}

	/**
	 * Returns the partition files in the response body.
	 *
	 * @param id      The partition id.
	 * @param request The partition file request.
	 * @return The partition files in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns partition files in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Partition Files", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = PartitionFileResponse.class))) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(fileRequestMapping)
	public ResponseEntity<List<PartitionFileResponse>> files(
			@Parameter(description = "the partition id - this is the folder name") @RequestParam String id,
			@RequestBody PartitionFileRequest request) {
		try {
			PartitionService.Partition partition = service.getPartition(id);

			if (partition == null)
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			else if (!partition.getRight().isReadFulfilled())
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			else {
				List<PartitionService.PartitionFile> list = service.getFiles(id, request.getSubFolder());

				if (list == null)
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
				else {
					List<PartitionFileResponse> files = new ArrayList<>();

					for (PartitionService.PartitionFile file : list)
						files.add(new PartitionFileResponse(file));

					return ResponseEntity.ok().body(files);
				}
			}
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines partition requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class PartitionRequest implements Serializable {
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
	 * Defines partition security requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class PartitionSecurityRequest extends SecurityGrantRW {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

	}

	/**
	 * Defines partition responses for the api without security.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class PartitionResponse implements Serializable {
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
		 * Creates a partition response for the api without security.
		 *
		 * @param partition The partition configuration.
		 * @since 1.8
		 */
		public PartitionResponse(PartitionService.Partition partition) {

			id = partition.getConfiguration().getFolder().getFileName().toString();

			final PartitionConfiguration.Configuration.Information information = partition.getConfiguration()
					.getConfiguration().getInformation();

			name = information.getName();
			description = information.getDescription();
			keywords = information.getKeywords();

			tracking = new TrackingResponse(partition.getConfiguration().getConfiguration());
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
	 * Defines partition with right responses for the api without security.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class PartitionRightResponse extends PartitionResponse {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The right.
		 */
		private SecurityGrantRW.Right right;

		/**
		 * Creates a partition with right response for the api without security.
		 *
		 * @param partition The partition.
		 * @since 1.8
		 */
		public PartitionRightResponse(PartitionService.Partition partition) {
			super(partition);

			right = partition.getRight();
		}

		/**
		 * Returns the right.
		 *
		 * @return The right.
		 * @since 1.8
		 */
		public SecurityGrantRW.Right getRight() {
			return right;
		}

		/**
		 * Set the right.
		 *
		 * @param right The right to set.
		 * @since 1.8
		 */
		public void setRight(SecurityGrantRW.Right right) {
			this.right = right;
		}

	}

	/**
	 * Defines partition with security responses for the api without security.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class PartitionSecurityResponse extends PartitionResponse {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The security.
		 */
		private SecurityGrantRW security;

		/**
		 * Creates a partition with right response for the api without security.
		 *
		 * @param partition The partition.
		 * @since 1.8
		 */
		public PartitionSecurityResponse(PartitionService.Partition partition) {
			super(partition);

			security = partition.getConfiguration().getConfiguration().getSecurity();
		}

		/**
		 * Returns the security.
		 *
		 * @return The security.
		 * @since 1.8
		 */
		public SecurityGrantRW getSecurity() {
			return security;
		}

		/**
		 * Set the security.
		 *
		 * @param security The security to set.
		 * @since 1.8
		 */
		public void setSecurity(SecurityGrantRW security) {
			this.security = security;
		}

	}

	/**
	 * Defines partition file requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class PartitionFileRequest implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The sub folder of the partition data folder. If null or blank the partition
		 * data folder is used.
		 */
		@JsonProperty("sub-folder")
		private String subFolder;

		/**
		 * Returns the sub folder of the partition data folder. If null or blank the
		 * partition data folder is used.
		 *
		 * @return The sub folder of the partition data folder.
		 * @since 17
		 */
		public String getSubFolder() {
			return subFolder;
		}

		/**
		 * Set the sub folder of the partition data folder. If null or blank the
		 * partition data folder is used.
		 *
		 * @param subFolder The sub folder to set.
		 * @since 17
		 */
		public void setSubFolder(String subFolder) {
			this.subFolder = subFolder;
		}
	}

	/**
	 * Defines partition file responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class PartitionFileResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The name.
		 */
		private String name;

		/**
		 * The content type.
		 */
		@JsonProperty("content-type")
		private String contentType;

		/**
		 * Default constructor for a partition file response for the api.
		 * 
		 * @since 17
		 */
		public PartitionFileResponse() {
			super();
		}

		/**
		 * Creates a partition file response for the api.
		 * 
		 * @param file The partition file.
		 * @since 17
		 */
		public PartitionFileResponse(PartitionService.PartitionFile file) {
			super();

			name = file.getName();
			contentType = file.getContentType();
		}

		/**
		 * Returns the name.
		 *
		 * @return The name.
		 * @since 17
		 */
		public String getName() {
			return name;
		}

		/**
		 * Set the name.
		 *
		 * @param name The name to set.
		 * @since 17
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Returns the content type.
		 *
		 * @return The content type.
		 * @since 17
		 */
		public String getContentType() {
			return contentType;
		}

		/**
		 * Set the content type.
		 *
		 * @param contentType The content type to set.
		 * @since 17
		 */
		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

	}
}
