/**
 * File:     ApiSecurityServerConfig.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.security
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     01.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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

/**
 * Defines security configurations for api server profiles.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Configuration
@Profile("api & server")
@EnableWebSecurity
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
	 * @since 1.8
	 */
	public ApiSecurityServerConfig(AccountService accountService, JwtTokenFilter jwtTokenFilter) {
		super();

		this.accountService = accountService;
		this.jwtTokenFilter = jwtTokenFilter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.config.annotation.web.configuration.
	 * WebSecurityConfigurerAdapter#configure(org.springframework.security.config.
	 * annotation.authentication.builders.AuthenticationManagerBuilder)
	 */
	/*~~(Migrate manually based on https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter)~~>*/@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(accountService).passwordEncoder(accountService.getPasswordEncoder());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.config.annotation.web.configuration.
	 * WebSecurityConfigurerAdapter#configure(org.springframework.security.config.
	 * annotation.web.builders.HttpSecurity)
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// Enable CORS and disable CSRF
		http = http.cors().and().csrf().disable();

		// Set session management to stateless
		http = http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and();

		// Set unauthorized requests exception handler
		http = http.exceptionHandling().authenticationEntryPoint((request, response, ex) -> {
			logger.error("Unauthorized request - {}", ex.getMessage());
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
		}).and();

		/*
		 * Set permissions on end points
		 */
		http.authorizeRequests()
				/*
				 * The public end points
				 */
				// instance
				.antMatchers(HttpMethod.GET, InstanceApiController.contextPath).permitAll()

				// login
				.antMatchers(HttpMethod.POST, AuthenticationApiController.contextPath).permitAll()

				// RESTful web API documentation
				.antMatchers(HttpMethod.GET, matchAll(ApiDocumentationConfiguration.contextPath)).permitAll()

				/*
				 * The private end points
				 */
				// administration
				.antMatchers(HttpMethod.GET,
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

				.antMatchers(matchAll(AdministrationApiController.contextPath))
				.hasRole(AccountService.Role.ADMIN.name())

				// job
				.antMatchers(HttpMethod.GET,
						JobApiController.contextPath + JobApiController.schedulerInformationRequestMapping)
				.hasRole(AccountService.Role.COORD.name())

				.antMatchers(HttpMethod.GET,
						matchAll(JobApiController.contextPath + JobApiController.schedulerActionRequestMapping))
				.hasRole(AccountService.Role.ADMIN.name())

				// project
				.antMatchers(HttpMethod.GET, ProjectApiController.contextPath + CoreApiController.createRequestMapping,
						ProjectApiController.contextPath + CoreApiController.removeRequestMapping)
				.hasRole(AccountService.Role.COORD.name())

				.antMatchers(HttpMethod.POST, ProjectApiController.contextPath + CoreApiController.updateRequestMapping)
				.hasRole(AccountService.Role.COORD.name())

				.antMatchers(matchAll(ProjectSecurityApiController.contextPath))
				.hasRole(AccountService.Role.COORD.name())

				// remainder
				.anyRequest().hasRole(AccountService.Role.USER.name());

		// Add JWT token filter
		http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
	}

	/**
	 * Defines a cross-origin filter that allows requests for any origin by default.
	 * Used by spring security if CORS is enabled.
	 * 
	 * @return The CORS filter that allows requests for any origin by default.
	 * @since 1.8
	 */
	@Bean
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.config.annotation.web.configuration.
	 * WebSecurityConfigurerAdapter#authenticationManagerBean()
	 */
	/*~~(Migrate manually based on https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter)~~>*/@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

}
