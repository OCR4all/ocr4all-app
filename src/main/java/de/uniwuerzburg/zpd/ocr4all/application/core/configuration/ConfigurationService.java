/**
 * File:     ConfigurationService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration
 *
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     03.11.2020
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.OCR4all;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.repository.RepositoryConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Instance;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Framework;

/**
 * Defines configuration services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Service
@ApplicationScope
@Configuration
public class ConfigurationService {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigurationService.class);

	/**
	 * Defines operating systems.
	 */
	public enum OperatingSystem {
		/**
		 * The unix operating system.
		 */
		unix,
		/**
		 * The mac operating system.
		 */
		mac,
		/**
		 * The windows operating system.
		 */
		windows,
		/**
		 * A notSupported operating system.
		 */
		notSupported;

		/**
		 * Returns the respective operating systems for frameworks.
		 *
		 * @return The respective operating systems for frameworks.
		 * @since 1.8
		 */
		public Framework.OperatingSystem getFramework() {
			switch (this) {
			case unix:
				return Framework.OperatingSystem.unix;
			case mac:
				return Framework.OperatingSystem.mac;
			case windows:
				return Framework.OperatingSystem.windows;
			case notSupported:
			default:
				return Framework.OperatingSystem.notSupported;
			}
		}
	}

	/**
	 * The operating system.
	 */
	private static OperatingSystem operatingSystem;
	{
		String os = System.getProperty("os.name").toLowerCase();

		if (os.contains("nix") || os.contains("aix") || os.contains("nux"))
			operatingSystem = OperatingSystem.unix;
		else if (os.contains("osx"))
			operatingSystem = OperatingSystem.mac;
		else if (os.contains("win"))
			operatingSystem = OperatingSystem.windows;
		else
			operatingSystem = OperatingSystem.notSupported;
	}

	/**
	 * The effective system user ID. -1 if not defined.
	 */
	private static int uid;

	/**
	 * The effective system group ID. -1 if not defined.
	 */
	private static int gid;
	{
		switch (operatingSystem) {
		case mac:
		case unix:
			uid = getSystemID(true);
			gid = getSystemID(false);
			break;
		default:
			uid = -1;
			gid = -1;
			break;
		}
	}

	/**
	 * The system command.
	 */
	private final SystemCommand systemCommand;

	/**
	 * The configuration for the application.
	 */
	private final ApplicationConfiguration application;

	/**
	 * The configuration for the image.
	 */
	private final ImageConfiguration image;

	/**
	 * The configuration for the exchange.
	 */
	private final ExchangeConfiguration exchange;

	/**
	 * The configuration for the repository.
	 */
	private final RepositoryConfiguration repository;

	/**
	 * The configuration for the workspace.
	 */
	private final WorkspaceConfiguration workspace;

	/**
	 * The configuration for the opt, reserved for the installation of add-on
	 * application software packages.
	 */
	private final OptConfiguration opt;

	/**
	 * The configuration for the temporary files/directories.
	 */
	private final TemporaryConfiguration temporary;

	/**
	 * The configuration for the api.
	 */
	private final ApiConfiguration api;

	/**
	 * The environment that this component runs.
	 */
	private Environment environment;

	/**
	 * The server properties for a web server (e.g. port and path settings).
	 */
	private ServerProperties serverProperties;

	/**
	 * Creates a configuration service.
	 *
	 * @param environment      The environment that this component runs.
	 * @param serverProperties The server properties for a web server (e.g. port and
	 *                         path settings).
	 * @param properties       The ocr4all properties.
	 * @since 1.8
	 */
	public ConfigurationService(Environment environment, ServerProperties serverProperties, OCR4all properties) {
		super();

		this.environment = environment;
		this.serverProperties = serverProperties;

		systemCommand = new SystemCommand(properties.getSystem());
		application = new ApplicationConfiguration(properties.getApplication());
		image = new ImageConfiguration(properties.getImage());
		exchange = new ExchangeConfiguration(properties);
		repository = new RepositoryConfiguration(properties);
		opt = new OptConfiguration(properties);
		workspace = new WorkspaceConfiguration(properties.getWorkspace(), systemCommand, application, exchange, opt);
		api = new ApiConfiguration(properties.getApi());
		temporary = new TemporaryConfiguration(properties.getTemporary());
	}

	/**
	 * Returns the effective system user/group IDs.
	 *
	 * @param isUser True if returns the user ID. Otherwise, returns the group ID.
	 * @return The effective system user/group IDs. On troubles, returns -1.
	 * @since 1.8
	 */
	private static int getSystemID(boolean isUser) {
		ProcessBuilder builder = new ProcessBuilder(new ArrayList<String>(Arrays.asList("id", (isUser ? "-u" : "-g"))));

		try {
			// Start the system process
			Process process = builder.start();

			int exitValue = -1;
			try {
				exitValue = process.waitFor();
			} catch (InterruptedException ie) {
				try {
					exitValue = process.exitValue();
				} catch (IllegalThreadStateException itse) {
					// can not recover system job exit value
				}
			}

			if (exitValue == 0)
				return Integer.parseInt(OCR4allUtils.getString(process.getInputStream()));

		} catch (Exception e) {
			// Nothing to do
		}

		return -1;
	}

	/**
	 * Returns the server port.
	 *
	 * @return The server port.
	 * @since 1.8
	 */
	public int getServerPort() {
		return serverProperties.getPort();
	}

	/**
	 * Returns the active profiles.
	 *
	 * @return The active profiles.
	 * @since 1.8
	 */
	public String[] getActiveProfiles() {
		return environment.getActiveProfiles();
	}

	/**
	 * Returns the active profiles separated by commas.
	 *
	 * @return The active profiles separated by commas.
	 * @since 1.8
	 */
	public String getActiveProfilesCSV() {
		StringBuffer buffer = new StringBuffer();

		for (String profile : environment.getActiveProfiles()) {
			if (buffer.length() > 0)
				buffer.append(", ");

			buffer.append(profile);
		}

		return buffer.toString();
	}

	/**
	 * Returns the operating system.
	 *
	 * @return The operating system.
	 * @since 1.8
	 */
	public static OperatingSystem getOperatingSystem() {
		return operatingSystem;
	}

	/**
	 * Returns the effective system user ID.
	 *
	 * @return The effective system user ID. -1 if not defined.
	 * @since 1.8
	 */
	public static int getUID() {
		return uid;
	}

	/**
	 * Returns the effective system group ID.
	 *
	 * @return The effective system group ID. -1 if not defined.
	 * @since 1.8
	 */
	public static int getGID() {
		return gid;
	}

	/**
	 * Returns the instance.
	 *
	 * @return The instance.
	 * @since 1.8
	 */
	public Instance getInstance() {
		return new Instance(getApplication().getId(), getWorkspace().getConfiguration().getInstance());
	}

	/**
	 * Returns the system command.
	 *
	 * @return The system command.
	 * @since 1.8
	 */
	public SystemCommand getSystemCommand() {
		return systemCommand;
	}

	/**
	 * Returns the configuration for the application.
	 *
	 * @return The configuration for the application.
	 * @since 1.8
	 */
	public ApplicationConfiguration getApplication() {
		return application;
	}

	/**
	 * Returns the configuration for the image.
	 *
	 * @return The configuration for the image.
	 * @since 1.8
	 */
	public ImageConfiguration getImage() {
		return image;
	}

	/**
	 * Returns the configuration for the exchange.
	 *
	 * @return The configuration for the exchange.
	 * @since 1.8
	 */
	public ExchangeConfiguration getExchange() {
		return exchange;
	}

	/**
	 * Returns the configuration for the repository.
	 *
	 * @return The configuration for the repository.
	 * @since 1.8
	 */
	public RepositoryConfiguration getRepository() {
		return repository;
	}

	/**
	 * Returns the configuration for the workspace.
	 *
	 * @return The configuration for the workspace.
	 * @since 1.8
	 */
	public WorkspaceConfiguration getWorkspace() {
		return workspace;
	}

	/**
	 * Returns the configuration for the opt, reserved for the installation of
	 * add-on application software packages.
	 *
	 * @return The configuration for the opt.
	 * @since 1.8
	 */
	public OptConfiguration getOpt() {
		return opt;
	}

	/**
	 * Returns the configuration for the temporary files/directories.
	 *
	 * @return The configuration for the temporary files/directories.
	 * @since 1.8
	 */
	public TemporaryConfiguration getTemporary() {
		return temporary;
	}

	/**
	 * Returns the api configuration.
	 *
	 * @return The api configuration.
	 * @since 1.8
	 */
	public ApiConfiguration getApi() {
		return api;
	}

	/**
	 * Initializes the folder. If it is not available, then creates it.
	 *
	 * @param isCreateParent True if create all nonexistent parent directories
	 *                       first.
	 * @param path           The folder to initialize.
	 * @param message        The folder short description for logging message.
	 * @return The path. Empty, if the path is not a directory or cannot be created.
	 * @since 1.8
	 */
	public static Optional<Path> initializeFolder(boolean isCreateParent, Path path, String message) {
		if (!Files.exists(path)) {
			try {
				if (isCreateParent)
					Files.createDirectories(path);
				else
					Files.createDirectory(path);

				logger.info("Created " + message + " folder '" + path + "'.");
			} catch (IOException e) {
				logger.warn("Cannot create " + message + " folder '" + path + "' - " + e.getMessage());

				return Optional.empty();
			}
		} else if (!Files.isDirectory(path)) {
			logger.warn("The " + message + " folder '" + path + "' is not a directory.");

			return Optional.empty();
		}

		return Optional.of(path);
	}

	/**
	 * SystemCommand is an immutable class that defines system commands.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class SystemCommand {
		/**
		 * The system properties.
		 */
		private final de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.System systemProperties;

		/**
		 * Creates system command.
		 *
		 * @param systemProperties The system properties.
		 * @since 1.8
		 */
		public SystemCommand(
				de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.System systemProperties) {
			super();

			this.systemProperties = systemProperties;
		}

		/**
		 * Returns true if the system command is available.
		 *
		 * @param command The command.
		 * @return True if the system command is available.
		 * @since 1.8
		 */
		public static boolean isAvailable(String command) {
			if (command == null || command.isBlank())
				return false;
			else {
				Path path = Paths.get(command.trim());

				return !Files.isDirectory(path) && Files.isExecutable(path);
			}
		}

		/**
		 * Returns true if the system command docker is available.
		 *
		 * @return True if the system command docker is available.
		 * @since 1.8
		 */
		public boolean isDockerAvailable() {
			return isAvailable(getDocker());
		}

		/**
		 * Returns the system command docker depending on the running operation system
		 * from the properties.
		 *
		 * @return The system command docker depending on the running operation system
		 *         from the properties.
		 * @since 1.8
		 */
		public String getDocker() {
			switch (getOperatingSystem()) {
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
		 * Returns true if the system command convert is available.
		 *
		 * @return True if the system command convert is available.
		 * @since 1.8
		 */
		public boolean isConvertAvailable() {
			return isAvailable(getConvert());
		}

		/**
		 * Returns the system command convert depending on the running operation system
		 * from the properties.
		 *
		 * @return The system command convert depending on the running operation system
		 *         from the properties.
		 * @since 1.8
		 */
		public String getConvert() {
			switch (getOperatingSystem()) {
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
		 * Returns true if the system command identify is available.
		 *
		 * @return True if the system command identify is available.
		 * @since 1.8
		 */
		public boolean isIdentifyAvailable() {
			return isAvailable(getIdentify());
		}

		/**
		 * Returns the system command identify depending on the running operation system
		 * from the properties.
		 *
		 * @return The system command identify depending on the running operation system
		 *         from the properties.
		 * @since 1.8
		 */
		public String getIdentify() {
			switch (getOperatingSystem()) {
			case mac:
				return systemProperties.getMac().getCommand().getIdentify();
			case windows:
				return systemProperties.getWindows().getCommand().getIdentify();
			case unix:
			default:
				return systemProperties.getUnix().getCommand().getIdentify();
			}
		}
	}
}
