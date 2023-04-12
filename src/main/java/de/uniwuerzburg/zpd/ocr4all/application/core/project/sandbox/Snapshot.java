/**
 * File:     Snapshot.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     16.03.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox;

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
	 * The sandbox.
	 */
	private final Sandbox sandbox;

	/**
	 * The configuration.
	 */
	private final SnapshotConfiguration configuration;

	/**
	 * Creates a snapshot.
	 * 
	 * @param configuration The configuration.
	 * @param sandbox       The sandbox to which this snapshot belongs.
	 * @since 1.8
	 */
	Snapshot(SnapshotConfiguration configuration, Sandbox sandbox) {
		super();

		this.sandbox = sandbox;
		this.configuration = configuration;
	}

	/**
	 * Creates a consistent snapshot.
	 * 
	 * @param track   The track. An empty list returns the root snapshot.
	 * @param sandbox The sandbox to which this snapshot belongs.
	 * @throws IllegalArgumentException Throws if the snapshot track is invalid for
	 *                                  the sandbox or it is inconsistent.
	 * @since 1.8
	 */
	Snapshot(List<Integer> track, Sandbox sandbox) throws IllegalArgumentException {
		super();

		this.sandbox = sandbox;

		configuration = sandbox.getConfiguration().getSnapshots().getLeaf(track);

		if (configuration == null) {
			String message = "invalid snapshot track " + (track == null ? "NULL" : track) + " for sandbox '"
					+ sandbox.getId() + "'.";
			logger.warn(message);

			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Returns the snapshots in the track.
	 * 
	 * @param track   The track. An empty list returns the root snapshot.
	 * @param sandbox The sandbox to which this snapshot belongs.
	 * @return The snapshots.
	 * @throws IllegalArgumentException Throws if the snapshot track is invalid for
	 *                                  the sandbox or it is inconsistent.
	 * @since 1.8
	 */
	public static List<Snapshot> getPath(List<Integer> track, Sandbox sandbox) throws IllegalArgumentException {
		List<SnapshotConfiguration> configurations = sandbox.getConfiguration().getSnapshots().getSnapshots(track);

		if (configurations == null) {
			String message = "invalid snapshot track " + (track == null ? "NULL" : track) + " for sandbox '"
					+ sandbox.getId() + "'.";
			logger.warn(message);

			throw new IllegalArgumentException(message);
		}

		List<Snapshot> path = new ArrayList<>();
		for (SnapshotConfiguration configuration : configurations)
			path.add(new Snapshot(configuration, sandbox));

		return path;
	}

	/**
	 * Returns the sandbox.
	 *
	 * @return The sandbox.
	 * @since 1.8
	 */
	public Sandbox getSandbox() {
		return sandbox;
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
			sandbox.add(new ActionHistory("remove derived snapshots '" + configuration.getLoggerIdentifier() + "'",
					null, null));

			return true;
		} else {
			sandbox.add(new ActionHistory(History.Level.error, "reset derived snapshots",
					"problems removing the derived snapshots of snapshots '" + configuration.getLoggerIdentifier()
							+ "'",
					null));

			return false;
		}
	}

}
