/**
 * File:     ApiConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     01.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.Api;

/**
 * Defines configurations for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class ApiConfiguration {
	/**
	 * The JWT configuration.
	 */
	private final JWT jwt;

	/**
	 * The documentation configuration.
	 */
	private final Documentation documentation;

	/**
	 * Creates a configuration for the application.
	 * 
	 * @param properties The application properties.
	 * @since 1.8
	 */
	public ApiConfiguration(Api properties) {
		super();

		jwt = new JWT(properties.getJwt());
		documentation = new Documentation(properties.getDocumentation());
	}

	/**
	 * Returns the jwt configuration.
	 *
	 * @return The jwt configuration.
	 * @since 1.8
	 */
	public JWT getJwt() {
		return jwt;
	}

	/**
	 * Returns the documentation configuration.
	 *
	 * @return The documentation configuration.
	 * @since 1.8
	 */
	public Documentation getDocumentation() {
		return documentation;
	}

	/**
	 * Defines JWT configurations for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class JWT {
		/**
		 * The JWT access token secret.
		 */
		private final String secret;

		/**
		 * The JWT access token issuer.
		 */
		private final String issuer;
		/**
		 * The JWT access token validity time in milliseconds.
		 */
		private final long validity;

		/**
		 * Creates a JWT configuration for the application.
		 * 
		 * @param properties The JWT application properties.
		 * @since 1.8
		 */
		public JWT(Api.JWT properties) {
			super();

			secret = properties.getSecret();
			issuer = properties.getIssuer();
			validity = properties.getValidity();
		}

		/**
		 * Returns the JWT access token secret.
		 *
		 * @return The JWT access token secret.
		 * @since 1.8
		 */
		public String getSecret() {
			return secret;
		}

		/**
		 * Returns the JWT access token issuer.
		 *
		 * @return The JWT access token issuer.
		 * @since 1.8
		 */
		public String getIssuer() {
			return issuer;
		}

		/**
		 * Returns the JWT access token validity time in milliseconds.
		 *
		 * @return The JWT access token validity time in milliseconds.
		 * @since 1.8
		 */
		public long getValidity() {
			return validity;
		}

	}

	/**
	 * Defines documentation configurations for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Documentation {
		/**
		 * The base package name.
		 */
		private final String basePackage;

		/**
		 * The title.
		 */
		private final String title;

		/**
		 * The description.
		 */
		private final String description;

		/**
		 * The version.
		 */
		private final String version;

		/**
		 * The contact configuration.
		 */
		private final Contact contact;

		/**
		 * The license. The default license is 'MIT License'.
		 */
		private final String license;

		/**
		 * The url configuration.
		 */
		private final URL url;

		/**
		 * Creates a documentation configuration for the application.
		 * 
		 * @param properties The documentation application properties.
		 * @since 1.8
		 */
		public Documentation(Api.Documentation properties) {
			super();

			basePackage = properties.getSource();
			title = properties.getTitle();
			description = properties.getDescription();
			version = properties.getVersion();
			contact = new Contact(properties.getContact());
			license = properties.getLicense();
			url = new URL(properties.getUrl());
		}

		/**
		 * Returns the base package name.
		 *
		 * @return The base package name.
		 * @since 1.8
		 */
		public String getBasePackage() {
			return basePackage;
		}

		/**
		 * Returns the title.
		 *
		 * @return The title.
		 * @since 1.8
		 */
		public String getTitle() {
			return title;
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
		 * Returns the version.
		 *
		 * @return The version.
		 * @since 1.8
		 */
		public String getVersion() {
			return version;
		}

		/**
		 * Returns the contact configuration.
		 *
		 * @return The contact configuration.
		 * @since 1.8
		 */
		public Contact getContact() {
			return contact;
		}

		/**
		 * Returns the license.
		 *
		 * @return The license.
		 * @since 1.8
		 */
		public String getLicense() {
			return license;
		}

		/**
		 * Returns the url configuration.
		 *
		 * @return The url configuration.
		 * @since 1.8
		 */
		public URL getUrl() {
			return url;
		}

		/**
		 * Defines contact configurations for the api.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class Contact {
			/**
			 * The name.
			 */
			private final String name;

			/**
			 * The email.
			 */
			private final String email;

			/**
			 * Creates a contact configuration for the application.
			 * 
			 * @param properties The contact application properties.
			 * @since 1.8
			 */
			public Contact(Api.Documentation.Contact properties) {
				super();

				name = properties.getName();
				email = properties.getEmail();
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
			 * Returns the email.
			 *
			 * @return The email.
			 * @since 1.8
			 */
			public String getEmail() {
				return email;
			}

		}

		/**
		 * Defines url configurations for the api.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class URL {
			/**
			 * The terms of service url.
			 */
			private final String termsOfService;

			/**
			 * The contact url.
			 */
			private final String contact;

			/**
			 * The license url.
			 */
			private final String license;

			/**
			 * Creates an url configuration for the application.
			 * 
			 * @param properties The url application properties.
			 * @since 1.8
			 */
			public URL(Api.Documentation.URL properties) {
				super();

				termsOfService = properties.getTos();
				contact = properties.getContact();
				license = properties.getLicense();
			}

			/**
			 * Returns the terms of service url.
			 *
			 * @return The terms of service url.
			 * @since 1.8
			 */
			public String getTermsOfService() {
				return termsOfService;
			}

			/**
			 * Returns the contact url.
			 *
			 * @return The contact url.
			 * @since 1.8
			 */
			public String getContact() {
				return contact;
			}

			/**
			 * Returns the license url.
			 *
			 * @return The license url.
			 * @since 1.8
			 */
			public String getLicense() {
				return license;
			}

		}
	}
}
