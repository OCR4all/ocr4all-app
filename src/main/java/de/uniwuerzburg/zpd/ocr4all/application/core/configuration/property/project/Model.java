/**
 * File:     Model.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.03.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project;

/**
 * Defines ocr4all model properties.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Model {
	/**
	 * The default model file name.
	 */
	public static final String defaultModelFileName = "model";

	/**
	 * The configuration.
	 */
	private Configuration configuration = new Configuration();

	/**
	 * The model file name. The default value is model.
	 */
	private String file = defaultModelFileName;

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
	 * Returns the model file name.
	 *
	 * @return The model file name.
	 * @since 1.8
	 */
	public String getFile() {
		return file;
	}

	/**
	 * Set the model file name.
	 *
	 * @param fileName The file name to set.
	 * @since 1.8
	 */
	public void setFile(String fileName) {
		file = fileName;
	}

	/**
	 * Defines configurations.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Configuration {
		/**
		 * The default folder.
		 */
		public static final String defaultFolder = ".model";

		/**
		 * The folder. The default value is .model.
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
		public static final String defaultMainFileName = "model";

		/**
		 * The default assembly file name.
		 */
		public static final String defaultAssemblyFileName = "assembly";

		/**
		 * The main file name. The default value is model.
		 */
		private String main = defaultMainFileName;

		/**
		 * The assembly file name. The default value is assembly.
		 */
		private String assembly = defaultAssemblyFileName;

		/**
		 * Returns the main file name.
		 *
		 * @return The main file name.
		 * @since 1.8
		 */
		public String getMain() {
			return main;
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

		/**
		 * Returns the assembly file name.
		 *
		 * @return The assembly file name.
		 * @since 1.8
		 */
		public String getAssembly() {
			return assembly;
		}

		/**
		 * Set the assembly file name.
		 *
		 * @param fileName The file name to set.
		 * @since 1.8
		 */
		public void setAssembly(String fileName) {
			assembly = fileName;
		}
	}
}
