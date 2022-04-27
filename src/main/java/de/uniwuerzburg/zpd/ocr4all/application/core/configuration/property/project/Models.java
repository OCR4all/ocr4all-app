/**
 * File:     Models.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project.projects
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.03.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project;

/**
 * Defines models.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Models {
	/**
	 * The default folder.
	 */
	public static final String defaultFolder = "models";

	/**
	 * The folder. The default value is models.
	 */
	private String folder = defaultFolder;

	/**
	 * The configuration.
	 */
	private Configuration configuration = new Configuration();

	/**
	 * The model.
	 */
	private Model model = new Model();

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
	 * Returns the model.
	 *
	 * @return The model.
	 * @since 1.8
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * Set the model.
	 *
	 * @param model The model to set.
	 * @since 1.8
	 */
	public void setModel(Model model) {
		this.model = model;
	}

	/**
	 * Defines models configurations.
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
		 * The folder. The default folder is .model.
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
	 * Defines models configurations files.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Files {
		/**
		 * The default security file name.
		 */
		public static final String defaultSecurityFileName = "security";

		/**
		 * The security. The default value is security.
		 */
		private String security = defaultSecurityFileName;

		/**
		 * Returns the security file name.
		 *
		 * @return The security file name.
		 * @since 1.8
		 */
		public String getSecurity() {
			return security;
		}

		/**
		 * Set the security file name.
		 *
		 * @param fileName The file name to set.
		 * @since 1.8
		 */
		public void setSecurity(String fileName) {
			security = fileName;
		}
	}

}
