/**
 * File:     WorkflowsConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     17.04.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration;

import java.nio.file.Paths;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.Workflows;

/**
 * Defines configurations for the workflow containers.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class WorkflowsConfiguration extends CoreFolder {
	/**
	 * The file extension.
	 */
	private final String fileExtension;

	/**
	 * Creates a configuration for a workflow container.
	 * 
	 * @param properties             The workflow properties.
	 * @param workspaceConfiguration The configuration for the workspace.
	 * @since 1.8
	 */
	public WorkflowsConfiguration(Workflows properties, WorkspaceConfiguration workspaceConfiguration) {
		super(Paths.get(workspaceConfiguration.getFolder().toString(), properties.getFolder()));

		fileExtension = properties.getFile().getExtension();

		ConfigurationService.initializeFolder(true, folder, "workflows");
	}

	/**
	 * Returns the file extension.
	 *
	 * @return The file extension.
	 * @since 1.8
	 */
	public String getFileExtension() {
		return fileExtension;
	}

}
