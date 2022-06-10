/**
 * File:     ApplicationConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     23.03.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project.ProjectConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.Application;

/**
 * Defines configurations for the application.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class ApplicationConfiguration {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProjectConfiguration.class);

	/**
	 * The label.
	 */
	private final String label;

	/**
	 * The name.
	 */
	private final String name;

	/**
	 * The start time.
	 */
	private Date start = new Date();

	/**
	 * The charset.
	 */
	private final Charset charset;

	/**
	 * The date format.
	 */
	private final SimpleDateFormat dateFormat;

	/**
	 * The locale.
	 */
	private final Locale locale;

	/**
	 * The view languages.
	 */
	private final List<String> viewLanguages = new ArrayList<>();

	/**
	 * The monitor interval. This is the amount of time in milliseconds to wait
	 * between checks.
	 */
	private final long monitorInterval;

	/**
	 * The administrator group.
	 */
	private final String administratorGroup;

	/**
	 * The coordinator group.
	 */
	private final String coordinatorGroup;

	/**
	 * The default administrator.
	 */
	private final DefaultAdministrator defaultAdministrator;

	/**
	 * Creates a configuration for the application.
	 * 
	 * @param properties The application properties.
	 * @since 1.8
	 */
	public ApplicationConfiguration(Application properties) {
		super();

		// The core data
		label = properties.getLabel();
		name = properties.getName();

		// The charset
		switch (properties.getCharset()) {
		case "ISO_8859_1":
			charset = StandardCharsets.ISO_8859_1;
			break;

		case "US_ASCII":
			charset = StandardCharsets.US_ASCII;
			break;

		case "UTF_16":
			charset = StandardCharsets.UTF_16;
			break;

		case "UTF_8":
			charset = StandardCharsets.UTF_8;
			break;

		default:
			charset = StandardCharsets.UTF_8;
			logger.warn("No char set defined, using default: " + charset.displayName());
		}

		// The date format
		SimpleDateFormat dateFormat;
		try {
			dateFormat = new SimpleDateFormat(properties.getDate().getFormat());
		} catch (Exception e) {
			dateFormat = new SimpleDateFormat(Application.Date.defaultFormat);
			logger.warn("Invalid date format '" + properties.getDate().getFormat() + "', switching to default: "
					+ Application.Date.defaultFormat);
		}
		this.dateFormat = dateFormat;

		// The locale
		Locale.Builder localeBuilder;
		try {
			localeBuilder = new Locale.Builder().setLanguage(properties.getLocale().getLanguage());
			if (properties.getLocale().getRegion() != null)
				localeBuilder.setRegion(properties.getLocale().getRegion());
			if (properties.getLocale().getScript() != null)
				localeBuilder.setScript(properties.getLocale().getScript());
		} catch (Exception e) {
			localeBuilder = new Locale.Builder().setLanguage(Application.Locale.defaultLanguage);
			logger.warn("Invalid locale, switching to default: " + Application.Locale.defaultLanguage);
		}

		locale = localeBuilder.build();

		// The view languages
		for (String language : properties.getView().getLanguages())
			if (!language.isBlank())
				viewLanguages.add(language.trim().toLowerCase());

		if (viewLanguages.isEmpty())
			logger.warn("No view languages available.");

		// The monitor interval
		monitorInterval = properties.getMonitor().getInterval();

		// The security groups group
		String securityGroup = properties.getSecurity().getGroups().getAdministrator();
		administratorGroup = securityGroup == null || securityGroup.isBlank() ? null
				: securityGroup.trim().toLowerCase();

		securityGroup = properties.getSecurity().getGroups().getCoordinator();
		coordinatorGroup = securityGroup == null || securityGroup.isBlank() ? null : securityGroup.trim().toLowerCase();

		Application.Administrator administrator = properties.getSecurity().getAdministrator();
		defaultAdministrator = new DefaultAdministrator(administrator.isCreate(), administrator.getLogin(),
				administrator.getPassword());
	}

	/**
	 * Returns
	 * 
	 * @return
	 * @since 1.8
	 */
	public long getId() {
		return start.getTime();
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
	 * Returns the name.
	 *
	 * @return The name.
	 * @since 1.8
	 */
	public String getName() {
		return name;
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
	 * Returns the formated start time using application pattern.
	 *
	 * @return The formated start time using application pattern.
	 * @since 1.8
	 */
	public String getStartFormated() {
		return format(start);
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
	 * Returns the date format using application pattern.
	 * 
	 * @return The date format using application pattern.
	 * @since 1.8
	 */
	public SimpleDateFormat getDateFormat() {
		return (SimpleDateFormat) dateFormat.clone();
	}

	/**
	 * Formats the current date/time string using application pattern.
	 * 
	 * @return The formatted time string.
	 * @since 1.8
	 */
	public String formatCurrentDate() {
		return format(null);
	}

	/**
	 * Formats a date into a date/time string using application pattern.
	 * 
	 * @param date The date/time to be formatted into a time string. If null, uses
	 *             current date.
	 * @return The formatted time string.
	 * @since 1.8
	 */
	public String format(Date date) {
		return dateFormat.format(date == null ? new Date() : date);
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
	 * Returns the view languages.
	 *
	 * @return The view languages.
	 * @since 1.8
	 */
	public List<String> getViewLanguages() {
		return viewLanguages;
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
	 * Returns true if the administrator group is set.
	 * 
	 * @return True if the administrator group is set.
	 * @since 1.8
	 */
	public boolean isAdministratorGroupSet() {
		return administratorGroup != null;
	}

	/**
	 * Returns the administrator group.
	 *
	 * @return The administrator group.
	 * @since 1.8
	 */
	public String getAdministratorGroup() {
		return administratorGroup;
	}

	/**
	 * Returns true if the coordinator group is set.
	 * 
	 * @return True if the coordinator group is set.
	 * @since 1.8
	 */
	public boolean isCoordinatorGroupSet() {
		return coordinatorGroup != null;
	}

	/**
	 * Returns the coordinator group.
	 *
	 * @return The coordinator group.
	 * @since 1.8
	 */
	public String getCoordinatorGroup() {
		return coordinatorGroup;
	}

	/**
	 * Returns the default administrator.
	 *
	 * @return The default administrator.
	 * @since 1.8
	 */
	public DefaultAdministrator getDefaultAdministrator() {
		return defaultAdministrator;
	}

	/**
	 * Defines default administrators.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class DefaultAdministrator {
		/**
		 * True if the administrator user should be created, if it is non available.
		 */
		private final boolean isCreate;

		/**
		 * The login.
		 */
		private final String login;

		/**
		 * The password.
		 */
		private final String password;

		/**
		 * Creates a default administrator.
		 * 
		 * @param isCreate True if the administrator user should be created, if it is
		 *                 non available.
		 * @param login    The login.
		 * @param password The password.
		 * @since 1.8
		 */
		public DefaultAdministrator(boolean isCreate, String login, String password) {
			super();
			this.isCreate = isCreate;
			this.login = login;
			this.password = password;
		}

		/**
		 * Returns true if the administrator user should be created, if it is non
		 * available.
		 *
		 * @return True if the administrator user should be created, if it is non
		 *         available.
		 * @since 1.8
		 */
		public boolean isCreate() {
			return isCreate;
		}

		/**
		 * Returns the login.
		 *
		 * @return The login.
		 * @since 1.8
		 */
		public String getLogin() {
			return login;
		}

		/**
		 * Returns the password.
		 *
		 * @return The password.
		 * @since 1.8
		 */
		public String getPassword() {
			return password;
		}

	}
}
