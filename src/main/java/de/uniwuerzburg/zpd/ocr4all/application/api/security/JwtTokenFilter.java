/**
 * File:     JwtTokenFilter.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.security
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     01.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Defines JWT access token filters.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api & server")
@Component
public class JwtTokenFilter extends OncePerRequestFilter {
	/**
	 * The JWT request header prefix.
	 */
	private static final String jwtRequestHeaderPrefix = "Bearer ";

	/**
	 * The JWT access token utilities.
	 */
	private final JwtTokenUtil jwtTokenUtil;

	/**
	 * Creates a JWT access token filter.
	 * 
	 * @param jwtTokenUtil The JWT access token utilities.
	 * @since 1.8
	 */
	@Autowired
	public JwtTokenFilter(JwtTokenUtil jwtTokenUtil) {
		super();

		this.jwtTokenUtil = jwtTokenUtil;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(javax.
	 * servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
	 * javax.servlet.FilterChain)
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		// Get authorization header and validate
		final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith(jwtRequestHeaderPrefix)) {
			chain.doFilter(request, response);

			return;
		}

		// Get JWT token and validate
		final String token = header.substring(jwtRequestHeaderPrefix.length());
		final UserDetails userDetails = jwtTokenUtil.validate(token);
		if (userDetails == null) {
			chain.doFilter(request, response);

			return;
		}

		// Set the user identity on the spring security context
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				new AuthenticationPrincipal(userDetails, token), null, userDetails.getAuthorities());

		authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(request, response);
	}

}
