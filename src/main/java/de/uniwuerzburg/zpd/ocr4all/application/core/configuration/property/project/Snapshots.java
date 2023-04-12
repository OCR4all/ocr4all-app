/**
 * File:     Snapshots.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     11.03.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.FolderDefault;

/**
 * Defines snapshots.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Snapshots {
	/**
	 * The default folder.
	 */
	public static final String defaultFolder = "snapshots";

	/**
	 * The folder. The default value is snapshots.
	 */
	private String folder = defaultFolder;

	/**
	 * The snapshot.
	 */
	private Snapshot snapshot = new Snapshot();

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
	 * Returns the snapshot.
	 *
	 * @return The snapshot.
	 * @since 1.8
	 */
	public Snapshot getSnapshot() {
		return snapshot;
	}

	/**
	 * Set the snapshot.
	 *
	 * @param snapshot The snapshot to set.
	 * @since 1.8
	 */
	public void setSnapshot(Snapshot snapshot) {
		this.snapshot = snapshot;
	}

	/**
	 * Defines snapshot configurations.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Snapshot {
		/**
		 * The default derived folder.
		 */
		public static final String defaultDerivedFolder = "derived";

		/**
		 * The configuration.
		 */
		private Configuration configuration = new Configuration();

		/**
		 * The sandbox.
		 */
		private Sandbox sandbox = new Sandbox();

		/**
		 * The derived.
		 */
		private FolderDefault derived = new FolderDefault(defaultDerivedFolder);

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
		 * Returns the sandbox.
		 *
		 * @return The sandbox.
		 * @since 1.8
		 */
		public Sandbox getSandbox() {
			return sandbox;
		}

		/**
		 * Set the sandbox.
		 *
		 * @param sandbox The sandbox to set.
		 * @since 1.8
		 */
		public void setSandbox(Sandbox sandbox) {
			this.sandbox = sandbox;
		}

		/**
		 * Returns the derived.
		 *
		 * @return The derived.
		 * @since 1.8
		 */
		public FolderDefault getDerived() {
			return derived;
		}

		/**
		 * Set the derived.
		 *
		 * @param derived The derived to set.
		 * @since 1.8
		 */
		public void setDerived(FolderDefault derived) {
			this.derived = derived;
		}

	}

	/**
	 * Defines snapshot configurations.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Configuration {
		/**
		 * The default folder.
		 */
		public static final String defaultFolder = ".snapshot";

		/**
		 * The folder. The default value is .snapshot.
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
	 * Defines snapshot configurations files.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Files {
		/**
		 * The default main file name.
		 */
		public static final String defaultMainFileName = "snapshot";

		/**
		 * The default process file name.
		 */
		public static final String defaultProcessFileName = "process";

		/**
		 * The main file name. The default value is snapshot.
		 */
		private String main = defaultMainFileName;

		/**
		 * The process file name. The default value is process.
		 */
		private String process = defaultProcessFileName;

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
		 * Returns the process file name.
		 *
		 * @return The process file name.
		 * @since 1.8
		 */
		public String getProcess() {
			return process;
		}

		/**
		 * Set the process file name.
		 *
		 * @param fileName The file name to set.
		 * @since 1.8
		 */
		public void setProcess(String fileName) {
			this.process = fileName;
		}
	}

	/**
	 * Defines sandbox.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Sandbox {
		/**
		 * The default folder.
		 */
		public static final String defaultFolder = "sandbox";

		/**
		 * The folder. The default value is .sandbox.
		 */
		private String folder = defaultFolder;

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
	}

}
