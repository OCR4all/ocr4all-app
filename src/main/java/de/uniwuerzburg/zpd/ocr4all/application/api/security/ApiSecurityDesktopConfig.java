/**
 * File:     ApiSecurityDesktopConfig.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.security
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     15.02.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Disables Spring Security for desktop profiles and defines CORS filters.
 * Authentication and any security protections like XSS are disabled.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@Profile("api & desktop")
@Configuration
public class ApiSecurityDesktopConfig extends ApiSecurityConfig {
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
		// enable CORS and disable CSRF
		http.cors(cors -> cors.configurationSource(corsConfigurationSource())).csrf(csrf -> csrf.disable());

		/*
		 * specify that all URLs are allowed by anyone
		 */
		http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

		return http.build();
	}

}
