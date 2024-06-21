/**
 * File:     AssembleConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.assemble
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:    12.06.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.assemble;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.CoreFolder;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.TrackingData;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.OCR4all;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.assemble.Assemble;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.assemble.Model;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.security.SecurityOwner;

/**
 * Defines configurations for the assemble.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public class AssembleConfiguration extends CoreFolder {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AssembleConfiguration.class);

	/**
	 * The configuration.
	 */
	private final Configuration configuration;

	/**
	 * The model properties.
	 */
	private final Model model;

	/**
	 * Creates a configuration for the assemble.
	 * 
	 * @param properties The ocr4all properties.
	 * @since 1.8
	 */
	public AssembleConfiguration(OCR4all properties) {
		super(Paths.get(properties.getAssemble().getFolder()));

		configuration = new Configuration(properties.getAssemble().getConfiguration());
		model = properties.getAssemble().getModel();
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
	 * Returns the model properties.
	 *
	 * @return The model properties.
	 * @since 1.8
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * Defines configurations for the assemble.
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
		 * The assemble.
		 */
		private de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Assemble assemble = null;

		/**
		 * Creates a configuration for the assemble.
		 * 
		 * @param properties The configuration properties for the assemble.
		 * @since 1.8
		 */
		Configuration(Assemble.Configuration properties) {
			super(Paths.get(AssembleConfiguration.this.folder.toString(), properties.getFolder()));

			// Initialize the assemble configuration folder and consequently the
			// assemble
			ConfigurationService.initializeFolder(true, folder, "assemble configuration");

			// Loads the main configuration file
			mainConfigurationManager = new PersistenceManager(getPath(properties.getFiles().getMain()),
					Type.assemble_v1);
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
				assemble = mainConfigurationManager.getEntity(
						de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Assemble.class, null,
						message -> logger.warn(message));

				if (!isMainConfigurationAvailable() || assemble.getSecurity() == null) {
					Date currentTimeStamp = new Date();
					if (!isMainConfigurationAvailable()) {
						assemble = new de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Assemble();

						assemble.setDate(currentTimeStamp);
					}

					if (assemble.getSecurity() == null)
						assemble.setSecurity(new SecurityOwner());

					persist(null, currentTimeStamp);
				}
			} catch (IOException e) {
				logger.warn(e.getMessage());

				assemble = null;
			}
		}

		/**
		 * Returns true if the main configuration is available.
		 * 
		 * @return True if the main configuration is available.
		 * @since 1.8
		 */
		public boolean isMainConfigurationAvailable() {
			return assemble != null;
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
					assemble.setUser(user);
					assemble.setUpdated(updated == null ? new Date() : updated);

					mainConfigurationManager.persist(assemble);

					logger.info("Persisted the assemble configuration.");

					return true;
				} catch (Exception e) {
					logger.warn("Could not persist the assemble configuration - " + e.getMessage());
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
		 * Returns the assemble security.
		 * 
		 * @return The assemble security.
		 * @since 1.8
		 */
		public SecurityOwner getSecurity() {
			if (isMainConfigurationAvailable())
				return new SecurityOwner(assemble.getSecurity().isSecured(),
						assemble.getSecurity().getUsers() == null ? null
								: new HashSet<>(assemble.getSecurity().getUsers()),
						assemble.getSecurity().getGroups() == null ? null
								: new HashSet<>(assemble.getSecurity().getGroups()));
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
				assemble.getSecurity().setSecured(security.isSecured());
				assemble.getSecurity().setUsers(security.getUsers());
				assemble.getSecurity().setGroups(security.getGroups());

				return persist(user);
			} else
				return false;
		}

		/**
		 * Secures the assemble and persists the main configuration if it is available.
		 *
		 * @param user      The user.
		 * @param isSecured True if the assemble is secured.
		 * @return True if the security was updated and persisted.
		 * @since 1.8
		 */
		public boolean secure(String user, boolean isSecured) {
			if (isMainConfigurationAvailable()) {
				assemble.getSecurity().setSecured(isSecured);

				return persist(user);
			} else
				return false;
		}

		/**
		 * Returns true if the user is allowed to create models take into account only
		 * the assemble security and not the system security.
		 * 
		 * @param user   The user.
		 * @param groups The user groups.
		 * @return True if the user is allowed to create models.
		 * @since 1.8
		 */
		public boolean isCreateModel(String user, Collection<String> groups) {
			if (!assemble.getSecurity().isSecured())
				return true;
			else {
				if (user != null && !user.isBlank())
					if (assemble.getSecurity().getUsers().contains(user.trim().toLowerCase()))
						return true;

				if (groups != null)
					for (String group : groups)
						if (group != null && !group.isBlank())
							if (assemble.getSecurity().getGroups().contains(group.trim().toLowerCase()))
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
			return isMainConfigurationAvailable() ? assemble.getUser() : null;
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
			return isMainConfigurationAvailable() ? assemble.getDate() : null;
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
			return isMainConfigurationAvailable() ? assemble.getUpdated() : null;
		}

	}

}
