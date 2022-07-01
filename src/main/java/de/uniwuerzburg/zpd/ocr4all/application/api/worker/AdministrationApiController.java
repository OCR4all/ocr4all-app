/**
 * File:     AdministrationApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     28.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ApplicationConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.TemporaryConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.WorkspaceConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.ConfigurationServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.SystemCommand;

/**
 * Defines administration controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Controller
@RequestMapping(path = AdministrationApiController.contextPath, produces = CoreApiController.applicationJson)
public class AdministrationApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = apiContextPathVersion_1_0 + "/administration";

	/**
	 * Creates an administration controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @since 1.8
	 */
	@Autowired
	public AdministrationApiController(ConfigurationService configurationService, SecurityService securityService) {
		super(AdministrationApiController.class, configurationService, securityService);
	}

	/**
	 * Returns the administration overview in the response body.
	 * 
	 * @return The administration overview in the response body.
	 * @since 1.8
	 */
	@GetMapping(overviewRequestMapping)
	public ResponseEntity<AdministrationResponse> overview() {
		try {
			return ResponseEntity.ok().body(new AdministrationResponse());
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines administration responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public class AdministrationResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The application.
		 */
		private ApplicationResponse application;

		/**
		 * The system.
		 */
		private SystemResponse system;

		/**
		 * The workspace.
		 */
		private WorkspaceResponse workspace;

		/**
		 * The exchange.
		 */
		private ExchangeResponse exchange;

		/**
		 * The opt.
		 */
		private OptResponse opt;

		/**
		 * The temporary.
		 */
		private TemporaryResponse temporary;

		/**
		 * The service provider properties.
		 */
		@JsonProperty("service-provider-properties")
		private List<ServiceProviderPropertyResponse> serviceProviderProperties;

		/**
		 * Default constructor for an administration response for the api.
		 * 
		 * @since 1.8
		 */
		public AdministrationResponse() {
			super();

			application = isSecured() ? new ApplicationSecuredResponse() : new ApplicationResponse();
			system = new SystemResponse();
			workspace = isSecured() ? new WorkspaceSecuredResponse() : new WorkspaceResponse();
			exchange = new ExchangeResponse();
			opt = new OptResponse();
			temporary = new TemporaryResponse();

			/*
			 * The service provider properties
			 */
			serviceProviderProperties = new ArrayList<>();

			final ConfigurationServiceProvider configurationServiceProvider = configurationService.getWorkspace()
					.getConfiguration().getConfigurationServiceProvider();

			List<String> propertyCollections = new ArrayList<>(configurationServiceProvider.getPropertyCollections());
			Collections.sort(propertyCollections, (o1, o2) -> o1.compareToIgnoreCase(o2));

			for (String propertyCollection : propertyCollections) {
				List<String> propertyKeys = new ArrayList<>(
						configurationServiceProvider.getPropertyKeys(propertyCollection));
				Collections.sort(propertyKeys, (o1, o2) -> o1.compareToIgnoreCase(o2));

				for (String propertyKey : propertyKeys)
					serviceProviderProperties.add(new ServiceProviderPropertyResponse(
							configurationServiceProvider.getProperty(propertyCollection, propertyKey)));
			}
		}

		/**
		 * Returns the application.
		 *
		 * @return The application.
		 * @since 1.8
		 */
		public ApplicationResponse getApplication() {
			return application;
		}

		/**
		 * Set the application.
		 *
		 * @param application The application to set.
		 * @since 1.8
		 */
		public void setApplication(ApplicationResponse application) {
			this.application = application;
		}

		/**
		 * Returns the system.
		 *
		 * @return The system.
		 * @since 1.8
		 */
		public SystemResponse getSystem() {
			return system;
		}

		/**
		 * Set the system.
		 *
		 * @param system The system to set.
		 * @since 1.8
		 */
		public void setSystem(SystemResponse system) {
			this.system = system;
		}

		/**
		 * Returns the workspace.
		 *
		 * @return The workspace.
		 * @since 1.8
		 */
		public WorkspaceResponse getWorkspace() {
			return workspace;
		}

		/**
		 * Set the workspace.
		 *
		 * @param workspace The workspace to set.
		 * @since 1.8
		 */
		public void setWorkspace(WorkspaceResponse workspace) {
			this.workspace = workspace;
		}

		/**
		 * Returns the exchange.
		 *
		 * @return The exchange.
		 * @since 1.8
		 */
		public ExchangeResponse getExchange() {
			return exchange;
		}

		/**
		 * Set the exchange.
		 *
		 * @param exchange The exchange to set.
		 * @since 1.8
		 */
		public void setExchange(ExchangeResponse exchange) {
			this.exchange = exchange;
		}

		/**
		 * Returns the opt.
		 *
		 * @return The opt.
		 * @since 1.8
		 */
		public OptResponse getOpt() {
			return opt;
		}

		/**
		 * Set the opt.
		 *
		 * @param opt The opt to set.
		 * @since 1.8
		 */
		public void setOpt(OptResponse opt) {
			this.opt = opt;
		}

		/**
		 * Returns the temporary.
		 *
		 * @return The temporary.
		 * @since 1.8
		 */
		public TemporaryResponse getTemporary() {
			return temporary;
		}

		/**
		 * Set the temporary.
		 *
		 * @param temporary The temporary to set.
		 * @since 1.8
		 */
		public void setTemporary(TemporaryResponse temporary) {
			this.temporary = temporary;
		}

		/**
		 * Returns the service provider properties.
		 *
		 * @return The service provider properties.
		 * @since 1.8
		 */
		public List<ServiceProviderPropertyResponse> getServiceProviderProperties() {
			return serviceProviderProperties;
		}

		/**
		 * Set the service provider properties.
		 *
		 * @param serviceProviderProperties The service provider properties to set.
		 * @since 1.8
		 */
		public void setServiceProviderProperties(List<ServiceProviderPropertyResponse> serviceProviderProperties) {
			this.serviceProviderProperties = serviceProviderProperties;
		}

		/**
		 * Defines application responses for the api.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public class ApplicationResponse implements Serializable {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The name.
			 */
			private String name;

			/**
			 * The start time.
			 */
			private Date start;

			/**
			 * The active profiles.
			 */
			@JsonProperty("active-profiles")
			public List<String> activeProfiles;

			/**
			 * The charset.
			 */
			private Charset charset;

			/**
			 * The localized pattern string describing the date format.
			 */
			@JsonProperty("date-format-pattern")
			private String dateFormatPattern;

			/**
			 * The locale.
			 */
			private Locale locale;

			/**
			 * The view languages.
			 */
			@JsonProperty("view-languages")
			private List<String> viewLanguages;

			/**
			 * The monitor interval. This is the amount of time in milliseconds to wait
			 * between checks.
			 */
			@JsonProperty("monitor-interval")
			private long monitorInterval;

			/**
			 * The task executor.
			 */
			@JsonProperty("task-executor")
			private TaskExecutor taskExecutor;

			/**
			 * Default constructor for an application response for the api.
			 * 
			 * @since 1.8
			 */
			public ApplicationResponse() {
				super();

				activeProfiles = Arrays.asList(configurationService.getActiveProfiles());

				final ApplicationConfiguration configuration = configurationService.getApplication();

				name = configuration.getName();
				start = configuration.getStart();
				charset = configuration.getCharset();
				dateFormatPattern = configuration.getDateFormat().toLocalizedPattern();
				locale = configuration.getLocale();
				viewLanguages = configuration.getViewLanguages();
				monitorInterval = configuration.getMonitorInterval();
				taskExecutor = new TaskExecutor(configuration.getTaskExecutorProperties().getCorePoolSize(),
						configuration.getTaskExecutorProperties().getMaxPoolSize());
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
			 * Set the name.
			 *
			 * @param name The name to set.
			 * @since 1.8
			 */
			public void setName(String name) {
				this.name = name;
			}

			/**
			 * Returns the start time.
			 *
			 * @return The start time.
			 * @since 1.8
			 */
			public Date getStart() {
				return start;
			}

			/**
			 * Set the start time.
			 *
			 * @param start The start time to set.
			 * @since 1.8
			 */
			public void setStart(Date start) {
				this.start = start;
			}

			/**
			 * Returns the active profiles.
			 *
			 * @return The active profiles.
			 * @since 1.8
			 */
			public List<String> getActiveProfiles() {
				return activeProfiles;
			}

			/**
			 * Set the active profiles.
			 *
			 * @param activeProfiles The active profiles to set.
			 * @since 1.8
			 */
			public void setActiveProfiles(List<String> activeProfiles) {
				this.activeProfiles = activeProfiles;
			}

			/**
			 * Returns the charset.
			 *
			 * @return The charset.
			 * @since 1.8
			 */
			public Charset getCharset() {
				return charset;
			}

			/**
			 * Set the charset.
			 *
			 * @param charset The charset to set.
			 * @since 1.8
			 */
			public void setCharset(Charset charset) {
				this.charset = charset;
			}

			/**
			 * Returns the localized pattern string describing the date format.
			 *
			 * @return The localized pattern string describing the date format.
			 * @since 1.8
			 */
			public String getDateFormatPattern() {
				return dateFormatPattern;
			}

			/**
			 * Set the localized pattern string describing the date format.
			 *
			 * @param dateFormatPattern The date format pattern to set.
			 * @since 1.8
			 */
			public void setDateFormatPattern(String dateFormatPattern) {
				this.dateFormatPattern = dateFormatPattern;
			}

			/**
			 * Returns the locale.
			 *
			 * @return The locale.
			 * @since 1.8
			 */
			public Locale getLocale() {
				return locale;
			}

			/**
			 * Set the locale.
			 *
			 * @param locale The locale to set.
			 * @since 1.8
			 */
			public void setLocale(Locale locale) {
				this.locale = locale;
			}

			/**
			 * Returns the view languages.
			 *
			 * @return The view languages.
			 * @since 1.8
			 */
			public List<String> getViewLanguages() {
				return viewLanguages;
			}

			/**
			 * Set the view languages.
			 *
			 * @param viewLanguages The view languages to set.
			 * @since 1.8
			 */
			public void setViewLanguages(List<String> viewLanguages) {
				this.viewLanguages = viewLanguages;
			}

			/**
			 * Returns the monitor interval. This is the amount of time in milliseconds to
			 * wait between checks.
			 *
			 * @return The monitor interval.
			 * @since 1.8
			 */
			public long getMonitorInterval() {
				return monitorInterval;
			}

			/**
			 * Set the monitor interval. This is the amount of time in milliseconds to wait
			 * between checks.
			 *
			 * @param monitorInterval The monitor interval to set.
			 * @since 1.8
			 */
			public void setMonitorInterval(long monitorInterval) {
				this.monitorInterval = monitorInterval;
			}

			/**
			 * Returns the task executor.
			 *
			 * @return The task executor.
			 * @since 1.8
			 */
			public TaskExecutor getTaskExecutor() {
				return taskExecutor;
			}

			/**
			 * Set the task executor.
			 *
			 * @param taskExecutor The task executor to set.
			 * @since 1.8
			 */
			public void setTaskExecutor(TaskExecutor taskExecutor) {
				this.taskExecutor = taskExecutor;
			}

			/**
			 * Defines task executor.
			 *
			 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
			 * @version 1.0
			 * @since 1.8
			 */
			public class TaskExecutor {
				/**
				 * The task executor core pool size.
				 */
				@JsonProperty("core-pool-size")
				private int corePoolSize;

				/**
				 * The task executor max pool size.
				 */
				@JsonProperty("max-pool-size")
				private int maxPoolSize;

				/**
				 * Creates a task executor.
				 * 
				 * @param corePoolSize The task executor core pool size.
				 * @param maxPoolSize  The task executor max pool size.
				 * @since 1.8
				 */
				public TaskExecutor(int corePoolSize, int maxPoolSize) {
					super();

					this.corePoolSize = corePoolSize;
					this.maxPoolSize = maxPoolSize;
				}

				/**
				 * Returns the task executor core pool size.
				 *
				 * @return The task executor core pool size.
				 * @since 1.8
				 */
				public int getCorePoolSize() {
					return corePoolSize;
				}

				/**
				 * Set the task executor core pool size.
				 *
				 * @param corePoolSize The core pool size to set.
				 * @since 1.8
				 */
				public void setCorePoolSize(int corePoolSize) {
					this.corePoolSize = corePoolSize;
				}

				/**
				 * Returns the task executor max pool size.
				 *
				 * @return The task executor max pool size.
				 * @since 1.8
				 */
				public int getMaxPoolSize() {
					return maxPoolSize;
				}

				/**
				 * Set the task executor max pool size.
				 *
				 * @param maxPoolSize The max pool size to set.
				 * @since 1.8
				 */
				public void setMaxPoolSize(int maxPoolSize) {
					this.maxPoolSize = maxPoolSize;
				}

			}
		}

		/**
		 * Defines application secured responses for the api.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public class ApplicationSecuredResponse extends ApplicationResponse {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The administrator group. Null if not secured.
			 */
			@JsonProperty("administrator-group")
			private String administratorGroup;

			/**
			 * The coordinator group. Null if not secured.
			 */
			@JsonProperty("coordinator-group")
			private String coordinatorGroup;

			/**
			 * Default constructor for an application secured response for the api.
			 * 
			 * @since 1.8
			 */
			public ApplicationSecuredResponse() {
				super();

				final ApplicationConfiguration configuration = configurationService.getApplication();

				administratorGroup = configuration.getAdministratorGroup();
				coordinatorGroup = configuration.getCoordinatorGroup();
			}

			/**
			 * Returns the administrator group.
			 *
			 * @return The administrator group. Null if not secured.
			 * @since 1.8
			 */
			public String getAdministratorGroup() {
				return administratorGroup;
			}

			/**
			 * Set the administrator group. Null if not secured.
			 *
			 * @param administratorGroup The administrator group to set.
			 * @since 1.8
			 */
			public void setAdministratorGroup(String administratorGroup) {
				this.administratorGroup = administratorGroup;
			}

			/**
			 * Returns the coordinator group.
			 *
			 * @return The coordinator group. Null if not secured.
			 * @since 1.8
			 */
			public String getCoordinatorGroup() {
				return coordinatorGroup;
			}

			/**
			 * Set the coordinator group. Null if not secured.
			 *
			 * @param coordinatorGroup The coordinator group to set.
			 * @since 1.8
			 */
			public void setCoordinatorGroup(String coordinatorGroup) {
				this.coordinatorGroup = coordinatorGroup;
			}
		}

		/**
		 * Defines system responses for the api.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public class SystemResponse implements Serializable {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The operating system.
			 */
			@JsonProperty("operating-system")
			private ConfigurationService.OperatingSystem operatingSystem;

			/**
			 * The effective system user ID. -1 if not defined.
			 */
			@JsonProperty("system-user-id")
			private int uid;

			/**
			 * The effective system group ID. -1 if not defined.
			 */
			@JsonProperty("system-group-id")
			private int gid;

			/**
			 * The commands.
			 */
			public List<CommandResponse> commands;

			/**
			 * Default constructor for a system response for the api.
			 * 
			 * @since 1.8
			 */
			public SystemResponse() {
				super();

				operatingSystem = ConfigurationService.getOperatingSystem();
				uid = ConfigurationService.getUID();
				gid = ConfigurationService.getGID();

				final ConfigurationServiceProvider configuration = configurationService.getWorkspace()
						.getConfiguration().getConfigurationServiceProvider();

				commands = new ArrayList<>();
				for (SystemCommand.Type type : SystemCommand.Type.values()) {
					SystemCommand command = configuration.getSystemCommand(type);

					if (command != null)
						commands.add(new CommandResponse(command));
				}
			}

			/**
			 * Returns the operating system.
			 *
			 * @return The operating system.
			 * @since 1.8
			 */
			public ConfigurationService.OperatingSystem getOperatingSystem() {
				return operatingSystem;
			}

			/**
			 * Set the operating system.
			 *
			 * @param operatingSystem The operating system to set.
			 * @since 1.8
			 */
			public void setOperatingSystem(ConfigurationService.OperatingSystem operatingSystem) {
				this.operatingSystem = operatingSystem;
			}

			/**
			 * Returns the effective system user ID.
			 *
			 * @return The effective system user ID. -1 if not defined.
			 * @since 1.8
			 */
			public int getUid() {
				return uid;
			}

			/**
			 * Set the effective system user ID. -1 if not defined.
			 *
			 * @param uid The user ID to set.
			 * @since 1.8
			 */
			public void setUid(int uid) {
				this.uid = uid;
			}

			/**
			 * Returns the effective system group ID.
			 *
			 * @return The effective system group ID. -1 if not defined.
			 * @since 1.8
			 */
			public int getGid() {
				return gid;
			}

			/**
			 * Set the effective system group ID. -1 if not defined.
			 *
			 * @param gid The group ID to set.
			 * @since 1.8
			 */
			public void setGid(int gid) {
				this.gid = gid;
			}

			/**
			 * Defines command responses for the api.
			 *
			 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
			 * @version 1.0
			 * @since 1.8
			 */
			public class CommandResponse implements Serializable {
				/**
				 * The serial version UID.
				 */
				private static final long serialVersionUID = 1L;

				/**
				 * The type.
				 */
				private SystemCommand.Type type;

				/**
				 * The command.
				 */
				private String command;

				/**
				 * True if available.
				 */
				private boolean isAvailable;

				/**
				 * Creates a command response for the api.
				 * 
				 * @param command The command.
				 * @since 1.8
				 */
				public CommandResponse(SystemCommand command) {
					super();

					type = command.getType();
					this.command = command.getCommand().toString();
					isAvailable = command.isAvailable();
				}

				/**
				 * Returns the type.
				 *
				 * @return The type.
				 * @since 1.8
				 */
				public SystemCommand.Type getType() {
					return type;
				}

				/**
				 * Set the type.
				 *
				 * @param type The type to set.
				 * @since 1.8
				 */
				public void setType(SystemCommand.Type type) {
					this.type = type;
				}

				/**
				 * Returns the command.
				 *
				 * @return The command.
				 * @since 1.8
				 */
				public String getCommand() {
					return command;
				}

				/**
				 * Set the command.
				 *
				 * @param command The command to set.
				 * @since 1.8
				 */
				public void setCommand(String command) {
					this.command = command;
				}

				/**
				 * Returns true if available.
				 *
				 * @return True if available.
				 * @since 1.8
				 */
				public boolean isAvailable() {
					return isAvailable;
				}

				/**
				 * Set to true if available.
				 *
				 * @param isAvailable The available flag to set.
				 * @since 1.8
				 */
				public void setAvailable(boolean isAvailable) {
					this.isAvailable = isAvailable;
				}
			}
		}

		/**
		 * Defines workspace responses for the api.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public class WorkspaceResponse implements Serializable {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The folder.
			 */
			private String folder;

			/**
			 * the version.
			 */
			private String version;

			/**
			 * the default version.
			 */
			@JsonProperty("default-version")
			private String defaultVersion;

			/**
			 * The projects folder.
			 */
			@JsonProperty("projects-folder")
			private String projectsFolder;

			/**
			 * Default constructor for a workspace response for the api.
			 * 
			 * @since 1.8
			 */
			public WorkspaceResponse() {
				super();

				final WorkspaceConfiguration configuration = configurationService.getWorkspace();

				folder = configuration.getFolder().toString();

				version = configuration.getConfiguration().getVersion().getLabel();
				defaultVersion = WorkspaceConfiguration.Version.defaultVertsion.getLabel();

				projectsFolder = configuration.getProjects().getFolder().toString();
			}

			/**
			 * Returns the folder.
			 *
			 * @return The folder.
			 * @since 1.8
			 */
			public String getFolder() {
				return folder;
			}

			/**
			 * Set the folder.
			 *
			 * @param folder The folder to set.
			 * @since 1.8
			 */
			public void setFolder(String folder) {
				this.folder = folder;
			}

			/**
			 * Returns the version.
			 *
			 * @return The version.
			 * @since 1.8
			 */
			public String getVersion() {
				return version;
			}

			/**
			 * Set the version.
			 *
			 * @param version The version to set.
			 * @since 1.8
			 */
			public void setVersion(String version) {
				this.version = version;
			}

			/**
			 * Returns the default version.
			 *
			 * @return The default version.
			 * @since 1.8
			 */
			public String getDefaultVersion() {
				return defaultVersion;
			}

			/**
			 * Set the default version.
			 *
			 * @param defaultVersion The default version to set.
			 * @since 1.8
			 */
			public void setDefaultVersion(String defaultVersion) {
				this.defaultVersion = defaultVersion;
			}

			/**
			 * Returns the projects folder.
			 *
			 * @return The projects folder.
			 * @since 1.8
			 */
			public String getProjectsFolder() {
				return projectsFolder;
			}

			/**
			 * Set the projects folder.
			 *
			 * @param projectsFolder The projects folder to set.
			 * @since 1.8
			 */
			public void setProjectsFolder(String projectsFolder) {
				this.projectsFolder = projectsFolder;
			}
		}

		/**
		 * Defines workspace secured responses for the api.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public class WorkspaceSecuredResponse extends WorkspaceResponse {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The user file.
			 */
			@JsonProperty("user-file")
			private String userFile;

			/**
			 * The group file.
			 */
			@JsonProperty("group-file")
			private String groupFile;

			/**
			 * The password file.
			 */
			@JsonProperty("password-file")
			private String passwordFile;

			/**
			 * Default constructor for a workspace secured response for the api.
			 * 
			 * @since 1.8
			 */
			public WorkspaceSecuredResponse() {
				super();

				final WorkspaceConfiguration configuration = configurationService.getWorkspace();

				userFile = configuration.getConfiguration().getUserFile().toString();
				groupFile = configuration.getConfiguration().getGroupFile().toString();
				passwordFile = configuration.getConfiguration().getPasswordFile().toString();
			}

			/**
			 * Returns the user file.
			 *
			 * @return The user file.
			 * @since 1.8
			 */
			public String getUserFile() {
				return userFile;
			}

			/**
			 * Set the user file.
			 *
			 * @param userFile The user file to set.
			 * @since 1.8
			 */
			public void setUserFile(String userFile) {
				this.userFile = userFile;
			}

			/**
			 * Returns the group file.
			 *
			 * @return The group file.
			 * @since 1.8
			 */
			public String getGroupFile() {
				return groupFile;
			}

			/**
			 * Set the group file.
			 *
			 * @param groupFile The group file to set.
			 * @since 1.8
			 */
			public void setGroupFile(String groupFile) {
				this.groupFile = groupFile;
			}

			/**
			 * Returns the password file.
			 *
			 * @return The password file.
			 * @since 1.8
			 */
			public String getPasswordFile() {
				return passwordFile;
			}

			/**
			 * Set the password file.
			 *
			 * @param passwordFile The password file to set.
			 * @since 1.8
			 */
			public void setPasswordFile(String passwordFile) {
				this.passwordFile = passwordFile;
			}
		}

		/**
		 * Defines exchange responses for the api.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public class ExchangeResponse implements Serializable {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The folder.
			 */
			private String folder;

			/**
			 * Default constructor for an exchange response for the api.
			 * 
			 * @since 1.8
			 */
			public ExchangeResponse() {
				super();

				folder = configurationService.getExchange().getFolder().toString();
			}

			/**
			 * Returns the folder.
			 *
			 * @return The folder.
			 * @since 1.8
			 */
			public String getFolder() {
				return folder;
			}

			/**
			 * Set the folder.
			 *
			 * @param folder The folder to set.
			 * @since 1.8
			 */
			public void setFolder(String folder) {
				this.folder = folder;
			}
		}

		/**
		 * Defines opt responses for the api.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public class OptResponse implements Serializable {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The folder.
			 */
			private String folder;

			/**
			 * Default constructor for an exchange response for the api.
			 * 
			 * @since 1.8
			 */
			public OptResponse() {
				super();

				folder = configurationService.getOpt().getFolder().toString();
			}

			/**
			 * Returns the folder.
			 *
			 * @return The folder.
			 * @since 1.8
			 */
			public String getFolder() {
				return folder;
			}

			/**
			 * Set the folder.
			 *
			 * @param folder The folder to set.
			 * @since 1.8
			 */
			public void setFolder(String folder) {
				this.folder = folder;
			}
		}

		/**
		 * Defines temporary responses for the api.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public class TemporaryResponse implements Serializable {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The folder.
			 */
			private String folder;

			/**
			 * The prefix.
			 */
			private final String prefix;

			/**
			 * Default constructor for a temporary response for the api.
			 * 
			 * @since 1.8
			 */
			public TemporaryResponse() {
				super();

				final TemporaryConfiguration configuration = configurationService.getTemporary();

				folder = configuration.getFolder().toString();
				prefix = configuration.getPrefix();
			}

			/**
			 * Returns the folder.
			 *
			 * @return The folder.
			 * @since 1.8
			 */
			public String getFolder() {
				return folder;
			}

			/**
			 * Set the folder.
			 *
			 * @param folder The folder to set.
			 * @since 1.8
			 */
			public void setFolder(String folder) {
				this.folder = folder;
			}

			/**
			 * Returns the prefix.
			 *
			 * @return The prefix.
			 * @since 1.8
			 */
			public String getPrefix() {
				return prefix;
			}
		}

		/**
		 * Defines service provider property responses for the api.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public class ServiceProviderPropertyResponse implements Serializable {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The collection.
			 */
			private String collection;

			/**
			 * The key.
			 */
			private String key;

			/**
			 * The value.
			 */
			private String value;

			/**
			 * Creates a service provider property response for the api.
			 * 
			 * @param property The property.
			 * @since 1.8
			 */
			public ServiceProviderPropertyResponse(ConfigurationServiceProvider.Property property) {
				super();

				collection = property.getCollection();
				key = property.getKey();
				value = property.getValue();
			}

			/**
			 * Returns the collection.
			 *
			 * @return The collection.
			 * @since 1.8
			 */
			public String getCollection() {
				return collection;
			}

			/**
			 * Set the collection.
			 *
			 * @param collection The collection to set.
			 * @since 1.8
			 */
			public void setCollection(String collection) {
				this.collection = collection;
			}

			/**
			 * Returns the key.
			 *
			 * @return The key.
			 * @since 1.8
			 */
			public String getKey() {
				return key;
			}

			/**
			 * Set the key.
			 *
			 * @param key The key to set.
			 * @since 1.8
			 */
			public void setKey(String key) {
				this.key = key;
			}

			/**
			 * Returns the value.
			 *
			 * @return The value.
			 * @since 1.8
			 */
			public String getValue() {
				return value;
			}

			/**
			 * Set the value.
			 *
			 * @param value The value to set.
			 * @since 1.8
			 */
			public void setValue(String value) {
				this.value = value;
			}
		}

	}

}
