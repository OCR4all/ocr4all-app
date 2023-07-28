/**
 * File:     SecurityDesktopConfig.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.security
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     09.11.2020
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.security;

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

}
