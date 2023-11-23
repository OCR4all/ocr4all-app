/**
 * File:     RepositoryConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.11.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.repository;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.CoreFolder;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.TrackingData;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.OCR4all;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.repository.Repository;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;

/**
 * Defines configurations for the repository.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class RepositoryConfiguration extends CoreFolder {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RepositoryConfiguration.class);

	/**
	 * The configuration.
	 */
	private final Configuration configuration;

	/**
	 * Creates a configuration for the repository.
	 * 
	 * @param properties The ocr4all properties.
	 * @since 1.8
	 */
	public RepositoryConfiguration(OCR4all properties) {
		super(Paths.get(properties.getRepository().getFolder()));

		configuration = new Configuration(properties.getRepository().getConfiguration());
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
	 * Defines configurations for the repository.
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
		 * The repository.
		 */
		private de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Repository repository = null;

		/**
		 * Creates a configuration for the repository.
		 * 
		 * @param properties The configuration properties for the repository.
		 * @since 1.8
		 */
		public Configuration(Repository.Configuration properties) {
			super(Paths.get(RepositoryConfiguration.this.folder.toString(), properties.getFolder()));

			// Initialize the repository configuration folder and consequently the
			// repository
			ConfigurationService.initializeFolder(true, folder, "repository configuration");

			// Loads the main configuration file
			mainConfigurationManager = new PersistenceManager(getPath(properties.getFiles().getMain()),
					Type.repository_v1);
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
				repository = mainConfigurationManager.getEntity(
						de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Repository.class, null,
						message -> logger.warn(message));

				if (!isMainConfigurationAvailable() || repository.getSecurity() == null) {
					Date currentTimeStamp = new Date();
					if (!isMainConfigurationAvailable()) {
						repository = new de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Repository();

						repository.setDate(currentTimeStamp);
					}

					if (repository.getSecurity() == null)
						repository.setSecurity(
								new de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Repository.Security());

					persist(null, currentTimeStamp);
				}
			} catch (IOException e) {
				logger.warn(e.getMessage());

				repository = null;
			}
		}

		/**
		 * Returns true if the main configuration is available.
		 * 
		 * @return True if the main configuration is available.
		 * @since 1.8
		 */
		public boolean isMainConfigurationAvailable() {
			return repository != null;
		}

		/**
		 * Persist the main configuration with current update time stamp.
		 * 
		 * @param user The user.
		 * @return True if the main configuration could be persisted.
		 * @since 1.8
		 */
		private boolean persist(String user) {
			return persist(user, null);
		}

		/**
		 * Persist the main configuration.
		 * 
		 * @param user    The user.
		 * @param updated The updated time. If null, uses the current time stamp.
		 * @return True if the main configuration could be persisted.
		 * @since 1.8
		 */
		private boolean persist(String user, Date updated) {
			if (isMainConfigurationAvailable())
				try {
					repository.setUser(user);
					repository.setUpdated(updated == null ? new Date() : updated);

					mainConfigurationManager.persist(repository);

					logger.info("Persisted the repository configuration.");

					return true;
				} catch (Exception e) {
					logger.warn("Could not persist the repository configuration - " + e.getMessage());
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
		 * Returns the repository security.
		 * 
		 * @return The repository security.
		 * @since 1.8
		 */
		public de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Repository.Security getSecurity() {
			if (isMainConfigurationAvailable())
				return new de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Repository.Security(
						repository.getSecurity().isSecured(),
						repository.getSecurity().getUsers() == null ? null
								: new HashSet<>(repository.getSecurity().getUsers()),
						repository.getSecurity().getGroups() == null ? null
								: new HashSet<>(repository.getSecurity().getGroups()));
			else
				return null;
		}

		/**
		 * updates the security and persists the main configuration if it is available.
		 *
		 * @param user     The user.
		 * @param security The new security.
		 * @return True if the security was updated and persisted.
		 * @since 1.8
		 */
		public boolean updateSecurity(String user,
				de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Repository.Security security) {
			if (security != null && isMainConfigurationAvailable()) {
				repository.getSecurity().setSecured(security.isSecured());
				repository.getSecurity().setUsers(security.getUsers());
				repository.getSecurity().setGroups(security.getGroups());

				return persist(user);
			} else
				return false;
		}

		/**
		 * Secures the repository and persists the main configuration if it is
		 * available.
		 *
		 * @param user      The user.
		 * @param isSecured True if the repository is secured.
		 * @return True if the security was updated and persisted.
		 * @since 1.8
		 */
		public boolean secure(String user, boolean isSecured) {
			if (isMainConfigurationAvailable()) {
				repository.getSecurity().setSecured(isSecured);

				return persist(user);
			} else
				return false;
		}

		/**
		 * Returns true if the user is allowed to create containers take into account
		 * only the repository security and not the system security.
		 * 
		 * @param user   The user.
		 * @param groups The user groups.
		 * @return True if the user is allowed to create containers.
		 * @since 1.8
		 */
		public boolean isCreateContainer(String user, Collection<String> groups) {
			if (!repository.getSecurity().isSecured())
				return true;
			else {
				if (user != null && !user.isBlank())
					if (repository.getSecurity().getUsers().contains(user.trim().toLowerCase()))
						return true;

				if (groups != null)
					for (String group : groups)
						if (group != null && !group.isBlank())
							if (repository.getSecurity().getGroups().contains(group.trim().toLowerCase()))
								return true;

				return false;
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
			return isMainConfigurationAvailable() ? repository.getUser() : null;
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
			return isMainConfigurationAvailable() ? repository.getDate() : null;
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
			return isMainConfigurationAvailable() ? repository.getUpdated() : null;
		}

	}
}
