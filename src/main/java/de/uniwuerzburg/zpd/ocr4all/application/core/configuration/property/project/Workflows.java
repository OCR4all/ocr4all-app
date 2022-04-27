/**
 * File:     Workflows.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project.projects
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.03.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project;

/**
 * Defines workflows.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Workflows {
	/**
	 * The default folder.
	 */
	public static final String defaultFolder = "workflows";

	/**
	 * The folder. The default value is workflows.
	 */
	private String folder = defaultFolder;

	/**
	 * The workflow.
	 */
	private Workflow workflow = new Workflow();

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
	 * Returns the workflow.
	 *
	 * @return The workflow.
	 * @since 1.8
	 */
	public Workflow getWorkflow() {
		return workflow;
	}

	/**
	 * Set the workflow.
	 *
	 * @param workflow The workflow to set.
	 * @since 1.8
	 */
	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

	/**
	 * Defines workflow configurations.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Workflow {
		/**
		 * The configuration.
		 */
		private Configuration configuration = new Configuration();

		/**
		 * The mets.
		 */
		private Mets mets = new Mets();

		/**
		 * The snapshots.
		 */
		private Snapshots snapshots = new Snapshots();

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
		 * Returns the mets.
		 *
		 * @return The mets.
		 * @since 1.8
		 */
		public Mets getMets() {
			return mets;
		}

		/**
		 * Set the mets.
		 *
		 * @param mets The mets to set.
		 * @since 1.8
		 */
		public void setMets(Mets mets) {
			this.mets = mets;
		}

		/**
		 * Returns the snapshots.
		 *
		 * @return The snapshots.
		 * @since 1.8
		 */
		public Snapshots getSnapshots() {
			return snapshots;
		}

		/**
		 * Set the snapshots.
		 *
		 * @param snapshots The snapshots to set.
		 * @since 1.8
		 */
		public void setSnapshots(Snapshots snapshots) {
			this.snapshots = snapshots;
		}

	}

	/**
	 * Defines workflow configurations.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Configuration {
		/**
		 * The default folder.
		 */
		public static final String defaultFolder = ".workflow";

		/**
		 * The folder. The default value is .workflow.
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
	 * Defines workflow configurations files.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Files {
		/**
		 * The default main file name.
		 */
		public static final String defaultMainFileName = "workflow";

		/**
		 * The default history file name.
		 */
		public static final String defaultHistoryFileName = "history";

		/**
		 * The main file name. The default value is workflow.
		 */
		private String main = defaultMainFileName;

		/**
		 * The history file name. The default value is history.
		 */
		private String history = defaultHistoryFileName;

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
		 * Returns the history file name.
		 *
		 * @return The history file name.
		 * @since 1.8
		 */
		public String getHistory() {
			return history;
		}

		/**
		 * Set the history file name.
		 *
		 * @param fileName The file name to set.
		 * @since 1.8
		 */
		public void setHistory(String fileName) {
			history = fileName;
		}
	}
}
