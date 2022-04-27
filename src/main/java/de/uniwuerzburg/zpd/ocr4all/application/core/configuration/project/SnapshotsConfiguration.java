/**
 * File:     SnapshotsConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     15.03.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.CoreFolder;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project.Snapshots;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Instance;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.project.workflow.Snapshot;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.ServiceProvider;

/**
 * SnapshotsConfiguration is an immutable class that defines snapshots
 * configurations for workflows.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class SnapshotsConfiguration extends CoreFolder {
	/**
	 * The snapshot properties.
	 */
	private final Snapshots.Snapshot properties;

	/**
	 * The root snapshot.
	 */
	private SnapshotConfiguration root;

	/**
	 * The user.
	 */
	private final String user;

	/**
	 * Creates snapshots configuration for a workflow.
	 * 
	 * @param properties The configuration properties for the snapshots.
	 * @param folder     The workflow folder.
	 * @param user       The user.
	 * @since 1.8
	 */
	SnapshotsConfiguration(Snapshots properties, Path folder, String user) {
		super(Paths.get(folder.toString(), properties.getFolder()));

		/*
		 * Initialize the snapshots folder
		 */
		ConfigurationService.initializeFolder(true, this.folder, "workflow '" + this.folder.toString() + "' snapshots");

		// The snapshot properties
		this.properties = properties.getSnapshot();
		this.user = user;

		root = new SnapshotConfiguration(this.properties, null, this.folder, new ArrayList<>(), user);
		if (!root.isConsistent())
			root = null;
	}

	/**
	 * Returns true if the root snapshot is available.
	 *
	 * @return True if the root snapshot is available.
	 * @since 1.8
	 */
	public boolean isRootSet() {
		return root != null;
	}

	/**
	 * Returns the root snapshot.
	 *
	 * @return The root snapshot. Null if not available.
	 * @since 1.8
	 */
	public SnapshotConfiguration getRoot() {
		return root;
	}

	/**
	 * Creates the root snapshot if not available.
	 * 
	 * @param type            The type.
	 * @param label           The label. It can not be null nor blank.
	 * @param description     The description.
	 * @param serviceProvider The service provider. It can not be null and its id
	 *                        can not be null nor blank.
	 * @param instance        The instance.
	 * @return The created root snapshot. Null if could not be created.
	 * @since 1.8
	 */
	public SnapshotConfiguration createRoot(Snapshot.Type type, String label, String description,
			ServiceProvider serviceProvider, Instance instance) {
		if (isRootSet())
			return null;
		else {
			root = new SnapshotConfiguration(this.properties, null, this.folder, new ArrayList<>(), user, type, label,
					description, serviceProvider, instance);
			
			return root.isConsistent() ? root : null;
		}
	}

	/**
	 * Returns the configuration of the leaf snapshot in the track. If track is
	 * empty then returns the root.
	 *
	 * @param track The track.
	 * @return The configuration of the leaf snapshot in the track. Null if the
	 *         track is invalid.
	 * @since 1.8
	 */
	public SnapshotConfiguration getLeaf(List<Integer> track) {
		List<SnapshotConfiguration> snapshots = getSnapshots(track);

		return snapshots == null ? null : snapshots.get(snapshots.size() - 1);
	}

	/**
	 * Returns the configuration of the snapshots in the track. If track is empty
	 * then returns the root.
	 *
	 * @param track The track.
	 * @return The configuration of the snapshots in the track. Null if the track is
	 *         invalid.
	 * @since 1.8
	 */
	public List<SnapshotConfiguration> getSnapshots(List<Integer> track) {
		if (root == null || track == null)
			return null;

		List<SnapshotConfiguration> snapshots = new ArrayList<>();
		snapshots.add(root);
		for (Integer id : track)
			if (id == null || id < 1)
				return null;
			else {
				SnapshotConfiguration derived = snapshots.get(snapshots.size() - 1).getDerived(id);
				if (derived == null)
					return null;
				else
					snapshots.add(derived);
			}

		return snapshots;
	}

	/**
	 * Resets the snapshots.
	 *
	 * @return True if the snapshots could be reset.
	 * 
	 * @since 1.8
	 */
	public boolean reset() {
		if (deleteContents()) {
			root = null;

			return true;
		} else
			return false;
	}
}
