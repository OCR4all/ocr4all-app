/**
 * File:     InstanceApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     03.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.core.administration.AdministrationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.ModelService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.Group;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.State;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.User;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Instance;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;

/**
 * Defines instance controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Tag(name = "instance", description = "the instance API")
@RestController
@RequestMapping(path = InstanceApiController.contextPath, produces = CoreApiController.applicationJson)
public class InstanceApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = apiContextPathVersion_1_0 + "/instance";

	/**
	 * The administration service.
	 */
	private final AdministrationService administrationService;

	/**
	 * Creates an instance controller for the api.
	 * 
	 * @param configurationService  The configuration service.
	 * @param securityService       The security service.
	 * @param collectionService     The collection service.
	 * @param modelService          The model service.
	 * @param administrationService The administration service.
	 * @since 1.8
	 */
	public InstanceApiController(ConfigurationService configurationService, SecurityService securityService,
			CollectionService collectionService, ModelService modelService,
			AdministrationService administrationService) {
		super(InstanceApiController.class, configurationService, securityService, collectionService, modelService);

		this.administrationService = administrationService;
	}

	/**
	 * Returns the instance main information.
	 * 
	 * @return The instance main information.
	 * @since 1.8
	 */
	@Operation(summary = "returns the instance main information")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Information", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = InstanceResponse.class)) }),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping
	public ResponseEntity<InstanceResponse> info() {
		try {
			return ResponseEntity.ok().body(new InstanceResponse(configurationService, securityService.isSecured()));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the environment.
	 * 
	 * @return The environment.
	 * @since 1.8
	 */
	@Operation(summary = "returns the environment")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Environment", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = EvironmentResponse.class)) }),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(environmentRequestMapping)
	public ResponseEntity<EvironmentResponse> configuration() {
		try {
			return ResponseEntity.ok().body(new EvironmentResponse(configurationService));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the users and groups.
	 * 
	 * @return The users and groups.
	 * @since 1.8
	 */
	@Operation(summary = "returns the users and groups")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User and Groups", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = UserGroupResponse.class)) }),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(domainRequestMapping)
	public ResponseEntity<UserGroupResponse> usersGroups() {
		try {
			return ResponseEntity.ok()
					.body(new UserGroupResponse(administrationService.getUsers(), administrationService.getGroups()));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines instance responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class InstanceResponse extends Instance {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * True if the application is secured.
		 */
		private boolean isSecured;

		/**
		 * The view languages.
		 */
		@JsonProperty("view-languages")
		private List<String> viewLanguages;

		/**
		 * Creates an instance responses for the api.
		 * 
		 * @param service   The configuration service.
		 * @param isSecured True if the application is secured.
		 * @since 1.8
		 */
		public InstanceResponse(ConfigurationService service, boolean isSecured) {
			super(service.getInstance().getId(), service.getInstance().getName());

			this.isSecured = isSecured;
			this.viewLanguages = service.getApplication().getViewLanguages();
		}

		/**
		 * Returns true if the application is secured.
		 *
		 * @return True if the application is secured.
		 * @since 1.8
		 */
		public boolean isSecured() {
			return isSecured;
		}

		/**
		 * Set to true if the application is secured.
		 *
		 * @param isSecured The secured flag to set.
		 * @since 1.8
		 */
		public void setSecured(boolean isSecured) {
			this.isSecured = isSecured;
		}

		/**
		 * Returns the view languages.
		 *
		 * @return The view languages.
		 * @since 1.8
		 */
		public List<String> getViewLanguages() {
			return viewLanguages;
		}

		/**
		 * Set the view languages.
		 *
		 * @param viewLanguages The view languages to set.
		 * @since 1.8
		 */
		public void setViewLanguages(List<String> viewLanguages) {
			this.viewLanguages = viewLanguages;
		}

	}

	/**
	 * Defines environment responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class EvironmentResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The operating system.
		 */
		@JsonProperty("operating-system")
		private String operatingSystem;

		/**
		 * The charset.
		 */
		private String charset;

		/**
		 * The locale.
		 */
		private String locale;

		/**
		 * The folders.
		 */
		private List<FolderResponse> folders;

		/**
		 * Creates an environment responses for the api.
		 * 
		 * @param service The configuration service.
		 * @since 1.8
		 */
		public EvironmentResponse(ConfigurationService service) {
			super();

			operatingSystem = ConfigurationService.getOperatingSystem().toString();

			charset = service.getApplication().getCharset().displayName();
			locale = service.getApplication().getLocale().toString();

			folders = new ArrayList<>();

			folders.add(
					new FolderResponse(FolderResponse.Type.workspace, service.getWorkspace().getFolder().toString()));
			folders.add(new FolderResponse(FolderResponse.Type.projects,
					service.getWorkspace().getProjects().getFolder().toString()));
			folders.add(new FolderResponse(FolderResponse.Type.workflows,
					service.getWorkspace().getWorkflows().getFolder().toString()));
			folders.add(new FolderResponse(FolderResponse.Type.exchange, service.getExchange().getFolder().toString()));
			folders.add(
					new FolderResponse(FolderResponse.Type.repository, service.getRepository().getFolder().toString()));
			folders.add(new FolderResponse(FolderResponse.Type.data, service.getData().getFolder().toString()));
			folders.add(new FolderResponse(FolderResponse.Type.assemble, service.getAssemble().getFolder().toString()));
			folders.add(new FolderResponse(FolderResponse.Type.opt, service.getOpt().getFolder().toString()));
			folders.add(
					new FolderResponse(FolderResponse.Type.temporary, service.getTemporary().getFolder().toString()));
		}

		/**
		 * Returns the operatingSystem.
		 *
		 * @return The operatingSystem.
		 * @since 17
		 */
		public String getOperatingSystem() {
			return operatingSystem;
		}

		/**
		 * Set the operatingSystem.
		 *
		 * @param operatingSystem The operatingSystem to set.
		 * @since 17
		 */
		public void setOperatingSystem(String operatingSystem) {
			this.operatingSystem = operatingSystem;
		}

		/**
		 * Returns the charset.
		 *
		 * @return The charset.
		 * @since 17
		 */
		public String getCharset() {
			return charset;
		}

		/**
		 * Set the charset.
		 *
		 * @param charset The charset to set.
		 * @since 17
		 */
		public void setCharset(String charset) {
			this.charset = charset;
		}

		/**
		 * Returns the locale.
		 *
		 * @return The locale.
		 * @since 17
		 */
		public String getLocale() {
			return locale;
		}

		/**
		 * Set the locale.
		 *
		 * @param locale The locale to set.
		 * @since 17
		 */
		public void setLocale(String locale) {
			this.locale = locale;
		}

		/**
		 * Returns the folders.
		 *
		 * @return The folders.
		 * @since 17
		 */
		public List<FolderResponse> getFolders() {
			return folders;
		}

		/**
		 * Set the folders.
		 *
		 * @param folders The folders to set.
		 * @since 17
		 */
		public void setFolders(List<FolderResponse> folders) {
			this.folders = folders;
		}

		/**
		 * Defines folder responses for the api.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class FolderResponse implements Serializable {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The types.
			 */
			public enum Type {
				workspace, projects, workflows, exchange, repository, data, assemble, opt, temporary
			}

			/**
			 * The type.
			 */
			private Type type;

			/**
			 * The folder.
			 */
			private String folder;

			/**
			 * Creates a folder responses for the api.
			 * 
			 * @param type   The type.
			 * @param folder The folder.
			 * @since 17
			 */
			public FolderResponse(Type type, String folder) {
				super();

				this.type = type;
				this.folder = folder;
			}

			/**
			 * Returns the type.
			 *
			 * @return The type.
			 * @since 17
			 */
			public Type getType() {
				return type;
			}

			/**
			 * Set the type.
			 *
			 * @param type The type to set.
			 * @since 17
			 */
			public void setType(Type type) {
				this.type = type;
			}

			/**
			 * Returns the folder.
			 *
			 * @return The folder.
			 * @since 17
			 */
			public String getFolder() {
				return folder;
			}

			/**
			 * Set the folder.
			 *
			 * @param folder The folder to set.
			 * @since 17
			 */
			public void setFolder(String folder) {
				this.folder = folder;
			}

		}
	}

	/**
	 * Defines user and group responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class UserGroupResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The users.
		 */
		private List<UserResponse> users;

		/**
		 * The groups.
		 */
		private List<GroupResponse> groups;

		/**
		 * Default constructor for a user and group response for the api.
		 * 
		 * @since 17
		 */
		public UserGroupResponse() {
			super();
		}

		/**
		 * Creates a user and group response for the api.
		 * 
		 * @param users  The users.
		 * @param groups The groups.
		 * @since 17
		 */
		public UserGroupResponse(List<User> users, List<Group> groups) {
			super();

			this.users = new ArrayList<>();
			if (users != null)
				for (User user : users)
					if (user != null)
						this.users.add(new UserResponse(user));

			this.groups = new ArrayList<>();
			if (groups != null)
				for (Group group : groups)
					if (group != null)
						this.groups.add(new GroupResponse(group));
		}

		/**
		 * Returns the users.
		 *
		 * @return The users.
		 * @since 17
		 */
		public List<UserResponse> getUsers() {
			return users;
		}

		/**
		 * Set the users.
		 *
		 * @param users The users to set.
		 * @since 17
		 */
		public void setUsers(List<UserResponse> users) {
			this.users = users;
		}

		/**
		 * Returns the groups.
		 *
		 * @return The groups.
		 * @since 17
		 */
		public List<GroupResponse> getGroups() {
			return groups;
		}

		/**
		 * Set the groups.
		 *
		 * @param groups The groups to set.
		 * @since 17
		 */
		public void setGroups(List<GroupResponse> groups) {
			this.groups = groups;
		}

		/**
		 * Defines user responses for the api.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class UserResponse implements Serializable {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The login.
			 */
			@NotBlank
			private String login;

			/**
			 * The state.
			 */
			private State state;

			/**
			 * The email.
			 */
			private String email;

			/**
			 * The name.
			 */
			private String name;

			/**
			 * Default constructor for a user response for the api.
			 * 
			 * @since 1.8
			 */
			public UserResponse() {
				super();
			}

			/**
			 * Creates a user response for the api.
			 * 
			 * @param user The user.
			 * @since 1.8
			 */
			public UserResponse(User user) {
				super();

				login = user.getLogin();
				state = user.getState();
				email = user.getEmail();
				name = user.getName();
			}

			/**
			 * Returns the login.
			 *
			 * @return The login.
			 * @since 1.8
			 */
			public String getLogin() {
				return login;
			}

			/**
			 * Set the login.
			 *
			 * @param login The login to set.
			 * @since 1.8
			 */
			public void setLogin(String login) {
				this.login = login;
			}

			/**
			 * Returns the state.
			 *
			 * @return The state.
			 * @since 1.8
			 */
			public State getState() {
				return state;
			}

			/**
			 * Set the state.
			 *
			 * @param state The state to set.
			 * @since 1.8
			 */
			public void setState(State state) {
				this.state = state;
			}

			/**
			 * Returns the email.
			 *
			 * @return The email.
			 * @since 1.8
			 */
			public String getEmail() {
				return email;
			}

			/**
			 * Set the email.
			 *
			 * @param email The email to set.
			 * @since 1.8
			 */
			public void setEmail(String email) {
				this.email = email;
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
		}

		/**
		 * Defines group responses for the api.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class GroupResponse implements Serializable {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The label.
			 */
			@NotBlank
			private String label;

			/**
			 * The state.
			 */
			private State state;

			/**
			 * The name.
			 */
			private String name;

			/**
			 * Default constructor for a group response for the api.
			 * 
			 * @since 1.8
			 */
			public GroupResponse() {
				super();
			}

			/**
			 * Creates a group response for the api.
			 * 
			 * @param group The group.
			 * @since 1.8
			 */
			public GroupResponse(Group group) {
				super();

				label = group.getLabel();
				state = group.getState();
				name = group.getName();
			}

			/**
			 * Returns the label.
			 *
			 * @return The label.
			 * @since 1.8
			 */
			public String getLabel() {
				return label;
			}

			/**
			 * Set the label.
			 *
			 * @param label The label to set.
			 * @since 1.8
			 */
			public void setLabel(String label) {
				this.label = label;
			}

			/**
			 * Returns the state.
			 *
			 * @return The state.
			 * @since 1.8
			 */
			public State getState() {
				return state;
			}

			/**
			 * Set the state.
			 *
			 * @param state The state to set.
			 * @since 1.8
			 */
			public void setState(State state) {
				this.state = state;
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

		}

	}
}
