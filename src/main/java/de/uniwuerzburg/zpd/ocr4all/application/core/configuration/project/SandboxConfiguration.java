/**
 * File:     SandboxConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     14.04.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Set;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.CoreFolder;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.TrackingData;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project.Sandboxes;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Sandbox;

/**
 * Defines configurations for the sandboxes.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class SandboxConfiguration extends CoreFolder {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SandboxConfiguration.class);

	/**
	 * The configuration.
	 */
	private final Configuration configuration;

	/**
	 * The snapshots configuration.
	 */
	private final SnapshotsConfiguration snapshots;

	/**
	 * The configuration properties for the sandbox.
	 */
	private final Sandboxes.Sandbox properties;

	/**
	 * Creates a configuration for a sandbox.
	 * 
	 * @param properties The configuration properties for the sandbox.
	 * @param folder     The sandbox folder.
	 * @param user       The user.
	 * @since 1.8
	 */
	SandboxConfiguration(Sandboxes.Sandbox properties, Path folder, String user) {
		super(folder);

		this.properties = properties;

		snapshots = new SnapshotsConfiguration(properties.getSnapshots(), folder, user);

		configuration = new Configuration(properties.getConfiguration(), user);
	}

	/**
	 * Returns the mets file name.
	 *
	 * @return The mets file name.
	 * @since 1.8
	 */
	public String getMetsFileName() {
		return properties.getMets().getFile();
	}

	/**
	 * Returns the mets group.
	 *
	 * @return The mets group.
	 * @since 1.8
	 */
	public String getMetsGroup() {
		return properties.getMets().getGroup();
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
	 * Returns the snapshots configuration.
	 *
	 * @return The snapshots configuration.
	 * @since 1.8
	 */
	public SnapshotsConfiguration getSnapshots() {
		return snapshots;
	}

	/**
	 * Defines configurations for the sandbox.
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
		 * The sandbox.
		 */
		private Sandbox sandbox = null;

		/**
		 * The main configuration file.
		 */
		private final Path mainFile;

		/**
		 * The history configuration file.
		 */
		private final Path historyFile;

		/**
		 * The user.
		 */
		private String user;

		/**
		 * Creates a configuration for the sandbox.
		 * 
		 * @param properties The configuration properties for the sandbox.
		 * @param user       The user.
		 * @since 1.8
		 */
		Configuration(Sandboxes.Configuration properties, String user) {
			super(Paths.get(SandboxConfiguration.this.folder.toString(), properties.getFolder()));

			this.user = user;

			/*
			 * Initialize the sandbox configuration folder
			 */
			ConfigurationService.initializeFolder(true, folder,
					"sandbox '" + SandboxConfiguration.this.folder.getFileName() + "' configuration");

			// Initializes the configuration files
			mainFile = getPath(properties.getFiles().getMain());
			historyFile = getPath(properties.getFiles().getHistory());

			// Loads the main configuration file
			mainConfigurationManager = new PersistenceManager(mainFile, Type.project_sandbox_v1);

			loadMainConfiguration();
		}

		/**
		 * Loads the main configuration file.
		 * 
		 * @since 1.8
		 */
		private void loadMainConfiguration() {
			// Load main configuration
			try {
				Date currentTimeStamp = new Date();

				sandbox = mainConfigurationManager.getEntity(Sandbox.class, null, message -> logger.warn(message));

				if (!isMainConfigurationAvailable() || sandbox.getName() == null || sandbox.getState() == null
						|| !isStateConsistent()) {
					if (!isMainConfigurationAvailable()) {
						sandbox = new Sandbox();

						sandbox.setDate(currentTimeStamp);
					}

					sandbox.setName(getName());

					if (sandbox.getState() == null || !isStateConsistent())
						sandbox.setState(Sandbox.State.secured);

					persist(currentTimeStamp);
				}
			} catch (IOException e) {
				logger.warn(e.getMessage());

				sandbox = null;
			}
		}

		/**
		 * Returns true if the main configuration is available.
		 * 
		 * @return True if the main configuration is available.
		 * @since 1.8
		 */
		public boolean isMainConfigurationAvailable() {
			return sandbox != null;
		}

		/**
		 * Persist the main configuration with current update time stamp.
		 * 
		 * @return True if the main configuration could be persisted.
		 * @since 1.8
		 */
		private boolean persist() {
			return persist(null);
		}

		/**
		 * Persist the main configuration.
		 * 
		 * @param updated The updated time. If null, uses the current time stamp.
		 * @return True if the main configuration could be persisted.
		 * @since 1.8
		 */
		private boolean persist(Date updated) {
			if (isMainConfigurationAvailable())
				try {
					sandbox.setUser(user);
					sandbox.setUpdated(updated == null ? new Date() : updated);

					mainConfigurationManager.persist(sandbox);

					logger.info("Persisted the main configuration of the sandbox '" + sandbox.getName() + "'.");

					return true;
				} catch (Exception e) {
					logger.warn("Could not persist the main configuration of the sandbox '" + sandbox.getName() + "' - "
							+ e.getMessage());
				}

			return false;
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
		 * Returns the id.
		 *
		 * @return The id.
		 * @since 1.8
		 */
		public String getId() {
			return SandboxConfiguration.this.folder.getFileName().toString();
		}

		/**
		 * Returns the name.
		 *
		 * @return The name.
		 * @since 1.8
		 */
		public String getName() {
			return isMainConfigurationAvailable() && sandbox.getName() != null ? sandbox.getName()
					: SandboxConfiguration.this.folder.getFileName().toString();
		}

		/**
		 * Set the name and persists the main configuration if the main configuration is
		 * available and the given name is not null and empty.
		 *
		 * @param name The name to set.
		 * @return True if the main configuration could be persisted.
		 * @since 1.8
		 */
		public boolean setName(String name) {
			if (isMainConfigurationAvailable() && name != null && !name.isBlank()) {
				sandbox.setName(name.trim());

				return persist();
			} else
				return false;
		}

		/**
		 * Returns true if the the description is set.
		 *
		 * @return True if the the description is set.
		 * @since 1.8
		 */
		public boolean isDescriptionSet() {
			return getDescription() != null;
		}

		/**
		 * Returns the description.
		 *
		 * @return The description. Null if not set or the main configuration is not
		 *         available.
		 * @since 1.8
		 */
		public String getDescription() {
			return isMainConfigurationAvailable() ? sandbox.getDescription() : null;
		}

		/**
		 * Set the description and persists the main configuration if the main
		 * configuration is available.
		 *
		 * @param description The description to set.
		 * @return True if the main configuration could be persisted.
		 * @since 1.8
		 */
		public boolean setDescription(String description) {
			if (isMainConfigurationAvailable()) {
				sandbox.setDescription(description == null || description.isBlank() ? null : description.trim());

				return persist();
			} else
				return false;
		}

		/**
		 * Returns the keywords.
		 *
		 * @return The keywords.
		 * @since 1.8
		 */
		public Set<String> getKeywords() {
			return isMainConfigurationAvailable() ? sandbox.getKeywords() : null;
		}

		/**
		 * Set the keywords.
		 *
		 * @param keywords The keywords to set.
		 * @return True if the main configuration could be persisted.
		 * @since 1.8
		 */
		public boolean setKeywords(Set<String> keywords) {
			if (isMainConfigurationAvailable()) {
				sandbox.setKeywords(keywords);

				return persist();
			} else
				return false;
		}

		/**
		 * Set the basic data and persists the main configuration if the main
		 * configuration is available and the given name is not null and empty.
		 *
		 * @param name        The name to set.
		 * @param description The description to set.
		 * @param keywords    The keywords to set.
		 * @param state       The state. If null, do not update the state.
		 * @return True if the basic data was updated and persisted.
		 * @since 1.8
		 */
		public boolean updateBasicData(String name, String description, Set<String> keywords, Sandbox.State state) {
			if (state == null)
				state = getState();

			if (isMainConfigurationAvailable() && name != null && !name.isBlank()
					&& (state.equals(getState()) || isUpdate(state))) {
				sandbox.setName(name.trim());
				sandbox.setDescription(description == null || description.isBlank() ? null : description.trim());
				sandbox.setKeywords(keywords);

				Date currentTimeStamp = new Date();

				boolean wasDone = isDone();
				sandbox.setState(state);
				boolean isDone = isDone();

				if (wasDone != isDone)
					sandbox.setDone(isDone ? currentTimeStamp : null);

				return persist(currentTimeStamp);
			} else
				return false;
		}

		/**
		 * Returns the state.
		 *
		 * @return The state. Null if not set or the main configuration is not
		 *         available.
		 * @since 1.8
		 */
		public Sandbox.State getState() {
			return isMainConfigurationAvailable() ? sandbox.getState() : null;
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
			return isMainConfigurationAvailable() ? sandbox.getUser() : null;
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
			return isMainConfigurationAvailable() ? sandbox.getDate() : null;
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
			return isMainConfigurationAvailable() ? sandbox.getUpdated() : null;
		}

		/**
		 * Returns true if the done time is set.
		 *
		 * @return True if the done time is set.
		 * @since 1.8
		 */
		public boolean isDoneSet() {
			return getDone() != null;
		}

		/**
		 * Returns the done time.
		 *
		 * @return The done time. Null if not done or the main configuration is not
		 *         available.
		 * @since 1.8
		 */
		public Date getDone() {
			return isMainConfigurationAvailable() ? sandbox.getDone() : null;
		}

		/**
		 * Returns true if the sandbox requires special right.
		 * 
		 * @return True if the sandbox is available and requires special right.
		 * @since 1.8
		 */
		public boolean isSpecialRightRequired() {
			return isMainConfigurationAvailable()
					&& (Sandbox.State.secured.equals(getState()) || Sandbox.State.canceled.equals(getState()));
		}

		/**
		 * Returns true if the sandbox is done.
		 * 
		 * @return True if the sandbox is available and done.
		 * @since 1.8
		 */
		public boolean isDone() {
			return isMainConfigurationAvailable()
					&& (Sandbox.State.canceled.equals(getState()) || Sandbox.State.closed.equals(getState()));
		}

		/**
		 * Sets the sandbox state and persists the main configuration if the state was
		 * updated.
		 * 
		 * @param state The state to set.
		 * @return True if the sandbox is available and the state was updated and
		 *         persisted.
		 * @since 1.8
		 */
		private boolean setState(Sandbox.State state) {
			// Test for state diagram consistency
			if (!isUpdate(state))
				return false;

			// Update the state
			Date currentTimeStamp = new Date();

			boolean wasDone = isDone();
			sandbox.setState(state);
			boolean isDone = isDone();

			if (wasDone != isDone)
				sandbox.setDone(isDone ? currentTimeStamp : null);

			return persist(currentTimeStamp);
		}

		/**
		 * Returns true if the state can be updated to given state.
		 * 
		 * @param state The state.
		 * @return True if the state can be updated to given state. If the current state
		 *         matches the given state, returns false.
		 * @since 1.8
		 */
		public boolean isUpdate(Sandbox.State state) {
			if (state == null || !isMainConfigurationAvailable() || state.equals(getState()))
				return false;

			// Test for state diagram consistency
			if (Sandbox.State.secured.equals(state))
				return true;
			else {
				/*
				 * The sandbox state depends on the state of the process of the root snapshot
				 */
				if (!snapshots.getRoot().isProcessCompleted())
					return Sandbox.State.canceled.equals(state);
				else
					switch (getState()) {
					case paused:
						return Sandbox.State.active.equals(state);
					case closed:
						return Sandbox.State.canceled.equals(state);
					case canceled:
						return Sandbox.State.closed.equals(state);
					case active:
					case secured:
					default:
						return true;
					}
			}
		}

		/**
		 * Returns true if the state is consistent.
		 * 
		 * @return True if the state is consistent.
		 * @since 1.8
		 */
		private boolean isStateConsistent() {
			if (!isMainConfigurationAvailable())
				return false;

			/*
			 * The sandbox state depends on the state of the process of the root snapshot
			 */
			return snapshots.getRoot() == null || !snapshots.getRoot().isProcessCompleted()
					? Sandbox.State.secured.equals(getState()) || Sandbox.State.canceled.equals(getState())
					: true;
		}

		/**
		 * Secures the sandbox and persists the main configuration if the state was
		 * updated. The sandbox can be secured from all other current states.
		 * 
		 * @return True if the sandbox is available and could be activated.
		 * @since 1.8
		 */
		public boolean secure() {
			return setState(Sandbox.State.secured);
		}

		/**
		 * Activates the sandbox and persists the main configuration if the state was
		 * updated. The sandbox can only be activated if the current state is either
		 * secured or paused.
		 * 
		 * @return True if the sandbox is available and could be activated.
		 * @since 1.8
		 */
		public boolean active() {
			return setState(Sandbox.State.active);
		}

		/**
		 * Pauses the sandbox and persists the main configuration if the state was
		 * updated. The sandbox can only be paused if the current state is either
		 * secured or active.
		 * 
		 * @return True if the sandbox is available and could be activated.
		 * @since 1.8
		 */
		public boolean pause() {
			return setState(Sandbox.State.paused);
		}

		/**
		 * Closes the sandbox and persists the main configuration if the state was
		 * updated. The sandbox can only be closed if the current state is either
		 * secured, active or canceled.
		 * 
		 * @return True if the sandbox is available and could be closed.
		 * @since 1.8
		 */
		public boolean close() {
			return setState(Sandbox.State.closed);
		}

		/**
		 * Cancels the sandbox and persists the main configuration if the state was
		 * updated. The sandbox can only be canceled if the current state is either
		 * secured, active or closed.
		 * 
		 * @return True if the sandbox is available and could be closed.
		 * @since 1.8
		 */
		public boolean cancel() {
			return setState(Sandbox.State.canceled);
		}

		/**
		 * Returns true if the history file exists.
		 * 
		 * @return True if the history file exists.
		 * @since 1.8
		 */
		public boolean isHistoryExist() {
			return Files.exists(historyFile);
		}

		/**
		 * Returns the history file.
		 *
		 * @return The history file.
		 * @since 1.8
		 */
		public Path getHistoryFile() {
			return historyFile;
		}
	}

}
