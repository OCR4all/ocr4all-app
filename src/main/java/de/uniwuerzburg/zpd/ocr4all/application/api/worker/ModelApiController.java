/**
 * File:     ModelApiController.java
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 *
 * Author:   Herbert Baier
 * Date:     05.07.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.TrackingResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.ModelService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.assemble.ModelConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
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
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
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
			 * The type.
			 */
			private de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Engine.Type type;

			/**
			 * The state.
			 */
			private de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Engine.State state;

			/**
			 * The version.
			 */
			private String version;

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

				type = engine.getType();
				state = engine.getState();
				version = engine.getVersion();
				arguments = engine.getArguments();
				user = engine.getUser();
				create = engine.getDate();
				done = engine.getDone();
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
