/**
 * File:     AuthenticationApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     02.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.Serializable;
import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.api.security.AuthenticationPrincipal;
import de.uniwuerzburg.zpd.ocr4all.application.api.security.JwtTokenUtil;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.AccountService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.AccountService.Role;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Instance;

/**
 * Defines authentication controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api & server")
@Controller
@RequestMapping(path = AuthenticationApiController.contextPath, produces = CoreApiController.applicationJson)
public class AuthenticationApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = apiContextPathVersion_1_0 + "/login";

	/**
	 * The account service.
	 */
	private final AccountService accountService;

	/**
	 * The manager to process the authentication requests.
	 */
	private final AuthenticationManager authenticationManager;

	/**
	 * The JWT access token utilities.
	 */
	private final JwtTokenUtil jwtTokenUtil;

	/**
	 * Creates an authentication controller for the api.
	 * 
	 * @param configurationService  The configuration service.
	 * @param securityService       The security service.
	 * @param accountService        The account service.
	 * @param authenticationManager The manager to process the authentication
	 *                              requests.
	 * @param jwtTokenUtil          The JWT access token utilities.
	 * @since 1.8
	 */
	@Autowired
	public AuthenticationApiController(ConfigurationService configurationService, SecurityService securityService,
			AccountService accountService, AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil) {
		super(AuthenticationApiController.class, configurationService, securityService);

		this.accountService = accountService;
		this.authenticationManager = authenticationManager;
		this.jwtTokenUtil = jwtTokenUtil;
	}

	/**
	 * Authenticates the user and returns the authorization header of the response
	 * with the JWT access token along with the user identity information in the
	 * response body.
	 * 
	 * @param request The authentication request.
	 * @return The authorization header of the response with the JWT access token
	 *         along with the user identity information in the response body.
	 * @since 1.8
	 */
	@PostMapping
	public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid AuthenticationRequest request) {
		try {
			final Authentication authenticate = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

			final UserDetails userDetails = (UserDetails) authenticate.getPrincipal();
			final String token = jwtTokenUtil.generateToken(request.getUsername());

			return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, token).body(new AuthenticationResponse(
					userDetails, configurationService.getInstance(), jwtTokenUtil.getExpirationDate(token)));
		} catch (BadCredentialsException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the user identity information in the response body.
	 * 
	 * @return The user identity information in the response body.
	 * @since 1.8
	 */
	@GetMapping(informationRequestMapping)
	public ResponseEntity<AuthenticationResponse> information() {
		try {
			AuthenticationPrincipal principal = (AuthenticationPrincipal) accountService.getAuthentication()
					.getPrincipal();

			return ResponseEntity.ok().body(new AuthenticationResponse(principal.getUserDetails(),
					configurationService.getInstance(), jwtTokenUtil.getExpirationDate(principal.getJwtToken())));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines authentication requests.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class AuthenticationRequest implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The user name.
		 */
		@NotBlank
		private String username;

		/**
		 * The password.
		 */
		@NotNull
		private String password;

		/**
		 * Default constructor for an authentication request.
		 * 
		 * @since 1.8
		 */
		public AuthenticationRequest() {
			super();
		}

		/**
		 * Returns the user name.
		 *
		 * @return The user name.
		 * @since 1.8
		 */
		public String getUsername() {
			return username;
		}

		/**
		 * Set the user name.
		 *
		 * @param username The user name to set.
		 * @since 1.8
		 */
		public void setUsername(String username) {
			this.username = username;
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
	 * Defines authentication responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class AuthenticationResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The user name.
		 */
		private String username;

		/**
		 * The instance.
		 */
		private Instance instance = null;

		/**
		 * The authorities granted to the user.
		 */
		private String authority;

		/**
		 * The expiration date from the JWT access token.
		 */
		private Date expiration;

		/**
		 * Creates an authentication response for the api.
		 * 
		 * @param userDetails The core user information.
		 * @param instance    The instance.
		 * @param expiration  The expiration date from the JWT access token.
		 * @since 1.8
		 */
		public AuthenticationResponse(UserDetails userDetails, Instance instance, Date expiration) {
			super();

			this.username = userDetails.getUsername();
			this.instance = instance;
			this.expiration = expiration;

			boolean isCoordinator = false;
			boolean isAdministrator = false;
			for (GrantedAuthority authority : userDetails.getAuthorities()) {
				if (Role.COORD.isGrantedAuthority(authority.getAuthority()))
					isCoordinator = true;
				else if (Role.ADMIN.isGrantedAuthority(authority.getAuthority()))
					isAdministrator = true;
			}

			authority = isAdministrator ? "administrator" : (isCoordinator ? "coordinator" : "user");
		}

		/**
		 * Returns the user name.
		 *
		 * @return The user name.
		 * @since 1.8
		 */
		public String getUsername() {
			return username;
		}

		/**
		 * Set the user name.
		 *
		 * @param username The user name to set.
		 * @since 1.8
		 */
		public void setUsername(String username) {
			this.username = username;
		}

		/**
		 * Returns the instance name. Null if not defined.
		 *
		 * @return The instance name.
		 * @since 1.8
		 */
		public Instance getInstance() {
			return instance;
		}

		/**
		 * Set the instance name. Null if not defined.
		 *
		 * @param instance The instance name to set.
		 * @since 1.8
		 */
		public void setInstance(Instance instance) {
			this.instance = instance;
		}

		/**
		 * Returns the authority.
		 *
		 * @return The authority.
		 * @since 1.8
		 */
		public String getAuthority() {
			return authority;
		}

		/**
		 * Set the authority.
		 *
		 * @param authority The authority to set.
		 * @since 1.8
		 */
		public void setAuthority(String authority) {
			this.authority = authority;
		}

		/**
		 * Returns the expiration date from the JWT access token.
		 *
		 * @return The expiration date from the JWT access token.
		 * @since 1.8
		 */
		public Date getExpiration() {
			return expiration;
		}

		/**
		 * Set the expiration date from the JWT access token.
		 *
		 * @param expiration The expiration date to set.
		 * @since 1.8
		 */
		public void setExpiration(Date expiration) {
			this.expiration = expiration;
		}

	}

}
