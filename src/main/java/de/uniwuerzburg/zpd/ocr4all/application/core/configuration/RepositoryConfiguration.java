/**
 * File:     RepositoryConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.11.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration;

import java.nio.file.Paths;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.OCR4all;

/**
 * Defines configurations for the repository.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class RepositoryConfiguration extends CoreFolder {

	/**
	 * Creates a configuration for the repository.
	 * 
	 * @param properties The ocr4all properties.
	 * @since 1.8
	 */
	public RepositoryConfiguration(OCR4all properties) {
		super(Paths.get(properties.getRepository().getFolder()));

		ConfigurationService.initializeFolder(true, folder, "repository");
	}

}
