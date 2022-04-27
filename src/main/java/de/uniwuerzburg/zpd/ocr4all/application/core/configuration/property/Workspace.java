/**
 * File:     Workspace.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.03.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property;

import javax.validation.constraints.NotEmpty;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project.Model;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project.Projects;

/**
 * Defines ocr4all workspace properties.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Workspace {
	/**
	 * The folder.
	 */
	@NotEmpty(message = "the ocr4all workspace folder cannot be null nor empty")
	private String folder;

	/**
	 * The configuration.
	 */
	private Configuration configuration = new Configuration();

	/**
	 * The models.
	 */
	private Models models = new Models();

	/**
	 * The projects.
	 */
	private Projects projects = new Projects();

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
	 * Returns the models.
	 *
	 * @return The models.
	 * @since 1.8
	 */
	public Models getModels() {
		return models;
	}

	/**
	 * Set the models.
	 *
	 * @param models The models to set.
	 * @since 1.8
	 */
	public void setModels(Models models) {
		this.models = models;
	}

	/**
	 * Returns the projects.
	 *
	 * @return The projects.
	 * @since 1.8
	 */
	public Projects getProjects() {
		return projects;
	}

	/**
	 * Set the projects.
	 *
	 * @param projects The projects to set.
	 * @since 1.8
	 */
	public void setProjects(Projects projects) {
		this.projects = projects;
	}

	/**
	 * Defines configuration properties for ocr4all workspaces.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Configuration {
		/**
		 * The default folder.
		 */
		private static final String defaultFolder = ".ocr4all";

		/**
		 * The folder. The default value is .ocr4all.
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
		private static final String defaultMainFileName = "ocr4all";

		/**
		 * The default user file name.
		 */
		private static final String defaultUserFileName = "user";

		/**
		 * The default group file name.
		 */
		private static final String defaultGroupFileName = "group";

		/**
		 * The default password file name.
		 */
		private static final String defaultPasswordFileName = "password";

		/**
		 * The main file name. The default value is ocr4all.
		 */
		private String main = defaultMainFileName;

		/**
		 * The user file name. The default value is user.
		 */
		private String user = defaultUserFileName;

		/**
		 * The group file name. The default value is group.
		 */
		private String group = defaultGroupFileName;

		/**
		 * The password file name. The default value is password.
		 */
		private String password = defaultPasswordFileName;

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

		/**
		 * Returns the user file name.
		 *
		 * @return The user file name.
		 * @since 1.8
		 */
		public String getUser() {
			return OCR4all.getNotEmpty(user, defaultUserFileName);
		}

		/**
		 * Set the user file name.
		 *
		 * @param fileName The file name to set.
		 * @since 1.8
		 */
		public void setUser(String fileName) {
			user = fileName;
		}

		/**
		 * Returns the group file name.
		 *
		 * @return The group file name.
		 * @since 1.8
		 */
		public String getGroup() {
			return OCR4all.getNotEmpty(group, defaultGroupFileName);
		}

		/**
		 * Set the group file name.
		 *
		 * @param fileName The file name to set.
		 * @since 1.8
		 */
		public void setGroup(String fileName) {
			group = fileName;
		}

		/**
		 * Returns the password file name.
		 *
		 * @return The password file name.
		 * @since 1.8
		 */
		public String getPassword() {
			return OCR4all.getNotEmpty(password, defaultPasswordFileName);
		}

		/**
		 * Set the password file name.
		 *
		 * @param fileName The file name to set.
		 * @since 1.8
		 */
		public void setPassword(String fileName) {
			password = fileName;
		}
	}

	/**
	 * Defines models.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Models {
		/**
		 * The default folder.
		 */
		private static final String defaultFolder = "models";

		/**
		 * The folder. The default value is models.
		 */
		private String folder = defaultFolder;

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
	}

}
