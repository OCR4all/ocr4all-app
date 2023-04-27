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
	 * The snapshot track.
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("snapshot-track")
	private List<Integer> snapshotTrack;

	/**
	 * Creates a JSON object for a scheduled job to send responses to clients.
	 * 
	 * @param jobId         The job id.
	 * @param jobState      The job state.
	 * @param snapshotTrack The snapshot track.
	 * @since 1.8
	 */
	public JobJsonResponse(int jobId, Job.State jobState, List<Integer> snapshotTrack) {
		super();

		this.jobId = jobId;
		this.jobState = jobState;

		this.snapshotTrack = snapshotTrack;
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
	 * Returns the snapshot track.
	 *
	 * @return The snapshot track.
	 * @since 1.8
	 */
	public List<Integer> getSnapshotTrack() {
		return snapshotTrack;
	}

	/**
	 * Set the snapshot track.
	 *
	 * @param snapshotTrack The snapshot track to set.
	 * @since 1.8
	 */
	public void setSnapshotTrack(List<Integer> snapshotTrack) {
		this.snapshotTrack = snapshotTrack;
	}

}
