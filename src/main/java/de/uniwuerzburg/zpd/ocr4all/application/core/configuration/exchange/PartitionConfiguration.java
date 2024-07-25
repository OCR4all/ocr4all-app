/**
 * File:     PartitionConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.exchange
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     24.06.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.exchange;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.CoreFolder;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.TrackingData;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.exchange.Partition;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.security.SecurityGrantRW;

/**
 * Defines configurations for the partition.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public class PartitionConfiguration extends CoreFolder {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PartitionConfiguration.class);

	/**
	 * The id.
	 */
	private final String id;

	/**
	 * The configuration.
	 */
	private final Configuration configuration;

	/**
	 * The folder for data.
	 */
	private final Path data;

	/**
	 * Creates a configuration for the partition.
	 * 
	 * @param properties The ocr4all partition properties.
	 * @param folder     The partition folder.
	 * @since 17
	 */
	public PartitionConfiguration(Partition properties, Path folder) {
		this(properties, folder, null);
	}

	/**
	 * Creates a configuration for the partition.
	 * 
	 * @param properties The ocr4all partition properties.
	 * @param folder     The partition folder.
	 * @param coreData   The core data for a container configuration. If non null
	 *                   and the main configuration is not available, then this
	 *                   basic data is used to initialize the main configuration.
	 * @since 17
	 */
	public PartitionConfiguration(Partition properties, Path folder, Configuration.CoreData coreData) {
		super(folder);

		id = folder == null || !Files.isDirectory(folder) ? null : folder.getFileName().toString();

		configuration = new Configuration(properties.getConfiguration(), coreData);

		// Initializes the folders
		data = this.folder.resolve(properties.getData().getFolder()).normalize();

		ConfigurationService.initializeFolder(true, data, "partition '" + this.folder.getFileName() + "' data ");
	}

	/**
	 * Returns the id.
	 *
	 * @return The id.
	 * @since 17
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the configuration.
	 *
	 * @return The configuration.
	 * @since 17
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Returns the folder for data.
	 *
	 * @return The folder for data.
	 * @since 17
	 */
	public Path getData() {
		return data;
	}

	/**
	 * Defines configurations for the partition.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public class Configuration extends CoreFolder implements TrackingData {
		/**
		 * The main configuration persistence manager.
		 */
		private final PersistenceManager mainConfigurationManager;

		/**
		 * The partition.
		 */
		private de.uniwuerzburg.zpd.ocr4all.application.persistence.exchange.Partition partition = null;

		/**
		 * Creates a configuration for the partition.
		 * 
		 * @param properties The configuration properties for the partition.
		 * @param coreData   The core data for a container configuration. If non null
		 *                   and the main configuration is not available, then this
		 *                   basic data is used to initialize the main configuration.
		 * @since 17
		 */
		Configuration(Partition.Configuration properties, CoreData coreData) {
			super(PartitionConfiguration.this.folder.resolve(properties.getFolder()));

			// Initialize the partition configuration folder and consequently the
			// partition
			ConfigurationService.initializeFolder(true, folder, "partition configuration");

			// Loads the main configuration file
			mainConfigurationManager = new PersistenceManager(getPath(properties.getFiles().getMain()),
					Type.exchange_partition_v1);
			loadMainConfiguration(coreData);
		}

		/**
		 * Loads the main configuration file.
		 * 
		 * @param coreData The core data for a container configuration. If non null and
		 *                 the main configuration is not available, then this basic data
		 *                 is used to initialize the main configuration.
		 * @since 17
		 */
		private void loadMainConfiguration(CoreData coreData) {
			// Load main configuration
			try {
				partition = mainConfigurationManager.getEntity(
						de.uniwuerzburg.zpd.ocr4all.application.persistence.exchange.Partition.class, null,
						message -> logger.warn(message));

				if (!isMainConfigurationAvailable() || partition.getName() == null || partition.getSecurity() == null) {
					Date currentTimeStamp = new Date();
					if (!isMainConfigurationAvailable()) {
						partition = new de.uniwuerzburg.zpd.ocr4all.application.persistence.exchange.Partition();

						partition.setDate(currentTimeStamp);

						if (coreData != null) {
							partition.setName(coreData.getName());
							partition.setDescription(coreData.getDescription());
							partition.setKeywords(coreData.getKeywords());
						}
					} else
						// avoid using basic data if not creating a main configuration
						coreData = null;

					if (partition.getName() == null)
						partition.setName(PartitionConfiguration.this.folder.getFileName().toString());

					if (partition.getSecurity() == null)
						partition.setSecurity(new SecurityGrantRW());

					persist(coreData == null ? null : coreData.getUser(), currentTimeStamp);
				}
			} catch (IOException e) {
				logger.warn(e.getMessage());

				partition = null;
			}
		}

		/**
		 * Returns true if the main configuration is available.
		 * 
		 * @return True if the main configuration is available.
		 * @since 17
		 */
		public boolean isMainConfigurationAvailable() {
			return partition != null;
		}

		/**
		 * Persist the main configuration with current update time stamp.
		 * 
		 * @param user The user.
		 * @return True if the main configuration could be persisted.
		 * @since 17
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
		 * @since 17
		 */
		private boolean persist(String user, Date updated) {
			if (isMainConfigurationAvailable())
				try {
					partition.setUser(user);
					partition.setUpdated(updated == null ? new Date() : updated);

					mainConfigurationManager.persist(partition);

					logger.info("Persisted the partition configuration.");

					return true;
				} catch (Exception e) {
					reloadMainConfiguration();

					logger.warn("Could not persist the partition configuration - " + e.getMessage());
				}

			return false;
		}

		/**
		 * Reloads the main configuration file.
		 * 
		 * @return True if the main configuration is available.
		 * @since 17
		 */
		public boolean reloadMainConfiguration() {
			loadMainConfiguration(null);

			return isMainConfigurationAvailable();
		}

		/**
		 * Returns the name.
		 * 
		 * @return The name.
		 * @since 17
		 */
		public String getName() {
			return isMainConfigurationAvailable() ? partition.getName() : null;
		}

		/**
		 * Returns the partition information.
		 * 
		 * @return The partition information.
		 * @since 17
		 */
		public Information getInformation() {
			return isMainConfigurationAvailable()
					? new Information(partition.getName(), partition.getDescription(),
							partition.getKeywords() == null ? null : new HashSet<>(partition.getKeywords()))
					: null;
		}

		/**
		 * Updates the information and persists the main configuration if the main
		 * configuration is available and the name is not null and empty.
		 *
		 * @param user        The user.
		 * @param information The partition information.
		 * @return True if the partition information was updated and persisted.
		 * @since 17
		 */
		public boolean update(String user, Information information) {
			if (isMainConfigurationAvailable() && information != null && information.getName() != null
					&& !information.getName().isBlank()) {
				partition.setName(information.getName().trim());
				partition.setDescription(
						information.getDescription() == null || information.getDescription().isBlank() ? null
								: information.getDescription().trim());
				partition.setKeywords(information.getKeywords());

				return persist(user);
			} else
				return false;
		}

		/**
		 * Clones the grants.
		 * 
		 * @param grants The grants to clone.
		 * @return The cloned the grants.
		 * @since 17
		 */
		private static Set<SecurityGrantRW.Grant<SecurityGrantRW.Right>> cloneGrant(
				Set<SecurityGrantRW.Grant<SecurityGrantRW.Right>> grants) {
			Set<SecurityGrantRW.Grant<SecurityGrantRW.Right>> clone = new HashSet<>();

			if (grants != null)
				for (SecurityGrantRW.Grant<SecurityGrantRW.Right> grant : grants) {
					if (grant.getRight() != null && grant.getTargets() != null)
						clone.add(
								new SecurityGrantRW.Grant<SecurityGrantRW.Right>(grant.getRight(), grant.getTargets()));

				}

			return clone.isEmpty() ? null : clone;
		}

		/**
		 * Clones the security.
		 * 
		 * @param grants The security grants to clone.
		 * @return The cloned the security grants.
		 * @since 17
		 */
		private static SecurityGrantRW cloneSecurity(SecurityGrantRW security) {

			return security == null ? null
					: new SecurityGrantRW(cloneGrant(security.getUsers()), cloneGrant(security.getGroups()),
							security.getOther());
		}

		/**
		 * Returns the security.
		 * 
		 * @return The security.
		 * @since 17
		 */
		public SecurityGrantRW getSecurity() {
			return cloneSecurity(partition.getSecurity());
		}

		/**
		 * Updates the security.
		 *
		 * @param user     The user.
		 * @param security The partition security.
		 * @return True if the partition security was updated and persisted.
		 * @since 17
		 */
		public boolean update(String user, SecurityGrantRW security) {
			if (isMainConfigurationAvailable()) {
				partition.setSecurity(cloneSecurity(security));

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
		 * @since 17
		 */
		private boolean isRightFulfilled(SecurityGrantRW.Right source, SecurityGrantRW.Right target) {
			return source != null && source.iFulfilled(target);
		}

		/**
		 * Returns the partition right for given user and groups.
		 * 
		 * @param target If not null, the search is ended as soon as the target right is
		 *               fulfilled. Otherwise search for the maximal fulfilled right.
		 * @param user   The user.
		 * @param groups The user groups.
		 * @return The partition right.
		 * @since 17
		 */
		private SecurityGrantRW.Right getRightFulfilled(SecurityGrantRW.Right target, String user,
				Collection<String> groups) {
			if (target == null)
				target = SecurityGrantRW.Right.maximal;

			SecurityGrantRW.Right right = null;

			if (isMainConfigurationAvailable()) {
				right = SecurityGrantRW.Right.getMaximnal(right, partition.getSecurity().getOther());

				if (isRightFulfilled(right, target))
					return right;

				if (partition.getSecurity().getUsers() != null && user != null && !user.isBlank()) {
					user = user.trim().toLowerCase();

					for (SecurityGrantRW.Grant<SecurityGrantRW.Right> grant : partition.getSecurity().getUsers())
						if (grant.getTargets().contains(user)) {
							right = SecurityGrantRW.Right.getMaximnal(right, grant.getRight());

							if (isRightFulfilled(right, target))
								return right;
						}
				}

				if (partition.getSecurity().getGroups() != null && groups != null)
					for (String group : groups) {
						if (group != null && !group.isBlank()) {
							group = group.trim().toLowerCase();

							for (SecurityGrantRW.Grant<SecurityGrantRW.Right> grant : partition.getSecurity()
									.getGroups())
								if (grant.getTargets().contains(group)) {
									right = SecurityGrantRW.Right.getMaximnal(right, grant.getRight());

									if (isRightFulfilled(right, target))
										return right;
								}
						}

					}
			}

			return right;
		}

		/**
		 * Returns the maximal fulfilled partition right for given user and groups.
		 * 
		 * @param user   The user.
		 * @param groups The user groups.
		 * @return The partition right.
		 * @since 17
		 */
		public SecurityGrantRW.Right getRight(String user, Collection<String> groups) {
			return getRightFulfilled(null, user, groups);
		}

		/**
		 * Returns true if the read right is fulfilled for given user and groups.
		 * 
		 * @param user   The user.
		 * @param groups The user groups.
		 * @return True if the read right is fulfilled for given user and groups.
		 * @since 17
		 */
		public boolean isRightRead(String user, Collection<String> groups) {
			SecurityGrantRW.Right right = getRightFulfilled(SecurityGrantRW.Right.read, user, groups);

			return right != null && right.isReadFulfilled();
		}

		/**
		 * Returns true if the write right is fulfilled for given user and groups.
		 * 
		 * @param user   The user.
		 * @param groups The user groups.
		 * @return True if the write right is fulfilled for given user and groups.
		 * @since 17
		 */
		public boolean isRightWrite(String user, Collection<String> groups) {
			SecurityGrantRW.Right right = getRightFulfilled(SecurityGrantRW.Right.write, user, groups);

			return right != null && right.isWriteFulfilled();
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
			return isMainConfigurationAvailable() ? partition.getUser() : null;
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
			return isMainConfigurationAvailable() ? partition.getDate() : null;
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
			return isMainConfigurationAvailable() ? partition.getUpdated() : null;
		}

		/**
		 * Information is an immutable class that defines information for partition
		 * configurations.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 17
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
			 * Creates an information for a partition configuration.
			 * 
			 * @param name        The name.
			 * @param description The description.
			 * @param keywords    The keywords.
			 * @since 17
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
			 * @since 17
			 */
			public String getName() {
				return name;
			}

			/**
			 * Returns the description.
			 *
			 * @return The description.
			 * @since 17
			 */
			public String getDescription() {
				return description;
			}

			/**
			 * Returns the keywords.
			 *
			 * @return The keywords.
			 * @since 17
			 */
			public Set<String> getKeywords() {
				return keywords;
			}

		}

		/**
		 * CoreData is an immutable class that defines core data for partition
		 * configurations.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 17
		 */
		public static class CoreData extends Information {
			/**
			 * The user.
			 */
			private final String user;

			/**
			 * Creates core data for a partition configuration.
			 * 
			 * @param user        The user.
			 * @param name        The name.
			 * @param description The description.
			 * @param keywords    The keywords.
			 * @since 17
			 */
			public CoreData(String user, String name, String description, Set<String> keywords) {
				super(name, description, keywords);

				this.user = user;
			}

			/**
			 * Returns the user.
			 *
			 * @return The user.
			 * @since 17
			 */
			public String getUser() {
				return user;
			}
		}

	}

}
