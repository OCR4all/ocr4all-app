/**
 * File:     System.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     12.04.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;

/**
 * Defines ocr4all system properties.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class System {
	/**
	 * The unix operating system.
	 */
	private OperatingSystem unix = new OperatingSystem(ConfigurationService.OperatingSystem.unix);

	/**
	 * The mac operating system.
	 */
	private OperatingSystem mac = new OperatingSystem(ConfigurationService.OperatingSystem.mac);

	/**
	 * The windows operating system.
	 */
	private OperatingSystem windows = new OperatingSystem(ConfigurationService.OperatingSystem.windows);

	/**
	 * Returns the unix operating system.
	 *
	 * @return The unix operating system.
	 * @since 1.8
	 */
	public OperatingSystem getUnix() {
		return unix;
	}

	/**
	 * Set the unix operating system.
	 *
	 * @param operatingSystem The operating system to set.
	 * @since 1.8
	 */
	public void setUnix(OperatingSystem operatingSystem) {
		unix = operatingSystem;
	}

	/**
	 * Returns the mac operating system.
	 *
	 * @return The mac operating system.
	 * @since 1.8
	 */
	public OperatingSystem getMac() {
		return mac;
	}

	/**
	 * Set the mac operating system.
	 *
	 * @param operatingSystem The operating system to set.
	 * @since 1.8
	 */
	public void setMac(OperatingSystem operatingSystem) {
		mac = operatingSystem;
	}

	/**
	 * Returns the windows operating system.
	 *
	 * @return The windows operating system.
	 * @since 1.8
	 */
	public OperatingSystem getWindows() {
		return windows;
	}

	/**
	 * Set the windows operating system.
	 *
	 * @param operatingSystem The operating system to set.
	 * @since 1.8
	 */
	public void setWindows(OperatingSystem operatingSystem) {
		windows = operatingSystem;
	}

	/**
	 * Defines operating systems.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class OperatingSystem {
		/**
		 * The command properties.
		 */
		private Command command = new Command(this);

		/**
		 * The operating system.
		 */
		private final ConfigurationService.OperatingSystem operatingSystem;

		/**
		 * Default constructor for an operating system.
		 * 
		 * @since 1.8
		 */
		public OperatingSystem() {
			this(null);
		}

		/**
		 * Creates an operating system.
		 * 
		 * @param operatingSystem The operating system.
		 * @since 1.8
		 */
		public OperatingSystem(ConfigurationService.OperatingSystem operatingSystem) {
			super();

			this.operatingSystem = operatingSystem;
		}

		/**
		 * Returns the operating system.
		 *
		 * @return The operating system.
		 * @since 1.8
		 */
		private ConfigurationService.OperatingSystem getOperatingSystem() {
			return operatingSystem;
		}

		/**
		 * Returns the command properties.
		 *
		 * @return The command properties.
		 * @since 1.8
		 */
		public Command getCommand() {
			return command;
		}

		/**
		 * Set the command properties.
		 *
		 * @param command The command properties to set.
		 * @since 1.8
		 */
		public void setCommand(Command command) {
			this.command = command;
		}
	}

	/**
	 * Defines command properties.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Command {
		/**
		 * The default mac docker folder.
		 */
		private static final String defaultMacDocker = "/usr/local/bin/";

		/**
		 * The default mac ImageMagick folder.
		 */
		private static final String defaultMacImageMagick = "/opt/local/bin/";

		/**
		 * The default windows docker folder.
		 */
		private static final String defaultWindowsDocker = "C:/Program Files/Docker/";

		/**
		 * The default windows ImageMagick folder.
		 */
		private static final String defaultWindowsImageMagick = "C:/Program Files/ImageMagick-7.0.11/";

		/**
		 * The default command docker.
		 */
		public static final String defaultDocker = "/usr/bin/docker";

		/**
		 * The default command convert.
		 */
		public static final String defaultConvert = "/usr/bin/convert";

		/**
		 * The default command identify.
		 */
		public static final String defaultIdentify = "/usr/bin/identify";

		/**
		 * Defines programs.
		 */
		private enum Program {
			docker, convert, identify
		}

		/**
		 * The operating system.
		 */
		private final OperatingSystem operatingSystem;

		/**
		 * The command docker. The default value is /usr/bin/docker.
		 */
		private String docker = defaultDocker;

		/**
		 * The command convert. The default value is /usr/bin/convert.
		 */
		private String convert = defaultConvert;

		/**
		 * The command identify. The default value is /usr/bin/identify.
		 */
		private String identify = defaultIdentify;

		/**
		 * Default constructor for a command property.
		 * 
		 * @since 1.8
		 */
		public Command() {
			this(null);
		}

		/**
		 * Creates a command property.
		 * 
		 * @param operatingSystem The operating system.
		 * @since 1.8
		 */
		public Command(OperatingSystem operatingSystem) {
			super();

			this.operatingSystem = operatingSystem;
		}

		/**
		 * Returns the default value for given program.
		 * 
		 * @param program The program.
		 * @return The default value for given program.
		 * @since 1.8
		 */
		private String getDefault(Program program) {
			ConfigurationService.OperatingSystem operatingSystem = this.operatingSystem == null
					|| this.operatingSystem.getOperatingSystem() == null ? ConfigurationService.OperatingSystem.unix
							: this.operatingSystem.getOperatingSystem();
			switch (program) {
			case docker:
				switch (operatingSystem) {
				case mac:
					return defaultMacDocker + "docker";
				case windows:
					return defaultWindowsDocker + "docker.exe";
				case unix:
				default:
					return defaultDocker;
				}
			case convert:
				switch (operatingSystem) {
				case mac:
					return defaultMacImageMagick + "convert";
				case windows:
					return defaultWindowsImageMagick + "convert.exe";
				case unix:
				default:
					return defaultConvert;
				}
			case identify:
				switch (operatingSystem) {
				case mac:
					return defaultMacImageMagick + "identify";
				case windows:
					return defaultWindowsImageMagick + "identify.exe";
				case unix:
				default:
					return defaultIdentify;
				}
			default:
				return null;
			}
		}

		/**
		 * Returns the command docker.
		 *
		 * @return The command docker.
		 * @since 1.8
		 */
		public String getDocker() {
			return OCR4all.getNotEmpty(docker, getDefault(Program.docker));
		}

		/**
		 * Set the command docker.
		 *
		 * @param command The command to set.
		 * @since 1.8
		 */
		public void setDocker(String command) {
			docker = command;
		}

		/**
		 * Returns the command convert.
		 *
		 * @return The command convert.
		 * @since 1.8
		 */
		public String getConvert() {
			return OCR4all.getNotEmpty(convert, getDefault(Program.convert));
		}

		/**
		 * Set the command convert.
		 *
		 * @param command The command to set.
		 * @since 1.8
		 */
		public void setConvert(String command) {
			convert = command;
		}

		/**
		 * Returns the command identify.
		 *
		 * @return The command identify.
		 * @since 1.8
		 */
		public String getIdentify() {
			return OCR4all.getNotEmpty(identify, getDefault(Program.identify));
		}

		/**
		 * Set the command identify.
		 *
		 * @param command The command to set.
		 * @since 1.8
		 */
		public void setIdentify(String command) {
			identify = command;
		}
	}
}
