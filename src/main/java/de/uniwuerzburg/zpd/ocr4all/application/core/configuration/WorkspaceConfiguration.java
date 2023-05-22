/**
 * File:     ExchangeConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     23.03.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.Workspace;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Entity;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Identifier;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.DisabledServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.LazyInitializedServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.TaskExecutorServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.ConfigurationServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.SystemCommand;

/**
 * Defines configurations for the workspace.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class WorkspaceConfiguration extends CoreFolder {
	/**
	 * The comment character for application files.
	 */
	public static final String applicationFileCommentCharacter = "#";

	/**
	 * The comment line for application files.
	 */
	public static final String applicationFileCommentLine = applicationFileCommentCharacter + System.lineSeparator();

	/**
	 * Defines versions.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public enum Version {
		_1_0("1.0");

		/**
		 * The default version.
		 */
		public final static Version defaultVertsion = _1_0;

		/**
		 * The label.
		 */
		private final String label;

		/**
		 * Creates a version.
		 * 
		 * @param label
		 * @since 1.8
		 */
		private Version(String label) {
			this.label = label;
		}

		/**
		 * Returns true if the given label matches the version label.
		 *
		 * @param label The label.
		 * @return True if the given label matches the version label.
		 * @since 1.8
		 */
		public boolean isLabel(String label) {
			return label != null && this.label.equals(label);
		}

		/**
		 * Returns the label.
		 *
		 * @return The label.
		 * @since 1.8
		 */
		public String getLabel() {
			return label;
		}

		/**
		 * Returns the version for given label.
		 * 
		 * @param label The label.
		 * @return The version for given label. Empty, if the version is not defined.
		 * @since 1.8
		 */
		public static Optional<Version> getVersion(String label) {
			if (label != null) {
				label = label.trim();

				for (Version version : Version.values())
					if (version.isLabel(label))
						return Optional.of(version);
			}
			return Optional.empty();
		}
	}

	/**
	 * Defines fields for main configuration files.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	private enum MainConfigurationField {
		configurationVersion("configuration-version"), instanceName("instance-name"),
		systemCommandDocker("system-command.docker"), systemCommandConvert("system-command.convert"),
		systemCommandIdentify("system-command.identify"), serviceProvider(true, "service-provider.");

		/**
		 * The label.
		 */
		private final String label;

		/**
		 * True if this is a prefix.
		 */
		private final boolean isPrefix;

		/**
		 * Creates a field for a main configuration file.
		 * 
		 * @param label The label.
		 * @since 1.8
		 */
		private MainConfigurationField(String label) {
			isPrefix = false;
			this.label = label;
		}

		/**
		 * Creates a field for a main configuration file.
		 * 
		 * @param isPrefix The label.
		 * @param label    The label.
		 * @since 1.8
		 */
		private MainConfigurationField(boolean isPrefix, String label) {
			this.isPrefix = isPrefix;
			this.label = label;
		}

		/**
		 * Returns true if the given label matches the field label.
		 *
		 * @param label The label.
		 * @return True if the given label matches the field label.
		 * @since 1.8
		 */
		public boolean isLabel(String label) {
			return label != null && (isPrefix ? label.startsWith(this.label) : label.equals(this.label));
		}

		/**
		 * Returns the label.
		 *
		 * @return The label.
		 * @since 1.8
		 */
		public String getLabel() {
			return label;
		}

		/**
		 * Returns the suffix part of the label.
		 * 
		 * @param label The label.
		 * @return The suffix. Null, if the label is not a prefix.
		 * @since 1.8
		 */
		public String getSuffix(String label) {
			return isLabel(label) && label.length() > this.label.length() ? label.substring(this.label.length()) : null;
		}

		/**
		 * Returns the field for given label.
		 * 
		 * @param label The label. The label is case insensitive.
		 * @return The field for given label. Empty, if the field is not defined.
		 * @since 1.8
		 */
		public static Optional<MainConfigurationField> getField(String label) {
			if (label != null) {
				label = label.trim().toLowerCase();

				for (MainConfigurationField field : MainConfigurationField.values())
					if (field.isLabel(label))
						return Optional.of(field);
			}
			return Optional.empty();
		}

	}

	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WorkspaceConfiguration.class);

	/**
	 * The configuration for the application.
	 */
	private final ApplicationConfiguration applicationConfiguration;

	/**
	 * The configuration.
	 */
	private final Configuration configuration;

	/**
	 * The workflows.
	 */
	private final WorkflowsConfiguration workflows;

	/**
	 * The projects.
	 */
	private final ProjectsConfiguration projects;

	/**
	 * Creates a configuration for the workspace.
	 * 
	 * @param properties               The workspace properties.
	 * @param systemProperties         The system properties.
	 * @param applicationConfiguration The configuration for the application.
	 * @param exchangeConfiguration    The configuration for the exchange.
	 * @param optConfiguration         The configuration for the opt.
	 * @since 1.8
	 */
	public WorkspaceConfiguration(Workspace properties,
			de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.System systemProperties,
			ApplicationConfiguration applicationConfiguration, ExchangeConfiguration exchangeConfiguration,
			OptConfiguration optConfiguration) {
		super(Paths.get(properties.getFolder()));

		this.applicationConfiguration = applicationConfiguration;

		ConfigurationService.initializeFolder(true, folder, "workspace");

		configuration = new Configuration(properties.getConfiguration(), systemProperties);
		workflows = new WorkflowsConfiguration(properties.getWorkflows(), this);
		projects = new ProjectsConfiguration(properties.getProjects(), exchangeConfiguration, optConfiguration, this);
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
	 * Returns the workflows.
	 *
	 * @return The workflows.
	 * @since 1.8
	 */
	public WorkflowsConfiguration getWorkflows() {
		return workflows;
	}

	/**
	 * Returns the projects.
	 *
	 * @return The projects.
	 * @since 1.8
	 */
	public ProjectsConfiguration getProjects() {
		return projects;
	}

	/**
	 * Defines functional interfaces to load application file entries.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	@FunctionalInterface
	public interface ApplicationFileEntry {
		/**
		 * Adds the application file entry.
		 * 
		 * @param line  The entry line number.
		 * @param entry The entry to add.
		 * @return True iff the entry could be added.
		 * @since 1.8
		 */
		public boolean add(int line, String entry);
	}

	/**
	 * Defines callback methods to perform after load application files.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	@FunctionalInterface
	public interface ApplicationFileLoad {
		/**
		 * Callback method to perform after load the application file.
		 * 
		 * @param file  The configuration file.
		 * @param lines The number of lines in the configuration file.
		 * @param added The number of added entries.
		 * @param total The total number of entries.
		 * @since 1.8
		 */
		public void callback(Path file, int lines, int added, int total);
	}

	/**
	 * Loads the data of the given application file. Empty lines or lines starting
	 * with # are ignored. If the application file does not exist, it is created.
	 * 
	 * @param applicationFile      The application file.
	 * @param defaultEntry         The default entry to add to the application file
	 *                             if it is created.
	 * @param applicationFileEntry The functional interfaces to add the application
	 *                             file entries.
	 * @param applicationFileLoad  The callback method to perform after load the
	 *                             application file. Null if not required.
	 * @return The application configuration file. Empty, if the file does not exist
	 *         or the data cannot loaded.
	 * @since 1.8
	 */
	public Optional<Path> load(Optional<Path> applicationFile, String defaultEntry,
			ApplicationFileEntry applicationFileEntry, ApplicationFileLoad applicationFileLoad) {
		if (applicationFile.isPresent()) {
			Path file = applicationFile.get();

			if (Files.exists(file)) {
				try {
					int lineNumber = 0;
					int added = 0;
					int total = 0;
					for (String line : Files.readAllLines(file, applicationConfiguration.getCharset())) {
						lineNumber++;
						line = line.trim();

						if (line.isEmpty() || line.startsWith(applicationFileCommentCharacter))
							continue;

						if (applicationFileEntry.add(lineNumber, line))
							added++;

						total++;
					}

					if (applicationFileLoad != null)
						applicationFileLoad.callback(file, lineNumber, added, total);

				} catch (IOException e) {
					logger.warn("Cannot read application file '" + file + "' - " + e.getMessage());

					return Optional.empty();
				}
			} else
				try {
					if (defaultEntry == null)
						Files.createFile(file);
					else
						try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file.toFile()),
								applicationConfiguration.getCharset())) {
							// TODO: Java 11
							// use Files.writeString();
							writer.write(defaultEntry);
						}

					logger.info("Created application file '" + file + "'.");
				} catch (IOException e) {
					e.printStackTrace();
					logger.warn("Cannot create application file '" + file + "' - " + e.getMessage());

					return Optional.empty();
				}
		}

		return applicationFile;
	}

	/**
	 * Returns the application file header.
	 * 
	 * @param file    The file. Null if no file name is required.
	 * @param head    The head. Null if no head is required.
	 * @param version The version of the application file. Null if no version is
	 *                required.
	 * @param syntax  The syntax lines.
	 * @return The application file header.
	 * @since 1.8
	 */
	public static String getApplicationFileHeader(Path file, String head, Version version, String... syntax) {
		return getApplicationFileHeader(file, head, version, null, syntax);
	}

	/**
	 * Returns the application file header.
	 * 
	 * @param file        The file. Null if no file name is required.
	 * @param head        The head. Null if no head is required.
	 * @param version     The version of the application file. Null if no version is
	 *                    required.
	 * @param description The description. Null if no description is required.
	 * @param syntax      The syntax lines.
	 * @return The application file header.
	 * @since 1.8
	 */
	public static String getApplicationFileHeader(Path file, String head, Version version, List<String> description,
			String... syntax) {
		if (head != null && head.isBlank())
			head = null;

		if (file != null)
			head = file.getFileName() + (head == null ? "" : " - " + head);

		StringBuffer descriptionBuffer = new StringBuffer();
		if (description != null)
			for (String line : description)
				if (line != null)
					descriptionBuffer.append(applicationFileCommentCharacter + (line.isBlank() ? "" : " ") + line
							+ System.lineSeparator());

		StringBuffer syntaxBuffer = new StringBuffer();
		for (String line : syntax)
			if (line != null)
				syntaxBuffer.append(applicationFileCommentCharacter + (line.isBlank() ? "" : "   ") + line
						+ System.lineSeparator());

		return head == null && version == null && descriptionBuffer.length() == 0 && syntaxBuffer.length() == 0 ? ""
				: applicationFileCommentLine
						+ (head == null ? ""
								: applicationFileCommentCharacter + " " + head + System.lineSeparator()
										+ (version == null ? applicationFileCommentLine : ""))
						+ (version == null ? ""
								: applicationFileCommentCharacter + " (v" + version.getLabel() + ")"
										+ System.lineSeparator() + applicationFileCommentLine)
						+ (descriptionBuffer.length() == 0 ? ""
								: descriptionBuffer.toString() + applicationFileCommentLine)
						+ (syntaxBuffer.length() == 0 ? ""
								: applicationFileCommentCharacter + " syntax:" + System.lineSeparator()
										+ syntaxBuffer.toString() + applicationFileCommentLine);
	}

	/**
	 * Defines configurations for the workspaces.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public class Configuration extends CoreFolder {
		/**
		 * True if the application needs to be restarted due to configuration updates.
		 */
		private boolean isRestart;

		/**
		 * The version.
		 */
		private Version version;

		/**
		 * The instance name. Null if not defined.
		 */
		private String instance = null;

		/**
		 * The configuration for service providers.
		 */
		private ConfigurationServiceProvider configurationServiceProvider;

		/**
		 * The user configuration file.
		 */
		private final Path userFile;

		/**
		 * The group configuration file.
		 */
		private final Path groupFile;

		/**
		 * The password configuration file.
		 */
		private final Path passwordFile;

		/**
		 * The system properties.
		 */
		private final de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.System systemProperties;

		/**
		 * The system command docker.
		 */
		private String systemCommandDocker = null;

		/**
		 * The system command convert.
		 */
		private String systemCommandConvert = null;

		/**
		 * The system command identify.
		 */
		private String systemCommandIdentify = null;

		/**
		 * The service provider configuration persistence manager.
		 */
		private final PersistenceManager serviceProviderConfigurationManager;

		/**
		 * The disabled service providers, this means, they are inactive when the
		 * application is launched.
		 */
		private final Hashtable<String, DisabledServiceProvider> disabledServiceProviders = new Hashtable<>();

		/**
		 * The lazy initialized service providers, this means, their initialization are
		 * deferred and will be performed in a new thread. Otherwise, initialization are
		 * performed as soon as the provider is loaded.
		 */
		private final Hashtable<String, LazyInitializedServiceProvider> lazyInitializedServiceProviders = new Hashtable<>();

		/**
		 * The task executor service providers, this means, the scheduler service
		 * executes the service providers in a separate pool of threads.
		 */
		private final Hashtable<String, TaskExecutorServiceProvider> taskExecutorServiceProviders = new Hashtable<>();

		/**
		 * Creates a configuration for the workspace.
		 * 
		 * @param properties       The configuration properties for the workspace.
		 * @param systemProperties The system properties.
		 * @since 1.8
		 */
		public Configuration(Workspace.Configuration properties,
				de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.System systemProperties) {
			super(Paths.get(WorkspaceConfiguration.this.folder.toString(), properties.getFolder()));

			this.systemProperties = systemProperties;

			/*
			 * Initialize the workspace configuration folder and consequently the workspace
			 * folder
			 */
			ConfigurationService.initializeFolder(true, folder, "workspace configuration");

			// Initializes the configuration files
			userFile = getPath(properties.getFiles().getUser());
			groupFile = getPath(properties.getFiles().getGroup());
			passwordFile = getPath(properties.getFiles().getPassword());

			// Loads the main configuration file
			loadMainConfiguration(Paths.get(folder.toString(), properties.getFiles().getMain()));

			// Loads the service provider configuration file
			serviceProviderConfigurationManager = new PersistenceManager(getPath(properties.getFiles().getProvider()),
					Type.service_provider_disabled_v1, Type.service_provider_lazy_initialized_v1,
					Type.service_provider_task_executor_v1);
			loadServiceProviderConfiguration();
		}

		/**
		 * Loads the main configuration file.
		 * 
		 * @param mainFile The main file.
		 * @since 1.8
		 */
		private void loadMainConfiguration(Path mainFile) {
			Set<String> serviceProviderCollectionKeyProperties = new HashSet<>();
			List<ConfigurationServiceProvider.Property> serviceProviderProperties = new ArrayList<>();

			load(Optional.of(mainFile),
					getApplicationFileHeader(mainFile, "workspace main configuration file", null, getParseSyntax()),
					(line, entry) -> {
						String[] split = entry.split(":", 2);

						if (split.length == 2) {
							Optional<MainConfigurationField> field = MainConfigurationField.getField(split[0]);

							if (field.isPresent()) {
								switch (field.get()) {
								case configurationVersion:
									if (version == null) {
										Optional<Version> value = Version.getVersion(split[1]);
										if (value.isPresent()) {
											version = value.get();

											logger.debug("Read configuration version " + version.getLabel() + ": '"
													+ entry + "' (line " + line + ").");

											return true;
										} else
											logger.warn("Not supported configuration version '" + split[1].trim()
													+ "' (line " + line + ").");
									} else
										logger.warn("Configuration version already set to " + version.getLabel()
												+ " - ignored main application configuration line " + line + ": "
												+ entry + ".");

									break;
								case instanceName:
									if (instance == null) {
										instance = split[1].trim();

										logger.debug("Read instance name: '" + instance + "' (line " + line + ").");

										return true;
									} else
										logger.warn("Instance name already set to '" + instance
												+ "' - ignored main application configuration line " + line + ": "
												+ entry + ".");

									break;
								case systemCommandDocker:
									if (systemCommandDocker == null) {
										systemCommandDocker = split[1].trim();

										logger.debug(
												"Read system command docker: '" + entry + "' (line " + line + ").");

										return true;
									} else
										logger.warn("System command docker already set to '" + systemCommandDocker
												+ "' - ignored main application configuration line " + line + ": "
												+ entry + ".");

									break;
								case systemCommandConvert:
									if (systemCommandConvert == null) {
										systemCommandConvert = split[1].trim();

										logger.debug(
												"Read system command convert: '" + entry + "' (line " + line + ").");

										return true;
									} else
										logger.warn("System command convert already set to '" + systemCommandConvert
												+ "' - ignored main application configuration line " + line + ": "
												+ entry + ".");

									break;
								case systemCommandIdentify:
									if (systemCommandIdentify == null) {
										systemCommandIdentify = split[1].trim();

										logger.debug(
												"Read system command identify: '" + entry + "' (line " + line + ").");

										return true;
									} else
										logger.warn("System command identify already set to '" + systemCommandIdentify
												+ "' - ignored main application configuration line " + line + ": "
												+ entry + ".");

									break;
								case serviceProvider:
									String suffix = field.get().getSuffix(split[0]);
									if (suffix != null) {
										String[] collectionKey = suffix.split("\\.", 2);

										if (collectionKey.length == 2 && !collectionKey[0].isBlank()
												&& !collectionKey[1].isBlank()) {
											ConfigurationServiceProvider.Property property = new ConfigurationServiceProvider.Property(
													collectionKey[0].trim(), collectionKey[1].trim(), split[1]);
											if (serviceProviderCollectionKeyProperties
													.add(property.getCollection() + "." + property.getKey())) {
												serviceProviderProperties.add(property);

												logger.debug("Read service provider collection '"
														+ property.getCollection() + "' / key '" + property.getKey()
														+ "' (line " + line + ").");

												return true;
											} else
												logger.warn("Duplicated service provider collection '"
														+ property.getCollection() + "' / key '" + property.getKey()
														+ " - ignored main application configuration line " + line
														+ ": " + entry + ".");

										} else
											logger.warn(
													"Can not parse service provider collection/key part - ignored main application configuration line "
															+ line + ": " + entry + ".");

									} else
										logger.warn(
												"Empty service provider collection/key part - ignored main application configuration line "
														+ line + ": " + entry + ".");

									break;
								}
							} else
								logger.warn("Unknown main application configuration field '" + split[0].trim()
										+ "' (line " + line + ").");
						} else
							logger.warn("Ignored main application configuration line " + line + ": " + entry + ".");

						return false;
					}, (file, lines, added, total) -> logger.info("Loaded " + added + "/" + total
							+ " entries of configuration main file " + file + " (lines " + lines + ")."));

			if (version == null) {
				version = Version.defaultVertsion;

				logger.warn("Using default configuration version " + version.getLabel() + ".");
			} else
				logger.info("Configuration version " + version.getLabel() + ".");

			configurationServiceProvider = new ConfigurationServiceProvider(serviceProviderProperties,
					getSystemCommand(SystemCommand.Type.docker, systemCommandDocker, getPropertySystemCommandDocker()),
					getSystemCommand(SystemCommand.Type.convert, systemCommandConvert,
							getPropertySystemCommandConvert()),
					getSystemCommand(SystemCommand.Type.identify, systemCommandIdentify,
							getPropertySystemCommandIdentify()));
		}

		/**
		 * Returns the system command docker depending on the running operation system
		 * from the properties.
		 * 
		 * @return The system command docker depending on the running operation system
		 *         from the properties.
		 * @since 1.8
		 */
		private String getPropertySystemCommandDocker() {
			switch (ConfigurationService.getOperatingSystem()) {
			case mac:
				return systemProperties.getMac().getCommand().getDocker();
			case windows:
				return systemProperties.getWindows().getCommand().getDocker();
			case unix:
			default:
				return systemProperties.getUnix().getCommand().getDocker();
			}
		}

		/**
		 * Returns the system command convert depending on the running operation system
		 * from the properties.
		 * 
		 * @return The system command convert depending on the running operation system
		 *         from the properties.
		 * @since 1.8
		 */
		private String getPropertySystemCommandConvert() {
			switch (ConfigurationService.getOperatingSystem()) {
			case mac:
				return systemProperties.getMac().getCommand().getConvert();
			case windows:
				return systemProperties.getWindows().getCommand().getConvert();
			case unix:
			default:
				return systemProperties.getUnix().getCommand().getConvert();
			}
		}

		/**
		 * Returns the system command identify depending on the running operation system
		 * from the properties.
		 * 
		 * @return The system command identify depending on the running operation system
		 *         from the properties.
		 * @since 1.8
		 */
		private String getPropertySystemCommandIdentify() {
			switch (ConfigurationService.getOperatingSystem()) {
			case mac:
				return systemProperties.getMac().getCommand().getIdentify();
			case windows:
				return systemProperties.getWindows().getCommand().getIdentify();
			case unix:
			default:
				return systemProperties.getUnix().getCommand().getIdentify();
			}
		}

		/**
		 * Returns the system command.
		 * 
		 * @param type           The type.
		 * @param command        The command.
		 * @param defaultCommand The default command to be used if command is not
		 *                       defined.
		 * @return The system command.
		 * @since 1.8
		 */
		private SystemCommand getSystemCommand(SystemCommand.Type type, String command, String defaultCommand) {
			Path path = Paths.get(command == null ? defaultCommand : command);

			SystemCommand systemCommand = new SystemCommand(type, path,
					!Files.isDirectory(path) && Files.isExecutable(path));

			if (systemCommand.isAvailable())
				logger.info("The system command " + type.name() + " '" + path + "' is available.");
			else
				logger.warn("The system command " + type.name() + " '" + path + "' is not available.");

			return systemCommand;
		}

		/**
		 * Returns the parse syntax.
		 * 
		 * @return The parse syntax.
		 * @since 1.8
		 */
		public String[] getParseSyntax() {
			return new String[] {
					MainConfigurationField.configurationVersion.getLabel() + ": " + Version.defaultVertsion.getLabel(),
					MainConfigurationField.instanceName.getLabel() + ": <name>",
					MainConfigurationField.systemCommandDocker.getLabel() + ": " + getPropertySystemCommandDocker(),
					MainConfigurationField.systemCommandConvert.getLabel() + ": " + getPropertySystemCommandConvert(),
					MainConfigurationField.systemCommandIdentify.getLabel() + ": " + getPropertySystemCommandIdentify(),
					MainConfigurationField.serviceProvider.getLabel() + "{collection.key}: [value]" };
		}

		/**
		 * Loads the service provider configuration.
		 * 
		 * @since 1.8
		 */
		private void loadServiceProviderConfiguration() {
			try {
				for (Entity entity : serviceProviderConfigurationManager.getEntities()) {
					Identifier identifier = (Identifier) entity;
					if (identifier.getId() != null && !identifier.getId().isBlank()) {
						identifier.setId(identifier.getId().trim());

						if (identifier instanceof DisabledServiceProvider)
							disabledServiceProviders.put(identifier.getId(), (DisabledServiceProvider) identifier);
						else if (identifier instanceof LazyInitializedServiceProvider)
							lazyInitializedServiceProviders.put(identifier.getId(),
									(LazyInitializedServiceProvider) identifier);
						else if (identifier instanceof TaskExecutorServiceProvider)
							taskExecutorServiceProviders.put(identifier.getId(),
									(TaskExecutorServiceProvider) identifier);
						else
							logger.warn("The class type '" + identifier.getClass().getName()
									+ "' is not implemented for service provider configuration.");
					}
				}
			} catch (Exception e) {
				logger.warn(e.getMessage());
			}
		}

		/**
		 * Persist the service provider configuration.
		 * 
		 * @since 1.8
		 */
		private void persistServiceProviderConfiguration() {
			try {
				List<Entity> entities = new ArrayList<>(disabledServiceProviders.values());
				entities.addAll(lazyInitializedServiceProviders.values());
				entities.addAll(taskExecutorServiceProviders.values());

				serviceProviderConfigurationManager.persist(entities);

				logger.info("Persisted service provider configuration file.");
			} catch (Exception e) {
				logger.warn(e.getMessage());
			}
		}

		/**
		 * Returns the disabled service providers, this means, the service providers
		 * that are inactive when the application is launched.
		 * 
		 * @return The disabled service providers.
		 * @since 1.8
		 */
		public Set<String> getDisabledServiceProviders() {
			return new HashSet<>(disabledServiceProviders.keySet());
		}

		/**
		 * Enables the service provider, this means, the service provider will be active
		 * when the application is launched.
		 * 
		 * @param id The service provider id.
		 * @since 1.8
		 */
		public void enableServiceProvider(String id) {
			if (id != null && !id.isBlank() && disabledServiceProviders.remove(id.trim()) != null)
				persistServiceProviderConfiguration();
		}

		/**
		 * Disables the service provider, this means, the service provider will be
		 * inactive when the application is launched.
		 * 
		 * @param id   The service provider id.
		 * @param user The user.
		 * @since 1.8
		 */
		public void disableServiceProvider(String id, String user) {
			if (id != null && !id.isBlank()) {
				id = id.trim();

				if (!disabledServiceProviders.containsKey(id)) {
					disabledServiceProviders.put(id, new DisabledServiceProvider(user, id));

					persistServiceProviderConfiguration();
				}
			}
		}

		/**
		 * Returns the lazy initialized service providers, this means, the service
		 * providers whose initialization are deferred and will be performed in a new
		 * thread. The initialization of the remainder service providers are performed
		 * as soon as their are loaded.
		 * 
		 * @return The lazy initialized service providers.
		 * @since 1.8
		 */
		public Set<String> getLazyInitializedServiceProviders() {
			return new HashSet<>(lazyInitializedServiceProviders.keySet());
		}

		/**
		 * Eager initializes the service provider, this means, the service provider
		 * initialization is performed as soon as the provider is loaded.
		 * 
		 * @param id The service provider id.
		 * @since 1.8
		 */
		public void eagerInitializeServiceProvider(String id) {
			if (id != null && !id.isBlank() && lazyInitializedServiceProviders.remove(id.trim()) != null)
				persistServiceProviderConfiguration();
		}

		/**
		 * Lazy initializes the service provider, this means, the service provider
		 * initialization is deferred and will be performed in a new thread.
		 * 
		 * @param id   The service provider id.
		 * @param user The user.
		 * @since 1.8
		 */
		public void lazyInitializeServiceProvider(String id, String user) {
			if (id != null && !id.isBlank()) {
				id = id.trim();

				if (!lazyInitializedServiceProviders.containsKey(id)) {
					lazyInitializedServiceProviders.put(id, new LazyInitializedServiceProvider(user, id));

					persistServiceProviderConfiguration();
				}
			}
		}

		/**
		 * Returns the task executor pool size. The key is the thread name and the value
		 * its core pool size.
		 *
		 * @return The task executor pool size.
		 * @since 1.8
		 */
		public Hashtable<String, Integer> getTaskExecutorPoolSize() {
			Hashtable<String, Integer> poolSize = new Hashtable<>();
			for (TaskExecutorServiceProvider executor : taskExecutorServiceProviders.values())
				poolSize.put(executor.getThreadName(),
						poolSize.contains(executor.getThreadName())
								? Math.max(poolSize.get(executor.getThreadName()), executor.getCorePoolSize())
								: executor.getCorePoolSize());

			return poolSize;
		}

		/**
		 * Returns the task executor service providers, this means, the scheduler
		 * service executes the service providers in a separate pool of threads. The key
		 * is the service provider id.
		 * 
		 * @return The task executor service providers.
		 * @since 1.8
		 */
		public Hashtable<String, TaskExecutorServiceProvider> getTaskExecutorServiceProvider() {
			return new Hashtable<>(taskExecutorServiceProviders);
		}

		/**
		 * Removes the task executor for given service provider.
		 * 
		 * @param id The service provider id.
		 * @since 1.8
		 */
		public void removeTaskExecutorServiceProvider(String id) {
			if (id != null && !id.isBlank() && taskExecutorServiceProviders.remove(id.trim()) != null) {
				isRestart = true;

				persistServiceProviderConfiguration();
			}
		}

		/**
		 * Set the task executor service providers, this means, the scheduler service
		 * executes the service provider in a separate pool of threads.
		 * 
		 * @param id           The service provider id.
		 * @param threadName   The thread name. It is be trimmed. If null, the task
		 *                     executor is removed from service provider.
		 * @param corePoolSize The core pool size. The value must be greater than 0.
		 * @param user         The user.
		 * @since 1.8
		 */
		public void setTaskExecutorServiceProvider(String id, String threadName, int corePoolSize, String user) {
			if (threadName == null || threadName.isBlank())
				removeTaskExecutorServiceProvider(id);
			else if (id != null && !id.isBlank()) {
				isRestart = true;

				id = id.trim();

				taskExecutorServiceProviders.put(id,
						new TaskExecutorServiceProvider(user, id, threadName, corePoolSize));

				persistServiceProviderConfiguration();
			}
		}

		/**
		 * Returns true if the application needs to be restarted due to configuration
		 * updates.
		 *
		 * @return True if the application needs to be restarted due to configuration
		 *         updates.
		 * @since 1.8
		 */
		public boolean isRestart() {
			return isRestart;
		}

		/**
		 * Set the isRestart.
		 *
		 * @param isRestart The isRestart to set.
		 * @since 1.8
		 */
		public void setRestart(boolean isRestart) {
			this.isRestart = isRestart;
		}

		/**
		 * Returns the version.
		 *
		 * @return The version.
		 * @since 1.8
		 */
		public Version getVersion() {
			return version;
		}

		/**
		 * Returns true if the instance name is set.
		 *
		 * @return The instance name.
		 * @since 1.8
		 */
		public boolean isInstanceSet() {
			return instance != null;
		}

		/**
		 * Returns the instance name. Null if not defined.
		 *
		 * @return The instance name.
		 * @since 1.8
		 */
		public String getInstance() {
			return instance;
		}

		/**
		 * Returns true if the system command is registered and available.
		 *
		 * @param type The system command type.
		 * @return True if the system command is registered and available.
		 * @since 1.8
		 */
		public boolean isSystemCommandAvailable(SystemCommand.Type type) {
			return configurationServiceProvider.isSystemCommandAvailable(type);
		}

		/**
		 * Returns the system command.
		 *
		 * @param type The system command type.
		 * @return The system command. Null if not registered.
		 * @since 1.8
		 */
		public SystemCommand getSystemCommand(SystemCommand.Type type) {
			return configurationServiceProvider.getSystemCommand(type);
		}

		/**
		 * Returns the configuration for service providers.
		 *
		 * @return The configuration for service providers.
		 * @since 1.8
		 */
		public ConfigurationServiceProvider getConfigurationServiceProvider() {
			return configurationServiceProvider;
		}

		/**
		 * Returns the user configuration file.
		 *
		 * @return The user configuration file.
		 * @since 1.8
		 */
		public Path getUserFile() {
			return userFile;
		}

		/**
		 * Returns the group configuration file.
		 *
		 * @return The group configuration file.
		 * @since 1.8
		 */
		public Path getGroupFile() {
			return groupFile;
		}

		/**
		 * Returns the password configuration file.
		 *
		 * @return The password configuration file.
		 * @since 1.8
		 */
		public Path getPasswordFile() {
			return passwordFile;
		}

	}

}
