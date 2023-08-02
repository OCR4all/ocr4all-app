/**
 * File:     AdministrationSecurityApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     01.03.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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

import de.uniwuerzburg.zpd.ocr4all.application.core.administration.AdministrationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.Group;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.Password;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityEntity;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.State;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Defines security administration controllers for the api and server.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api & server")
@Tag(name = "administration security", description = "the security administration API")
@RestController
@RequestMapping(path = AdministrationSecurityApiController.contextPath, produces = CoreApiController.applicationJson)
public class AdministrationSecurityApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = AdministrationApiController.contextPath + securityRequestMapping;

	/**
	 * The user request mapping.
	 */
	public static final String userRequestMapping = "/user";

	/**
	 * The groups request mapping.
	 */
	public static final String groupRequestMapping = "/group";

	/**
	 * The administration service.
	 */
	private final AdministrationService service;

	/**
	 * Creates a security administration controller for the api and server.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param service              The administration service.
	 * @since 1.8
	 */
	public AdministrationSecurityApiController(ConfigurationService configurationService,
			SecurityService securityService, AdministrationService service) {
		super(AdministrationSecurityApiController.class, configurationService, securityService);

		this.service = service;
	}

	/**
	 * Returns the user with his groups in the response body.
	 * 
	 * @param login The user login.
	 * @return The project in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the user with his groups in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User with Groups", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = UserGroupResponse.class)) }),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(userRequestMapping + entityRequestMapping)
	public ResponseEntity<UserGroupResponse> userEntity(
			@Parameter(description = "the user login") @RequestParam String login) {
		try {
			User user = service.getUser(login);

			return user == null ? ResponseEntity.status(HttpStatus.NOT_FOUND).build()
					: ResponseEntity.ok().body(new UserGroupResponse(user, service.getGroups()));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the users sorted by login in the response body.
	 * 
	 * @return The users sorted by login in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the users sorted by login in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Users with Groups", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))) }),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(userRequestMapping + listRequestMapping)
	public ResponseEntity<List<UserResponse>> userList() {
		try {
			List<UserResponse> users = new ArrayList<>();

			for (User user : service.getUsers())
				users.add(new UserResponse(user));

			return ResponseEntity.ok().body(users);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Creates the user and returns it in the response body. If the user is
	 * available, it will be returned and not recreated.
	 * 
	 * @param request The user request.
	 * @return The created user in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "creates the user and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Creatred User", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = UserResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "409", description = "Conflict", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(userRequestMapping + createRequestMapping)
	public ResponseEntity<UserResponse> userCreate(@RequestBody @Valid UserRequest request) {
		String login = SecurityEntity.filter(request.getLogin());
		if (login == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

		try {
			User user = service.getUser(login);
			if (user != null)
				return ResponseEntity.status(HttpStatus.CONFLICT).body(new UserResponse(user));

			if (service.persist(new User(login, request.getName(), request.getEmail(), request.getState()))) {
				if (request.getPassword() != null)
					service.persist(new Password(login, request.getPassword()));

				user = service.getUser(login);

				return user == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
						: ResponseEntity.ok().body(new UserResponse(user));
			} else
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates the user and returns it in the response body.
	 * 
	 * @param request The user request.
	 * @return The updated user in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the user and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Updated User", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = UserResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(userRequestMapping + updateRequestMapping)
	public ResponseEntity<UserResponse> userUpdate(@RequestBody @Valid UserRequest request) {
		String login = SecurityEntity.filter(request.getLogin());
		if (login == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

		try {
			if (!service.isUserAvailable(login))
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

			if (service.persist(new User(login, request.getName(), request.getEmail(), request.getState()))) {
				if (request.getPassword() != null)
					service.persist(new Password(login, request.getPassword()));

				User user = service.getUser(login);

				return user == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
						: ResponseEntity.ok().body(new UserResponse(user));
			} else
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates the user groups and returns the user with his groups in the response
	 * body.
	 * 
	 * @param request The user group request.
	 * @return The user with updated groups in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the user groups and returns the user with his groups in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Updated User Groups", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = UserGroupResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(userRequestMapping + updateRequestMapping + groupRequestMapping)
	public ResponseEntity<UserGroupResponse> userGroupUpdate(@RequestBody @Valid UserGroupRequest request) {
		String login = SecurityEntity.filter(request.getLogin());
		if (login == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

		try {
			User user = service.getUser(login);
			if (user == null)
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

			if (service.setGroups(user, request.getGroups())) {
				user = service.getUser(login);

				return user == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
						: ResponseEntity.ok().body(new UserGroupResponse(user, service.getGroups()));
			} else
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Removes the user and returns it in the response body.
	 * 
	 * @param login The user login.
	 * @return The removed user in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "removes the user and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Removed User", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = UserResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(userRequestMapping + removeRequestMapping)
	public ResponseEntity<UserResponse> userRemove(
			@Parameter(description = "the user login") @RequestParam String login) {
		login = SecurityEntity.filter(login);
		if (login == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

		try {
			User user = service.getUser(login);
			if (user == null)
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

			if (service.remove(user)) {
				service.remove(new Password(login));
				service.resetGroups(user);

				return ResponseEntity.ok().body(new UserResponse(user));
			} else
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the group in the response body.
	 * 
	 * @param label The group label.
	 * @return The group in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the group in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Group", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = GroupResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(groupRequestMapping + entityRequestMapping)
	public ResponseEntity<GroupResponse> groupEntity(@RequestParam String label) {
		try {
			Group group = service.getGroup(label);

			return group == null ? ResponseEntity.status(HttpStatus.NOT_FOUND).build()
					: ResponseEntity.ok().body(new GroupResponse(group));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the groups sorted by label in the response body.
	 * 
	 * @return The groups sorted by label in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the groups sorted by label in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Groups", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = GroupResponse.class))) }),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(groupRequestMapping + listRequestMapping)
	public ResponseEntity<List<GroupResponse>> groupList() {
		try {
			List<GroupResponse> groups = new ArrayList<>();

			for (Group group : service.getGroups())
				groups.add(new GroupResponse(group));

			return ResponseEntity.ok().body(groups);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Creates the group and returns it in the response body. If the group is
	 * available, it will be returned and not recreated.
	 * 
	 * @param request The group request.
	 * @return The created group in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "creates the group and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Creatred Group", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = GroupResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "409", description = "Conflict", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(groupRequestMapping + createRequestMapping)
	public ResponseEntity<GroupResponse> groupCreate(
			@Parameter(description = "the group") @RequestBody @Valid GroupRequest request) {
		String label = SecurityEntity.filter(request.getLabel());
		if (label == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

		try {
			Group group = service.getGroup(label);
			if (group != null)
				return ResponseEntity.status(HttpStatus.CONFLICT).body(new GroupResponse(group));

			if (service.persist(new Group(label, request.getName(), request.getUsers(), request.getState()))) {
				group = service.getGroup(label);

				return group == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
						: ResponseEntity.ok().body(new GroupResponse(group));
			} else
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates the group and returns it in the response body.
	 * 
	 * @param request The group request.
	 * @return The updated group in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the group and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Updated Group", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = GroupResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(groupRequestMapping + updateRequestMapping)
	public ResponseEntity<GroupResponse> groupUpdate(@RequestBody @Valid GroupRequest request) {
		String label = SecurityEntity.filter(request.getLabel());
		if (label == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

		try {
			if (!service.isGroupAvailable(label))
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

			if (service.persist(new Group(label, request.getName(), request.getUsers(), request.getState()))) {
				Group group = service.getGroup(label);

				return group == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
						: ResponseEntity.ok().body(new GroupResponse(group));
			} else
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Removes the group and returns it in the response body.
	 * 
	 * @param label The group label.
	 * @return The removed group in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "removes the group and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Removed Group", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = GroupResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(groupRequestMapping + removeRequestMapping)
	public ResponseEntity<GroupResponse> groupRemove(
			@Parameter(description = "the group label") @RequestParam String label) {
		label = SecurityEntity.filter(label);
		if (label == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

		try {
			Group group = service.getGroup(label);

			return group == null ? ResponseEntity.status(HttpStatus.NOT_FOUND).build()
					: (service.remove(group) ? ResponseEntity.ok().body(new GroupResponse(group))
							: ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines user cores for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class UserCore implements Serializable {
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
		 * Default constructor for a user core for the api.
		 * 
		 * @since 1.8
		 */
		public UserCore() {
			super();
		}

		/**
		 * Creates a user core for the api.
		 * 
		 * @param user The user.
		 * @since 1.8
		 */
		public UserCore(User user) {
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
	 * Defines user responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class UserResponse extends UserCore {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a user response for the api.
		 * 
		 * @param user The user.
		 * @since 1.8
		 */
		public UserResponse(User user) {
			super(user);
		}
	}

	/**
	 * Defines user with group requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class UserGroupResponse extends UserResponse {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The groups.
		 */
		private List<UserGroup> groups;

		/**
		 * Creates a user response for the api.
		 * 
		 * @param user   The user.
		 * @param groups The groups.
		 * @since 1.8
		 */
		public UserGroupResponse(User user, List<Group> groups) {
			super(user);

			this.groups = new ArrayList<>();
			for (Group group : groups)
				if (group.getUsers().contains(user.getLogin()))
					this.groups.add(new UserGroup(group));
		}

		/**
		 * Returns the groups.
		 *
		 * @return The groups.
		 * @since 1.8
		 */
		public List<UserGroup> getGroups() {
			return groups;
		}

		/**
		 * Set the groups.
		 *
		 * @param groups The groups to set.
		 * @since 1.8
		 */
		public void setGroups(List<UserGroup> groups) {
			this.groups = groups;
		}

		/**
		 * Defines user groups.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class UserGroup implements Serializable {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The label.
			 */
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
			 * Creates a user group.
			 * 
			 * @param group The group
			 * @since 1.8
			 */
			public UserGroup(Group group) {
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

	/**
	 * Defines user requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class UserRequest extends UserCore {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The password.
		 */
		private String password;

		/**
		 * Default constructor for a user request for the api.
		 * 
		 * @since 1.8
		 */
		public UserRequest() {
			super();
		}

		/**
		 * Returns the password.
		 *
		 * @return The password.
		 * @since 1.8
		 */
		public String getPassword() {
			return password;
		}

		/**
		 * Set the password.
		 *
		 * @param password The password to set.
		 * @since 1.8
		 */
		public void setPassword(String password) {
			this.password = password;
		}
	}

	/**
	 * Defines user group request for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class UserGroupRequest implements Serializable {
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
		 * The groups.
		 */
		private Set<String> groups;

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
		 * Returns the groups.
		 *
		 * @return The groups.
		 * @since 1.8
		 */
		public Set<String> getGroups() {
			return groups;
		}

		/**
		 * Set the groups.
		 *
		 * @param groups The groups to set.
		 * @since 1.8
		 */
		public void setGroups(Set<String> groups) {
			this.groups = groups;
		}
	}

	/**
	 * Defines group cores for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class GroupCore implements Serializable {
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
		 * Default constructor for a group core for the api.
		 * 
		 * @since 1.8
		 */
		public GroupCore() {
			super();
		}

		/**
		 * Creates a group core for the api.
		 * 
		 * @param group The group.
		 * @since 1.8
		 */
		public GroupCore(Group group) {
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

	/**
	 * Defines group responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public class GroupResponse extends GroupCore {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The users.
		 */
		private List<GroupUser> users;

		/**
		 * Creates a group response for the api.
		 * 
		 * @param group The group.
		 * @since 1.8
		 */
		public GroupResponse(Group group) {
			super(group);

			users = new ArrayList<>();
			for (String login : group.getUsers())
				users.add(new GroupUser(login));

			Collections.sort(users, (o1, o2) -> o1.getLogin().compareToIgnoreCase(o2.getLogin()));
		}

		/**
		 * Returns the users.
		 *
		 * @return The users.
		 * @since 1.8
		 */
		public List<GroupUser> getUsers() {
			return users;
		}

		/**
		 * Set the users.
		 *
		 * @param users The users to set.
		 * @since 1.8
		 */
		public void setUsers(List<GroupUser> users) {
			this.users = users;
		}

		/**
		 * Defines group users.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public class GroupUser implements Serializable {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The user login.
			 */
			private String login;

			/**
			 * The user state. Null if the user is unknown.
			 */
			private State state;

			/**
			 * Creates a group user.
			 * 
			 * @param login The user login.
			 * @since 1.8
			 */
			public GroupUser(String login) {
				super();

				this.login = login;

				final User user = service.getUser(login);
				state = user == null ? null : user.getState();
			}

			/**
			 * Returns the user login.
			 *
			 * @return The user login.
			 * @since 1.8
			 */
			public String getLogin() {
				return login;
			}

			/**
			 * Set the user login.
			 *
			 * @param login The login to set.
			 * @since 1.8
			 */
			public void setLogin(String login) {
				this.login = login;
			}

			/**
			 * Returns the user state.
			 *
			 * @return The user state. Null if the user is unknown.
			 * @since 1.8
			 */
			public State getState() {
				return state;
			}

			/**
			 * Set the user state. Null if the user is unknown.
			 *
			 * @param state The state to set.
			 * @since 1.8
			 */
			public void setState(State state) {
				this.state = state;
			}
		}
	}

	/**
	 * Defines group requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class GroupRequest extends GroupCore {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The user logins.
		 */
		private List<String> users;

		/**
		 * Default constructor for a group requests for the api.
		 * 
		 * @since 1.8
		 */
		public GroupRequest() {
			super();
		}

		/**
		 * Returns the users.
		 *
		 * @return The users.
		 * @since 1.8
		 */
		public List<String> getUsers() {
			return users;
		}

		/**
		 * Set the users.
		 *
		 * @param users The users to set.
		 * @since 1.8
		 */
		public void setUsers(List<String> users) {
			this.users = users;
		}

	}

}
