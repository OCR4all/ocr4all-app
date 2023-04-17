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
	 * The file extension metadata.
	 */
	private final String metadataFileExtension;

	/**
	 * The file extension workflow.
	 */
	private final String workflowFileExtension;

	/**
	 * Creates a configuration for a workflow container.
	 * 
	 * @param properties             The workflow properties.
	 * @param workspaceConfiguration The configuration for the workspace.
	 * @since 1.8
	 */
	public WorkflowsConfiguration(Workflows properties, WorkspaceConfiguration workspaceConfiguration) {
		super(Paths.get(workspaceConfiguration.getFolder().toString(), properties.getFolder()));

		metadataFileExtension = properties.getFile().getExtension().getMetadata();
		workflowFileExtension = properties.getFile().getExtension().getWorkflow();

		ConfigurationService.initializeFolder(true, folder, "workflows");
	}

	/**
	 * Returns the file extension metadata.
	 *
	 * @return The file extension metadata.
	 * @since 1.8
	 */
	public String getMetadataFileExtension() {
		return metadataFileExtension;
	}

	/**
	 * Returns the file extension workflow.
	 *
	 * @return The file extension workflow.
	 * @since 1.8
	 */
	public String getWorkflowFileExtension() {
		return workflowFileExtension;
	}

}
