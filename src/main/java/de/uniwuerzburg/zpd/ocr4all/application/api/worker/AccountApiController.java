/**
 * File:     AuthenticationApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     02.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.ModelService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.AccountService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.Group;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.Password;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.State;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Defines account controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api & server")
@Tag(name = "account", description = "the account API")
@RestController
@RequestMapping(path = AccountApiController.contextPath, produces = CoreApiController.applicationJson)
public class AccountApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = apiContextPathVersion_1_0 + "/account";

	/**
	 * The password request mapping.
	 */
	public static final String passwordRequestMapping = "/password";

	/**
	 * The account service.
	 */
	private final AccountService accountService;

	/**
	 * Creates an account controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param collectionService    The collection service.
	 * @param modelService         The model service.
	 * @param accountService       The account service.
	 * @since 1.8
	 */
	public AccountApiController(ConfigurationService configurationService, SecurityService securityService,
			CollectionService collectionService, ModelService modelService, AccountService accountService) {
		super(AccountApiController.class, configurationService, securityService, collectionService, modelService);

		this.accountService = accountService;
	}

	/**
	 * Returns the account in the response body.
	 * 
	 * @return The account in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the account in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Available Account", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = AccountResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "Unknown Account", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping
	public ResponseEntity<AccountResponse> entity() {
		try {
			final String login = securityService.getUser();
			final User account = accountService.getUser(login);

			if (account == null)
				return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

			List<GroupResponse> groups = new ArrayList<>();
			for (Group group : accountService.getGroups())
				if (group.getUsers().contains(login))
					groups.add(new GroupResponse(group.getLabel(), group.getState()));

			return ResponseEntity.ok().body(new AccountResponse(account, groups));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates the password.
	 * 
	 * @param request  The placeholder request.
	 * @param response The HTTP-specific functionality in sending a response to the
	 *                 client.
	 * @since 1.8
	 */
	@Operation(summary = "Updates the password.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Updated Password"),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(passwordRequestMapping)
	public void passwordUpdate(@RequestBody @Valid PasswordRequest request, HttpServletResponse response) {
		try {
			accountService.persist(new Password(securityService.getUser(), request.getPassword()));

			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines account responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class AccountResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The login.
		 */
		private String login;

		/**
		 * The email.
		 */
		private String email;

		/**
		 * The name.
		 */
		private String name;

		/**
		 * The groups.
		 */
		private List<GroupResponse> groups;

		/**
		 * Creates an account response for the api.
		 * 
		 * @param user   The user.
		 * @param groups The groups.
		 * @since 1.8
		 */
		public AccountResponse(User user, List<GroupResponse> groups) {
			super();

			login = user.getLogin();
			email = user.getEmail();
			name = user.getName();

			this.groups = groups;
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

		/**
		 * Returns the groups.
		 *
		 * @return The groups.
		 * @since 1.8
		 */
		public List<GroupResponse> getGroups() {
			return groups;
		}

		/**
		 * Set the groups.
		 *
		 * @param groups The groups to set.
		 * @since 1.8
		 */
		public void setGroups(List<GroupResponse> groups) {
			this.groups = groups;
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
		 * The name.
		 */
		private String name = null;

		/**
		 * The state.
		 */
		private final State state;

		/**
		 * Creates a group response for the api.
		 * 
		 * @param name  The name.
		 * @param state The state.
		 * @since 1.8
		 */
		public GroupResponse(String name, State state) {
			super();

			this.name = name;
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

		/**
		 * Returns the state.
		 *
		 * @return The state.
		 * @since 1.8
		 */
		public State getState() {
			return state;
		}

	}

	/**
	 * Defines requests with passwords.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class PasswordRequest implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The password.
		 */
		@NotBlank
		private String password;

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

}
