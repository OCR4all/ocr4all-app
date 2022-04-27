/**
 * File:     WorkflowResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     09.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow.Workflow;

/**
 * Defines workflow responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class WorkflowResponse implements Serializable {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The project id.
	 */
	private String projectId;

	/**
	 * The id.
	 */
	private String id;

	/**
	 * The name.
	 */
	private String name;

	/**
	 * The description.
	 */
	private String description;

	/**
	 * The state.
	 */
	private String state;

	/**
	 * The tracking.
	 */
	private TrackingResponse tracking;

	/**
	 * The done time stamp.
	 */
	private Date done;

	/**
	 * The keywords.
	 */
	private Set<String> keywords;

	/**
	 * True if the user can access the snapshots.
	 */
	@JsonProperty("snapshot-access")
	private boolean isSnapshotAccess;

	/**
	 * True if there are snapshots.
	 */
	private boolean isSnapshotAvailable;

	/**
	 * Creates a workflow response for the api.
	 * 
	 * @param workflow The workflow.
	 * @since 1.8
	 */
	public WorkflowResponse(Workflow workflow) {
		super();

		projectId = workflow.getProject().getId();
		id = workflow.getId();

		name = workflow.getName();
		description = workflow.getDescription();

		state = workflow.getState().name();

		tracking = new TrackingResponse(workflow.getUser(), workflow.getConfiguration().getConfiguration().getCreated(),
				workflow.getConfiguration().getConfiguration().getUpdated());
		keywords = workflow.getConfiguration().getConfiguration().getKeywords();

		done = workflow.getConfiguration().getConfiguration().getDone();

		isSnapshotAccess = workflow.isSnapshotAccess();
		isSnapshotAvailable = isSnapshotAccess && workflow.isSnapshotAvailable();
	}

	/**
	 * Returns the project id.
	 *
	 * @return The project id.
	 * @since 1.8
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * Set the project id.
	 *
	 * @param projectId The project id to set.
	 * @since 1.8
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	/**
	 * Returns the id.
	 *
	 * @return The id.
	 * @since 1.8
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the id.
	 *
	 * @param id The id to set.
	 * @since 1.8
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the name.
	 *
	 * @return The name.
	 * @since 1.8
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name.
	 *
	 * @param name The name to set.
	 * @since 1.8
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the description.
	 *
	 * @return The description.
	 * @since 1.8
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description.
	 *
	 * @param description The description to set.
	 * @since 1.8
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the state.
	 *
	 * @return The state.
	 * @since 1.8
	 */
	public String getState() {
		return state;
	}

	/**
	 * Set the state.
	 *
	 * @param state The state to set.
	 * @since 1.8
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * Returns the tracking.
	 *
	 * @return The tracking.
	 * @since 1.8
	 */
	public TrackingResponse getTracking() {
		return tracking;
	}

	/**
	 * Set the tracking.
	 *
	 * @param tracking The tracking to set.
	 * @since 1.8
	 */
	public void setTracking(TrackingResponse tracking) {
		this.tracking = tracking;
	}

	/**
	 * Returns the done.
	 *
	 * @return The done.
	 * @since 1.8
	 */
	public Date getDone() {
		return done;
	}

	/**
	 * Set the done.
	 *
	 * @param done The done to set.
	 * @since 1.8
	 */
	public void setDone(Date done) {
		this.done = done;
	}

	/**
	 * Returns the keywords.
	 *
	 * @return The keywords.
	 * @since 1.8
	 */
	public Set<String> getKeywords() {
		return keywords;
	}

	/**
	 * Set the keywords.
	 *
	 * @param keywords The keywords to set.
	 * @since 1.8
	 */
	public void setKeywords(Set<String> keywords) {
		this.keywords = keywords;
	}

	/**
	 * Returns true if the user can access the snapshots.
	 *
	 * @return True if the user can access the snapshots.
	 * @since 1.8
	 */
	public boolean isSnapshotAccess() {
		return isSnapshotAccess;
	}

	/**
	 * Set to true if the user can access the snapshots.
	 *
	 * @param isSnapshotAccess The snapshots access flag to set.
	 * @since 1.8
	 */
	public void setSnapshotAccess(boolean isSnapshotAccess) {
		this.isSnapshotAccess = isSnapshotAccess;
	}

	/**
	 * Returns true if there are snapshots.
	 *
	 * @return True if there are snapshots.
	 * @since 1.8
	 */
	public boolean isSnapshotAvailable() {
		return isSnapshotAvailable;
	}

	/**
	 * Set to true if there are snapshots.
	 *
	 * @param isSnapshotAvailable The available flag to set.
	 * @since 1.8
	 */
	public void setSnapshotAvailable(boolean isSnapshotAvailable) {
		this.isSnapshotAvailable = isSnapshotAvailable;
	}

}
