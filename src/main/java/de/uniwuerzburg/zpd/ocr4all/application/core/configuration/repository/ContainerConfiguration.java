/**
 * File:     ContainerConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.repository
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.11.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.CoreFolder;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.TrackingData;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.repository.Container;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.ImageFormat;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.security.SecurityGrantRWS;

/**
 * Defines configurations for the container.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public class ContainerConfiguration extends CoreFolder {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ContainerConfiguration.class);

	/**
	 * The id.
	 */
	private final String id;

	/**
	 * The configuration.
	 */
	private final Configuration configuration;

	/**
	 * The images.
	 */
	private final Images images;

	/**
	 * Creates a configuration for the container.
	 * 
	 * @param properties The ocr4all container properties.
	 * @param folder     The container folder.
	 * @since 17
	 */
	public ContainerConfiguration(Container properties, Path folder) {
		this(properties, folder, null);
	}

	/**
	 * Creates a configuration for the container.
	 * 
	 * @param properties The ocr4all container properties.
	 * @param folder     The container folder.
	 * @param coreData   The core data for a container configuration. If non null
	 *                   and the main configuration is not available, then this
	 *                   basic data is used to initialize the main configuration.
	 * @since 17
	 */
	public ContainerConfiguration(Container properties, Path folder, Configuration.CoreData coreData) {
		super(folder);

		id = folder == null || !Files.isDirectory(folder) ? null : folder.getFileName().toString();

		configuration = new Configuration(properties.getConfiguration(), coreData);
		images = new Images(properties);
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
	 * Returns the images.
	 *
	 * @return The images.
	 * @since 17
	 */
	public Images getImages() {
		return images;
	}

	/**
	 * Defines configurations for the container.
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
		 * The folio configuration file.
		 */
		private final Path folioFile;

		/**
		 * The container.
		 */
		private de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Container container = null;

		/**
		 * Creates a configuration for the container.
		 * 
		 * @param properties The configuration properties for the container.
		 * @param coreData   The core data for a container configuration. If non null
		 *                   and the main configuration is not available, then this
		 *                   basic data is used to initialize the main configuration.
		 * @since 17
		 */
		Configuration(Container.Configuration properties, CoreData coreData) {
			super(Paths.get(ContainerConfiguration.this.folder.toString(), properties.getFolder()));

			// Initialize the container configuration folder and consequently the
			// container
			ConfigurationService.initializeFolder(true, folder, "container configuration");

			// Initializes the configuration files
			folioFile = getPath(properties.getFiles().getFolio());

			// Loads the main configuration file
			mainConfigurationManager = new PersistenceManager(getPath(properties.getFiles().getMain()),
					Type.repository_container_v1);
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
				container = mainConfigurationManager.getEntity(
						de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Container.class, null,
						message -> logger.warn(message));

				if (!isMainConfigurationAvailable() || container.getName() == null || container.getSecurity() == null) {
					Date currentTimeStamp = new Date();
					if (!isMainConfigurationAvailable()) {
						container = new de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Container();

						container.setDate(currentTimeStamp);

						if (coreData != null) {
							container.setName(coreData.getName());
							container.setDescription(coreData.getDescription());
							container.setKeywords(coreData.getKeywords());

							if (coreData.getUser() != null)
								container.setSecurity(new SecurityGrantRWS(

										SecurityGrantRWS.Right.maximal, coreData.getUser()));

						}
					} else
						// avoid using basic data if not creating a main configuration
						coreData = null;

					if (container.getName() == null)
						container.setName(ContainerConfiguration.this.folder.getFileName().toString());

					if (container.getSecurity() == null)
						container.setSecurity(new SecurityGrantRWS());

					persist(coreData == null ? null : coreData.getUser(), currentTimeStamp);
				}
			} catch (IOException e) {
				logger.warn(e.getMessage());

				container = null;
			}
		}

		/**
		 * Returns true if the main configuration is available.
		 * 
		 * @return True if the main configuration is available.
		 * @since 17
		 */
		public boolean isMainConfigurationAvailable() {
			return container != null;
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
					container.setUser(user);
					container.setUpdated(updated == null ? new Date() : updated);

					mainConfigurationManager.persist(container);

					logger.info("Persisted the container configuration.");

					return true;
				} catch (Exception e) {
					reloadMainConfiguration();

					logger.warn("Could not persist the container configuration - " + e.getMessage());
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
		 * Returns the folio configuration file.
		 *
		 * @return The folio configuration file.
		 * @since 17
		 */
		public Path getFolioFile() {
			return folioFile;
		}

		/**
		 * Returns the name.
		 * 
		 * @return The name.
		 * @since 17
		 */
		public String getName() {
			return isMainConfigurationAvailable() ? container.getName() : null;
		}

		/**
		 * Returns the container information.
		 * 
		 * @return The container information.
		 * @since 17
		 */
		public Information getInformation() {
			return isMainConfigurationAvailable()
					? new Information(container.getName(), container.getDescription(),
							container.getKeywords() == null ? null : new HashSet<>(container.getKeywords()))
					: null;
		}

		/**
		 * Updates the information and persists the main configuration if the main
		 * configuration is available and the name is not null and empty.
		 *
		 * @param user        The user.
		 * @param information The container information.
		 * @return True if the container information was updated and persisted.
		 * @since 17
		 */
		public boolean update(String user, Information information) {
			if (isMainConfigurationAvailable() && information != null && information.getName() != null
					&& !information.getName().isBlank()) {
				container.setName(information.getName().trim());
				container.setDescription(
						information.getDescription() == null || information.getDescription().isBlank() ? null
								: information.getDescription().trim());
				container.setKeywords(information.getKeywords());

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
		private static Set<SecurityGrantRWS.Grant<SecurityGrantRWS.Right>> cloneGrant(
				Set<SecurityGrantRWS.Grant<SecurityGrantRWS.Right>> grants) {
			Set<SecurityGrantRWS.Grant<SecurityGrantRWS.Right>> clone = new HashSet<>();

			if (grants != null)
				for (SecurityGrantRWS.Grant<SecurityGrantRWS.Right> grant : grants) {
					if (grant.getRight() != null && grant.getTargets() != null)
						clone.add(new SecurityGrantRWS.Grant<SecurityGrantRWS.Right>(grant.getRight(),
								grant.getTargets()));

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
		private static SecurityGrantRWS cloneSecurity(SecurityGrantRWS security) {

			return security == null ? null
					: new SecurityGrantRWS(cloneGrant(security.getUsers()), cloneGrant(security.getGroups()),
							security.getOther());
		}

		/**
		 * Returns the security.
		 * 
		 * @return The security.
		 * @since 17
		 */
		public SecurityGrantRWS getSecurity() {
			return cloneSecurity(container.getSecurity());
		}

		/**
		 * Updates the security.
		 *
		 * @param user     The user.
		 * @param security The container security.
		 * @return True if the container security was updated and persisted.
		 * @since 17
		 */
		public boolean update(String user, SecurityGrantRWS security) {
			if (isMainConfigurationAvailable()) {
				container.setSecurity(cloneSecurity(security));

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
		private boolean isRightFulfilled(SecurityGrantRWS.Right source, SecurityGrantRWS.Right target) {
			return source != null && source.iFulfilled(target);
		}

		/**
		 * Returns the container right for given user and groups.
		 * 
		 * @param target If not null, the search is ended as soon as the target right is
		 *               fulfilled. Otherwise search for the maximal fulfilled right.
		 * @param user   The user.
		 * @param groups The user groups.
		 * @return The container right.
		 * @since 17
		 */
		private SecurityGrantRWS.Right getRightFulfilled(SecurityGrantRWS.Right target, String user,
				Collection<String> groups) {
			if (target == null)
				target = SecurityGrantRWS.Right.maximal;

			SecurityGrantRWS.Right right = null;

			if (isMainConfigurationAvailable()) {
				right = SecurityGrantRWS.Right.getMaximnal(right, container.getSecurity().getOther());

				if (isRightFulfilled(right, target))
					return right;

				if (container.getSecurity().getUsers() != null && user != null && !user.isBlank()) {
					user = user.trim().toLowerCase();

					for (SecurityGrantRWS.Grant<SecurityGrantRWS.Right> grant : container.getSecurity().getUsers())
						if (grant.getTargets().contains(user)) {
							right = SecurityGrantRWS.Right.getMaximnal(right, grant.getRight());

							if (isRightFulfilled(right, target))
								return right;
						}
				}

				if (container.getSecurity().getGroups() != null && groups != null)
					for (String group : groups) {
						if (group != null && !group.isBlank()) {
							group = group.trim().toLowerCase();

							for (SecurityGrantRWS.Grant<SecurityGrantRWS.Right> grant : container.getSecurity()
									.getGroups())
								if (grant.getTargets().contains(group)) {
									right = SecurityGrantRWS.Right.getMaximnal(right, grant.getRight());

									if (isRightFulfilled(right, target))
										return right;
								}
						}

					}
			}

			return right;
		}

		/**
		 * Returns the maximal fulfilled container right for given user and groups.
		 * 
		 * @param user   The user.
		 * @param groups The user groups.
		 * @return The container right.
		 * @since 17
		 */
		public SecurityGrantRWS.Right getRight(String user, Collection<String> groups) {
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
			SecurityGrantRWS.Right right = getRightFulfilled(SecurityGrantRWS.Right.read, user, groups);

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
			SecurityGrantRWS.Right right = getRightFulfilled(SecurityGrantRWS.Right.write, user, groups);

			return right != null && right.isWriteFulfilled();
		}

		/**
		 * Returns true if the special right is fulfilled for given user and groups.
		 * 
		 * @param user   The user.
		 * @param groups The user groups.
		 * @return True if the special right is fulfilled for given user and groups.
		 * @since 17
		 */
		public boolean isRightSpecial(String user, Collection<String> groups) {
			SecurityGrantRWS.Right right = getRightFulfilled(SecurityGrantRWS.Right.special, user, groups);

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
			return isMainConfigurationAvailable() ? container.getUser() : null;
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
			return isMainConfigurationAvailable() ? container.getDate() : null;
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
			return isMainConfigurationAvailable() ? container.getUpdated() : null;
		}

		/**
		 * Information is an immutable class that defines information for container
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
			 * Creates an information for a container configuration.
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
		 * CoreData is an immutable class that defines core data for container
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
			 * Creates core data for a container configuration.
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

	/**
	 * Images is an immutable class that defines images folder for containers.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public class Images {

		/**
		 * The folder for folios.
		 */
		private final Path folios;

		/**
		 * The normalized.
		 */
		private final Normalized normalized;

		/**
		 * The folios derivatives.
		 */
		private final Derivatives derivatives;

		/**
		 * Creates images folder for a project.
		 * 
		 * @param properties The container properties.
		 * @since 17
		 */
		public Images(Container properties) {
			super();

			// Initializes the folders
			folios = getPath(null, properties.getFolios().getFolder(), "folios");
			normalized = new Normalized(ImageFormat.getImageFormat(properties.getNormalized().getFormat()),
					getPath(null, properties.getNormalized().getFolder(), "normalized"));

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
		 * @since 17
		 */
		private Path getPath(String derivatives, String folder, String name) {
			Path path = derivatives == null
					? Paths.get(ContainerConfiguration.this.folder.toString(), folder).normalize()
					: Paths.get(ContainerConfiguration.this.folder.toString(), derivatives, folder).normalize();

			ConfigurationService.initializeFolder(true, path,
					"container '" + ContainerConfiguration.this.folder.getFileName() + "' " + name);

			return path;
		}

		/**
		 * Returns true if the folder for folios is a directory.
		 *
		 * @return True if the folder is a directory; false if the folder does not
		 *         exist, is not a directory, or it cannot be determined if the folder
		 *         is a directory or not.
		 * 
		 * @since 17
		 */
		public boolean isFoliosDirectory() {
			return Files.isDirectory(folios);
		}

		/**
		 * Returns the folder for folios.
		 *
		 * @return The folder for folios.
		 * @since 17
		 */
		public Path getFolios() {
			return folios;
		}

		/**
		 * Returns the normalized.
		 *
		 * @return The normalized.
		 * @since 17
		 */
		public Normalized getNormalized() {
			return normalized;
		}

		/**
		 * Returns the derivatives quality image folders for folios.
		 *
		 * @return The derivatives quality image folders for folios.
		 * @since 17
		 */
		public Derivatives getDerivatives() {
			return derivatives;
		}

		/**
		 * Reset the images.
		 *
		 * @return True if the images could be reset.
		 * @since 17
		 */
		public boolean reset() {
			boolean isResetFolios = Files.exists(folios) ? deleteContents(folios) : true;
			boolean isResetNormalized = Files.exists(normalized.getFolder()) ? deleteContents(normalized.getFolder())
					: true;
			boolean isResetDerivatives = derivatives.reset();

			return isResetFolios && isResetNormalized && isResetDerivatives;
		}

		/**
		 * Derivatives is an immutable class that defines normalized image folders for
		 * folios.
		 *
		 * 
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 17
		 */
		public class Normalized {
			/**
			 * The normalized image format. The format must be suitable for use on web
			 * pages, since are required by LAREX.
			 */
			private final ImageFormat format;

			/**
			 * The folder.
			 */
			private final Path folder;

			/**
			 * Creates normalized image folders for folios.
			 * 
			 * @param format The normalized image format. The format must be suitable for
			 *               use on web pages, since are required by LAREX.
			 * @param folder The folder.
			 * @since 17
			 */
			public Normalized(ImageFormat format, Path folder) {
				super();

				this.format = format.isWebPages() ? format : ImageFormat.png;
				this.folder = folder;
			}

			/**
			 * Returns the folios derivatives format.
			 *
			 * @return The folios derivatives format.
			 * @since 17
			 */
			public ImageFormat getFormat() {
				return format;
			}

			/**
			 * Returns true if the folder is a directory.
			 *
			 * @return True if the folder is a directory; false if the folder does not
			 *         exist, is not a directory, or it cannot be determined if the folder
			 *         is a directory or not.
			 * 
			 * @since 17
			 */
			public boolean isDirectory() {
				return Files.isDirectory(folder);
			}

			/**
			 * Returns the folder.
			 *
			 * @return The folder.
			 * @since 17
			 */
			public Path getFolder() {
				return folder;
			}

		}

		/**
		 * Derivatives is an immutable class that defines derivatives quality image
		 * folders for folios.
		 *
		 * 
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 17
		 */
		public class Derivatives {
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
			 * @since 17
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
			 * @since 17
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
			 * @since 17
			 */
			public boolean isThumbnailDirectory() {
				return Files.isDirectory(thumbnail);
			}

			/**
			 * Returns the folder for folios derivatives quality thumbnail.
			 *
			 * @return The folder for folios derivatives quality thumbnail.
			 * @since 17
			 */
			public Path getThumbnail() {
				return thumbnail;
			}

			/**
			 * Reset the folios derivatives quality thumbnail.
			 *
			 * @return True if the folios derivatives quality thumbnail could be reset.
			 * @since 17
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
			 * @since 17
			 */
			public boolean isDetailDirectory() {
				return Files.isDirectory(detail);
			}

			/**
			 * Returns the folder for folios derivatives quality detail.
			 *
			 * @return The folder for folios derivatives quality detail.
			 * @since 17
			 */
			public Path getDetail() {
				return detail;
			}

			/**
			 * Reset the folios derivatives quality detail.
			 *
			 * @return True if the folios derivatives quality detail could be reset.
			 * @since 17
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
			 * @since 17
			 */
			public boolean isBestDirectory() {
				return Files.isDirectory(best);
			}

			/**
			 * Returns the folder for folios derivatives quality best.
			 *
			 * @return The folder for folios derivatives quality best.
			 * @since 17
			 */
			public Path getBest() {
				return best;
			}

			/**
			 * Reset the folios derivatives quality best.
			 *
			 * @return True if the folios derivatives quality best could be reset.
			 * @since 17
			 */
			public boolean resetBest() {
				return Files.exists(best) ? deleteContents(best) : true;
			}

			/**
			 * Reset the folios derivatives.
			 *
			 * @return True if the folios derivatives could be reset.
			 * @since 17
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
	}

}
