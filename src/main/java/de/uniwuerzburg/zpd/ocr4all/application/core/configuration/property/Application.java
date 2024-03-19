/**
 * File:     Application.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.03.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

/**
 * Defines ocr4all application properties.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Application {
	/**
	 * The default label.
	 */
	private static final String defaultLabel = "ocr4all-app";

	/**
	 * The default name.
	 */
	private static final String defaultName = "ocr4all app";

	/**
	 * The label. The default label is ocr4all-app.
	 */
	private String label = defaultLabel;

	/**
	 * The name. The default name is ocr4all app.
	 */
	private String name = defaultName;

	/**
	 * The charset. The default charset is UTF_8.
	 */
	private String charset = "UTF_8";

	/**
	 * The date properties.
	 */
	private Date date = new Date();

	/**
	 * The locale properties.
	 */
	private Locale locale = new Locale();

	/**
	 * The view.
	 */
	private View view = new View();

	/**
	 * The file monitor properties.
	 */
	private Monitor monitor = new Monitor();

	/**
	 * The thread properties.
	 */
	private Thread thread = new Thread();

	/**
	 * The SPI properties.
	 */
	private SPI spi = new SPI();

	/**
	 * The security properties.
	 */
	private Security security = new Security();

	/**
	 * Returns the label.
	 *
	 * @return The label.
	 * @since 1.8
	 */
	public String getLabel() {
		return OCR4all.getNotEmpty(label, defaultLabel);
	}

	/**
	 * Set the label.
	 *
	 * @param label The label to set.
	 * @since 1.8
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Returns the name.
	 *
	 * @return The name.
	 * @since 1.8
	 */
	public String getName() {
		return OCR4all.getNotEmpty(name, defaultName);
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
	 * Returns the charset.
	 *
	 * @return The charset.
	 * @since 1.8
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * Set the charset.
	 *
	 * @param charset The charset to set.
	 * @since 1.8
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * Returns the date properties.
	 *
	 * @return The date properties.
	 * @since 1.8
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Set the date properties.
	 *
	 * @param date The date properties to set.
	 * @since 1.8
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Returns the locale properties.
	 *
	 * @return The locale properties.
	 * @since 1.8
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Set the locale properties.
	 *
	 * @param locale The locale properties to set.
	 * @since 1.8
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * Returns the view.
	 *
	 * @return The view.
	 * @since 1.8
	 */
	public View getView() {
		return view;
	}

	/**
	 * Set the view.
	 *
	 * @param view The view to set.
	 * @since 1.8
	 */
	public void setView(View view) {
		this.view = view;
	}

	/**
	 * Returns the file monitor properties.
	 *
	 * @return The file monitor properties.
	 * @since 1.8
	 */
	public Monitor getMonitor() {
		return monitor;
	}

	/**
	 * Set the file monitor properties.
	 *
	 * @param monitor The monitor properties to set.
	 * @since 1.8
	 */
	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
	}

	/**
	 * Returns the thread properties.
	 *
	 * @return The thread properties.
	 * @since 1.8
	 */
	public Thread getThread() {
		return thread;
	}

	/**
	 * Set the thread properties.
	 *
	 * @param thread The thread properties to set.
	 * @since 1.8
	 */
	public void setThread(Thread thread) {
		this.thread = thread;
	}

	/**
	 * Returns the spi properties.
	 *
	 * @return The spi properties.
	 * @since 17
	 */
	public SPI getSpi() {
		return spi;
	}

	/**
	 * Set the spi properties.
	 *
	 * @param spi The spi properties to set.
	 * @since 17
	 */
	public void setSpi(SPI spi) {
		this.spi = spi;
	}

	/**
	 * Returns the security properties.
	 *
	 * @return The security properties.
	 * @since 1.8
	 */
	public Security getSecurity() {
		return security;
	}

	/**
	 * Set the security properties.
	 *
	 * @param security The security properties to set.
	 * @since 1.8
	 */
	public void setSecurity(Security security) {
		this.security = security;
	}

	/**
	 * Defines date properties.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Date {
		/**
		 * The default date format.
		 */
		public static final String defaultFormat = "yyyy-MM-dd HH:mm:ss";

		/**
		 * The format. It uses the date and time patterns as specified in
		 * {@link java.text.SimpleDateFormat}. The default value is yyyy-MM-dd HH:mm:ss.
		 */
		private String format = defaultFormat;

		/**
		 * Returns the format.
		 *
		 * @return The format.
		 * @since 1.8
		 */
		public String getFormat() {
			return OCR4all.getNotEmpty(format, defaultFormat);
		}

		/**
		 * Set the format.
		 *
		 * @param format The format to set.
		 * @since 1.8
		 */
		public void setFormat(String format) {
			this.format = format;
		}
	}

	/**
	 * Defines locale properties.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Locale {
		/**
		 * The default language.
		 */
		public static final String defaultLanguage = "en";

		/**
		 * The language that conforms to the IETF BCP 47 syntax. The default value is
		 * en.
		 */
		private String language = defaultLanguage;

		/**
		 * The region that conforms to the IETF BCP 47 syntax.
		 */
		private String region = null;

		/**
		 * The script that conforms to the IETF BCP 47 syntax.
		 */
		private String script = null;

		/**
		 * Returns the language that conforms to the IETF BCP 47 syntax.
		 *
		 * @return The language that conforms to the IETF BCP 47 syntax.
		 * @since 1.8
		 */
		public String getLanguage() {
			return OCR4all.getNotEmpty(language, defaultLanguage);
		}

		/**
		 * Set the language that conforms to the IETF BCP 47 syntax.
		 *
		 * @param language The language to set.
		 * @since 1.8
		 */
		public void setLanguage(String language) {
			this.language = language;
		}

		/**
		 * Returns the region that conforms to the IETF BCP 47 syntax.
		 *
		 * @return The region that conforms to the IETF BCP 47 syntax.
		 * @since 1.8
		 */
		public String getRegion() {
			return region;
		}

		/**
		 * Set the region that conforms to the IETF BCP 47 syntax.
		 *
		 * @param region The region to set.
		 * @since 1.8
		 */
		public void setRegion(String region) {
			this.region = region;
		}

		/**
		 * Returns the script that conforms to the IETF BCP 47 syntax.
		 *
		 * @return The script that conforms to the IETF BCP 47 syntax.
		 * @since 1.8
		 */
		public String getScript() {
			return script;
		}

		/**
		 * Set the script that conforms to the IETF BCP 47 syntax.
		 *
		 * @param script The script to set.
		 * @since 1.8
		 */
		public void setScript(String script) {
			this.script = script;
		}
	}

	/**
	 * Defines view configurations.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class View {
		/**
		 * The languages that conforms to the IETF BCP 47 syntax. The default languages
		 * are English and German.
		 */
		private List<String> languages = new ArrayList<>();
		{
			languages.add("en");
			languages.add("de");
		}

		/**
		 * Returns the languages that conforms to the IETF BCP 47 syntax.
		 *
		 * @return The languages.
		 * @since 1.8
		 */
		public List<String> getLanguages() {
			return languages;
		}

		/**
		 * Set the languages that conforms to the IETF BCP 47 syntax.
		 *
		 * @param languages The languages to set.
		 * @since 1.8
		 */
		public void setLanguages(List<String> languages) {
			this.languages = languages;
		}

	}

	/**
	 * Defines file monitor properties.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Monitor {
		/**
		 * The file monitor interval. This is the amount of time in milliseconds to wait
		 * between checks of the file system. The default value is 15000.
		 */
		@Min(value = 1000, message = "The monitor interval should not be less than 1000 ms")
		private long interval = 15000;

		/**
		 * Returns the file monitor interval.
		 *
		 * @return The file monitor interval.
		 * @since 1.8
		 */
		public long getInterval() {
			return interval;
		}

		/**
		 * Set the file monitor interval.
		 *
		 * @param interval The interval to set.
		 * @since 1.8
		 */
		public void setInterval(long interval) {
			this.interval = interval;
		}
	}

	/**
	 * Defines thread properties.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Thread {
		/**
		 * The pool.
		 */
		private Pool pool = new Pool();

		/**
		 * Returns the pool.
		 *
		 * @return The pool.
		 * @since 1.8
		 */
		public Pool getPool() {
			return pool;
		}

		/**
		 * Set the pool.
		 *
		 * @param pool The pool to set.
		 * @since 1.8
		 */
		public void setPool(Pool pool) {
			this.pool = pool;
		}

		/**
		 * Defines pool properties.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class Pool {
			/**
			 * The size.
			 */
			private Size size = new Size();

			/**
			 * Returns the size.
			 *
			 * @return The size.
			 * @since 1.8
			 */
			public Size getSize() {
				return size;
			}

			/**
			 * Set the size.
			 *
			 * @param size The size to set.
			 * @since 1.8
			 */
			public void setSize(Size size) {
				this.size = size;
			}

			/**
			 * Defines size properties.
			 *
			 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
			 * @version 1.0
			 * @since 1.8
			 */
			public static class Size {
				/**
				 * The thread pool size for task. The default value is 6.
				 */
				@Min(value = 1, message = "The thread pool size for task should not be less than 1")
				private int task = 6;

				/**
				 * The thread pool size for workflow. The default value is 12.
				 */
				@Min(value = 1, message = "The thread pool size for workflow should not be less than 1")
				private int workflow = 12;

				/**
				 * Returns the thread pool size for task.
				 *
				 * @return The size.
				 * @since 1.8
				 */
				public int getTask() {
					return task;
				}

				/**
				 * Set the thread pool size for task.
				 *
				 * @param size The size to set.
				 * @since 1.8
				 */
				public void setTask(int size) {
					task = size;
				}

				/**
				 * Returns the thread pool size for workflow.
				 *
				 * @return The size.
				 * @since 1.8
				 */
				public int getWorkflow() {
					return workflow;
				}

				/**
				 * Set the thread pool size for workflow.
				 *
				 * @param size The size to set.
				 * @since 1.8
				 */
				public void setWorkflow(int size) {
					workflow = size;
				}
			}
		}
	}

	/**
	 * Defines SPI properties.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class SPI {
		/**
		 * The microservice architecture (MSA) properties.
		 */
		private List<MSA> msa = new ArrayList<>();

		/**
		 * Returns the microservice architecture (MSA) properties.
		 *
		 * @return The microservice architecture (MSA) properties.
		 * @since 17
		 */
		public List<MSA> getMsa() {
			return msa;
		}

		/**
		 * Set the microservice architecture (MSA) properties.
		 *
		 * @param msa The microservice architecture (MSA) properties to set.
		 * @since 17
		 */
		public void setMsa(List<MSA> msa) {
			this.msa = msa;
		}

		/**
		 * Defines microservice architecture (MSA) properties.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 17
		 */
		public static class MSA {
			/**
			 * The id.
			 */
			@NotEmpty(message = "the microservice architecture (MSA) id cannot be null nor empty")
			private String id;

			/**
			 * The url.
			 */
			@NotEmpty(message = "the microservice architecture (MSA) url cannot be null nor empty")
			private String url;

			/**
			 * The Websocket property.
			 */
			private Websocket websocket = new Websocket();

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
			 * Set the id.
			 *
			 * @param id The id to set.
			 * @since 17
			 */
			public void setId(String id) {
				this.id = id;
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
			 * Set the url.
			 *
			 * @param url The url to set.
			 * @since 17
			 */
			public void setUrl(String url) {
				this.url = url;
			}

			/**
			 * Returns the Websocket property.
			 *
			 * @return The Websocket property.
			 * @since 17
			 */
			public Websocket getWebsocket() {
				return websocket;
			}

			/**
			 * Set the Websocket property.
			 *
			 * @param websocket The Websocket property to set.
			 * @since 17
			 */
			public void setWebsocket(Websocket websocket) {
				this.websocket = websocket;
			}

			/**
			 * Defines Websocket properties.
			 *
			 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
			 * @version 1.0
			 * @since 17
			 */

			public static class Websocket {
				/**
				 * The event topic end point.
				 */
				private String event;

				/**
				 * Returns the event.
				 *
				 * @return The event.
				 * @since 17
				 */
				public String getEvent() {
					return event;
				}

				/**
				 * Set the event.
				 *
				 * @param event The event to set.
				 * @since 17
				 */
				public void setEvent(String event) {
					this.event = event;
				}
			}
		}
	}

	/**
	 * Defines security properties.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Security {
		/**
		 * The groups.
		 */
		private Groups groups = new Groups();

		/**
		 * The default administrator.
		 */
		private Administrator administrator = new Administrator();

		/**
		 * Returns the groups.
		 *
		 * @return The groups.
		 * @since 1.8
		 */
		public Groups getGroups() {
			return groups;
		}

		/**
		 * Set the groups.
		 *
		 * @param groups The groups to set.
		 * @since 1.8
		 */
		public void setGroups(Groups groups) {
			this.groups = groups;
		}

		/**
		 * Returns the default administrator.
		 *
		 * @return The default administrator.
		 * @since 1.8
		 */
		public Administrator getAdministrator() {
			return administrator;
		}

		/**
		 * Set the default administrator.
		 *
		 * @param administrator The default administrator to set.
		 * @since 1.8
		 */
		public void setAdministrator(Administrator administrator) {
			this.administrator = administrator;
		}
	}

	/**
	 * Defines groups properties.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Groups {
		/**
		 * The administrator group. The default value is admin.
		 */
		private String administrator = "admin";

		/**
		 * The coordinator group. The default value is coord.
		 */
		private String coordinator = "coord";

		/**
		 * Returns the administrator group.
		 *
		 * @return The administrator group.
		 * @since 1.8
		 */
		public String getAdministrator() {
			return administrator;
		}

		/**
		 * Set the administrator group.
		 *
		 * @param group The group to set.
		 * @since 1.8
		 */
		public void setAdministrator(String group) {
			administrator = group;
		}

		/**
		 * Returns the coordinator group.
		 *
		 * @return The coordinator group.
		 * @since 1.8
		 */
		public String getCoordinator() {
			return coordinator;
		}

		/**
		 * Set the coordinator group.
		 *
		 * @param group The group to set.
		 * @since 1.8
		 */
		public void setCoordinator(String group) {
			coordinator = group;
		}

	}

	/**
	 * Defines administrator properties.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Administrator {
		/**
		 * True if the administrator user should be created, if it is non available.
		 */
		private boolean isCreate = false;

		/**
		 * The login. The default value is admin.
		 */
		private String login = "admin";

		/**
		 * The password. The default value is ocr4all.
		 */
		private String password = "ocr4all";

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
		 * Set to true if the administrator user should be created, if it is non
		 * available.
		 *
		 * @param isCreate The create flag to set.
		 * @since 1.8
		 */
		public void setCreate(boolean isCreate) {
			this.isCreate = isCreate;
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
		 * Set the login.
		 *
		 * @param login The login to set.
		 * @since 1.8
		 */
		public void setLogin(String login) {
			this.login = login;
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

		/**
		 * Set the password.
		 *
		 * @param password The password to set.
		 * @since 1.8
		 */
		public void setPassword(String password) {
			this.password = password;
		}

	}

}
