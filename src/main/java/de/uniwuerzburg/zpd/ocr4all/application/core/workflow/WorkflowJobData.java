/**
 * File:     WorkflowCoreData.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.workflow
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     27.04.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.workflow;

import de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.Metadata;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.Workflow;

/**
 * WorkflowJobData is an immutable class that defines workflow job data.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class WorkflowJobData {
	/**
	 * The metadata.
	 */
	private final Metadata metadata;

	/**
	 * The workflow.
	 */
	private final Workflow workflow;

	/**
	 * Creates a workflow job data.
	 * 
	 * @param metadata The metadata.
	 * @param workflow The workflow.
	 * @since 1.8
	 */
	public WorkflowJobData(Metadata metadata, Workflow workflow) {
		super();
		this.metadata = metadata;
		this.workflow = workflow;
	}

	/**
	 * Returns the metadata.
	 *
	 * @return The metadata.
	 * @since 1.8
	 */
	public Metadata getMetadata() {
		return metadata;
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

}
