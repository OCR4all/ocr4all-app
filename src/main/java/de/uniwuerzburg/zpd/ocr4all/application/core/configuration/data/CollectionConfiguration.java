/**
 * File:     CollectionConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.data
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.05.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.data;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.CoreFolder;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.TrackingData;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.security.SecurityGrant;

/**
 * Defines configurations for the collection.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public class CollectionConfiguration extends CoreFolder {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CollectionConfiguration.class);

	/**
	 * The configuration.
	 */
	private final Configuration configuration;

	/**
	 * Creates a configuration for the collection.
	 * 
	 * @param properties The ocr4all collection properties.
	 * @param folder     The collection folder.
	 * @since 1.8
	 */
	public CollectionConfiguration(
			de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.data.Collection properties,
			Path folder) {
		this(properties, folder, null);
	}

	/**
	 * Creates a configuration for the collection.
	 * 
	 * @param properties The ocr4all collection properties.
	 * @param folder     The collection folder.
	 * @param coreData   The core data for a collection configuration. If non null
	 *                   and the main configuration is not available, then this
	 *                   basic data is used to initialize the main configuration.
	 * @since 1.8
	 */
	public CollectionConfiguration(
			de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.data.Collection properties, Path folder,
			Configuration.CoreData coreData) {
		super(folder);

		configuration = new Configuration(properties.getConfiguration(), coreData);
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
	 * Defines configurations for the collection.
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
		 * The sets configuration file.
		 */
		private final Path setsFile;

		/**
		 * The collection.
		 */
		private de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Collection collection = null;

		/**
		 * Creates a configuration for the collection.
		 * 
		 * @param properties The configuration properties for the collection.
		 * @param coreData   The core data for a collection configuration. If non null
		 *                   and the main configuration is not available, then this
		 *                   basic data is used to initialize the main configuration.
		 * @since 1.8
		 */
		Configuration(
				de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.data.Collection.Configuration properties,
				CoreData coreData) {
			super(Paths.get(CollectionConfiguration.this.folder.toString(), properties.getFolder()));

			// Initialize the collection configuration folder and consequently the
			// collection
			ConfigurationService.initializeFolder(true, folder, "collection configuration");

			// Initializes the configuration files
			setsFile = getPath(properties.getFiles().getSets());

			// Loads the main configuration file
			mainConfigurationManager = new PersistenceManager(getPath(properties.getFiles().getMain()),
					Type.data_collection_v1);
			loadMainConfiguration(coreData);
		}

		/**
		 * Loads the main configuration file.
		 * 
		 * @param coreData The core data for a collection configuration. If non null and
		 *                 the main configuration is not available, then this basic data
		 *                 is used to initialize the main configuration.
		 * @since 1.8
		 */
		private void loadMainConfiguration(CoreData coreData) {
			// Load main configuration
			try {
				collection = mainConfigurationManager.getEntity(
						de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Collection.class, null,
						message -> logger.warn(message));

				if (!isMainConfigurationAvailable() || collection.getName() == null
						|| collection.getSecurity() == null) {
					Date currentTimeStamp = new Date();
					if (!isMainConfigurationAvailable()) {
						collection = new de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Collection();

						collection.setDate(currentTimeStamp);

						if (coreData != null) {
							collection.setName(coreData.getName());
							collection.setDescription(coreData.getDescription());
							collection.setKeywords(coreData.getKeywords());

							if (coreData.getUser() != null)
								collection.setSecurity(new SecurityGrant(

										SecurityGrant.Right.maximal, coreData.getUser()));

						}
					} else
						// avoid using basic data if not creating a main configuration
						coreData = null;

					if (collection.getName() == null)
						collection.setName(CollectionConfiguration.this.folder.getFileName().toString());

					if (collection.getSecurity() == null)
						collection.setSecurity(new SecurityGrant());

					persist(coreData == null ? null : coreData.getUser(), currentTimeStamp);
				}
			} catch (IOException e) {
				logger.warn(e.getMessage());

				collection = null;
			}
		}

		/**
		 * Returns true if the main configuration is available.
		 * 
		 * @return True if the main configuration is available.
		 * @since 1.8
		 */
		public boolean isMainConfigurationAvailable() {
			return collection != null;
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
					collection.setUser(user);
					collection.setUpdated(updated == null ? new Date() : updated);

					mainConfigurationManager.persist(collection);

					logger.info("Persisted the collection configuration.");

					return true;
				} catch (Exception e) {
					reloadMainConfiguration();

					logger.warn("Could not persist the collection configuration - " + e.getMessage());
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
			loadMainConfiguration(null);

			return isMainConfigurationAvailable();
		}

		/**
		 * Returns the sets configuration file.
		 *
		 * @return The sets configuration file.
		 * @since 1.8
		 */
		public Path getSetsFile() {
			return setsFile;
		}

		/**
		 * Returns the name.
		 * 
		 * @return The name.
		 * @since 1.8
		 */
		public String getName() {
			return isMainConfigurationAvailable() ? collection.getName() : null;
		}

		/**
		 * Returns the collection information.
		 * 
		 * @return The collection information.
		 * @since 1.8
		 */
		public Information getInformation() {
			return isMainConfigurationAvailable()
					? new Information(collection.getName(), collection.getDescription(),
							collection.getKeywords() == null ? null : new HashSet<>(collection.getKeywords()))
					: null;
		}

		/**
		 * Updates the information and persists the main configuration if the main
		 * configuration is available and the name is not null and empty.
		 *
		 * @param user        The user.
		 * @param information The collection information.
		 * @return True if the collection information was updated and persisted.
		 * @since 1.8
		 */
		public boolean update(String user, Information information) {
			if (isMainConfigurationAvailable() && information != null && information.getName() != null
					&& !information.getName().isBlank()) {
				collection.setName(information.getName().trim());
				collection.setDescription(
						information.getDescription() == null || information.getDescription().isBlank() ? null
								: information.getDescription().trim());
				collection.setKeywords(information.getKeywords());

				return persist(user);
			} else
				return false;
		}

		/**
		 * Clones the grants.
		 * 
		 * @param grants The grants to clone.
		 * @return The cloned the grants.
		 * @since 1.8
		 */
		private static Set<SecurityGrant.Grant> cloneGrant(Set<SecurityGrant.Grant> grants) {
			Set<SecurityGrant.Grant> clone = new HashSet<>();

			if (grants != null)
				for (SecurityGrant.Grant grant : grants) {
					if (grant.getRight() != null && grant.getTargets() != null)
						clone.add(new SecurityGrant.Grant(grant.getRight(), grant.getTargets()));

				}

			return clone.isEmpty() ? null : clone;
		}

		/**
		 * Clones the security.
		 * 
		 * @param grants The security grants to clone.
		 * @return The cloned the security grants.
		 * @since 1.8
		 */
		private static SecurityGrant cloneSecurity(SecurityGrant security) {

			return security == null ? null
					: new SecurityGrant(cloneGrant(security.getUsers()), cloneGrant(security.getGroups()),
							security.getOther());
		}

		/**
		 * Returns the security.
		 * 
		 * @return The security.
		 * @since 1.8
		 */
		public SecurityGrant getSecurity() {
			return cloneSecurity(collection.getSecurity());
		}

		/**
		 * Updates the security.
		 *
		 * @param user     The user.
		 * @param security The collection security.
		 * @return True if the collection security was updated and persisted.
		 * @since 1.8
		 */
		public boolean update(String user, SecurityGrant security) {
			if (isMainConfigurationAvailable()) {
				collection.setSecurity(cloneSecurity(security));

				return persist(user);
			} else
				return false;
		}

		/**
		 * Returns true if the source right is fulfilled with the target right.
		 * 
		 * @param source The source right.
		 * @param target The target right.
		 * @return True if the source right is fulfilled with the target right.
		 * @since 1.8
		 */
		private boolean isRightFulfilled(SecurityGrant.Right source, SecurityGrant.Right target) {
			return source != null && source.iFulfilled(target);
		}

		/**
		 * Returns the collection right for given user and groups.
		 * 
		 * @param target If not null, the search is ended as soon as the target right is
		 *               fulfilled. Otherwise search for the maximal fulfilled right.
		 * @param user   The user.
		 * @param groups The user groups.
		 * @return The collection right.
		 * @since 1.8
		 */
		private SecurityGrant.Right getRightFulfilled(SecurityGrant.Right target, String user,
				Collection<String> groups) {
			if (target == null)
				target = SecurityGrant.Right.maximal;

			SecurityGrant.Right right = null;

			if (isMainConfigurationAvailable()) {
				right = SecurityGrant.Right.getMaximnal(right, collection.getSecurity().getOther());

				if (isRightFulfilled(right, target))
					return right;

				if (collection.getSecurity().getUsers() != null && user != null && !user.isBlank()) {
					user = user.trim().toLowerCase();

					for (SecurityGrant.Grant grant : collection.getSecurity().getUsers())
						if (grant.getTargets().contains(user)) {
							right = SecurityGrant.Right.getMaximnal(right, grant.getRight());

							if (isRightFulfilled(right, target))
								return right;
						}
				}

				if (collection.getSecurity().getGroups() != null && groups != null)
					for (String group : groups) {
						if (group != null && !group.isBlank()) {
							group = group.trim().toLowerCase();

							for (SecurityGrant.Grant grant : collection.getSecurity().getGroups())
								if (grant.getTargets().contains(group)) {
									right = SecurityGrant.Right.getMaximnal(right, grant.getRight());

									if (isRightFulfilled(right, target))
										return right;
								}
						}

					}
			}

			return right;
		}

		/**
		 * Returns the maximal fulfilled collection right for given user and groups.
		 * 
		 * @param user   The user.
		 * @param groups The user groups.
		 * @return The collection right.
		 * @since 1.8
		 */
		public SecurityGrant.Right getRight(String user, Collection<String> groups) {
			return getRightFulfilled(null, user, groups);
		}

		/**
		 * Returns true if the read right is fulfilled for given user and groups.
		 * 
		 * @param user   The user.
		 * @param groups The user groups.
		 * @return True if the read right is fulfilled for given user and groups.
		 * @since 1.8
		 */
		public boolean isRightRead(String user, Collection<String> groups) {
			SecurityGrant.Right right = getRightFulfilled(SecurityGrant.Right.read, user, groups);

			return right != null && right.isReadFulfilled();
		}

		/**
		 * Returns true if the write right is fulfilled for given user and groups.
		 * 
		 * @param user   The user.
		 * @param groups The user groups.
		 * @return True if the write right is fulfilled for given user and groups.
		 * @since 1.8
		 */
		public boolean isRightWrite(String user, Collection<String> groups) {
			SecurityGrant.Right right = getRightFulfilled(SecurityGrant.Right.write, user, groups);

			return right != null && right.isWriteFulfilled();
		}

		/**
		 * Returns true if the special right is fulfilled for given user and groups.
		 * 
		 * @param user   The user.
		 * @param groups The user groups.
		 * @return True if the special right is fulfilled for given user and groups.
		 * @since 1.8
		 */
		public boolean isRightSpecial(String user, Collection<String> groups) {
			SecurityGrant.Right right = getRightFulfilled(SecurityGrant.Right.special, user, groups);

			return right != null && right.isSpecialFulfilled();
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
			return isMainConfigurationAvailable() ? collection.getUser() : null;
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
			return isMainConfigurationAvailable() ? collection.getDate() : null;
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
			return isMainConfigurationAvailable() ? collection.getUpdated() : null;
		}

		/**
		 * Information is an immutable class that defines information for collection
		 * configurations.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class Information {
			/**
			 * The name.
			 */
			private final String name;

			/**
			 * The description.
			 */
			private final String description;

			/**
			 * The keywords.
			 */
			private final Set<String> keywords;

			/**
			 * Creates an information for a collection configuration.
			 * 
			 * @param name        The name.
			 * @param description The description.
			 * @param keywords    The keywords.
			 * @since 1.8
			 */
			public Information(String name, String description, Set<String> keywords) {
				super();

				this.name = name;
				this.description = description;
				this.keywords = keywords;
			}

			/**
			 * Returns the name.
			 *
			 * @return The name.
			 * @since 1.8
			 */
			public String getName() {
				return name;
			}

			/**
			 * Returns the description.
			 *
			 * @return The description.
			 * @since 1.8
			 */
			public String getDescription() {
				return description;
			}

			/**
			 * Returns the keywords.
			 *
			 * @return The keywords.
			 * @since 1.8
			 */
			public Set<String> getKeywords() {
				return keywords;
			}

		}

		/**
		 * CoreData is an immutable class that defines core data for collection
		 * configurations.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class CoreData extends Information {
			/**
			 * The user.
			 */
			private final String user;

			/**
			 * Creates core data for a collection configuration.
			 * 
			 * @param user        The user.
			 * @param name        The name.
			 * @param description The description.
			 * @param keywords    The keywords.
			 * @since 1.8
			 */
			public CoreData(String user, String name, String description, Set<String> keywords) {
				super(name, description, keywords);

				this.user = user;
			}

			/**
			 * Returns the user.
			 *
			 * @return The user.
			 * @since 1.8
			 */
			public String getUser() {
				return user;
			}
		}

	}

}
