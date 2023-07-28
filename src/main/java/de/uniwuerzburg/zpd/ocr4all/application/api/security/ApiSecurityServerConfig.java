/**
 * File:     ApiSecurityServerConfig.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.security
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     01.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import de.uniwuerzburg.zpd.ocr4all.application.api.documentation.ApiDocumentationConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.api.worker.AdministrationApiController;
import de.uniwuerzburg.zpd.ocr4all.application.api.worker.AdministrationSecurityApiController;
import de.uniwuerzburg.zpd.ocr4all.application.api.worker.AuthenticationApiController;
import de.uniwuerzburg.zpd.ocr4all.application.api.worker.CoreApiController;
import de.uniwuerzburg.zpd.ocr4all.application.api.worker.InstanceApiController;
import de.uniwuerzburg.zpd.ocr4all.application.api.worker.JobApiController;
import de.uniwuerzburg.zpd.ocr4all.application.api.worker.ProjectApiController;
import de.uniwuerzburg.zpd.ocr4all.application.api.worker.ProjectSecurityApiController;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.AccountService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityConfig;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Defines security configurations for api server profiles.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@Configuration
@Profile("api & server")
public class ApiSecurityServerConfig extends SecurityConfig {
	/**
	 * The logger.
	 */
	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ApiSecurityServerConfig.class);

	/**
	 * The account service.
	 */
	private final AccountService accountService;

	/**
	 * The JWT access token filter.
	 */
	private final JwtTokenFilter jwtTokenFilter;

	/**
	 * Creates a security configuration for an api server profile.
	 * 
	 * @param accountService The account service.
	 * @param jwtTokenFilter The JWT access token filter.
	 * @since 17
	 */
	public ApiSecurityServerConfig(AccountService accountService, JwtTokenFilter jwtTokenFilter) {
		super();

		this.accountService = accountService;
		this.jwtTokenFilter = jwtTokenFilter;
	}

	/**
	 * Returns the {@link AuthenticationProvider} implementation that retrieves user
	 * details from the {@link UserDetailsService}.
	 * 
	 * @return The {@link AuthenticationProvider} implementation that retrieves user
	 *         details from the {@link UserDetailsService}.
	 * @since 17
	 */
	@Bean
	DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider auth = new DaoAuthenticationProvider();

		auth.setUserDetailsService(accountService);
		auth.setPasswordEncoder(accountService.getPasswordEncoder());

		return auth;
	}

	/**
	 * Configures the {@code FilterChainProxy}.
	 * 
	 * @param http The {@link HttpSecurity} is similar to Spring Security's XML
	 *             &lt;http&gt; element in the namespace configuration. It allows
	 *             configuring web based security for specific http requests. By
	 *             default it will be applied to all requests, but can be restricted
	 *             using {@link #requestMatcher(RequestMatcher)} or other similar
	 *             methods.
	 * @return The filter chain which is capable of being matched against an
	 *         {@code HttpServletRequest} in order to decide whether it applies to
	 *         that request.
	 * @throws Exception Throws on filter chain exceptions.
	 * @since 17
	 */
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		// Enable CORS and disable CSRF
		http.csrf(csrf -> csrf.disable());

		// Set session management to state less
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// Set unauthorized requests exception handler
		http.exceptionHandling(exception -> exception.authenticationEntryPoint((request, response, ex) -> {
			logger.error("Unauthorized request - {}", ex.getMessage());
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
		}));

		/*
		 * Set permissions on end points
		 */
		http.authorizeHttpRequests(auth -> auth
				/*
				 * The public end points
				 */
				// instance
				.requestMatchers(HttpMethod.GET, InstanceApiController.contextPath).permitAll()

				// login
				.requestMatchers(HttpMethod.POST, AuthenticationApiController.contextPath).permitAll()

				// RESTful web API documentation
				.requestMatchers(HttpMethod.GET, matchAll(ApiDocumentationConfiguration.contextPath)).permitAll()

				/*
				 * The private end points
				 */
				// administration
				.requestMatchers(HttpMethod.GET,
						AdministrationApiController.contextPath + CoreApiController.overviewRequestMapping,
						AdministrationApiController.contextPath + CoreApiController.providerRequestMapping
								+ CoreApiController.overviewRequestMapping,
						AdministrationSecurityApiController.contextPath
								+ AdministrationSecurityApiController.userRequestMapping
								+ CoreApiController.entityRequestMapping,
						AdministrationSecurityApiController.contextPath
								+ AdministrationSecurityApiController.userRequestMapping
								+ CoreApiController.listRequestMapping,
						AdministrationSecurityApiController.contextPath
								+ AdministrationSecurityApiController.groupRequestMapping
								+ CoreApiController.entityRequestMapping,
						AdministrationSecurityApiController.contextPath
								+ AdministrationSecurityApiController.groupRequestMapping
								+ CoreApiController.listRequestMapping)
				.hasRole(AccountService.Role.COORD.name())

				.requestMatchers(matchAll(AdministrationApiController.contextPath))
				.hasRole(AccountService.Role.ADMIN.name())

				// job
				.requestMatchers(HttpMethod.GET,
						JobApiController.contextPath + JobApiController.schedulerInformationRequestMapping)
				.hasRole(AccountService.Role.COORD.name())

				.requestMatchers(HttpMethod.GET,
						matchAll(JobApiController.contextPath + JobApiController.schedulerActionRequestMapping))
				.hasRole(AccountService.Role.ADMIN.name())

				// project
				.requestMatchers(HttpMethod.GET,
						ProjectApiController.contextPath + CoreApiController.createRequestMapping,
						ProjectApiController.contextPath + CoreApiController.removeRequestMapping)
				.hasRole(AccountService.Role.COORD.name())

				.requestMatchers(HttpMethod.POST,
						ProjectApiController.contextPath + CoreApiController.updateRequestMapping)
				.hasRole(AccountService.Role.COORD.name())

				.requestMatchers(matchAll(ProjectSecurityApiController.contextPath))
				.hasRole(AccountService.Role.COORD.name())

				// remainder
				.anyRequest().hasRole(AccountService.Role.USER.name()));

		// The authentication provider
		http.authenticationProvider(authenticationProvider());

		// Add JWT token filter
		http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * Defines a cross-origin filter that allows requests for any origin by default.
	 * Used by spring security if CORS is enabled.
	 * 
	 * @return The CORS filter that allows requests for any origin by default.
	 * @since 17
	 */
//	@Bean
	CorsFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration(patternMatchZeroMoreDirectories, config);

		return new CorsFilter(source);
	}

	/**
	 * Returns the manager that processes an {@link Authentication} request.
	 * 
	 * @param authConfiguration The authentication {@link Configuration}.
	 * @return The manager that processes an {@link Authentication} request.
	 * @throws Exception Throws on authentication manager exceptions.
	 * @since 17
	 */
	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authConfiguration) throws Exception {
		return authConfiguration.getAuthenticationManager();
	}

}
