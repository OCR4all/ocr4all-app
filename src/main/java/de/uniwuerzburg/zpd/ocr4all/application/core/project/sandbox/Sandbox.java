/**
 * File:     Sandbox.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     14.04.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project.SnapshotConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project.SandboxConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.History;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Instance;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.project.ActionHistory;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.project.ProjectHistory;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.ServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.util.PersistenceTools;

/**
 * Defines sandboxes.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Sandbox {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Sandbox.class);

	/**
	 * The project.
	 */
	private final Project project;

	/**
	 * The configuration.
	 */
	private final SandboxConfiguration configuration;

	/**
	 * The history persistence manager.
	 */
	private PersistenceManager historyManager = null;

	/**
	 * Creates a sandbox.
	 * 
	 * @param folder  The sandbox folder.
	 * @param project The project to which this sandbox belongs.
	 * @since 1.8
	 */
	public Sandbox(Path folder, Project project) {
		super();

		this.project = project;
		configuration = project.getConfiguration().getSandboxesConfiguration().getSandbox(folder, project.getUser());
	}

	/**
	 * Returns the id.
	 *
	 * @return The id.
	 * @since 1.8
	 */
	public String getId() {
		return configuration.getConfiguration().getId();
	}

	/**
	 * Returns the user.
	 *
	 * @return The user. Null if not set or the main configuration is not available.
	 * @since 1.8
	 */
	public String getUser() {
		return configuration.getConfiguration().getUser();
	}

	/**
	 * Returns the name.
	 *
	 * @return The name.
	 * @since 1.8
	 */
	public String getName() {
		return configuration.getConfiguration().getName();
	}

	/**
	 * Returns the description.
	 *
	 * @return The description.
	 * @since 1.8
	 */
	public String getDescription() {
		return configuration.getConfiguration().getDescription();
	}

	/**
	 * Returns the state.
	 *
	 * @return The state. Null if not set or the main configuration is not
	 *         available.
	 * @since 1.8
	 */
	public de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Sandbox.State getState() {
		return configuration.getConfiguration().getState();
	}

	/**
	 * Returns the project to which this sandbox belongs.
	 *
	 * @return The project.
	 * @since 1.8
	 */
	public Project getProject() {
		return project;
	}

	/**
	 * Returns the configuration.
	 *
	 * @return The configuration.
	 * @since 1.8
	 */
	public SandboxConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Returns the history persistence manager. Create a new one if necessary.
	 * 
	 * @return The history persistence manager.
	 * @since 1.8
	 */
	private PersistenceManager getHistoryManager() {
		if (historyManager == null)
			historyManager = new PersistenceManager(configuration.getConfiguration().getHistoryFile(),
					Type.project_action_history_v1, Type.project_process_history_v1);

		return historyManager;
	}

	/**
	 * Adds the history to the project.
	 * 
	 * @param history The history to be added.
	 * @since 1.8
	 */
	public void add(ProjectHistory history) {
		if (history != null)
			try {
				history.setDate(new Date());
				history.setUser(project.getUser());

				getHistoryManager().persist(true, history);
			} catch (Exception e) {
				logger.warn("Could not add the history to the sandbox '" + configuration.getConfiguration().getName()
						+ "' - " + e.getMessage());
			}
	}

	/**
	 * Returns the history.
	 * 
	 * @return The history.
	 * @since 1.8
	 */
	public List<History> getHistory() {
		try {
			return getHistoryManager().getEntities(History.class, message -> logger.warn(message),
					PersistenceTools.getTrackingDateComparator(false));
		} catch (IOException e) {
			logger.warn(e.getMessage());

			return new ArrayList<>();
		}
	}

	/**
	 * Zips the history.
	 * 
	 * @param outputStream The output stream for writing the zipped history.
	 * @since 1.8
	 */
	public void zipHistory(OutputStream outputStream) {
		try {
			getHistoryManager().zip(outputStream);
		} catch (NullPointerException | IOException e) {
			logger.warn(e.getMessage());
		}
	}

	/**
	 * Reset the sandbox. The snapshots are removed.
	 * 
	 * @return True if the sandbox could be reseted.
	 * @since 1.8
	 */
	public boolean reset() {
		if (configuration.getSnapshots().reset()) {
			add(new ActionHistory("reset snapshots", null, null));

			return true;
		} else {
			add(new ActionHistory(History.Level.error, "reset snapshots", "problems resetting the sandbox snapshots",
					null));

			return false;
		}
	}

	/**
	 * Returns true if the user can access the snapshots.
	 * 
	 * @return True if the user can access the snapshots.
	 * @since 1.8
	 */
	public boolean isSnapshotAccess() {
		switch (getConfiguration().getConfiguration().getState()) {
		case active:
		case paused:
		case closed:
			return project.isExecute() || project.isRead();

		case secured:
		case canceled:
		default:
			return project.isSpecial();
		}
	}

	/**
	 * Returns true if there are snapshots.
	 * 
	 * @return True if there are snapshots.
	 * @since 1.8
	 */
	public boolean isSnapshotAvailable() {
		return getConfiguration().getSnapshots().isRootSet();
	}

	/**
	 * Returns true if sandbox was launched.
	 * 
	 * @return True if sandbox was launched.
	 * @since 1.8
	 */
	public boolean isLaunched() {
		return isSnapshotAvailable() && getConfiguration().getSnapshots().getRoot().isProcessAssigned();
	}

	/**
	 * Returns the root snapshot.
	 * 
	 * @return The snapshot. Null if not available or it is inconsistent.
	 * @since 1.8
	 */
	public Snapshot getSnapshot() {
		try {
			return new Snapshot(new ArrayList<>(), this);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Returns the leaf snapshot in the track.
	 * 
	 * @param track The track. An empty list returns the root snapshot.
	 * @return The snapshot.
	 * @throws IllegalArgumentException Throws if the snapshot track is invalid for
	 *                                  the sandbox or it is inconsistent.
	 * @since 1.8
	 */
	public Snapshot getSnapshot(List<Integer> track) throws IllegalArgumentException {
		return new Snapshot(track, this);
	}

	/**
	 * Returns the derived snapshots of the leaf snapshot in the track.
	 * 
	 * @param track The track. An empty list returns the derived snapshots of the
	 *              root snapshot.
	 * @return The derived snapshots.
	 * @throws IllegalArgumentException Throws if the snapshot track is invalid for
	 *                                  the sandbox or it is inconsistent.
	 * @since 1.8
	 */
	public List<Snapshot> getDerived(List<Integer> track) throws IllegalArgumentException {
		Snapshot snapshot = getSnapshot(track);

		List<Snapshot> snapshots = new ArrayList<>();

		for (SnapshotConfiguration derived : snapshot.getConfiguration().getDerived())
			snapshots.add(new Snapshot(derived, this));

		return snapshots;
	}

	/**
	 * Returns the snapshots in the track.
	 * 
	 * @param track The track. An empty list returns the root snapshot.
	 * @return The snapshot.
	 * @throws IllegalArgumentException Throws if the snapshot track is invalid for
	 *                                  the sandbox or it is inconsistent.
	 * @since 1.8
	 */
	public List<Snapshot> getSnapshots(List<Integer> track) throws IllegalArgumentException {
		return Snapshot.getPath(track, this);
	}

	/**
	 * Creates the snapshot.
	 * 
	 * @param type            The type.
	 * @param trackParent     The track to the parent snapshot. Null if the snapshot
	 *                        being created is the root.
	 * @param label           The label. It can not be null nor blank.
	 * @param description     The description.
	 * @param serviceProvider The service provider. It can not be null and its id
	 *                        can not be null nor blank.
	 * @param instance        The instance.
	 * @return The created snapshot.
	 * @throws IllegalArgumentException Throws if the snapshot could not be created.
	 * @since 1.8
	 */
	public Snapshot createSnapshot(
			de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Snapshot.Type type,
			List<Integer> trackParent, String label, String description, ServiceProvider serviceProvider,
			Instance instance) throws IllegalArgumentException {

		SnapshotConfiguration snapshotConfiguration = null;
		if (trackParent == null)
			snapshotConfiguration = getConfiguration().getSnapshots().createRoot(type, label, description,
					serviceProvider, instance);
		else {
			final Snapshot snapshot = getSnapshot(trackParent);
			if (snapshot.getConfiguration().isAllowDerivedSnapshots())
				snapshotConfiguration = snapshot.getConfiguration().createDerived(type, label, description,
						serviceProvider, instance);
		}

		if (snapshotConfiguration == null)
			throw new IllegalArgumentException("the snapshot could not be created");
		else
			return new Snapshot(snapshotConfiguration, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "name=" + configuration.getConfiguration().getName()
				+ (configuration.getConfiguration().isCreatedSet()
						? ", created=" + configuration.getConfiguration().getCreated()
						: "")
				+ (configuration.getConfiguration().isUpdatedSet()
						? ", updated=" + configuration.getConfiguration().getUpdated()
						: "")
				+ (configuration.getConfiguration().isDoneSet() ? ", done=" + configuration.getConfiguration().getDone()
						: "")
				+ ", state=" + configuration.getConfiguration().getState().name()
				+ (configuration.getConfiguration().isUserSet() ? ", user=" + configuration.getConfiguration().getUser()
						: "")
				+ ", path=" + configuration.getFolder()
				+ (configuration.getConfiguration().isDescriptionSet()
						? ", description=" + configuration.getConfiguration().getDescription()
						: "");
	}

	/**
	 * Return true if the current and given sandboxes are the same.
	 * 
	 * @param sandbox The sandbox to test.
	 * @return True if the current and given sandboxes are the same.
	 * @since 1.8
	 */
	public boolean isSame(Sandbox sandbox) {
		try {
			return sandbox != null
					&& Files.isSameFile(configuration.getFolder(), sandbox.getConfiguration().getFolder());
		} catch (IOException e) {
			return false;
		}
	}

}
