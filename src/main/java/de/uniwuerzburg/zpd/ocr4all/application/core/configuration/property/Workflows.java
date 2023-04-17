/**
 * File:     Workflows.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     17.04.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property;

/**
 * Defines ocr4all workflows properties.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Workflows {
	/**
	 * The default folder.
	 */
	private static final String defaultFolder = "workflows";

	/**
	 * The folder. The default value is workflows.
	 */
	private String folder = defaultFolder;

	/**
	 * The file.
	 */
	private File file = new File();

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
	 * Returns the file.
	 *
	 * @return The file.
	 * @since 1.8
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Set the file.
	 *
	 * @param file The file to set.
	 * @since 1.8
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * Defines files.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class File {
		/**
		 * The extension.
		 */
		private Extension extension = new Extension();

		/**
		 * Returns the extension.
		 *
		 * @return The extension.
		 * @since 1.8
		 */
		public Extension getExtension() {
			return extension;
		}

		/**
		 * Set the extension.
		 *
		 * @param extension The extension to set.
		 * @since 1.8
		 */
		public void setExtension(Extension extension) {
			this.extension = extension;
		}

		/**
		 * Defines extensions.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class Extension {
			/**
			 * The default metadata.
			 */
			private static final String defaultMetadata = ".metadata";

			/**
			 * The default workflow.
			 */
			private static final String defaultWorkflow = ".workflow";

			/**
			 * The metadata. The default value is .metadata.
			 */
			private String metadata = defaultMetadata;

			/**
			 * The workflow. The default value is .workflow.
			 */
			private String workflow = defaultWorkflow;

			/**
			 * Returns the metadata.
			 *
			 * @return The metadata.
			 * @since 1.8
			 */
			public String getMetadata() {
				return OCR4all.getNotEmpty(metadata, defaultMetadata);
			}

			/**
			 * Set the metadata.
			 *
			 * @param metadata The metadata to set.
			 * @since 1.8
			 */
			public void setMetadata(String metadata) {
				this.metadata = metadata;
			}

			/**
			 * Returns the workflow.
			 *
			 * @return The workflow.
			 * @since 1.8
			 */
			public String getWorkflow() {
				return OCR4all.getNotEmpty(workflow, defaultWorkflow);
			}

			/**
			 * Set the workflow.
			 *
			 * @param workflow The workflow to set.
			 * @since 1.8
			 */
			public void setWorkflow(String workflow) {
				this.workflow = workflow;
			}
		}
	}
}
