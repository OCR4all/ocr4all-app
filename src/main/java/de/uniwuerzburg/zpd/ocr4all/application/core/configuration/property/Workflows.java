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
		 * The default extension.
		 */
		private static final String defaultExtension = ".wf";

		/**
		 * The extension.
		 */
		private String extension = defaultExtension;

		/**
		 * Returns the extension.
		 *
		 * @return The extension.
		 * @since 1.8
		 */
		public String getExtension() {
			return OCR4all.getNotEmpty(extension, defaultExtension);
		}

		/**
		 * Set the extension.
		 *
		 * @param extension The extension to set.
		 * @since 1.8
		 */
		public void setExtension(String extension) {
			this.extension = extension;
		}
	}
}
