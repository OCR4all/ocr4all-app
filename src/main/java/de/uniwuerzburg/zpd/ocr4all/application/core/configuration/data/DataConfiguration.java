/**
 * File:     DataConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.data
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.05.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.data;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.CoreFolder;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.TrackingData;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.OCR4all;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.data.Data;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.security.SecurityOwner;

/**
 * Defines configurations for the data.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public class DataConfiguration extends CoreFolder {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DataConfiguration.class);

	/**
	 * The configuration.
	 */
	private final Configuration configuration;

	/**
	 * The collection properties.
	 */
	private final de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.data.Collection collection;

	/**
	 * Creates a configuration for the data.
	 * 
	 * @param properties The ocr4all properties.
	 * @since 1.8
	 */
	public DataConfiguration(OCR4all properties) {
		super(Paths.get(properties.getData().getFolder()));

		configuration = new Configuration(properties.getData().getConfiguration());
		collection = properties.getData().getCollection();
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
	 * Returns the collection properties.
	 *
	 * @return The collection properties.
	 * @since 1.8
	 */
	public de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.data.Collection getCollection() {
		return collection;
	}

	/**
	 * Defines configurations for the data.
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
		 * The data.
		 */
		private de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Data data = null;

		/**
		 * Creates a configuration for the data.
		 * 
		 * @param properties The configuration properties for the data.
		 * @since 1.8
		 */
		Configuration(Data.Configuration properties) {
			super(Paths.get(DataConfiguration.this.folder.toString(), properties.getFolder()));

			// Initialize the data configuration folder and consequently the
			// data
			ConfigurationService.initializeFolder(true, folder, "data configuration");

			// Loads the main configuration file
			mainConfigurationManager = new PersistenceManager(getPath(properties.getFiles().getMain()), Type.data_v1);
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
				data = mainConfigurationManager.getEntity(
						de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Data.class, null,
						message -> logger.warn(message));

				if (!isMainConfigurationAvailable() || data.getSecurity() == null) {
					Date currentTimeStamp = new Date();
					if (!isMainConfigurationAvailable()) {
						data = new de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Data();

						data.setDate(currentTimeStamp);
					}

					if (data.getSecurity() == null)
						data.setSecurity(new SecurityOwner());

					persist(null, currentTimeStamp);
				}
			} catch (IOException e) {
				logger.warn(e.getMessage());

				data = null;
			}
		}

		/**
		 * Returns true if the main configuration is available.
		 * 
		 * @return True if the main configuration is available.
		 * @since 1.8
		 */
		public boolean isMainConfigurationAvailable() {
			return data != null;
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
					data.setUser(user);
					data.setUpdated(updated == null ? new Date() : updated);

					mainConfigurationManager.persist(data);

					logger.info("Persisted the data configuration.");

					return true;
				} catch (Exception e) {
					logger.warn("Could not persist the data configuration - " + e.getMessage());
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
		 * Returns the data security.
		 * 
		 * @return The data security.
		 * @since 1.8
		 */
		public SecurityOwner getSecurity() {
			if (isMainConfigurationAvailable())
				return new SecurityOwner(data.getSecurity().isSecured(),
						data.getSecurity().getUsers() == null ? null : new HashSet<>(data.getSecurity().getUsers()),
						data.getSecurity().getGroups() == null ? null : new HashSet<>(data.getSecurity().getGroups()));
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
		public boolean updateSecurity(String user, SecurityOwner security) {
			if (security != null && isMainConfigurationAvailable()) {
				data.getSecurity().setSecured(security.isSecured());
				data.getSecurity().setUsers(security.getUsers());
				data.getSecurity().setGroups(security.getGroups());

				return persist(user);
			} else
				return false;
		}

		/**
		 * Secures the data and persists the main configuration if it is
		 * available.
		 *
		 * @param user      The user.
		 * @param isSecured True if the data is secured.
		 * @return True if the security was updated and persisted.
		 * @since 1.8
		 */
		public boolean secure(String user, boolean isSecured) {
			if (isMainConfigurationAvailable()) {
				data.getSecurity().setSecured(isSecured);

				return persist(user);
			} else
				return false;
		}

		/**
		 * Returns true if the user is allowed to create collections take into account
		 * only the data security and not the system security.
		 * 
		 * @param user   The user.
		 * @param groups The user groups.
		 * @return True if the user is allowed to create collections.
		 * @since 1.8
		 */
		public boolean isCreateCollection(String user, Collection<String> groups) {
			if (!data.getSecurity().isSecured())
				return true;
			else {
				if (user != null && !user.isBlank())
					if (data.getSecurity().getUsers().contains(user.trim().toLowerCase()))
						return true;

				if (groups != null)
					for (String group : groups)
						if (group != null && !group.isBlank())
							if (data.getSecurity().getGroups().contains(group.trim().toLowerCase()))
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
			return isMainConfigurationAvailable() ? data.getUser() : null;
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
			return isMainConfigurationAvailable() ? data.getDate() : null;
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
			return isMainConfigurationAvailable() ? data.getUpdated() : null;
		}

	}

}
