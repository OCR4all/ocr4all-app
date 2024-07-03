/**
 * File:     ProjectConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     24.03.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.CoreFolder;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ExchangeConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.OptConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.TrackingData;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.assemble.AssembleConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.data.DataConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.ImageFormat;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.State;

/**
 * Defines configurations for the projects.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class ProjectConfiguration extends CoreFolder {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProjectConfiguration.class);

	/**
	 * The configuration.
	 */
	private final Configuration configuration;

	/**
	 * The images.
	 */
	private final Images images;

	/**
	 * The configuration for the sandbox container.
	 */
	private final SandboxesConfiguration sandboxesConfiguration;

	/**
	 * Creates a configuration for a project.
	 * 
	 * @param properties            The project properties.
	 * @param exchangeConfiguration The configuration for the exchange.
	 * @param optConfiguration      The configuration for the opt.
	 * @param dataConfiguration     The configuration for the data.
	 * @param assembleConfiguration The configuration for the assemble.
	 * @param folder                The project folder.
	 * @param user                  The user.
	 * @since 1.8
	 */
	public ProjectConfiguration(Project properties, ExchangeConfiguration exchangeConfiguration,
			OptConfiguration optConfiguration, DataConfiguration dataConfiguration,
			AssembleConfiguration assembleConfiguration, Path folder, String user) {
		super(folder);

		configuration = new Configuration(properties.getConfiguration(), exchangeConfiguration, optConfiguration,
				dataConfiguration, assembleConfiguration, user);
		images = new Images(properties.getImages());
		sandboxesConfiguration = new SandboxesConfiguration(properties.getSandboxes(), this);
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
	 * Returns the images.
	 *
	 * @return The images.
	 * @since 1.8
	 */
	public Images getImages() {
		return images;
	}

	/**
	 * Returns the configuration for the sandbox container.
	 *
	 * @return The configuration for the sandbox container.
	 * @since 1.8
	 */
	public SandboxesConfiguration getSandboxesConfiguration() {
		return sandboxesConfiguration;
	}

	/**
	 * Resets the folios.
	 * 
	 * @return True if the folio configuration could be reseted.
	 * @since 1.8
	 */
	public boolean resetFolios() {
		boolean isReset = images.reset();

		if (!configuration.resetFolioConfiguration())
			isReset = false;

		return isReset;
	}

	/**
	 * Defines configurations for the project.
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
		 * The configuration for the exchange.
		 */
		private final ExchangeConfiguration exchangeConfiguration;

		/**
		 * The configuration for the opt.
		 */
		private final OptConfiguration optConfiguration;

		/**
		 * The configuration for the data.
		 */
		private final DataConfiguration dataConfiguration;

		/**
		 * The configuration for the assemble.
		 */
		private final AssembleConfiguration assembleConfiguration;

		/**
		 * The project.
		 */
		private de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project project = null;

		/**
		 * The folio configuration file.
		 */
		private final Path folioFile;

		/**
		 * The history configuration file.
		 */
		private final Path historyFile;

		/**
		 * The user.
		 */
		private String user;

		/**
		 * Creates a configuration for the project.
		 * 
		 * @param properties            The configuration properties for the project.
		 * @param exchangeConfiguration The configuration for the exchange.
		 * @param optConfiguration      The configuration for the opt.
		 * @param dataConfiguration     The configuration for the data.
		 * @param assembleConfiguration The configuration for the assemble.
		 * @param user                  The user.
		 * @since 1.8
		 */
		Configuration(Project.Configuration properties, ExchangeConfiguration exchangeConfiguration,
				OptConfiguration optConfiguration, DataConfiguration dataConfiguration,
				AssembleConfiguration assembleConfiguration, String user) {
			super(Paths.get(ProjectConfiguration.this.folder.toString(), properties.getFolder()));

			this.exchangeConfiguration = exchangeConfiguration;
			this.optConfiguration = optConfiguration;
			this.dataConfiguration = dataConfiguration;
			this.assembleConfiguration = assembleConfiguration;

			this.user = user;

			/*
			 * Initialize the project configuration folder and consequently the project
			 * folder
			 */
			ConfigurationService.initializeFolder(true, folder, "project '" + getId() + "' configuration");

			// Initializes the configuration files
			folioFile = getPath(properties.getFiles().getFolio());
			historyFile = getPath(properties.getFiles().getHistory());

			// Loads the main configuration file
			mainConfigurationManager = new PersistenceManager(getPath(properties.getFiles().getMain()),
					Type.project_v1);
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
				project = mainConfigurationManager.getEntity(
						de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.class, null,
						message -> logger.warn(message));

				if (!isMainConfigurationAvailable() || project.getName() == null || project.getExchange() == null
						|| project.getState() == null || project.getSecurity() == null) {
					Date currentTimeStamp = new Date();
					if (!isMainConfigurationAvailable()) {
						project = new de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project();

						project.setDate(currentTimeStamp);
					}

					project.setName(getName());

					if (project.getExchange() == null)
						updateExchange(null);

					if (project.getState() == null)
						project.setState(
								de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.State.active);

					if (project.getSecurity() == null)
						project.setSecurity(
								new de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.Security());

					persist(currentTimeStamp);
				}
			} catch (IOException e) {
				logger.warn(e.getMessage());

				project = null;
			}
		}

		/**
		 * Returns true if the main configuration is available.
		 * 
		 * @return True if the main configuration is available.
		 * @since 1.8
		 */
		public boolean isMainConfigurationAvailable() {
			return project != null;
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
					project.setUser(user);
					project.setUpdated(updated == null ? new Date() : updated);

					mainConfigurationManager.persist(project);

					logger.info("Persisted the configuration of the project '" + project.getName() + "'.");

					return true;
				} catch (Exception e) {
					reloadMainConfiguration();

					logger.warn("Could not persist the configuration of the project '" + project.getName() + "' - "
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
			return ProjectConfiguration.this.folder.getFileName().toString();
		}

		/**
		 * Returns the name.
		 *
		 * @return The name.
		 * @since 1.8
		 */
		public String getName() {
			return isMainConfigurationAvailable() && project.getName() != null ? project.getName() : getId();
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
				project.setName(name.trim());

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
			return isMainConfigurationAvailable() ? project.getDescription() : null;
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
				project.setDescription(description == null || description.isBlank() ? null : description.trim());

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
			return isMainConfigurationAvailable() ? project.getKeywords() : null;
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
				project.setKeywords(keywords);

				return persist();
			} else
				return false;
		}

		/**
		 * Updates the folder for exchange.
		 * 
		 * @param exchange The folder for exchange. If null, set the default folder for
		 *                 exchange.
		 * @return True if the folder for exchange was updated.
		 * @since 1.8
		 */
		private boolean updateExchange(String exchange) {
			if (isMainConfigurationAvailable()) {
				if (exchange == null) {
					project.setExchange(getId());

					return true;
				} else {
					final Path folder = exchangeConfiguration.getFolder();
					Path path = Paths.get(folder.toString(), exchange).normalize();

					if (path.startsWith(folder) && !folder.equals(path)) {
						project.setExchange(path.toString().substring(folder.toString().length() + 1));

						return true;
					} else
						return false;
				}
			} else
				return false;
		}

		/**
		 * Returns true if the folder for exchange is a directory.
		 *
		 * @return True if the folder is a directory; false if the folder does not
		 *         exist, is not a directory, or it cannot be determined if the folder
		 *         is a directory or not.
		 * 
		 * @since 1.8
		 */
		public boolean isExchangeDirectory() {
			return isMainConfigurationAvailable() && Files.isDirectory(getExchange());
		}

		/**
		 * Returns the folder for exchange.
		 *
		 * @return The folder for exchange.
		 * @since 1.8
		 */
		public Path getExchange() {
			return Paths.get(exchangeConfiguration.getFolder().toString(), project.getExchange()).normalize();
		}

		/**
		 * Returns the subfolder for exchange.
		 *
		 * @return The subfolder for exchange.
		 * @since 1.8
		 */
		public String getExchangeSubfolder() {
			return project.getExchange();
		}

		/**
		 * Returns true if the folder for opt is a directory.
		 *
		 * @return True if the folder is a directory; false if the folder does not
		 *         exist, is not a directory, or it cannot be determined if the folder
		 *         is a directory or not.
		 * 
		 * @since 1.8
		 */
		public boolean isOptDirectory() {
			return isMainConfigurationAvailable() && Files.isDirectory(getOpt());
		}

		/**
		 * Returns the folder for opt.
		 *
		 * @return The folder for opt.
		 * @since 1.8
		 */
		public Path getOpt() {
			return optConfiguration.getFolder().normalize();
		}

		/**
		 * Returns the folder for data.
		 *
		 * @return The folder for data.
		 * @since 1.8
		 */
		public Path getData() {
			return dataConfiguration.getFolder().normalize();
		}

		/**
		 * Returns the folder for assemble.
		 *
		 * @return The folder for assemble.
		 * @since 1.8
		 */
		public Path getAssemble() {
			return assembleConfiguration.getFolder().normalize();
		}

		/**
		 * Set the basic data and persists the main configuration if the main
		 * configuration is available and the given name is not null and empty.
		 *
		 * @param name        The name to set.
		 * @param description The description to set.
		 * @param keywords    The keywords to set.
		 * @param exchange    The folder for exchange. If null, set the default folder
		 *                    for exchange.
		 * @param state       The state. If null, do not update the state.
		 * @return True if the basic data was updated and persisted.
		 * @since 1.8
		 */
		public boolean updateBasicData(String name, String description, Set<String> keywords, String exchange,
				de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.State state) {
			if (state == null)
				state = getState();

			if (isMainConfigurationAvailable() && name != null && !name.isBlank()
					&& (state.equals(getState()) || isUpdate(state))) {
				project.setName(name.trim());
				project.setDescription(description == null || description.isBlank() ? null : description.trim());
				project.setKeywords(keywords);

				if (exchange == null || exchange.isBlank())
					updateExchange(null);
				else
					updateExchange(exchange.trim());

				Date currentTimeStamp = new Date();

				boolean wasDone = isDone();
				project.setState(state);
				boolean isDone = isDone();

				if (wasDone != isDone)
					project.setDone(isDone ? currentTimeStamp : null);

				return persist(currentTimeStamp);
			} else
				return false;
		}

		/**
		 * Returns the persistence grants version of given grants.
		 * 
		 * @param grants The grants.
		 * @return The persistence grants.
		 * @since 1.8
		 */
		private Set<de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.Security.Grant> getPersistenceGrants(
				Collection<Grant> grants) {
			Set<de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.Security.Grant> persistenceGrants = new HashSet<>();
			if (grants != null)
				for (Grant grant : grants)
					if (grant != null && grant.isAvailable())
						persistenceGrants.add(grant.getPersistenceGrant());

			return persistenceGrants;
		}

		/**
		 * Updates the security and persists the main configuration if it is available.
		 *
		 * @param users  The user grants.
		 * @param groups The group grants.
		 * @param other  The other.
		 * @return True if the security was updated and persisted.
		 * @since 1.8
		 */
		public boolean updateSecurity(Collection<Grant> users, Collection<Grant> groups, Right other) {
			if (isMainConfigurationAvailable()) {
				project.getSecurity().setUsers(getPersistenceGrants(users));
				project.getSecurity().setGroups(getPersistenceGrants(groups));
				project.getSecurity().setOther(other == null ? null : other.getPersistenceRight());

				return persist();
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
		public de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.State getState() {
			return isMainConfigurationAvailable() ? project.getState() : null;
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
			return isMainConfigurationAvailable() ? project.getUser() : null;
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
			return isMainConfigurationAvailable() ? project.getDate() : null;
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
			return isMainConfigurationAvailable() ? project.getUpdated() : null;
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
			return isMainConfigurationAvailable() ? project.getDone() : null;
		}

		/**
		 * Returns true if the project requires coordinator authorization.
		 * 
		 * @return True if the project requires coordinator authorization.
		 * @since 1.8
		 */
		public boolean isCoordinatorRequired() {
			return isMainConfigurationAvailable()
					&& de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.State.blocked
							.equals(getState());
		}

		/**
		 * Returns true if the project is done.
		 * 
		 * @return True if the project is available and done.
		 * @since 1.8
		 */
		public boolean isDone() {
			return isMainConfigurationAvailable()
					&& (de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.State.blocked
							.equals(getState())
							|| de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.State.closed
									.equals(getState()));
		}

		/**
		 * Returns true if the state is active.
		 *
		 * @return True if the state is active.
		 * @since 1.8
		 */
		public boolean isStateActive() {
			return isMainConfigurationAvailable()
					&& de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.State.active
							.equals(project.getState());
		}

		/**
		 * Returns true if the state is closed.
		 *
		 * @return True if the state is closed.
		 * @since 1.8
		 */
		public boolean isStateClosed() {
			return isMainConfigurationAvailable()
					&& de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.State.closed
							.equals(project.getState());
		}

		/**
		 * Returns true if the state is blocked.
		 *
		 * @return True if the state is blocked.
		 * @since 1.8
		 */
		public boolean isStateBlocked() {
			return isMainConfigurationAvailable()
					&& de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.State.blocked
							.equals(project.getState());
		}

		/**
		 * Sets the project state and persists the main configuration if the state was
		 * updated.
		 * 
		 * @param state The state to set.
		 * @return True if the project is available and the state was updated and
		 *         persisted.
		 * @since 1.8
		 */
		private boolean setState(de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.State state) {
			// Test for state diagram consistency
			if (!isUpdate(state))
				return false;

			// Update the state
			Date currentTimeStamp = new Date();

			boolean wasDone = isDone();
			project.setState(state);
			boolean isDone = isDone();

			if (wasDone != isDone)
				project.setDone(isDone ? currentTimeStamp : null);

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
		public boolean isUpdate(de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.State state) {
			return !(state == null || !isMainConfigurationAvailable() || state.equals(getState()));
		}

		/**
		 * Activates the project and persists the main configuration if the state was
		 * updated. The project can only be activated if the current state is either
		 * blocked or closed.
		 * 
		 * @return True if the project is available and could be activated.
		 * @since 1.8
		 */
		public boolean active() {
			return setState(de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.State.active);
		}

		/**
		 * Blocks the project and persists the main configuration if the state was
		 * updated. The project can only be canceled if the current state is either
		 * active or closed.
		 * 
		 * @return True if the sandbox is available and could be closed.
		 * @since 1.8
		 */
		public boolean cancel() {
			return setState(de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.State.blocked);
		}

		/**
		 * Closes the project and persists the main configuration if the state was
		 * updated. The project can only be closed if the current state is either active
		 * or blocked.
		 * 
		 * @return True if the project is available and could be closed.
		 * @since 1.8
		 */
		public boolean close() {
			return setState(de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.State.closed);
		}

		/**
		 * Returns the rights for given targets from given grants.
		 * 
		 * @param targets The targets.
		 * @param grants  The grants.
		 * @return The security rights.
		 * @since 1.8
		 */
		private Right getRights(Set<String> targets, Collection<Grant> grants) {
			Right rights = new Right();

			if (!isMainConfigurationAvailable() || targets == null || targets.isEmpty() || grants.isEmpty())
				return rights;

			Set<String> objectives = new HashSet<>();
			for (String target : targets)
				if (target != null && !target.isBlank())
					objectives.add(target.trim().toLowerCase());

			if (objectives.isEmpty())
				return rights;

			for (Grant grant : grants)
				for (String target : grant.getTargets())
					if (objectives.contains(target)) {
						if (rights.add(grant))
							return rights;

						break;
					}

			return rights;
		}

		/**
		 * Returns the grants.
		 * 
		 * @param grants The persistence grants.
		 * @return The grants.
		 * @since 1.8
		 */
		private List<Grant> getGrants(
				Set<de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.Security.Grant> grants) {
			List<Grant> list = new ArrayList<>();

			if (isMainConfigurationAvailable() && grants != null) {
				for (de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.Security.Grant grant : grants) {
					Grant item = new Grant(grant);

					if (item.isAvailable())
						list.add(item);
				}

				Collections.sort(list, new Comparator<Grant>() {

					/**
					 * Compare the booleans.
					 * 
					 * @param o1 The boolean of first object.
					 * @param o2 The boolean of second object.
					 * @return The comparation value.
					 * @since 1.8
					 */
					private int compare(boolean o1, boolean o2) {
						if ((o1 && o2) || (!o1 && !o2))
							return 0;
						else if (o1)
							return -1;
						else
							return 1;
					}

					/*
					 * (non-Javadoc)
					 * 
					 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
					 */
					@Override
					public int compare(Grant o1, Grant o2) {
						int ret;
						int index = 0;

						List<String> targets1 = o1.getTargetsAscendingOrder();
						List<String> targets2 = o2.getTargetsAscendingOrder();

						do {
							if (index >= targets1.size() && index >= targets2.size()) {
								ret = compare(o1.isRead(), o2.isRead());
								if (ret != 0)
									return ret;

								ret = compare(o1.isWrite(), o2.isWrite());
								if (ret != 0)
									return ret;

								ret = compare(o1.isExecute(), o2.isExecute());
								if (ret != 0)
									return ret;
								else
									return compare(o1.isSpecial(), o2.isSpecial());
							} else if (index >= targets1.size())
								return -1;
							else if (index >= targets2.size())
								return 1;

							ret = targets1.get(index).compareTo(targets2.get(index));
							index++;
						} while (ret == 0);

						return ret;
					}
				});
			}

			return list;
		}

		/**
		 * Returns the user grants.
		 * 
		 * @return The user grants.
		 * @since 1.8
		 */
		public List<Grant> getUserGrants() {
			return getGrants(project.getSecurity().getUsers());
		}

		/**
		 * Returns the rights for given user.
		 * 
		 * @param user The user.
		 * @return The rights.
		 * @since 1.8
		 */
		public Right getRights(String user) {
			Set<String> targets = new HashSet<>();
			targets.add(user);

			return getRights(targets, getUserGrants());
		}

		/**
		 * Returns the group grants.
		 * 
		 * @return The group grants.
		 * @since 1.8
		 */
		public List<Grant> getGroupGrants() {
			return getGrants(project.getSecurity().getGroups());
		}

		/**
		 * Returns the rights for given groups.
		 * 
		 * @param groups The groups.
		 * @return The rights.
		 * @since 1.8
		 */
		public Right getRights(Set<String> groups) {
			return getRights(groups, getGroupGrants());
		}

		/**
		 * Returns the rights for other.
		 * 
		 * @return The rights.
		 * @since 1.8
		 */
		public Right getRights() {
			return new Right(project.getSecurity().getOther());
		}

		/**
		 * Returns the folio configuration file.
		 *
		 * @return The folio configuration file. Empty, if file is not defined.
		 * @since 1.8
		 */
		public Path getFolioFile() {
			return folioFile;
		}

		/**
		 * Resets the folio configuration.
		 * 
		 * @return True if the folio configuration could be reseted.
		 * @since 1.8
		 */
		public boolean resetFolioConfiguration() {
			Date currentTimeStamp = new Date();

			if (!State.blocked.equals(project.getState())) {
				if (!isDone())
					project.setDone(currentTimeStamp);

				project.setState(State.blocked);

				if (!persist(currentTimeStamp))
					return false;
			}

			return Files.exists(folioFile) ? delete(folioFile) : true;
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
		 * @return The history file. Empty, if file is not defined.
		 * @since 1.8
		 */
		public Path getHistoryFile() {
			return historyFile;
		}

	}

	/**
	 * Images is an immutable class that defines images folder for projects.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public class Images extends CoreFolder {

		/**
		 * The folder for folios.
		 */
		private final Path folios;

		/**
		 * The folder for folios derivatives.
		 */
		private final Derivatives derivatives;

		/**
		 * Creates images folder for a project.
		 * 
		 * @param properties The configuration properties for the images.
		 * @since 1.8
		 */
		public Images(Project.Images properties) {
			super(Paths.get(ProjectConfiguration.this.folder.toString(), properties.getFolder()));

			// Initializes the folders
			folios = getPath(null, properties.getFolios().getFolder(), "folios");

			final String derivativesFolder = properties.getDerivatives().getFolder();

			derivatives = new Derivatives(
					ImageFormat.getImageFormat(properties.getDerivatives().getFormat(), ImageFormat.jpg),
					getPath(derivativesFolder, properties.getDerivatives().getQuality().getThumbnail().getFolder(),
							"thumbnail"),
					getPath(derivativesFolder, properties.getDerivatives().getQuality().getDetail().getFolder(),
							"detail"),
					getPath(derivativesFolder, properties.getDerivatives().getQuality().getBest().getFolder(), "best"));
		}

		/**
		 * Returns the path for given configuration folder. The folder is initialized.
		 * 
		 * @param derivatives The folder for derivatives. Null if not required.
		 * @param folder      The folder.
		 * @param name        The folder name.
		 * @return The path.
		 * @since 1.8
		 */
		private Path getPath(String derivatives, String folder, String name) {
			Path path = derivatives == null ? Paths.get(this.folder.toString(), folder).normalize()
					: Paths.get(this.folder.toString(), derivatives, folder).normalize();

			ConfigurationService.initializeFolder(true, path,
					"project '" + ProjectConfiguration.this.folder.getFileName() + "' " + name);

			return path;
		}

		/**
		 * Returns true if the folder for folios is a directory.
		 *
		 * @return True if the folder is a directory; false if the folder does not
		 *         exist, is not a directory, or it cannot be determined if the folder
		 *         is a directory or not.
		 * 
		 * @since 1.8
		 */
		public boolean isFoliosDirectory() {
			return Files.isDirectory(folios);
		}

		/**
		 * Returns the folder for folios.
		 *
		 * @return The folder for folios.
		 * @since 1.8
		 */
		public Path getFolios() {
			return folios;
		}

		/**
		 * Returns the derivatives quality image folders for folios.
		 *
		 * @return The derivatives quality image folders for folios.
		 * @since 1.8
		 */
		public Derivatives getDerivatives() {
			return derivatives;
		}

		/**
		 * Reset the folios.
		 *
		 * @return True if the folios could be reset.
		 * @since 1.8
		 */
		public boolean resetFolios() {
			return Files.exists(folios) ? deleteContents(folios) : true;
		}

		/**
		 * Reset the images.
		 *
		 * @return True if the images could be reset.
		 * @since 1.8
		 */
		public boolean reset() {
			boolean isReset = resetFolios();

			if (!derivatives.reset())
				isReset = false;

			return isReset;
		}

	}

	/**
	 * Derivatives is an immutable class that defines derivatives quality image
	 * folders for folios.
	 *
	 * 
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Derivatives {
		/**
		 * The folios derivatives format. The format must be suitable for use on web
		 * pages.
		 */
		private final ImageFormat format;

		/**
		 * The folder for folios derivatives quality thumbnail.
		 */
		private final Path thumbnail;

		/**
		 * The folder for folios derivatives quality detail.
		 */
		private final Path detail;

		/**
		 * The folder for folios derivatives quality best.
		 */
		private final Path best;

		/**
		 * Creates derivatives quality image folders for folios.
		 * 
		 * @param format    The folios derivatives format. The format must be suitable
		 *                  for use on web pages.
		 * @param thumbnail The folder for folios derivatives quality thumbnail.
		 * @param detail    The folder for folios derivatives quality detail.
		 * @param best      The folder for folios derivatives quality best.
		 * @since 1.8
		 */
		public Derivatives(ImageFormat format, Path thumbnail, Path detail, Path best) {
			super();

			this.format = format.isWebPages() ? format : ImageFormat.jpg;
			this.thumbnail = thumbnail;
			this.detail = detail;
			this.best = best;
		}

		/**
		 * Returns the folios derivatives format.
		 *
		 * @return The folios derivatives format.
		 * @since 1.8
		 */
		public ImageFormat getFormat() {
			return format;
		}

		/**
		 * Returns true if the folder for folios derivatives quality thumbnail is a
		 * directory.
		 *
		 * @return True if the folder is a directory; false if the folder does not
		 *         exist, is not a directory, or it cannot be determined if the folder
		 *         is a directory or not.
		 * 
		 * @since 1.8
		 */
		public boolean isThumbnailDirectory() {
			return Files.isDirectory(thumbnail);
		}

		/**
		 * Returns the folder for folios derivatives quality thumbnail.
		 *
		 * @return The folder for folios derivatives quality thumbnail.
		 * @since 1.8
		 */
		public Path getThumbnail() {
			return thumbnail;
		}

		/**
		 * Reset the folios derivatives quality thumbnail.
		 *
		 * @return True if the folios derivatives quality thumbnail could be reset.
		 * @since 1.8
		 */
		public boolean resetThumbnail() {
			return Files.exists(thumbnail) ? deleteContents(thumbnail) : true;
		}

		/**
		 * Returns true if the folder for folios derivatives quality detail is a
		 * directory.
		 *
		 * @return True if the folder is a directory; false if the folder does not
		 *         exist, is not a directory, or it cannot be determined if the folder
		 *         is a directory or not.
		 * 
		 * @since 1.8
		 */
		public boolean isDetailDirectory() {
			return Files.isDirectory(detail);
		}

		/**
		 * Returns the folder for folios derivatives quality detail.
		 *
		 * @return The folder for folios derivatives quality detail.
		 * @since 1.8
		 */
		public Path getDetail() {
			return detail;
		}

		/**
		 * Reset the folios derivatives quality detail.
		 *
		 * @return True if the folios derivatives quality detail could be reset.
		 * @since 1.8
		 */
		public boolean resetDetail() {
			return Files.exists(detail) ? deleteContents(detail) : true;
		}

		/**
		 * Returns true if the folder for folios derivatives quality best is a
		 * directory.
		 *
		 * @return True if the folder is a directory; false if the folder does not
		 *         exist, is not a directory, or it cannot be determined if the folder
		 *         is a directory or not.
		 * 
		 * @since 1.8
		 */
		public boolean isBestDirectory() {
			return Files.isDirectory(best);
		}

		/**
		 * Returns the folder for folios derivatives quality best.
		 *
		 * @return The folder for folios derivatives quality best.
		 * @since 1.8
		 */
		public Path getBest() {
			return best;
		}

		/**
		 * Reset the folios derivatives quality best.
		 *
		 * @return True if the folios derivatives quality best could be reset.
		 * @since 1.8
		 */
		public boolean resetBest() {
			return Files.exists(best) ? deleteContents(best) : true;
		}

		/**
		 * Reset the folios derivatives.
		 *
		 * @return True if the folios derivatives could be reset.
		 * @since 1.8
		 */
		public boolean reset() {
			boolean isReset = resetThumbnail();

			if (!resetDetail())
				isReset = false;

			if (!resetDetail())
				isReset = false;

			return isReset;
		}

	}

	/**
	 * Defines rights for securities.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Right {
		/**
		 * True if read right is available.
		 */
		private boolean isRead = false;

		/**
		 * True if write right is available.
		 */
		private boolean isWrite = false;

		/**
		 * True if execute right is available.
		 */
		private boolean isExecute = false;

		/**
		 * True if special right is available.
		 */
		private boolean isSpecial = false;

		/**
		 * Default constructor for rights.
		 * 
		 * @since 1.8
		 */
		public Right() {
			super();
		}

		/**
		 * Creates rights.
		 * 
		 * @param right The persistence right.
		 * @since 1.8
		 */
		public Right(de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.Security.Right right) {
			super();

			if (right != null) {
				isSpecial = right.isSpecial();
				isExecute = !isSpecial && right.isExecute();
				isWrite = right.isWrite();
				isRead = right.isRead();
			}

		}

		/**
		 * Creates rights.
		 * 
		 * @param isRead    True if read right is available.
		 * @param isWrite   True if write right is available.
		 * @param isExecute True if execute right is available.
		 * @param isSpecial True if special right is available.
		 * @since 1.8
		 */
		public Right(boolean isRead, boolean isWrite, boolean isExecute, boolean isSpecial) {
			super();

			this.isSpecial = isSpecial;
			this.isExecute = !this.isSpecial && isExecute;
			this.isWrite = isWrite;
			this.isRead = isRead;
		}

		/**
		 * Returns true if read right is available.
		 *
		 * @return True if read right is available.
		 * @since 1.8
		 */
		public boolean isRead() {
			return isRead;
		}

		/**
		 * Returns true if write right is available.
		 *
		 * @return True if write right is available.
		 * @since 1.8
		 */
		public boolean isWrite() {
			return isWrite;
		}

		/**
		 * Returns true if execute right is available.
		 *
		 * @return True if execute right is available.
		 * @since 1.8
		 */
		public boolean isExecute() {
			return isExecute;
		}

		/**
		 * Returns true if special right is available.
		 *
		 * @return True if special right is available.
		 * @since 1.8
		 */
		public boolean isSpecial() {
			return isSpecial;
		}

		/**
		 * Returns true if a right is set.
		 * 
		 * @return True if a right is set.
		 * @since 1.8
		 */
		public boolean isRight() {
			return isRead || isWrite || isExecute || isSpecial;
		}

		/**
		 * Adds the rights and returns true if the maximum rights level has been
		 * reached, i.e. 'rws'.
		 * 
		 * @param right The rights to add.
		 * @return True if the maximum rights level has been reached.
		 * @since 1.8
		 */
		public boolean add(Right right) {
			if (right != null) {
				isSpecial = isSpecial || right.isSpecial();
				isExecute = !isSpecial && (isExecute || right.isExecute());
				isWrite = isWrite || right.isWrite();
				isRead = isRead || right.isRead();
			}

			return isRead && isWrite && isSpecial;
		}

		/**
		 * Returns the persistence right.
		 * 
		 * @return The persistence right.
		 * @since 1.8
		 */
		public de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.Security.Right getPersistenceRight() {
			de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.Security.Right right = new de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.Security.Right();

			setPersistenceRight(right);

			return right;
		}

		/**
		 * Set the rights in the given persistence right.
		 * 
		 * @param right The persistence right.
		 * @since 1.8
		 */
		public void setPersistenceRight(
				de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.Security.Right right) {

			if (right != null) {
				right.setSpecial(isSpecial);
				right.setExecute(isExecute);
				right.setWrite(isWrite);
				right.setRead(isRead);
			}
		}

		/**
		 * Returns the right label.
		 * 
		 * @return The right label.
		 * @since 1.8
		 */
		public String getLabel() {
			return (isRead ? "r" : "-") + (isWrite ? "w" : "-") + (isSpecial ? "s" : isExecute ? "x" : "-");
		}
	}

	/**
	 * Grant is an immutable class that defines grants for securities.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Grant extends Right {
		/**
		 * The targets.
		 */
		private final Set<String> targets = new HashSet<>();

		/**
		 * Creates a grant.
		 * 
		 * @param grant The persistence grant.
		 * @since 1.8
		 */
		public Grant(de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.Security.Grant grant) {
			super(grant);

			if (grant != null && grant.getTargets() != null)
				for (String target : grant.getTargets())
					targets.add(target);
		}

		/**
		 * Creates a grant.
		 * 
		 * @param isRead    True if read right is available.
		 * @param isWrite   True if write right is available.
		 * @param isExecute True if execute right is available.
		 * @param isSpecial True if special right is available.
		 * @param targets   The targets.
		 * @since 1.8
		 */
		public Grant(boolean isRead, boolean isWrite, boolean isExecute, boolean isSpecial, Set<String> targets) {
			super(isRead, isWrite, isExecute, isSpecial);

			if (targets != null)
				for (String target : targets)
					if (!target.isBlank())
						this.targets.add(target.trim().toLowerCase());
		}

		/**
		 * Returns the targets.
		 *
		 * @return The targets.
		 * @since 1.8
		 */
		public Set<String> getTargets() {
			return new HashSet<>(targets);
		}

		/**
		 * Returns the targets in ascending order.
		 *
		 * @return The targets.
		 * @since 1.8
		 */
		public List<String> getTargetsAscendingOrder() {
			List<String> list = new ArrayList<>(targets);

			Collections.sort(list);
			return list;
		}

		/**
		 * Returns true if the grant is available, i.e., a right is set and at least one
		 * target exists.
		 * 
		 * @return True if the grant is available.
		 * @since 1.8
		 */
		public boolean isAvailable() {
			return isRight() && !targets.isEmpty();
		}

		/**
		 * Returns the persistence grant.
		 * 
		 * @return The persistence grant.
		 * @since 1.8
		 */
		public de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.Security.Grant getPersistenceGrant() {
			de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.Security.Grant grant = new de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.Security.Grant();

			setPersistenceRight(grant);

			grant.setTargets(targets);

			return grant;
		}
	}

}
