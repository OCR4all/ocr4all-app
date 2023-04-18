/**
 * File:     WorkflowCoreData.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.workflow
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     18.04.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.workflow;

import de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.Metadata;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.View;

/**
 * WorkflowCoreData is an immutable class that defines workflow core data.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class WorkflowCoreData {
	/**
	 * The metadata.
	 */
	private final Metadata metadata;

	/**
	 * The view.
	 */
	private final View view;

	/**
	 * Creates a workflow core data.
	 * 
	 * @param metadata The metadata.
	 * @param view     The view.
	 * @since 1.8
	 */
	public WorkflowCoreData(Metadata metadata, View view) {
		super();
		this.metadata = metadata;
		this.view = view;
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
	 * Returns the view.
	 *
	 * @return The view.
	 * @since 1.8
	 */
	public View getView() {
		return view;
	}

}
