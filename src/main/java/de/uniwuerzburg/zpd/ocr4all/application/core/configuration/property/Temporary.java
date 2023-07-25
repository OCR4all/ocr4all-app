/**
 * File:     Temporary.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     31.03.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property;

import jakarta.validation.constraints.NotEmpty;

/**
 * Defines ocr4all temporary properties.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Temporary {
	/**
	 * The default prefix.
	 */
	private static final String defaultPrefix = "ocr4all-";

	/**
	 * The folder.
	 */
	@NotEmpty(message = "the ocr4all temporary folder cannot be null nor empty")
	private String folder;

	/**
	 * The prefix. The default value is 'ocr4all-'.
	 */
	private String prefix = defaultPrefix;

	/**
	 * Returns the folder.
	 *
	 * @return The folder.
	 * @since 1.8
	 */
	public String getFolder() {
		return folder;
	}

	/**
	 * Set the folder.
	 *
	 * @param folder The folder to set.
	 * @since 1.8
	 */
	public void setFolder(String folder) {
		this.folder = folder;
	}

	/**
	 * Returns the prefix.
	 *
	 * @return The prefix.
	 * @since 1.8
	 */
	public String getPrefix() {
		return OCR4all.getNotEmpty(prefix, defaultPrefix);
	}

	/**
	 * Set the prefix.
	 *
	 * @param prefix The prefix to set.
	 * @since 1.8
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

}
