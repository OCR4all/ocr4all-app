/**
 * File:     SecurityEntity.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.security
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     09.06.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.security;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.WorkspaceConfiguration;

/**
 * Defines security entities.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public abstract class SecurityEntity<T> {
	/**
	 * The configuration separator.
	 */
	public static final String configurationSeparator = ":";

	/**
	 * The element separator.
	 */
	public static final String elementSeparator = ",";

	/**
	 * Default configuration for a security entity.
	 * 
	 * @since 1.8
	 */
	public SecurityEntity() {
		super();
	}

	/**
	 * Return true if the current and given entity are the same.
	 * 
	 * @param entity The entity to test.
	 * @return True if the current and given entity are the same.
	 * @since 1.8
	 */
	public abstract boolean isSame(T entity);

	/**
	 * Returns the configuration entry.
	 * 
	 * @param version The configuration entry.
	 * @return The configuration entry.
	 * @since 1.8
	 */
	public abstract String getConfigurationEntry(WorkspaceConfiguration.Version version);

	/**
	 * Filter the fields and remove the separator if necessary from a configuration
	 * entry.
	 * 
	 * @param field The field to filter.
	 * @return The filtered field.
	 * @since 1.8
	 */
	public static String filter(String field) {
		if (field != null)
			field = field.replace(configurationSeparator, "").replace(elementSeparator, "");

		return field == null || field.isBlank() ? null : field.trim();
	}

}
