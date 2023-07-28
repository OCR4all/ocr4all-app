/**
 * File:     JwtTokenUtil.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.security
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     01.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ApiConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.AccountService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;

/**
 * Defines JWT access token utilities.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@Profile("api & server")
@Component
public class JwtTokenUtil {
	/**
	 * The {@link SecretKey} instance suitable for use with the specified
	 * {@link SignatureAlgorithm} HS512.
	 */
	private static final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

	/**
	 * The logger.
	 */
	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JwtTokenUtil.class);

	/**
	 * The configuration for the api.
	 */
	private final ApiConfiguration.JWT configuration;

	/**
	 * The account service.
	 */
	private final AccountService accountService;

	/**
	 * Creates JWT access token utilities.
	 * 
	 * @param configurationService The configuration service.
	 * @param accountService       The account service.
	 * @since 17
	 */
	public JwtTokenUtil(ConfigurationService configurationService, AccountService accountService) {
		super();

		configuration = configurationService.getApi().getJwt();
		this.accountService = accountService;
	}

	/**
	 * Generates the JWT access token.
	 * 
	 * @param username The user name.
	 * @return The JWT access token.
	 * @since 17
	 */
	public String generateToken(String username) {
		final Date createdDate = new Date();
		final Date expirationDate = getExpiration(createdDate);

		return Jwts.builder().setSubject(username).setIssuer(configuration.getIssuer()).setIssuedAt(createdDate)
				.setExpiration(expirationDate).signWith(secretKey).compact();
//				.setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, configuration.getSecret()).compact();
	}

	/**
	 * Returns the JWT access token expiration timestamp for given date.
	 * 
	 * @param date The date to returns the expiration timestamp.
	 * @return The JWT access token expiration timestamp.
	 * @since 17
	 */
	private Date getExpiration(Date date) {
		return new Date(date.getTime() + configuration.getValidity());
	}

	/**
	 * Return the user name from the JWT access token.
	 * 
	 * @param token The JWT access token.
	 * @return The user name from the JWT access token.
	 * @since 17
	 */
	public String getUsername(String token) {
		return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
//		return Jwts.parserBuilder().setSigningKey(configuration.getSecret()).parseClaimsJws(token).getBody().getSubject();
	}

	/**
	 * Return the expiration date from the JWT access token.
	 * 
	 * @param token The JWT access token.
	 * @return The expiration date from the JWT access token.
	 * @since 17
	 */
	public Date getExpirationDate(String token) {
		return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getExpiration();
	}

	/**
	 * Validates the JWT access token.
	 * 
	 * @param token The JWT access token.
	 * @return The core user information. Null if the JWT access token is not valid.
	 * @since 17
	 */
	public UserDetails validate(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);

			return accountService.loadUserByUsername(getUsername(token));
		} catch (MalformedJwtException ex) {
			logger.warn("Invalid JWT token - {}", ex.getMessage());
		} catch (ExpiredJwtException ex) {
			logger.warn("Expired JWT token - {}", ex.getMessage());
		} catch (UnsupportedJwtException ex) {
			logger.warn("Unsupported JWT token - {}", ex.getMessage());
		} catch (IllegalArgumentException ex) {
			logger.warn("JWT claims string is empty - {}", ex.getMessage());
		} catch (UsernameNotFoundException ex) {
			logger.warn("Invalid JWT credentials - {}", ex.getMessage());
		} catch (Exception ex) {
			logger.warn("Unexpected JWT exception - {}", ex.getMessage());
		}

		return null;
	}
}
