/**
 * File:     Snapshot.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     16.03.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow;

import java.util.ArrayList;
import java.util.List;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project.SnapshotConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.History;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.project.ActionHistory;

/**
 * Defines snapshots.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Snapshot {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Snapshot.class);

	/**
	 * The workflow.
	 */
	private final Workflow workflow;

	/**
	 * The configuration.
	 */
	private final SnapshotConfiguration configuration;

	/**
	 * Creates a snapshot.
	 * 
	 * @param configuration The configuration.
	 * @param workflow      The workflow to which this snapshot belongs.
	 * @since 1.8
	 */
	Snapshot(SnapshotConfiguration configuration, Workflow workflow) {
		super();

		this.workflow = workflow;
		this.configuration = configuration;
	}

	/**
	 * Creates a consistent snapshot.
	 * 
	 * @param track    The track. An empty list returns the root snapshot.
	 * @param workflow The workflow to which this snapshot belongs.
	 * @throws IllegalArgumentException Throws if the snapshot track is invalid for
	 *                                  the workflow or it is inconsistent.
	 * @since 1.8
	 */
	Snapshot(List<Integer> track, Workflow workflow) throws IllegalArgumentException {
		super();

		this.workflow = workflow;

		configuration = workflow.getConfiguration().getSnapshots().getLeaf(track);
		
		if (configuration == null) {
			String message = "invalid snapshot track " + (track == null ? "NULL" : track) + " for workflow '"
					+ workflow.getId() + "'.";
			logger.warn(message);

			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Returns the snapshots in the track.
	 * 
	 * @param track    The track. An empty list returns the root snapshot.
	 * @param workflow The workflow to which this snapshot belongs.
	 * @return The snapshots.
	 * @throws IllegalArgumentException Throws if the snapshot track is invalid for
	 *                                  the workflow or it is inconsistent.
	 * @since 1.8
	 */
	public static List<Snapshot> getPath(List<Integer> track, Workflow workflow) throws IllegalArgumentException {
		List<SnapshotConfiguration> configurations = workflow.getConfiguration().getSnapshots().getSnapshots(track);

		if (configurations == null) {
			String message = "invalid snapshot track " + (track == null ? "NULL" : track) + " for workflow '"
					+ workflow.getId() + "'.";
			logger.warn(message);

			throw new IllegalArgumentException(message);
		}

		List<Snapshot> path = new ArrayList<>();
		for (SnapshotConfiguration configuration : configurations)
			path.add(new Snapshot(configuration, workflow));

		return path;
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

	/**
	 * Returns the configuration.
	 *
	 * @return The configuration.
	 * @since 1.8
	 */
	public SnapshotConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Reset the snapshot. The derived snapshots are removed.
	 * 
	 * @return True if the snapshot could be reseted.
	 * @since 1.8
	 */
	public boolean reset() {
		if (configuration.removeDerived()) {
			workflow.add(new ActionHistory("remove derived snapshots '" + configuration.getLoggerIdentifier() + "'",
					null, null));

			return true;
		} else {
			workflow.add(new ActionHistory(History.Level.error, "reset derived snapshots",
					"problems removing the derived snapshots of snapshots '" + configuration.getLoggerIdentifier()
							+ "'",
					null));

			return false;
		}
	}

}
