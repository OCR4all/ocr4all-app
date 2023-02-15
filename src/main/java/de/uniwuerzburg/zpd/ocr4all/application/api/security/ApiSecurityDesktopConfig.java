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
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityConfig;

/**
 * Disables Spring Security for desktop profiles and defines CORS filters.
 * Authentication and any security protections like XSS are disabled.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api & desktop")
@Configuration
public class ApiSecurityDesktopConfig extends SecurityConfig {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.config.annotation.web.configuration.
	 * WebSecurityConfigurerAdapter#configure(org.springframework.security.config.
	 * annotation.web.builders.HttpSecurity)
	 */
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers(patternMatchZeroMoreDirectories);
	}

	/**
	 * Defines a cross-origin filter that allows requests for any origin by default.
	 * Used by spring security if CORS is enabled.
	 * 
	 * @return The CORS filter that allows requests for any origin by default.
	 * @since 1.8
	 */
	@Bean
	public CorsFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration(patternMatchZeroMoreDirectories, config);

		return new CorsFilter(source);
	}

}
