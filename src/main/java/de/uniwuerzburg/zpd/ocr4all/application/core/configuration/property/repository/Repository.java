/**
 * File:     Repository.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.repository
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.11.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.repository;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.OCR4all;
import jakarta.validation.constraints.NotEmpty;

/**
 * Defines ocr4all repository properties.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Repository {
	/**
	 * The folder.
	 */
	@NotEmpty(message = "the ocr4all repository folder cannot be null nor empty")
	private String folder;

	/**
	 * The configuration.
	 */
	private Configuration configuration = new Configuration();

	/**
	 * The container.
	 */
	private Container container = new Container();

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
	 * Returns the configuration.
	 *
	 * @return The configuration.
	 * @since 1.8
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Set the configuration.
	 *
	 * @param configuration The configuration to set.
	 * @since 1.8
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Returns the container.
	 *
	 * @return The container.
	 * @since 1.8
	 */
	public Container getContainer() {
		return container;
	}

	/**
	 * Set the container.
	 *
	 * @param container The container to set.
	 * @since 1.8
	 */
	public void setContainer(Container container) {
		this.container = container;
	}

	/**
	 * Defines configuration properties for ocr4all repository.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Configuration {
		/**
		 * The default folder.
		 */
		private static final String defaultFolder = ".repository";

		/**
		 * The folder. The default value is .repository.
		 */
		private String folder = defaultFolder;

		/**
		 * The files.
		 */
		private Files files = new Files();

		/**
		 * Returns the folder.
		 *
		 * @return The folder.
		 * @since 1.8
		 */
		public String getFolder() {
			return OCR4all.getNotEmpty(folder, defaultFolder);
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
		 * Returns the files.
		 *
		 * @return The files.
		 * @since 1.8
		 */
		public Files getFiles() {
			return files;
		}

		/**
		 * Set the files.
		 *
		 * @param files The files to set.
		 * @since 1.8
		 */
		public void setFiles(Files files) {
			this.files = files;
		}
	}

	/**
	 * Defines files.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Files {
		/**
		 * The default main file name.
		 */
		private static final String defaultMainFileName = "repository";

		/**
		 * The main file name. The default value is repository.
		 */
		private String main = defaultMainFileName;

		/**
		 * Returns the main file name.
		 *
		 * @return The main file name.
		 * @since 1.8
		 */
		public String getMain() {
			return OCR4all.getNotEmpty(main, defaultMainFileName);
		}

		/**
		 * Set the main file name.
		 *
		 * @param fileName The file name to set.
		 * @since 1.8
		 */
		public void setMain(String fileName) {
			main = fileName;
		}

	}

}
