/**
 * File:     SecurityDesktopConfig.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.security
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     09.11.2020
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.security;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Defines security configurations.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public abstract class SecurityConfig {
	/**
	 * The ant pattern to match root directory.
	 */
	public static final String patternMatchRootDirectory = "/";

	/**
	 * The ant pattern to match zero or more directories in a path.
	 */
	public static final String patternMatchZeroMoreDirectories = "/**";

	/**
	 * Returns the ant pattern to match zero or more directories in given path.
	 * 
	 * @param path The path.
	 * @return The ant pattern to match zero or more directories in given path.
	 * @since 1.8
	 */
	public static String matchAll(String path) {
		return path + patternMatchZeroMoreDirectories;
	}

	/**
	 * Defines a CORS configuration that allows requests for any origin by default.
	 * Used by spring security if CORS is enabled.
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

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration(patternMatchZeroMoreDirectories, config);

		return source;
	}

}
