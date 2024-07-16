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
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ApplicationConfiguration.class);

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
	 * The thread pool size properties.
	 */
	private final ThreadPoolSizeProperties threadPoolSizeProperties;

	/**
	 * The SPI configuration.
	 */
	private final SPI spi;

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

		threadPoolSizeProperties = new ThreadPoolSizeProperties(properties.getThread().getPool().getSize().getTask(),
				properties.getThread().getPool().getSize().getWorkflow(),
				properties.getThread().getPool().getSize().getTraining());

		// The spi
		spi = new SPI(properties.getSpi());

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
	 * Returns the id.
	 * 
	 * @return The id.
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
	 * Returns the thread pool size properties.
	 *
	 * @return The thread pool size properties.
	 * @since 1.8
	 */
	public ThreadPoolSizeProperties getThreadPoolSizeProperties() {
		return threadPoolSizeProperties;
	}

	/**
	 * Returns the spi configuration.
	 *
	 * @return The spi configuration.
	 * @since 17
	 */
	public SPI getSpi() {
		return spi;
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
	 * Defines thread pool size properties.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ThreadPoolSizeProperties {
		/**
		 * The task pool size.
		 */
		private final int task;

		/**
		 * The workflow pool size.
		 */
		private final int workflow;

		/**
		 * The training pool size.
		 */
		private final int training;

		/**
		 * Creates properties for the thread pool size.
		 * 
		 * @param task     The task pool size.
		 * @param workflow The workflow pool size.
		 * @param training The training pool size.
		 * @since 1.8
		 */
		public ThreadPoolSizeProperties(int task, int workflow, int training) {
			super();

			this.task = task;
			this.workflow = workflow;
			this.training = training;
		}

		/**
		 * Returns the task pool size.
		 *
		 * @return The task pool size.
		 * @since 1.8
		 */
		public int getTask() {
			return task;
		}

		/**
		 * Returns the workflow pool size.
		 *
		 * @return The workflow pool size.
		 * @since 1.8
		 */
		public int getWorkflow() {
			return workflow;
		}

		/**
		 * Returns the training pool size.
		 *
		 * @return The training pool size.
		 * @since 17
		 */
		public int getTraining() {
			return training;
		}

	}

	/**
	 * Defines SPI configurations.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class SPI {
		/**
		 * The quarantine.
		 */
		private final Quarantine quarantine;

		/**
		 * The microservice architectures (MSA).
		 */
		private final List<MSA> msa = new ArrayList<>();

		/**
		 * Creates a SPI configuration.
		 * 
		 * @since 17
		 */
		public SPI(Application.SPI spi) {
			super();

			quarantine = new Quarantine(spi.getQuarantine());

			if (spi != null && spi.getMsa() != null)
				for (Application.SPI.MSA msa : spi.getMsa())
					this.msa.add(new MSA(msa));
		}

		/**
		 * Returns the quarantine.
		 *
		 * @return The quarantine.
		 * @since 17
		 */
		public Quarantine getQuarantine() {
			return quarantine;
		}

		/**
		 * Returns the microservice architectures (MSA).
		 *
		 * @return The microservice architectures (MSA).
		 * @since 17
		 */
		public List<MSA> getMsa() {
			return msa;
		}

		/**
		 * Defines quarantine configurations.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 17
		 */

		public static class Quarantine {
			/**
			 * The number of attempts before giving up, including the first call. This is a
			 * positive integer.
			 */
			private final int maxAttempts;

			/**
			 * The delay time in milliseconds between the attempt. This is a non negative
			 * integer.
			 */
			private final long delayBetweenAttempts;

			/**
			 * Creates an quarantine configuration.
			 * 
			 * @param quarantine The quarantine property.
			 * @since 17
			 */
			public Quarantine(Application.SPI.Quarantine quarantine) {
				super();
				this.maxAttempts = quarantine.getMaxAttempts();
				this.delayBetweenAttempts = quarantine.getDelayBetweenAttempts();
			}

			/**
			 * Returns the number of attempts before giving up, including the first call.
			 * This is a positive integer.
			 *
			 * @return The number of attempts before giving up, including the first call.
			 * @since 17
			 */
			public int getMaxAttempts() {
				return maxAttempts;
			}

			/**
			 * Returns the delay time in milliseconds between the attempt. This is a non
			 * negative integer.
			 *
			 * @return The delay time in milliseconds between the attempt.
			 * @since 17
			 */
			public long getDelayBetweenAttempts() {
				return delayBetweenAttempts;
			}
		}

		/**
		 * Defines microservice architecture (MSA) configurations.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 17
		 */
		public static class MSA {
			/**
			 * The id.
			 */
			private final String id;

			/**
			 * The url.
			 */
			private final String url;

			/**
			 * The WebSocket.
			 */
			private final WebSocket webSocket;

			/**
			 * Creates a microservice architecture (MSA) configuration.
			 * 
			 * @param msa The microservice architecture property.
			 * @since 17
			 */
			public MSA(Application.SPI.MSA msa) {
				super();
				id = msa.getId().trim();
				url = msa.getUrl().trim();

				Application.SPI.MSA.WebSocket socket = msa.getWebsocket();
				webSocket = socket != null && socket.getEndPoint() != null && !socket.getEndPoint().isBlank()
						&& socket.getTopic() != null && !socket.getTopic().isBlank() ? new WebSocket(socket) : null;
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
			 * Returns the url.
			 *
			 * @return The url.
			 * @since 17
			 */
			public String getUrl() {
				return url;
			}

			/**
			 * Returns true if the WebSocket is set.
			 *
			 * @return True if the WebSocket is set.
			 * @since 17
			 */
			public boolean isWebSocketSet() {
				return webSocket != null;
			}

			/**
			 * Returns the WebSocket.
			 *
			 * @return The EebSocket.
			 * @since 17
			 */
			public WebSocket getWebSocket() {
				return webSocket;
			}

			/**
			 * Defines WebSocket configurations.
			 *
			 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
			 * @version 1.0
			 * @since 17
			 */

			public static class WebSocket {
				/**
				 * The end point.
				 */
				private final String endPoint;

				/**
				 * The topic.
				 */
				private final String topic;

				/**
				 * The resilience.
				 */
				private final Resilience resilience;

				/**
				 * Creates a WebSocket configuration.
				 * 
				 * @param socket The WebSocket property.
				 * @since 17
				 */
				public WebSocket(Application.SPI.MSA.WebSocket socket) {
					super();

					this.endPoint = socket.getEndPoint().trim();
					this.topic = socket.getTopic().trim();

					resilience = new Resilience(socket.getResilience());
				}

				/**
				 * Returns the end point.
				 *
				 * @return The end point.
				 * @since 17
				 */
				public String getEndPoint() {
					return endPoint;
				}

				/**
				 * Returns the topic.
				 *
				 * @return The topic.
				 * @since 17
				 */
				public String getTopic() {
					return topic;
				}

				/**
				 * Returns the resilience.
				 *
				 * @return The resilience.
				 * @since 17
				 */
				public Resilience getResilience() {
					return resilience;
				}

				/**
				 * Defines resilience configurations.
				 *
				 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
				 * @version 1.0
				 * @since 17
				 */

				public static class Resilience {
					/**
					 * The number of attempts before giving up, including the first call. This is a
					 * positive integer.
					 */
					private final int maxAttempts;

					/**
					 * The delay time in milliseconds between the attempt. This is a non negative
					 * integer.
					 */
					private final long delayBetweenAttempts;

					/**
					 * The wait time in milliseconds before the next retry attempt. This is a
					 * positive integer.
					 */
					private final long waitDuration;

					/**
					 * Creates a resilience configuration.
					 * 
					 * @param resilience The resilience property.
					 * @since 17
					 */
					public Resilience(Application.SPI.MSA.WebSocket.Resilience resilience) {
						super();
						this.maxAttempts = resilience.getMaxAttempts();
						this.delayBetweenAttempts = resilience.getDelayBetweenAttempts();
						this.waitDuration = resilience.getWaitDuration();
					}

					/**
					 * Returns the number of attempts before giving up, including the first call.
					 * This is a positive integer.
					 *
					 * @return The number of attempts before giving up, including the first call.
					 * @since 17
					 */
					public int getMaxAttempts() {
						return maxAttempts;
					}

					/**
					 * Returns the delay time in milliseconds between the attempt. This is a non
					 * negative integer.
					 *
					 * @return The delay time in milliseconds between the attempt.
					 * @since 17
					 */
					public long getDelayBetweenAttempts() {
						return delayBetweenAttempts;
					}

					/**
					 * Returns the wait time in milliseconds before the next retry attempt. This is
					 * a positive integer.
					 *
					 * @return The wait time in milliseconds before the next retry attempt.
					 * @since 17
					 */
					public long getWaitDuration() {
						return waitDuration;
					}
				}
			}
		}
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
