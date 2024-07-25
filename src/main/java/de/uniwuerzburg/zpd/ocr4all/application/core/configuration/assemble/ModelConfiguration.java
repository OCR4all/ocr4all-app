/**
 * File:     ModelConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.assemble
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     12.06.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.assemble;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.CoreFolder;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.TrackingData;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.assemble.Model;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Engine;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.security.SecurityGrantRWS;

/**
 * Defines configurations for the model.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public class ModelConfiguration extends CoreFolder {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ModelConfiguration.class);

	/**
	 * The id.
	 */
	private final String id;

	/**
	 * The configuration.
	 */
	private final Configuration configuration;

	/**
	 * Creates a configuration for the model.
	 * 
	 * @param properties The ocr4all model properties.
	 * @param folder     The model folder.
	 * @since 1.8
	 */
	public ModelConfiguration(Model properties, Path folder) {
		this(properties, folder, null);
	}

	/**
	 * Creates a configuration for the model.
	 * 
	 * @param properties The ocr4all model properties.
	 * @param folder     The model folder.
	 * @param coreData   The core data for a model configuration. If non null and
	 *                   the main configuration is not available, then this basic
	 *                   data is used to initialize the main configuration.
	 * @since 1.8
	 */
	public ModelConfiguration(Model properties, Path folder, Configuration.CoreData coreData) {
		super(folder);

		id = folder == null || !Files.isDirectory(folder) ? null : folder.getFileName().toString();

		configuration = new Configuration(properties.getConfiguration(), coreData);
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
	 * @since 1.8
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Defines configurations for the model.
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
		 * The engine configuration file.
		 */
		private final Path engineFile;

		/**
		 * The engine persistence manager.
		 */
		private final PersistenceManager engineManager;

		/**
		 * The model.
		 */
		private de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Model model = null;

		/**
		 * The engine.
		 */
		private Engine engine = null;

		/**
		 * Creates a configuration for the model.
		 * 
		 * @param properties The configuration properties for the model.
		 * @param coreData   The core data for a model configuration. If non null and
		 *                   the main configuration is not available, then this basic
		 *                   data is used to initialize the main configuration.
		 * @since 1.8
		 */
		Configuration(Model.Configuration properties, CoreData coreData) {
			super(Paths.get(ModelConfiguration.this.folder.toString(), properties.getFolder()));

			// Initialize the model configuration folder and consequently the
			// model
			ConfigurationService.initializeFolder(true, folder, "model configuration");

			// Initializes the configuration files
			engineFile = getPath(properties.getFiles().getEngine());

			// Loads the main configuration file
			mainConfigurationManager = new PersistenceManager(getPath(properties.getFiles().getMain()),
					Type.assemble_model_v1);
			loadMainConfiguration(coreData);

			// Load the engine configuration file
			engineManager = new PersistenceManager(engineFile, Type.assemble_engine_v1);
			loadEngineConfiguration();
		}

		/**
		 * Loads the engine configuration file.
		 * 
		 * @since 1.8
		 */
		private void loadEngineConfiguration() {
			try {
				engine = engineManager.getEntity(Engine.class, null, message -> logger.warn(message));
			} catch (IOException e) {
				logger.warn(e.getMessage());

				engine = null;
			}
		}

		/**
		 * Loads the main configuration file.
		 * 
		 * @param coreData The core data for a model configuration. If non null and the
		 *                 main configuration is not available, then this basic data is
		 *                 used to initialize the main configuration.
		 * @since 1.8
		 */
		private void loadMainConfiguration(CoreData coreData) {
			// Load main configuration
			try {
				model = mainConfigurationManager.getEntity(
						de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Model.class, null,
						message -> logger.warn(message));

				if (!isMainConfigurationAvailable() || model.getName() == null || model.getSecurity() == null) {
					Date currentTimeStamp = new Date();
					if (!isMainConfigurationAvailable()) {
						model = new de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Model();

						model.setDate(currentTimeStamp);

						if (coreData != null) {
							model.setName(coreData.getName());
							model.setDescription(coreData.getDescription());
							model.setKeywords(coreData.getKeywords());

							if (coreData.getUser() != null)
								model.setSecurity(
										new SecurityGrantRWS(SecurityGrantRWS.Right.maximal, coreData.getUser()));

						}
					} else
						// avoid using basic data if not creating a main configuration
						coreData = null;

					if (model.getName() == null)
						model.setName(ModelConfiguration.this.folder.getFileName().toString());

					if (model.getSecurity() == null)
						model.setSecurity(new SecurityGrantRWS());

					persist(coreData == null ? null : coreData.getUser(), currentTimeStamp);
				}
			} catch (IOException e) {
				logger.warn(e.getMessage());

				model = null;
			}
		}

		/**
		 * Returns true if the main configuration is available.
		 * 
		 * @return True if the main configuration is available.
		 * @since 1.8
		 */
		public boolean isMainConfigurationAvailable() {
			return model != null;
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
					model.setUser(user);
					model.setUpdated(updated == null ? new Date() : updated);

					mainConfigurationManager.persist(model);

					logger.info("Persisted the model configuration.");

					return true;
				} catch (Exception e) {
					reloadMainConfiguration();

					logger.warn("Could not persist the model configuration - " + e.getMessage());
				}

			return false;
		}

		/**
		 * Returns the engine configuration file.
		 *
		 * @return The engine configuration file.
		 * @since 1.8
		 */
		public Path getEngineFile() {
			return engineFile;
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
		 * Returns true if the engine configuration is available.
		 * 
		 * @return True if the engine configuration is available.
		 * @since 1.8
		 */
		public boolean isEngineConfigurationAvailable() {
			return engine != null;
		}

		/**
		 * Reloads the engine configuration file.
		 * 
		 * @return True if the engine configuration is available.
		 * @since 1.8
		 */
		public boolean reloadEngineConfiguration() {
			loadEngineConfiguration();

			return isEngineConfigurationAvailable();
		}

		/**
		 * Returns the engine configuration.
		 *
		 * @return The engine configuration.
		 * @since 17
		 */
		public Engine getEngineConfiguration() {
			return engine;
		}

		/**
		 * Returns the name.
		 * 
		 * @return The name.
		 * @since 1.8
		 */
		public String getName() {
			return isMainConfigurationAvailable() ? model.getName() : null;
		}

		/**
		 * Returns the model information.
		 * 
		 * @return The model information.
		 * @since 1.8
		 */
		public Information getInformation() {
			return isMainConfigurationAvailable()
					? new Information(model.getName(), model.getDescription(),
							model.getKeywords() == null ? null : new HashSet<>(model.getKeywords()))
					: null;
		}

		/**
		 * Updates the information and persists the main configuration if the main
		 * configuration is available and the name is not null and empty.
		 *
		 * @param user        The user.
		 * @param information The model information.
		 * @return True if the model information was updated and persisted.
		 * @since 1.8
		 */
		public boolean update(String user, Information information) {
			if (isMainConfigurationAvailable() && information != null && information.getName() != null
					&& !information.getName().isBlank()) {
				model.setName(information.getName().trim());
				model.setDescription(
						information.getDescription() == null || information.getDescription().isBlank() ? null
								: information.getDescription().trim());
				model.setKeywords(information.getKeywords());

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
		 * @since 1.8
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
		 * @since 1.8
		 */
		public SecurityGrantRWS getSecurity() {
			return cloneSecurity(model.getSecurity());
		}

		/**
		 * Updates the security.
		 *
		 * @param user     The user.
		 * @param security The model security.
		 * @return True if the model security was updated and persisted.
		 * @since 1.8
		 */
		public boolean update(String user, SecurityGrantRWS security) {
			if (isMainConfigurationAvailable()) {
				model.setSecurity(cloneSecurity(security));

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
		private boolean isRightFulfilled(SecurityGrantRWS.Right source, SecurityGrantRWS.Right target) {
			return source != null && source.iFulfilled(target);
		}

		/**
		 * Returns the model right for given user and groups.
		 * 
		 * @param target If not null, the search is ended as soon as the target right is
		 *               fulfilled. Otherwise search for the maximal fulfilled right.
		 * @param user   The user.
		 * @param groups The user groups.
		 * @return The model right.
		 * @since 1.8
		 */
		private SecurityGrantRWS.Right getRightFulfilled(SecurityGrantRWS.Right target, String user,
				Collection<String> groups) {
			if (target == null)
				target = SecurityGrantRWS.Right.maximal;

			SecurityGrantRWS.Right right = null;

			if (isMainConfigurationAvailable()) {
				right = SecurityGrantRWS.Right.getMaximnal(right, model.getSecurity().getOther());

				if (isRightFulfilled(right, target))
					return right;

				if (model.getSecurity().getUsers() != null && user != null && !user.isBlank()) {
					user = user.trim().toLowerCase();

					for (SecurityGrantRWS.Grant<SecurityGrantRWS.Right> grant : model.getSecurity().getUsers())
						if (grant.getTargets().contains(user)) {
							right = SecurityGrantRWS.Right.getMaximnal(right, grant.getRight());

							if (isRightFulfilled(right, target))
								return right;
						}
				}

				if (model.getSecurity().getGroups() != null && groups != null)
					for (String group : groups) {
						if (group != null && !group.isBlank()) {
							group = group.trim().toLowerCase();

							for (SecurityGrantRWS.Grant<SecurityGrantRWS.Right> grant : model.getSecurity().getGroups())
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
		 * Returns the maximal fulfilled model right for given user and groups.
		 * 
		 * @param user   The user.
		 * @param groups The user groups.
		 * @return The model right.
		 * @since 1.8
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
		 * @since 1.8
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
		 * @since 1.8
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
		 * @since 1.8
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
			return isMainConfigurationAvailable() ? model.getUser() : null;
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
			return isMainConfigurationAvailable() ? model.getDate() : null;
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
			return isMainConfigurationAvailable() ? model.getUpdated() : null;
		}

		/**
		 * Updates the engine information and persists it if the main configuration is
		 * available.
		 *
		 * @param user        The user.
		 * @param information The model engine information.
		 * @return True if the model engine information was updated and persisted.
		 * @since 1.8
		 */
		public boolean update(String user, EngineInformation information) {
			if (isMainConfigurationAvailable() && information != null)
				try {
					if (!reloadEngineConfiguration())
						engine = new Engine();

					if (engine.getUser() == null)
						engine.setUser(user);

					engine.setMethod(information.getMethod());
					engine.setState(information.getState());
					engine.setType(information.getType());

					if (information.isVersionSet())
						engine.setVersion(information.getVersion());

					if (information.isNameSet())
						engine.setName(information.getName());

					if (information.isArgumentsSet())
						engine.setArguments(information.getArguments());

					engineManager.persist(engine);

					logger.info("Persisted the model engine configuration.");

					return true;
				} catch (Exception e) {
					reloadEngineConfiguration();

					logger.warn("Could not persist the model engine configuration - " + e.getMessage());
				}

			return false;
		}

		/**
		 * Information is an immutable class that defines information for model
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
			 * Creates an information for a model configuration.
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
		 * CoreData is an immutable class that defines core data for model
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
			 * Creates core data for a model configuration.
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

		/**
		 * EngineInformation is an immutable class that defines information for model
		 * engine configurations.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class EngineInformation {
			/**
			 * The method.
			 */
			private final Engine.Method method;

			/**
			 * The state.
			 */
			private final Engine.State state;

			/**
			 * The type.
			 */
			private final Engine.Type type;

			/**
			 * The version.
			 */
			private final String version;

			/**
			 * The name.
			 */
			private final String name;

			/**
			 * The arguments.
			 */
			private final List<String> arguments;

			/**
			 * Creates an information for a model engine configuration. Null arguments are
			 * not updated.
			 * 
			 * @param method    The method.
			 * @param state     The state.
			 * @param type      The type.
			 * @param version   The version. Empty resets the version.
			 * @param name      The name. Empty resets the name.
			 * @param arguments The arguments. Empty list resets the arguments.
			 * @since 17
			 */
			public EngineInformation(Engine.Method method, Engine.State state, Engine.Type type, String version,
					String name, List<String> arguments) {
				super();

				this.method = method;
				this.state = state;
				this.type = type;
				this.version = version;
				this.name = name;
				this.arguments = arguments;
			}

			/**
			 * Returns the method.
			 *
			 * @return The method.
			 * @since 17
			 */
			public Engine.Method getMethod() {
				return method;
			}

			/**
			 * Returns the state.
			 *
			 * @return The state.
			 * @since 17
			 */
			public Engine.State getState() {
				return state;
			}

			/**
			 * Returns the type.
			 *
			 * @return The type.
			 * @since 17
			 */
			public Engine.Type getType() {
				return type;
			}

			/**
			 * Returns true if the version is set.
			 *
			 * @return True if the version is set.
			 * @since 17
			 */
			public boolean isVersionSet() {
				return version != null;
			}

			/**
			 * Returns the version.
			 *
			 * @return The version.
			 * @since 17
			 */
			public String getVersion() {
				return isVersionSet() && !version.isBlank() ? version.trim() : null;
			}

			/**
			 * Returns true if the name is set.
			 *
			 * @return True if the name is set.
			 * @since 17
			 */
			public boolean isNameSet() {
				return name != null;
			}

			/**
			 * Returns the name.
			 *
			 * @return The name.
			 * @since 17
			 */
			public String getName() {
				return isNameSet() && !name.isBlank() ? name.trim() : null;
			}

			/**
			 * Returns true if the arguments are set.
			 *
			 * @return True if the arguments are set.
			 * @since 17
			 */
			public boolean isArgumentsSet() {
				return arguments != null;
			}

			/**
			 * Returns the arguments.
			 *
			 * @return The arguments.
			 * @since 17
			 */
			public List<String> getArguments() {
				return isArgumentsSet() && !arguments.isEmpty() ? arguments : null;
			}

		}

	}

}
