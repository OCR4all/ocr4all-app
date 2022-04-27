/**
 * File:     Projects.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project.projects
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.03.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.OCR4all;

/**
 * Defines ocr4all projects properties.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Projects {
	/**
	 * The default folder.
	 */
	private static final String defaultFolder = "projects";

	/**
	 * The folder. The default value is projects.
	 */
	private String folder = defaultFolder;

	/**
	 * The project.
	 */
	private Project project = new Project();

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
	 * Returns the project.
	 *
	 * @return The project.
	 * @since 1.8
	 */
	public Project getProject() {
		return project;
	}

	/**
	 * Set the project.
	 *
	 * @param project The project to set.
	 * @since 1.8
	 */
	public void setProject(Project project) {
		this.project = project;
	}

}
