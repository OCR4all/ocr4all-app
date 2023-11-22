/**
 * File:     ContainerConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.repository
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.11.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.repository;

import java.nio.file.Path;
import java.nio.file.Paths;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.CoreFolder;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.repository.Container;

/**
 * Defines configurations for the container.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class ContainerConfiguration extends CoreFolder {

	/**
	 * The configuration.
	 */
	private final Configuration configuration;

	/**
	 * Creates a configuration for the container.
	 * 
	 * @param properties The ocr4all properties.
	 * @since 1.8
	 */
	public ContainerConfiguration(Container properties, Path folder) {
		super(folder);

		configuration = new Configuration(properties.getConfiguration());
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
		 * The folio configuration file.
		 */
		private final Path folioFile;

		/**
		 * Creates a configuration for the repository.
		 * 
		 * @param properties The configuration properties for the repository.
		 * @since 1.8
		 */
		public Configuration(Container.Configuration properties) {
			super(Paths.get(ContainerConfiguration.this.folder.toString(), properties.getFolder()));

			// Initialize the repository configuration folder and consequently the
			// repository
			ConfigurationService.initializeFolder(true, folder, "repository configuration");

			// Initializes the configuration files
			mainFile = getPath(properties.getFiles().getMain());
			folioFile = getPath(properties.getFiles().getFolio());
		}

		/**
		 * Returns the main configuration file.
		 *
		 * @return The main configuration file.
		 * @since 1.8
		 */
		public Path getMainFile() {
			return mainFile;
		}

		/**
		 * Returns the folio configuration file.
		 *
		 * @return The folio configuration file.
		 * @since 1.8
		 */
		public Path getFolioFile() {
			return folioFile;
		}

	}
}
