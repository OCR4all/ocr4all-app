/**
 * File:     Mets.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     04.04.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project;

/**
 * Defines mets.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Mets {
	/**
	 * The default file name.
	 */
	public static final String defaultFileName = "mets.xml";

	/**
	 * The default group.
	 */
	public static final String defaultGroup = "ocr4all";

	/**
	 * The file name. The default value is mets.xml.
	 */
	private String file = defaultFileName;

	/**
	 * The group. The default value is ocr4all.
	 */
	private String group = defaultGroup;

	/**
	 * Returns the file name.
	 *
	 * @return The file name.
	 * @since 1.8
	 */
	public String getFile() {
		return file;
	}

	/**
	 * Set the file name.
	 *
	 * @param file The file name to set.
	 * @since 1.8
	 */
	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * Returns the group.
	 *
	 * @return The group.
	 * @since 1.8
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Set the group.
	 *
	 * @param group The group to set.
	 * @since 1.8
	 */
	public void setGroup(String group) {
		this.group = group;
	}

}
