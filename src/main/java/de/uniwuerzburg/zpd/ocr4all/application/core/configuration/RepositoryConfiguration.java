/**
 * File:     RepositoryConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.11.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.OCR4all;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.repository.Repository;

/**
 * Defines configurations for the repository.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class RepositoryConfiguration extends CoreFolder {

	/**
	 * The configuration.
	 */
	private final Configuration configuration;

	/**
	 * Creates a configuration for the repository.
	 * 
	 * @param properties The ocr4all properties.
	 * @since 1.8
	 */
	public RepositoryConfiguration(OCR4all properties) {
		super(Paths.get(properties.getRepository().getFolder()));

		configuration = new Configuration(properties.getRepository().getConfiguration());
	}

	/**
	 * Returns the configuration.
	 *
	 * @return The configuration.
	 * @since 1.8
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Defines configurations for the repository.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public class Configuration extends CoreFolder {
		/**
		 * The main configuration file.
		 */
		private final Path mainFile;

		/**
		 * Creates a configuration for the repository.
		 * 
		 * @param properties The configuration properties for the repository.
		 * @since 1.8
		 */
		public Configuration(Repository.Configuration properties) {
			super(Paths.get(RepositoryConfiguration.this.folder.toString(), properties.getFolder()));

			// Initialize the repository configuration folder and consequently the
			// repository
			ConfigurationService.initializeFolder(true, folder, "repository configuration");

			// Initializes the configuration files
			mainFile = getPath(properties.getFiles().getMain());
		}

	}
}
