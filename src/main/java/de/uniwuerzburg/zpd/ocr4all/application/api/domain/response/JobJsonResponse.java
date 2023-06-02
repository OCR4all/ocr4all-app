/**
 * File:     JobJsonResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     24.04.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.core.job.Job;

/**
 * Defines JSON objects for scheduled jobs to send responses to clients.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class JobJsonResponse implements Serializable {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The job id.
	 */
	@JsonProperty("job-id")
	private int jobId;

	/**
	 * The job state.
	 */
	@JsonProperty("job-state")
	private Job.State jobState;

	/**
	 * The track to the parent snapshot.
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("snapshot-track-parent")
	private List<Integer> snapshotTrackParent;

	/**
	 * Creates a JSON object for a scheduled job to send responses to clients.
	 * 
	 * @param jobId               The job id.
	 * @param jobState            The job state.
	 * @param snapshotTrackParent The track to the parent snapshot.
	 * @since 1.8
	 */
	public JobJsonResponse(int jobId, Job.State jobState, List<Integer> snapshotTrackParent) {
		super();

		this.jobId = jobId;
		this.jobState = jobState;

		this.snapshotTrackParent = snapshotTrackParent;
	}

	/**
	 * Returns the job id.
	 *
	 * @return The job id.
	 * @since 1.8
	 */
	public int getJobId() {
		return jobId;
	}

	/**
	 * Set the job id.
	 *
	 * @param jobId The job id to set.
	 * @since 1.8
	 */
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	/**
	 * Returns the job state.
	 *
	 * @return The job state.
	 * @since 1.8
	 */
	public Job.State getJobState() {
		return jobState;
	}

	/**
	 * Set the job state.
	 *
	 * @param jobState The job state to set.
	 * @since 1.8
	 */
	public void setJobState(Job.State jobState) {
		this.jobState = jobState;
	}

	/**
	 * Returns the track to the parent snapshot.
	 *
	 * @return The track to the parent snapshot.
	 * @since 1.8
	 */
	public List<Integer> getSnapshotTrackParent() {
		return snapshotTrackParent;
	}

	/**
	 * Set the track to the parent snapshot.
	 *
	 * @param track The track to set.
	 * @since 1.8
	 */
	public void setSnapshotTrackParent(List<Integer> track) {
		snapshotTrackParent = track;
	}

}
