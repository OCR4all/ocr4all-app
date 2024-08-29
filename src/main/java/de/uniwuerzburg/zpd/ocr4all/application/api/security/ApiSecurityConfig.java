/**
 * File:     ApiSecurityConfig.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.security
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     31.07.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.security;

import java.util.Arrays;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityConfig;

/**
 * Defines security configurations for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public class ApiSecurityConfig extends SecurityConfig {
	/**
	 * Defines a CORS configuration that allows requests for any origin by default.
	 * 
	 * @return The CORS configuration that allows requests for any origin by
	 *         default.
	 * @since 17
	 */
	protected CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");

		/*
		 * When allowPrivateNetwork is true, allowedOrigins cannot contain the special
		 * value "*" as it is not recommended from a security perspective. To allow
		 * private network access to a set of origins, list them explicitly or consider
		 * using "allowedOriginPatterns" instead.
		 */
		config.setAllowedOriginPatterns(Arrays.asList("http://localhost:[*]"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration(patternMatchZeroMoreDirectories, config);

		return source;
	}

}
