/**
 * File:     SnapshotConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     14.03.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.CoreFolder;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.TrackingData;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project.Snapshots;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.Job;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Instance;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Snapshot;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.ServiceProvider;

/**
 * Defines configurations for the snapshots.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class SnapshotConfiguration extends CoreFolder {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SnapshotConfiguration.class);

	/**
	 * The configuration properties for the snapshot.
	 */
	private final Snapshots.Snapshot properties;

	/**
	 * The parent snapshot configuration. Null if this is a root snapshot
	 * configuration.
	 */
	private final SnapshotConfiguration parent;

	/**
	 * The configuration.
	 */
	private Configuration configuration;

	/**
	 * The track.
	 */
	private final List<Integer> track;

	/**
	 * The user.
	 */
	private final String user;

	/**
	 * The sandbox.
	 */
	private final Sandbox sandbox;

	/**
	 * The container for derived snapshots.
	 */
	private final DerivedContainer derivedContainer;

	/**
	 * Creates a configuration for a snapshot.
	 * 
	 * @param properties The configuration properties for the snapshot.
	 * @param parent     The parent snapshot configuration. Null if this is a root
	 *                   snapshot configuration.
	 * @param folder     The snapshot folder.
	 * @param track      The track. An empty track is the root. It can not be null.
	 * @param user       The user.
	 * @throws IllegalArgumentException Throws if the track is null.
	 * @since 1.8
	 */
	SnapshotConfiguration(Snapshots.Snapshot properties, SnapshotConfiguration parent, Path folder, List<Integer> track,
			String user) throws IllegalArgumentException {
		this(properties, false, parent, folder, track, user, null, null, null, null, null);
	}

	/**
	 * Creates a configuration for a snapshot and initialize it.
	 * 
	 * @param properties      The configuration properties for the snapshot.
	 * @param parent          The parent snapshot configuration. Null if this is a
	 *                        root snapshot configuration.
	 * @param folder          The snapshot folder.
	 * @param track           The track. An empty track is the root. It can not be
	 *                        null.
	 * @param user            The user.
	 * @param type            The type.
	 * @param label           The label. It can not be null nor blank.
	 * @param description     The description.
	 * @param serviceProvider The service provider. It can not be null and its id
	 *                        can not be null nor blank.
	 * @param instance        The instance.
	 * @throws IllegalArgumentException Throws if the track is null.
	 * @since 1.8
	 */
	SnapshotConfiguration(Snapshots.Snapshot properties, SnapshotConfiguration parent, Path folder, List<Integer> track,
			String user, Snapshot.Type type, String label, String description, ServiceProvider serviceProvider,
			Instance instance) throws IllegalArgumentException {
		this(properties, true, parent, folder, track, user, type, label, description, serviceProvider, instance);
	}

	/**
	 * Creates a configuration for a snapshot.
	 * 
	 * @param properties      The configuration properties for the snapshot.
	 * @param isInitialize    True if initialize.
	 * @param parent          The parent snapshot configuration. Null if this is a
	 *                        root snapshot configuration.
	 * @param folder          The snapshot folder.
	 * @param track           The track. An empty track is the root. It can not be
	 *                        null.
	 * @param user            The user.
	 * @param type            The type.
	 * @param label           The label. It can not be null nor blank.
	 * @param description     The description.
	 * @param serviceProvider The service provider. It can not be null and its id
	 *                        can not be null nor blank.
	 * @param instance        The instance.
	 * @throws IllegalArgumentException Throws if the track is null.
	 * @since 1.8
	 */
	private SnapshotConfiguration(Snapshots.Snapshot properties, boolean isInitialize, SnapshotConfiguration parent,
			Path folder, List<Integer> track, String user, Snapshot.Type type, String label, String description,
			ServiceProvider serviceProvider, Instance instance) throws IllegalArgumentException {
		super(folder);

		if (track == null)
			throw new IllegalArgumentException("the track can not be null");

		this.properties = properties;
		this.parent = parent;
		this.track = track;
		this.user = user;

		configuration = isInitialize
				? new Configuration(properties.getConfiguration(), type, label, description, serviceProvider, instance)
				: new Configuration(properties.getConfiguration());

		if (isConsistent()) {
			sandbox = new Sandbox(properties);
			derivedContainer = new DerivedContainer(properties);
		} else {
			sandbox = null;
			derivedContainer = null;
		}
	}

	/**
	 * Returns true if the snapshot is consistent.
	 * 
	 * @return True if the snapshot is consistent.
	 * @since 1.8
	 */
	public boolean isConsistent() {
		return ((parent == null && track.isEmpty()) || (parent != null && !track.isEmpty()))
				&& configuration.isMainConfigurationAvailable() && configuration.isProcessConfigurationAvailable();
	}

	/**
	 * Returns true if it is a root snapshot configuration.
	 *
	 * @return True if it is a root snapshot configuration.
	 * @since 1.8
	 */
	public boolean isRoot() {
		return parent == null;
	}

	/**
	 * Returns the parent snapshot configuration.
	 *
	 * @return The parent snapshot configuration. Null if this is a root snapshot
	 *         configuration.
	 * @since 1.8
	 */
	public SnapshotConfiguration getParent() {
		return parent;
	}

	/**
	 * Returns the snapshot configuration track id.
	 * 
	 * @return The snapshot configuration track id. 0 if this is a root snapshot
	 *         configuration.
	 * @since 1.8
	 */
	public int getTrackId() {
		return track.isEmpty() ? 0 : track.get(track.size() - 1);
	}

	/**
	 * Returns the track. This is a clone of the original track.
	 * 
	 * @return The track.
	 * @since 1.8
	 */
	public List<Integer> getTrack() {
		return new ArrayList<Integer>(track);
	}

	/**
	 * Returns the configuration.
	 *
	 * @return The configuration.
	 * @since 1.8
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Returns the sandbox.
	 *
	 * @return The sandbox. Null if snapshot is inconsistent.
	 * @since 1.8
	 */
	public Sandbox getSandbox() {
		return isConsistent() ? sandbox : null;
	}

	/**
	 * Returns the container for derived snapshots.
	 *
	 * @return The container for derived snapshots. Null if snapshot is
	 *         inconsistent.
	 * @since 1.8
	 */
	public DerivedContainer getDerivedContainer() {
		return isConsistent() ? derivedContainer : null;
	}

	/**
	 * Returns the label.
	 * 
	 * @return The label. Null if not available.
	 * @since 1.8
	 */
	public String getLabel() {
		return isConsistent() ? configuration.snapshot.getLabel() : null;
	}

	/**
	 * Returns the process state.
	 * 
	 * @return The process state. Null if snapshot is inconsistent or the process
	 *         was not assigned.
	 * @since 1.8
	 */
	public Job.State getProcessState() {
		return isConsistent() ? Job.State.getState(configuration.process.getState()) : null;
	}

	/**
	 * Returns true if the process on the snapshot was assigned.
	 * 
	 * @return True if the process on the snapshot was assigned.
	 * @since 1.8
	 */
	public boolean isProcessAssigned() {
		return isConsistent() && Job.State.getState(configuration.process.getState()) != null;
	}

	/**
	 * Returns true if the process on the snapshot has been executed and completed.
	 * 
	 * @return True if the process on the snapshot has been executed and completed.
	 * @since 1.8
	 */
	public boolean isProcessCompleted() {
		return isConsistent() && de.uniwuerzburg.zpd.ocr4all.application.persistence.job.Process.State.completed
				.equals(configuration.process.getState());
	}

	/**
	 * Returns true if the process on the snapshot has been executed.
	 * 
	 * @return True if the process on the snapshot has been executed.
	 * @since 1.8
	 */
	public boolean isProcessExecuted() {
		return isConsistent() && (de.uniwuerzburg.zpd.ocr4all.application.persistence.job.Process.State.completed
				.equals(configuration.process.getState())
				|| de.uniwuerzburg.zpd.ocr4all.application.persistence.job.Process.State.canceled
						.equals(configuration.process.getState())
				|| de.uniwuerzburg.zpd.ocr4all.application.persistence.job.Process.State.interrupted
						.equals(configuration.process.getState()));
	}

	/**
	 * Returns true if the snapshot allows derived snapshots.
	 * 
	 * @return True if the snapshot allows derived snapshots.
	 * @since 1.8
	 */
	public boolean isAllowDerivedSnapshots() {
		return isProcessCompleted() && configuration.snapshot.getLock() == null;
	}

	/**
	 * Returns true if the given path matches a derived snapshot name.
	 * 
	 * @param path The derived snapshot.
	 * @return True if the given path matches a derived snapshot name.
	 * @since 1.8
	 */
	private static boolean isDerivedSnapshot(Path path) {
		try {
			return Integer.parseInt(path.getFileName().toString()) > 0;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Returns the track for derived snapshot.
	 * 
	 * @param id The derived snapshot id.
	 * @return The track for derived snapshot.
	 * @since 1.8
	 */
	private List<Integer> getTrackDerivedSnapshot(int id) {
		List<Integer> track = getTrack();
		track.add(id);

		return track;
	}

	/**
	 * Returns the derived snapshot with given id if it is consistent.
	 * 
	 * @param id The id.
	 * @return The derived snapshot. Null if unknown or it is inconsistent.
	 * @since 1.8
	 */
	public SnapshotConfiguration getDerived(int id) {
		return getDerived(true, id);
	}

	/**
	 * Returns the derived snapshot with given id.
	 * 
	 * @param isConsistent True if returns only a consistent derived snapshot.
	 * @param id           The id.
	 * @return The derived snapshot. Null if unknown or the consistent flag is set
	 *         and it is inconsistent.
	 * @since 1.8
	 */
	public SnapshotConfiguration getDerived(boolean isConsistent, int id) {
		Path path = Paths.get(derivedContainer.getFolder().toString(), "" + id);

		SnapshotConfiguration snapshot = Files.isDirectory(path)
				? new SnapshotConfiguration(properties, this, path, getTrackDerivedSnapshot(id), user)
				: null;

		return snapshot == null || (isConsistent && !snapshot.isConsistent()) ? null : snapshot;
	}

	/**
	 * Returns the consistent derived snapshots sorted by label.
	 * 
	 * @return The consistent derived snapshots sorted by label.
	 * @since 1.8
	 */
	public List<SnapshotConfiguration> getDerived() {
		return getDerived(true);
	}

	/**
	 * Returns the derived snapshots sorted by label.
	 * 
	 * @param isConsistent True if returns only the consistent derived snapshots.
	 * @return The derived snapshots sorted by label.
	 * @since 1.8
	 */
	public List<SnapshotConfiguration> getDerived(boolean isConsistent) {
		List<SnapshotConfiguration> snapshots = new ArrayList<>();

		try (Stream<Path> stream = Files.list(derivedContainer.getFolder())) {
			for (Path path : stream.filter(path -> Files.isDirectory(path)).filter(path -> isDerivedSnapshot(path))
					.collect(Collectors.toList())) {
				SnapshotConfiguration snapshot = new SnapshotConfiguration(properties, this, path,
						getTrackDerivedSnapshot(Integer.parseInt(path.getFileName().toString())), user);

				if (!isConsistent || snapshot.isConsistent())
					snapshots.add(snapshot);
			}

			Collections.sort(snapshots, new Comparator<SnapshotConfiguration>() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
				 */
				@Override
				public int compare(SnapshotConfiguration o1, SnapshotConfiguration o2) {
					String label1 = o1.getLabel();
					String label2 = o2.getLabel();

					if (label2 == null)
						return -1;
					else if (label1 == null)
						return 1;
					else
						return label1.compareToIgnoreCase(label2);
				}
			});

			return snapshots;
		} catch (IOException e) {
			logger.warn("can not recover derived snapshots directories - " + e.getMessage());

			return snapshots;
		}
	}

	/**
	 * Creates a derived snapshot. A derived can be created only when a process has
	 * been executed and completed on the snapshot and the snapshot is not locked.
	 * 
	 * @param type            The type.
	 * @param label           The label. It can not be null nor blank.
	 * @param description     The description.
	 * @param serviceProvider The service provider. It can not be null and its id
	 *                        can not be null nor blank.
	 * @param instance        The instance.
	 * @return The created derived snapshot. Null if could not be created.
	 * @since 1.8
	 */
	public SnapshotConfiguration createDerived(Snapshot.Type type, String label, String description,
			ServiceProvider serviceProvider, Instance instance) {
		if (isAllowDerivedSnapshots())
			try (Stream<Path> stream = Files.list(derivedContainer.getFolder())) {
				int id = 0;
				for (String number : stream.filter(path -> isDerivedSnapshot(path)).map(Path::getFileName)
						.map(Path::toString).collect(Collectors.toList()))
					id = Math.max(id, Integer.parseInt(number));

				id++;

				return new SnapshotConfiguration(properties, this,
						Paths.get(derivedContainer.getFolder().toString(), "" + id), getTrackDerivedSnapshot(id), user,
						type, label, description, serviceProvider, instance);
			} catch (IOException e) {
				logger.warn("can not create derived snapshot - " + e.getMessage());
			}

		return null;
	}

	/**
	 * Removes the derived snapshot with given id.
	 * 
	 * @param id The id of the derived snapshot to remove.
	 * @return True if the derived snapshot could be removed.
	 * @since 1.8
	 */
	public boolean removeDerived(int id) {
		return id > 0 && delete(Paths.get(derivedContainer.getFolder().toString(), "" + id));
	}

	/**
	 * Remove the derived snapshots.
	 * 
	 * @return True if the derived snapshots could be removed.
	 * @since 1.8
	 */
	public boolean removeDerived() {
		return derivedContainer.reset();
	}

	/**
	 * Returns the identifier for logging.
	 * 
	 * @return The identifier for logging.
	 * @since 1.8
	 */
	public String getLoggerIdentifier() {
		return getLabel() == null ? track.toString() : getLabel();
	}

	/**
	 * Defines configurations for the snapshots.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public class Configuration extends CoreFolder implements TrackingData {
		/**
		 * The main configuration persistence manager.
		 */
		private final PersistenceManager mainConfigurationManager;

		/**
		 * The process configuration persistence manager.
		 */
		private final PersistenceManager processConfigurationManager;

		/**
		 * The snapshot.
		 */
		private Snapshot snapshot = null;

		/**
		 * The process.
		 */
		private de.uniwuerzburg.zpd.ocr4all.application.persistence.job.Process process = null;

		/**
		 * The main configuration file.
		 */
		private final Path mainFile;

		/**
		 * The process configuration file.
		 */
		private final Path processFile;

		/**
		 * Creates a configuration for the snapshot.
		 * 
		 * @param properties The configuration properties for the snapshot.
		 * @param user       The user.
		 * @since 1.8
		 */
		private Configuration(Snapshots.Configuration properties) {
			this(properties, false, null, null, null, null, null);
		}

		/**
		 * Creates a configuration for the snapshot.
		 * 
		 * @param properties      The configuration properties for the snapshot.
		 * @param type            The type.
		 * @param label           The label. It can not be null nor blank.
		 * @param description     The description.
		 * @param serviceProvider The service provider. It can not be null and its id
		 *                        can not be null nor blank.
		 * @param instance        The instance.
		 * @since 1.8
		 */
		private Configuration(Snapshots.Configuration properties, Snapshot.Type type, String label, String description,
				ServiceProvider serviceProvider, Instance instance) {
			this(properties, true, type, label, description, serviceProvider, instance);
		}

		/**
		 * Creates a configuration for the snapshot.
		 * 
		 * @param properties      The configuration properties for the snapshot.
		 * @param isInitialize    True if initialize.
		 * @param type            The type.
		 * @param label           The label. It can not be null nor blank.
		 * @param description     The description.
		 * @param serviceProvider The service provider. It can not be null and its id
		 *                        can not be null nor blank.
		 * @param instance        The instance.
		 * @since 1.8
		 */
		private Configuration(Snapshots.Configuration properties, boolean isInitialize, Snapshot.Type type,
				String label, String description, ServiceProvider serviceProvider, Instance instance) {
			super(Paths.get(SnapshotConfiguration.this.folder.toString(), properties.getFolder()));

			// configuration files
			mainFile = getPath(properties.getFiles().getMain());
			processFile = getPath(properties.getFiles().getProcess());

			// configuration managers
			mainConfigurationManager = new PersistenceManager(mainFile, Type.project_sandbox_snapshot_v1);
			processConfigurationManager = new PersistenceManager(processFile, Type.job_process_v1);

			if (isInitialize) {
				ConfigurationService.initializeFolder(true, folder,
						"snapshot '" + (label == null ? track.toString() : label) + "' configuration");

				initialize(type, label, description, serviceProvider, instance);
			} else
				loadConfigurations();
		}

		/**
		 * Initializes the configuration files with the current update timestamp.
		 * 
		 * @param type            The type.
		 * @param label           The label. It can not be null nor blank.
		 * @param description     The description.
		 * @param serviceProvider The service provider. It can not be null and its id
		 *                        can not be null nor blank.
		 * @param instance        The instance.
		 * @since 1.8
		 */
		private void initialize(Snapshot.Type type, String label, String description, ServiceProvider serviceProvider,
				Instance instance) {
			if (label != null && !label.isBlank() && serviceProvider != null && serviceProvider.getId() != null
					&& !serviceProvider.getId().isBlank()) {
				final String loggerIdentifier = label == null ? track.toString() : label;
				try {
					process = new de.uniwuerzburg.zpd.ocr4all.application.persistence.job.Process(user);

					processConfigurationManager.persist(process);

					logger.info(
							"Persisted the initial process configuration of the snapshot '" + loggerIdentifier + "'.");
				} catch (IOException e) {
					logger.warn("Could not persist the initial process configuration of the snapshot '"
							+ loggerIdentifier + "' - " + e.getMessage());

					try {
						Files.delete(processFile);
					} catch (Exception ex) {
						// Nothing to do
					}

					process = null;
				}

				if (process != null) {
					serviceProvider.setId(serviceProvider.getId().trim());

					try {
						snapshot = new Snapshot(type, label, description, serviceProvider, instance, user);

						mainConfigurationManager.persist(snapshot);

						logger.info(
								"Persisted the initial main configuration of the snapshot '" + loggerIdentifier + "'.");
					} catch (IOException e) {
						logger.warn("Could not persist the initial main configuration of the snapshot '"
								+ loggerIdentifier + "' - " + e.getMessage());

						try {
							Files.delete(processFile);
						} catch (Exception ex) {
							// Nothing to do
						}

						try {
							Files.delete(mainFile);
						} catch (Exception ex) {
							// Nothing to do
						}

						process = null;
						snapshot = null;
					}
				}
			}
		}

		/**
		 * Loads the configuration files.
		 * 
		 * @return True if the main configuration is available.
		 * @since 1.8
		 */
		private void loadConfigurations() {
			loadMainConfiguration();
			loadProcessConfiguration();
		}

		/**
		 * Loads the main configuration file.
		 * 
		 * @since 1.8
		 */
		private void loadMainConfiguration() {
			try {
				snapshot = mainConfigurationManager.getEntity(Snapshot.class, null, message -> logger.warn(message));
			} catch (IOException e) {
				logger.warn(e.getMessage());

				snapshot = null;
			}
		}

		/**
		 * Loads the process configuration file.
		 * 
		 * @since 1.8
		 */
		private void loadProcessConfiguration() {
			try {
				process = processConfigurationManager.getEntity(
						de.uniwuerzburg.zpd.ocr4all.application.persistence.job.Process.class, null,
						message -> logger.warn(message));
			} catch (IOException e) {
				logger.warn(e.getMessage());

				snapshot = null;
			}
		}

		/**
		 * Returns true if the main configuration is available.
		 * 
		 * @return True if the main configuration is available.
		 * @since 1.8
		 */
		public boolean isMainConfigurationAvailable() {
			return snapshot != null;
		}

		/**
		 * Returns true if the process configuration is available, this means, a process
		 * is assigned to the snapshot.
		 * 
		 * @return True if the process configuration is available.
		 * @since 1.8
		 */
		public boolean isProcessConfigurationAvailable() {
			return process != null;
		}

		/**
		 * Reloads the main configuration file.
		 * 
		 * @return True if the main configuration is available.
		 * @since 1.8
		 */
		public boolean reloadMainConfiguration() {
			loadMainConfiguration();

			return isMainConfigurationAvailable();
		}

		/**
		 * Reloads the process configuration file.
		 * 
		 * @return True if the process configuration is available.
		 * @since 1.8
		 */
		public boolean reloadProcessConfiguration() {
			loadProcessConfiguration();

			return isProcessConfigurationAvailable();
		}

		/**
		 * Updates the main configuration.
		 * 
		 * @param label       The label. It can not be null nor empty.
		 * @param description The description.
		 * @return True if the main configuration was updated.
		 * @since 1.8
		 */
		public boolean updateMainConfiguration(String label, String description) {
			if (label != null && !label.isBlank() && reloadMainConfiguration())
				try {
					snapshot.setLabel(label.trim());
					snapshot.setDescription(description == null || description.isBlank() ? null : description.trim());

					snapshot.setUpdated(new Date());

					processConfigurationManager.persist(snapshot);

					logger.info("Updated the main configuration of the snapshot '" + getLoggerIdentifier() + "'.");

					return true;
				} catch (IOException e) {
					logger.warn("Could not update the main configuration of the snapshot '" + getLoggerIdentifier()
							+ "' - " + e.getMessage());

					loadMainConfiguration();
				}

			return false;
		}

		/**
		 * Locks the snapshot with current created time.
		 * 
		 * @param source  The source. The source can not be null or empty.
		 * @param comment The comment.
		 * @return True if the main configuration was updated.
		 * @since 1.8
		 */
		public boolean lockSnapshot(String source, String comment) {
			if (source != null && !source.isBlank() && reloadMainConfiguration())
				try {
					snapshot.setLock(new Snapshot.Lock(source, comment));

					snapshot.setUpdated(new Date());

					mainConfigurationManager.persist(snapshot);

					logger.info("Locked snapshot '" + getLoggerIdentifier() + "'.");

					return true;
				} catch (IOException e) {
					logger.warn("Could not lock snapshot '" + getLoggerIdentifier() + "' - " + e.getMessage());

					loadMainConfiguration();
				}

			return false;
		}

		/**
		 * Unlocks the snapshot.
		 * 
		 * @return True if the snapshot was locked and the main configuration was
		 *         updated.
		 * @since 1.8
		 */
		public boolean unlockSnapshot() {
			if (reloadMainConfiguration() && snapshot.getLock() != null)
				try {
					snapshot.setLock(null);

					snapshot.setUpdated(new Date());

					mainConfigurationManager.persist(snapshot);

					logger.info("unlocked snapshot '" + getLoggerIdentifier() + "'.");

					return true;
				} catch (IOException e) {
					logger.warn("Could not unlock snapshot '" + getLoggerIdentifier() + "' - " + e.getMessage());

					loadMainConfiguration();
				}

			return false;
		}

		/**
		 * Updates the process state if it is non null and the process configuration is
		 * available.
		 * 
		 * @param state The new state.
		 * @return True if the process state was updated.
		 * @since 1.8
		 */
		public boolean updateProcess(Job.State state) {
			if (state != null)
				synchronized (process) {
					if (reloadProcessConfiguration())
						try {
							process.setState(state.getPersistence());

							process.setUpdated(new Date());
							processConfigurationManager.persist(process);

							logger.info("Updated the process state of the snapshot '" + getLoggerIdentifier() + "'.");

							return true;
						} catch (IOException e) {
							logger.warn("Could not update the process state of the snapshot '" + getLoggerIdentifier()
									+ "' - " + e.getMessage());

							loadProcessConfiguration();
						}

				}

			return false;
		}

		/**
		 * Updates the process progress if the process configuration is available.
		 * 
		 * @param progress The progress. This is a value between 0 and 1 inclusive.
		 * @return True if the process progress was updated.
		 * @since 1.8
		 */
		public boolean updateProcess(float progress) {
			if (progress >= 0 && progress <= 1)
				synchronized (process) {
					if (reloadProcessConfiguration())
						try {
							process.setProgress(progress);

							process.setUpdated(new Date());
							processConfigurationManager.persist(process);

							logger.info(
									"Updated the process progress of the snapshot '" + getLoggerIdentifier() + "'.");

							return true;
						} catch (IOException e) {
							logger.warn("Could not update the process progress of the snapshot '"
									+ getLoggerIdentifier() + "' - " + e.getMessage());

							loadProcessConfiguration();
						}
				}

			return false;
		}

		/**
		 * Updates the process standard output message if the process configuration is
		 * available.
		 * 
		 * @param message The message.
		 * @return True if the process configuration was updated.
		 * @since 1.8
		 */
		public boolean updateProcessStandardOutput(String message) {
			synchronized (process) {
				if (reloadProcessConfiguration())
					try {
						process.setStandardOutput(message);

						process.setUpdated(new Date());
						processConfigurationManager.persist(process);

						logger.info("Updated the process standard output messages of the snapshot '"
								+ getLoggerIdentifier() + "'.");

						return true;
					} catch (IOException e) {
						logger.warn("Could not update the process standard output messages of the snapshot '"
								+ getLoggerIdentifier() + "' - " + e.getMessage());

						loadProcessConfiguration();
					}
			}

			return false;
		}

		/**
		 * Updates the process standard error message if the process configuration is
		 * available.
		 * 
		 * @param message The message.
		 * @return True if the process configuration was updated.
		 * @since 1.8
		 */
		public boolean updateProcessStandardError(String message) {
			synchronized (process) {
				if (reloadProcessConfiguration())
					try {
						process.setStandardError(message);

						process.setUpdated(new Date());
						processConfigurationManager.persist(process);

						logger.info("Updated the process standard error messages of the snapshot '"
								+ getLoggerIdentifier() + "'.");

						return true;
					} catch (IOException e) {
						logger.warn("Could not update the process standard error messages of the snapshot '"
								+ getLoggerIdentifier() + "' - " + e.getMessage());

						loadProcessConfiguration();
					}
			}

			return false;
		}

		/**
		 * Updates the process note if the process configuration is available.
		 * 
		 * @param note The note.
		 * @return True if the process configuration was updated.
		 * @since 1.8
		 */
		public boolean updateProcessNote(String note) {
			synchronized (process) {
				if (reloadProcessConfiguration())
					try {
						process.setNote(note);

						process.setUpdated(new Date());
						processConfigurationManager.persist(process);

						logger.info("Updated the process note of the snapshot '" + getLoggerIdentifier() + "'.");

						return true;
					} catch (IOException e) {
						logger.warn("Could not update the process note of the snapshot '" + getLoggerIdentifier()
								+ "' - " + e.getMessage());

						loadProcessConfiguration();
					}
			}

			return false;
		}

		/**
		 * Updates the process core data if the process configuration is available.
		 * 
		 * @param state          The state.
		 * @param progress       The progress. This is a value between 0 and 1
		 *                       inclusive.
		 * @param standardOutput The standard output message.
		 * @param standardError  The standard error message.
		 * @param note           The note.
		 * @return True if the process progress was updated.
		 * @since 1.8
		 */
		public boolean updateProcess(Job.State state, float progress, String standardOutput, String standardError,
				String note) {
			synchronized (process) {
				if (reloadProcessConfiguration())
					try {
						if (state != null)
							process.setState(state.getPersistence());

						if (progress >= 0 && progress <= 1)
							process.setProgress(progress);

						process.setStandardOutput(standardOutput);
						process.setStandardError(standardError);
						process.setNote(note);

						process.setUpdated(new Date());
						processConfigurationManager.persist(process);

						logger.info("Updated the process core data of the snapshot '" + getLoggerIdentifier() + "'.");

						return true;
					} catch (IOException e) {
						logger.warn("Could not update the process core data of the snapshot '" + getLoggerIdentifier()
								+ "' - " + e.getMessage());

						loadProcessConfiguration();
					}
			}

			return false;
		}

		/**
		 * Returns the main configuration.
		 * 
		 * @return The main configuration.
		 * @since 1.8
		 */
		public Snapshot getMainConfiguration() {
			try {
				return mainConfigurationManager.getEntity(Snapshot.class, null, message -> logger.warn(message));
			} catch (IOException e) {
				logger.warn(e.getMessage());

				return null;
			}
		}

		/**
		 * Returns the process configuration.
		 * 
		 * @return The process configuration.
		 * @since 1.8
		 */
		public de.uniwuerzburg.zpd.ocr4all.application.persistence.job.Process getProcessConfiguration() {
			try {
				return processConfigurationManager.getEntity(
						de.uniwuerzburg.zpd.ocr4all.application.persistence.job.Process.class, null,
						message -> logger.warn(message));
			} catch (IOException e) {
				logger.warn(e.getMessage());

				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.uniwuerzburg.zpd.ocr4all.application.core.configuration.TrackingData#
		 * isUserSet()
		 */
		@Override
		public boolean isUserSet() {
			return getUser() != null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.uniwuerzburg.zpd.ocr4all.application.core.configuration.TrackingData#
		 * getUser()
		 */
		@Override
		public String getUser() {
			return isMainConfigurationAvailable() ? snapshot.getUser() : null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.uniwuerzburg.zpd.ocr4all.application.core.configuration.TrackingData#
		 * isCreatedSet()
		 */
		@Override
		public boolean isCreatedSet() {
			return getCreated() != null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.uniwuerzburg.zpd.ocr4all.application.core.configuration.TrackingData#
		 * getCreated()
		 */
		@Override
		public Date getCreated() {
			return isMainConfigurationAvailable() ? snapshot.getDate() : null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.uniwuerzburg.zpd.ocr4all.application.core.configuration.TrackingData#
		 * isUpdatedSet()
		 */
		@Override
		public boolean isUpdatedSet() {
			return getUpdated() != null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.uniwuerzburg.zpd.ocr4all.application.core.configuration.TrackingData#
		 * getUpdated()
		 */
		@Override
		public Date getUpdated() {
			return isMainConfigurationAvailable() ? snapshot.getUpdated() : null;
		}
	}

	/**
	 * Sandbox is an immutable class that defines sandboxes for snapshots.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public class Sandbox extends CoreFolder {
		/**
		 * Creates a sandbox for a snapshot.
		 * 
		 * @param properties The configuration properties for the snapshot.
		 * @since 1.8
		 */
		public Sandbox(Snapshots.Snapshot properties) {
			super(Paths.get(SnapshotConfiguration.this.folder.toString(), properties.getSandbox().getFolder()));

			/*
			 * Initialize the snapshots folder
			 */
			ConfigurationService.initializeFolder(true, folder,
					"snapshot '" + SnapshotConfiguration.this.folder.getFileName() + "' sandbox");

		}

		/**
		 * Resets the sandbox.
		 *
		 * @return True if the sandbox could be reset.
		 * 
		 * @since 1.8
		 */
		public boolean reset() {
			return deleteContents();
		}
	}

	/**
	 * DerivedContainer is an immutable class that defines containers for derived
	 * snapshots.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public class DerivedContainer extends CoreFolder {
		/**
		 * Creates a container for derived snapshots.
		 * 
		 * @param properties The configuration properties for the snapshot.
		 * @since 1.8
		 */
		public DerivedContainer(Snapshots.Snapshot properties) {
			super(Paths.get(SnapshotConfiguration.this.folder.toString(), properties.getDerived().getFolder()));

			/*
			 * Initialize the snapshots folder
			 */
			ConfigurationService.initializeFolder(true, folder,
					"snapshot '" + SnapshotConfiguration.this.folder.getFileName() + "' derived");
		}

		/**
		 * Resets the container for derived snapshots.
		 *
		 * @return True if the container could be reset.
		 * 
		 * @since 1.8
		 */
		public boolean reset() {
			return deleteContents();
		}
	}

}
