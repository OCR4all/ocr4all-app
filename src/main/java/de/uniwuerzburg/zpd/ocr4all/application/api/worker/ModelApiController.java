/**
 * File:     ModelApiController.java
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 *
 * Author:   Herbert Baier
 * Date:     05.07.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.TrackingResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.ModelService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.assemble.ModelConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Engine;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.security.SecurityGrant;
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
import jakarta.validation.constraints.NotNull;

/**
 * Defines assemble model controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Tag(name = "model", description = "the data model API")
@RestController
@RequestMapping(path = ModelApiController.contextPath, produces = CoreApiController.applicationJson)
public class ModelApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = AssembleApiController.contextPath + modelRequestMapping;

	/**
	 * The engine request mapping.
	 */
	public static final String engineRequestMapping = "/engine";

	/**
	 * Creates an assemble model controller for the api.
	 *
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param collectionService    The collection service.
	 * @param modelService         The model service.
	 * @since 1.8
	 */
	public ModelApiController(ConfigurationService configurationService, SecurityService securityService,
			CollectionService collectionService, ModelService modelService) {
		super(ModelApiController.class, configurationService, securityService, collectionService, modelService);
	}

	/**
	 * Returns the model in the response body.
	 *
	 * @param id The model id.
	 * @return The model in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the model in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Model", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = ModelRightResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(entityRequestMapping)
	public ResponseEntity<ModelRightResponse> entity(
			@Parameter(description = "the model id - this is the folder name") @RequestParam String id) {
		try {
			ModelService.Model model = modelService.getModel(id);

			return model == null ? ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
					: ResponseEntity.ok().body(new ModelRightResponse(model));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the list of models sorted by name with rights in the response body.
	 *
	 * @return The list of models sorted by name with rights in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the list of models sorted by name in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Models", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = ModelRightResponse.class))) }),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(listRequestMapping)
	public ResponseEntity<List<ModelRightResponse>> list() {
		try {
			List<ModelRightResponse> models = new ArrayList<>();
			for (ModelService.Model model : modelService.getModels())
				models.add(new ModelRightResponse(model));

			return ResponseEntity.ok().body(models);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the available models sorted by name with desired files in the
	 * response body.
	 *
	 * @param request The available model request.
	 * @return The available models sorted by name with desired files in the
	 *         response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the available models sorted by name with desired files in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Available Models", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = ModelFileResponse.class))) }),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(availableRequestMapping)
	public ResponseEntity<List<ModelFileResponse>> available(@RequestBody AvailableModelRequest request) {
		try {
			List<ModelFileResponse> modelFiles = new ArrayList<>();
			for (ModelService.ModelFile modelFile : modelService.getAvailableModels(
					request == null ? null
							: new ModelService.ModelFilter(request.getType(), request.getStates(),
									request.getMinimumVersion(), request.getMaximumVersion()),
					request == null ? null : request.getFilenameSuffix()))
				modelFiles.add(new ModelFileResponse(modelFile));

			return ResponseEntity.ok().body(modelFiles);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Authorizes the session user for special security operations.
	 *
	 * @param id The model id.
	 * @return The authorized model.
	 * @throws ResponseStatusException Throw with http status:
	 *                                 <ul>
	 *                                 <li>400 (Bad Request): if the model is not
	 *                                 available.</li>
	 *                                 <li>401 (Unauthorized): if the special
	 *                                 security permission is not achievable by the
	 *                                 session user.</li>
	 *                                 </ul>
	 * @since 1.8
	 */
	private ModelService.Model authorizeSpecial(String id) throws ResponseStatusException {
		ModelService.Model model = modelService.getModel(id);

		if (model == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		else if (!model.getRight().isSpecialFulfilled())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		else
			return model;
	}

	/**
	 * Authorizes the session user for write security operations.
	 *
	 * @param id The model id.
	 * @return The authorized model.
	 * @throws ResponseStatusException Throw with http status:
	 *                                 <ul>
	 *                                 <li>400 (Bad Request): if the model is not
	 *                                 available.</li>
	 *                                 <li>401 (Unauthorized): if the write security
	 *                                 permission is not achievable by the session
	 *                                 user.</li>
	 *                                 </ul>
	 * @since 1.8
	 */
	private ModelService.Model authorizeWrite(String id) throws ResponseStatusException {
		ModelService.Model model = modelService.getModel(id);

		if (model == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		else if (!model.getRight().isWriteFulfilled())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		else
			return model;
	}

	/**
	 * Authorizes the session user for read security operations.
	 *
	 * @param id The model id.
	 * @return The authorized model.
	 * @throws ResponseStatusException Throw with http status:
	 *                                 <ul>
	 *                                 <li>400 (Bad Request): if the model is not
	 *                                 available.</li>
	 *                                 <li>401 (Unauthorized): if the read security
	 *                                 permission is not achievable by the session
	 *                                 user.</li>
	 *                                 </ul>
	 * @since 1.8
	 */
	private ModelService.Model authorizeRead(String id) throws ResponseStatusException {
		ModelService.Model model = modelService.getModel(id);

		if (model == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		else if (!model.getRight().isReadFulfilled())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		else
			return model;
	}

	/**
	 * Creates the model for upload and returns it in the response body.
	 *
	 * @param request The model engine request.
	 * @return The model in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "creates the the model for upload and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Created Model", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = ModelResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(createRequestMapping)
	public ResponseEntity<ModelResponse> create(@RequestBody @Valid ModelEngineRequest request) {
		if (!modelService.isCreate())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		else
			try {
				ModelService.Model model = modelService.create(request.getName(), request.getDescription(),
						request.getKeywords());

				if (model == null)
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
				else {
					model.getConfiguration().getConfiguration().update(securityService.getUser(),
							new ModelConfiguration.Configuration.EngineInformation(Engine.Method.manual,
									Engine.State.uploading, request.getEngineType(), request.getEngineVersion(),
									request.getEngineName(), null));

					return ResponseEntity.ok().body(new ModelResponse(model));
				}
			} catch (Exception ex) {
				log(ex);

				throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
			}
	}

	/**
	 * Removes the model.
	 *
	 * @param id       The model id.
	 * @param response The HTTP-specific functionality in sending a response to the
	 *                 client.
	 * @since 1.8
	 */
	@Operation(summary = "removes the model")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Removed model"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "405", description = "Method Not Allowed", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(removeRequestMapping)
	public void remove(@Parameter(description = "the model id - this is the folder name") @RequestParam String id,
			HttpServletResponse response) {
		authorizeSpecial(id);

		try {
			if (modelService.remove(id))
				response.setStatus(HttpServletResponse.SC_OK);
			else
				throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates the model information and returns it in the response body.
	 *
	 * @param id      The model id.
	 * @param request The model request.
	 * @return The model in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the model information and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Updated Model Information", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = ModelResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(updateRequestMapping)
	public ResponseEntity<ModelResponse> update(
			@Parameter(description = "the model id - this is the folder name") @RequestParam String id,
			@RequestBody @Valid ModelRequest request) {
		authorizeSpecial(id);

		try {
			ModelService.Model model = modelService.update(id, request.getName(), request.getDescription(),
					request.getKeywords());

			return model == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
					: ResponseEntity.ok().body(new ModelResponse(model));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Upload the models.
	 * 
	 * @param modelId  The model id.
	 * @param files    The files to upload in a multipart request.
	 * @param response The HTTP-specific functionality in sending a response to the
	 *                 client.
	 * @return The model in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "upload models")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Uploaded Models", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = ModelResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(uploadRequestMapping + modelPathVariable)
	public ResponseEntity<ModelResponse> upload(
			@Parameter(description = "the model id - this is the folder name") @PathVariable String modelId,
			@RequestParam MultipartFile[] files, HttpServletResponse response) {
		authorizeWrite(modelId);

		try {
			ModelService.Model model = modelService.store(modelId, files);

			return model == null ? ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
					: ResponseEntity.ok().body(new ModelResponse(model));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates the model engine information and returns the model in the response
	 * body.
	 *
	 * @param id      The model id.
	 * @param request The engine request.
	 * @return The model in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the model engine information and returns it in the response body")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Updated Model Engine Information", content = {
					@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = ModelResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(engineRequestMapping + updateRequestMapping)
	public ResponseEntity<ModelResponse> updateEngine(
			@Parameter(description = "the model id - this is the folder name") @RequestParam String id,
			@RequestBody @Valid EngineRequest request) {
		authorizeSpecial(id);

		try {
			ModelService.Model model = modelService.update(id,
					new ModelConfiguration.Configuration.EngineInformation(request.getMethod(), request.getState(),
							request.getType(), request.getVersion(), request.getName(), request.getArguments()));

			return model == null ? ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
					: ResponseEntity.ok().body(new ModelResponse(model));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the model with its file names.
	 * 
	 * @param modelId The model id.
	 * @return The model with its file names in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the model with file names")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Models File Names", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = ModelFileResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(fileRequestMapping + modelPathVariable)
	public ResponseEntity<ModelFileResponse> file(
			@Parameter(description = "the model id - this is the folder name") @PathVariable String modelId) {
		authorizeRead(modelId);

		try {
			ModelService.ModelFile modelFile = modelService.getModelFiles(modelId);

			return modelFile == null ? ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
					: ResponseEntity.ok().body(new ModelFileResponse(modelFile));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Zips the files of given model.
	 * 
	 * @param modelId  The model id. This is the folder name.
	 * @param response The HTTP-specific functionality in sending a response to the
	 *                 client.
	 * @throws IOException Signals that an I/O exception of some sort has occurred.
	 * @since 1.8
	 */
	@Operation(summary = "zip the model files")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Downloaded Model Files"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(zipRequestMapping + modelPathVariable)
	public void zip(@Parameter(description = "the model id - this is the folder name") @PathVariable String modelId,
			HttpServletResponse response) throws IOException {
		ModelService.Model model = authorizeRead(modelId);

		try {
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=\"model-" + modelId.trim() + ".zip\"");

			OCR4allUtils.zip(model.getConfiguration().getFolder(), true, response.getOutputStream(),
					new OCR4allUtils.ZipFilter() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see
						 * de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils.ZipFilter#
						 * accept(java.io.File)
						 */
						@Override
						public boolean accept(File entry) {
							// Ignore configuration folders
							return !entry.getName().startsWith(".");
						}
					});
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the model security in the response body.
	 *
	 * @param id The model id.
	 * @return The model security in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the model security in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Model Security", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = ModelSecurityResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content) })
	@GetMapping(securityRequestMapping)
	public ResponseEntity<ModelSecurityResponse> security(
			@Parameter(description = "the model id - this is the folder name") @RequestParam String id) {
		return ResponseEntity.ok().body(new ModelSecurityResponse(authorizeSpecial(id)));
	}

	/**
	 * Updates the model security and returns it in the response body.
	 *
	 * @param id      The model id.
	 * @param request The model security request.
	 * @return The model security in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the model security and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Updated Model Security", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = ModelSecurityResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(securityRequestMapping + updateRequestMapping)
	public ResponseEntity<ModelSecurityResponse> updateSecurity(
			@Parameter(description = "the model id - this is the folder name") @RequestParam String id,
			@RequestBody ModelSecurityRequest request) {
		authorizeSpecial(id);

		try {
			ModelService.Model model = modelService.update(id, request);

			return model == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
					: ResponseEntity.ok().body(new ModelSecurityResponse(model));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines model requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ModelRequest implements Serializable {
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
	 * Defines model engine requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ModelEngineRequest extends ModelRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The engine type.
		 */
		@NotNull
		@JsonProperty("engine-type")
		private Engine.Type engineType;

		/**
		 * The engine version.
		 */
		@NotBlank
		@JsonProperty("engine-version")
		private String engineVersion;

		/**
		 * The engine name.
		 */
		@JsonProperty("engine-name")
		private String engineName;

		/**
		 * Returns the engine type.
		 *
		 * @return The engine type.
		 * @since 17
		 */
		public Engine.Type getEngineType() {
			return engineType;
		}

		/**
		 * Set the engine type.
		 *
		 * @param engineType The engine type to set.
		 * @since 17
		 */
		public void setEngineType(Engine.Type engineType) {
			this.engineType = engineType;
		}

		/**
		 * Returns the engine version.
		 *
		 * @return The engine version.
		 * @since 17
		 */
		public String getEngineVersion() {
			return engineVersion;
		}

		/**
		 * Set the engine version.
		 *
		 * @param engineVersion The engine version to set.
		 * @since 17
		 */
		public void setEngineVersion(String engineVersion) {
			this.engineVersion = engineVersion;
		}

		/**
		 * Returns the engine name.
		 *
		 * @return The engine name.
		 * @since 17
		 */
		public String getEngineName() {
			return engineName;
		}

		/**
		 * Set the engine name.
		 *
		 * @param engineName The engine name to set.
		 * @since 17
		 */
		public void setEngineName(String engineName) {
			this.engineName = engineName;
		}

	}

	/**
	 * Defines engine requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class EngineRequest implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The method. If null, do not update.
		 */
		private Engine.Method method;

		/**
		 * The state. If null, do not update.
		 */
		private Engine.State state;

		/**
		 * The type. If null, do not update.
		 */
		private Engine.Type type;

		/**
		 * The version. If null, do not update. Empty resets the version.
		 */
		private String version;

		/**
		 * The name. If null, do not update. Empty resets the name.
		 */
		private String name;

		/**
		 * The arguments. If null, do not update. Empty list resets the arguments.
		 */
		private List<String> arguments;

		/**
		 * Returns the method. If null, do not update.
		 *
		 * @return The method.
		 * @since 17
		 */
		public Engine.Method getMethod() {
			return method;
		}

		/**
		 * Set the method. If null, do not update.
		 *
		 * @param method The method to set.
		 * @since 17
		 */
		public void setMethod(Engine.Method method) {
			this.method = method;
		}

		/**
		 * Returns the state. If null, do not update.
		 *
		 * @return The state.
		 * @since 17
		 */
		public Engine.State getState() {
			return state;
		}

		/**
		 * Set the state. If null, do not update.
		 *
		 * @param state The state to set.
		 * @since 17
		 */
		public void setState(Engine.State state) {
			this.state = state;
		}

		/**
		 * Returns the type. If null, do not update.
		 *
		 * @return The type.
		 * @since 17
		 */
		public Engine.Type getType() {
			return type;
		}

		/**
		 * Set the type. If null, do not update.
		 *
		 * @param type The type to set.
		 * @since 17
		 */
		public void setType(Engine.Type type) {
			this.type = type;
		}

		/**
		 * Returns the version. If null, do not update. Empty resets the version.
		 *
		 * @return The version.
		 * @since 17
		 */
		public String getVersion() {
			return version;
		}

		/**
		 * Set the version. If null, do not update. Empty resets the version.
		 *
		 * @param version The version to set.
		 * @since 17
		 */
		public void setVersion(String version) {
			this.version = version;
		}

		/**
		 * Returns the name. If null, do not update. Empty resets the name.
		 *
		 * @return The name.
		 * @since 17
		 */
		public String getName() {
			return name;
		}

		/**
		 * Set the name. If null, do not update. Empty resets the name.
		 *
		 * @param name The name to set.
		 * @since 17
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Returns the arguments. If null, do not update. Empty list resets the
		 * arguments.
		 *
		 * @return The arguments.
		 * @since 17
		 */
		public List<String> getArguments() {
			return arguments;
		}

		/**
		 * Set the arguments. If null, do not update. Empty list resets the arguments.
		 *
		 * @param arguments The arguments to set.
		 * @since 17
		 */
		public void setArguments(List<String> arguments) {
			this.arguments = arguments;
		}

	}

	/**
	 * Defines model security requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ModelSecurityRequest extends SecurityGrant {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

	}

	/**
	 * Defines available model requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class AvailableModelRequest implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The engine type.
		 */
		private Engine.Type type;

		/**
		 * The engine states.
		 */
		private Set<Engine.State> states;

		/**
		 * The minimum version.
		 */
		private String minimumVersion;

		/**
		 * The maximum version.
		 */
		private String maximumVersion;

		/**
		 * The suffix for the model file names to return.
		 */
		private String filenameSuffix;

		/**
		 * Returns the engine type.
		 *
		 * @return The engine type.
		 * @since 17
		 */
		public Engine.Type getType() {
			return type;
		}

		/**
		 * Set the engine type.
		 *
		 * @param type The engine type to set.
		 * @since 17
		 */
		public void setType(Engine.Type type) {
			this.type = type;
		}

		/**
		 * Returns the engine states.
		 *
		 * @return The engine states.
		 * @since 17
		 */
		public Set<Engine.State> getStates() {
			return states;
		}

		/**
		 * Set the engine states.
		 *
		 * @param states The engine states to set.
		 * @since 17
		 */
		public void setStates(Set<Engine.State> states) {
			this.states = states;
		}

		/**
		 * Returns the minimum version.
		 *
		 * @return The minimum version.
		 * @since 17
		 */
		public String getMinimumVersion() {
			return minimumVersion;
		}

		/**
		 * Set the minimum version.
		 *
		 * @param minimumVersion The minimum version to set.
		 * @since 17
		 */
		public void setMinimumVersion(String minimumVersion) {
			this.minimumVersion = minimumVersion;
		}

		/**
		 * Returns the maximum version.
		 *
		 * @return The maximum version.
		 * @since 17
		 */
		public String getMaximumVersion() {
			return maximumVersion;
		}

		/**
		 * Set the maximum version.
		 *
		 * @param maximumVersion The maximum version to set.
		 * @since 17
		 */
		public void setMaximumVersion(String maximumVersion) {
			this.maximumVersion = maximumVersion;
		}

		/**
		 * Returns the suffix for the model file names to return.
		 *
		 * @return The suffix for the model file names to return.
		 * @since 17
		 */
		public String getFilenameSuffix() {
			return filenameSuffix;
		}

		/**
		 * Set the suffix for the model file names to return.
		 *
		 * @param filenameSuffix The suffix to set.
		 * @since 17
		 */
		public void setFilenameSuffix(String filenameSuffix) {
			this.filenameSuffix = filenameSuffix;
		}

	}

	/**
	 * Defines model responses for the api without security.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ModelResponse implements Serializable {
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
		 * The engine.
		 */
		private Engine engine;

		/**
		 * The tracking.
		 */
		private TrackingResponse tracking;

		/**
		 * Creates a model response for the api without security.
		 *
		 * @param model The model configuration.
		 * @since 1.8
		 */
		public ModelResponse(ModelService.Model model) {

			id = model.getConfiguration().getFolder().getFileName().toString();

			final ModelConfiguration.Configuration.Information information = model.getConfiguration().getConfiguration()
					.getInformation();

			name = information.getName();
			description = information.getDescription();
			keywords = information.getKeywords();

			engine = model.getConfiguration().getConfiguration().isEngineConfigurationAvailable()
					? new Engine(model.getConfiguration().getConfiguration().getEngineConfiguration())
					: null;

			tracking = new TrackingResponse(model.getConfiguration().getConfiguration());
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
		 * Returns the engine.
		 *
		 * @return The engine.
		 * @since 17
		 */
		public Engine getEngine() {
			return engine;
		}

		/**
		 * Set the engine.
		 *
		 * @param engine The engine to set.
		 * @since 17
		 */
		public void setEngine(Engine engine) {
			this.engine = engine;
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

		/**
		 * Defines engine responses for the api
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 17
		 */
		public static class Engine implements Serializable {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The method.
			 */
			private de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Engine.Method method;

			/**
			 * The state.
			 */
			private de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Engine.State state;

			/**
			 * The type.
			 */
			private de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Engine.Type type;

			/**
			 * The version.
			 */
			private String version;

			/**
			 * The name.
			 */
			private String name;

			/**
			 * The arguments.
			 */
			private List<String> arguments;

			/**
			 * The user.
			 */
			private String user;

			/**
			 * The created.
			 */
			private Date create;

			/**
			 * The done time. Null if running.
			 */
			private Date done;

			/**
			 * Creates an engine response for the api.
			 * 
			 * @param engine The engine.
			 * @since 17
			 */
			public Engine(de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Engine engine) {
				super();

				method = engine.getMethod();
				state = engine.getState();
				type = engine.getType();

				version = engine.getVersion();
				name = engine.getName();
				arguments = engine.getArguments();
				user = engine.getUser();
				create = engine.getDate();
				done = engine.getDone();
			}

			/**
			 * Returns the method.
			 *
			 * @return The method.
			 * @since 17
			 */
			public de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Engine.Method getMethod() {
				return method;
			}

			/**
			 * Set the method.
			 *
			 * @param method The method to set.
			 * @since 17
			 */
			public void setMethod(de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Engine.Method method) {
				this.method = method;
			}

			/**
			 * Returns the state.
			 *
			 * @return The state.
			 * @since 17
			 */
			public de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Engine.State getState() {
				return state;
			}

			/**
			 * Set the state.
			 *
			 * @param state The state to set.
			 * @since 17
			 */
			public void setState(de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Engine.State state) {
				this.state = state;
			}

			/**
			 * Returns the type.
			 *
			 * @return The type.
			 * @since 17
			 */
			public de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Engine.Type getType() {
				return type;
			}

			/**
			 * Set the type.
			 *
			 * @param type The type to set.
			 * @since 17
			 */
			public void setType(de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Engine.Type type) {
				this.type = type;
			}

			/**
			 * Returns the version.
			 *
			 * @return The version.
			 * @since 17
			 */
			public String getVersion() {
				return version;
			}

			/**
			 * Set the version.
			 *
			 * @param version The version to set.
			 * @since 17
			 */
			public void setVersion(String version) {
				this.version = version;
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
			 * Returns the arguments.
			 *
			 * @return The arguments.
			 * @since 17
			 */
			public List<String> getArguments() {
				return arguments;
			}

			/**
			 * Set the arguments.
			 *
			 * @param arguments The arguments to set.
			 * @since 17
			 */
			public void setArguments(List<String> arguments) {
				this.arguments = arguments;
			}

			/**
			 * Returns the user.
			 *
			 * @return The user.
			 * @since 17
			 */
			public String getUser() {
				return user;
			}

			/**
			 * Set the user.
			 *
			 * @param user The user to set.
			 * @since 17
			 */
			public void setUser(String user) {
				this.user = user;
			}

			/**
			 * Returns the create time stamp.
			 *
			 * @return The create time stamp.
			 * @since 17
			 */
			public Date getCreate() {
				return create;
			}

			/**
			 * Set the create time stamp.
			 *
			 * @param create The create time stamp to set.
			 * @since 17
			 */
			public void setCreate(Date create) {
				this.create = create;
			}

			/**
			 * Returns the done time stamp.
			 *
			 * @return The done time stamp.
			 * @since 17
			 */
			public Date getDone() {
				return done;
			}

			/**
			 * Set the done time stamp.
			 *
			 * @param done The done time stamp to set.
			 * @since 17
			 */
			public void setDone(Date done) {
				this.done = done;
			}

		}
	}

	/**
	 * Defines model with right responses for the api without security.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ModelRightResponse extends ModelResponse {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The right.
		 */
		private SecurityGrant.Right right;

		/**
		 * Creates a model with right response for the api without security.
		 *
		 * @param model The model.
		 * @since 1.8
		 */
		public ModelRightResponse(ModelService.Model model) {
			super(model);

			right = model.getRight();
		}

		/**
		 * Returns the right.
		 *
		 * @return The right.
		 * @since 1.8
		 */
		public SecurityGrant.Right getRight() {
			return right;
		}

		/**
		 * Set the right.
		 *
		 * @param right The right to set.
		 * @since 1.8
		 */
		public void setRight(SecurityGrant.Right right) {
			this.right = right;
		}

	}

	/**
	 * Defines available model with right responses for the api without security.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ModelFileResponse extends ModelRightResponse {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The file names.
		 */
		@JsonProperty("file-names")
		private final List<String> filenames;

		/**
		 * Creates an available model with right response for the api without security.
		 *
		 * @param modelFile The model file.
		 * @since 1.8
		 */
		public ModelFileResponse(ModelService.ModelFile modelFile) {
			super(modelFile);

			filenames = modelFile.getFilenames();
		}

		/**
		 * Returns the filenames.
		 *
		 * @return The filenames.
		 * @since 17
		 */
		public List<String> getFilenames() {
			return filenames;
		}

	}

	/**
	 * Defines model with security responses for the api without security.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ModelSecurityResponse extends ModelResponse {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The security.
		 */
		private SecurityGrant security;

		/**
		 * Creates a model with right response for the api without security.
		 *
		 * @param model The model.
		 * @since 1.8
		 */
		public ModelSecurityResponse(ModelService.Model model) {
			super(model);

			security = model.getConfiguration().getConfiguration().getSecurity();
		}

		/**
		 * Returns the security.
		 *
		 * @return The security.
		 * @since 1.8
		 */
		public SecurityGrant getSecurity() {
			return security;
		}

		/**
		 * Set the security.
		 *
		 * @param security The security to set.
		 * @since 1.8
		 */
		public void setSecurity(SecurityGrant security) {
			this.security = security;
		}

	}
}
