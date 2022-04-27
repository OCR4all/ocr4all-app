/**
 * File:     AuthenticationPrincipal.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.security
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     03.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.security;

import org.springframework.security.core.userdetails.UserDetails;

import de.uniwuerzburg.zpd.ocr4all.application.core.security.AccountService;

/**
 * AuthenticationPrincipal is an immutable class that defines authentication
 * principals.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class AuthenticationPrincipal implements AccountService.Credential {
	/**
	 * The core user information.
	 */
	private final UserDetails userDetails;

	/**
	 * The JWT authentication token.
	 */
	private final String jwtToken;

	/**
	 * Creates an authentication principal.
	 * 
	 * @param userDetails The core user information.
	 * @param jwtToken    The JWT authentication token.
	 * @since 1.8
	 */
	public AuthenticationPrincipal(UserDetails userDetails, String jwtToken) {
		super();

		this.userDetails = userDetails;
		this.jwtToken = jwtToken;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.security.AccountService.
	 * Credential#getUsername()
	 */
	@Override
	public String getUsername() {
		return userDetails.getUsername();
	}

	/**
	 * Returns the core user information.
	 *
	 * @return The core user information.
	 * @since 1.8
	 */
	public UserDetails getUserDetails() {
		return userDetails;
	}

	/**
	 * Returns the JWT authentication token.
	 *
	 * @return The JWT authentication token.
	 * @since 1.8
	 */
	public String getJwtToken() {
		return jwtToken;
	}

}
