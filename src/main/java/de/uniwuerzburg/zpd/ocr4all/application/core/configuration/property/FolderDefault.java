/**
 * File:     Folder.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.03.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property;

/**
 * Defines ocr4all folder properties.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class FolderDefault {
	/**
	 * The folder.
	 */
	private String folder;

	/**
	 * The default folder.
	 */
	private final String defaultFolder;

	/**
	 * Default constructor for an ocr4all folder property.
	 * 
	 * @since 1.8
	 */
	public FolderDefault() {
		this(null);
	}

	/**
	 * Creates an ocr4all folder property.
	 * 
	 * @param folder The folder.
	 * @since 1.8
	 */
	public FolderDefault(String folder) {
		super();

		this.folder = folder;
		defaultFolder = folder;
	}

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

}
