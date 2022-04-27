/**
 * File:     Api.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     01.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property;

import javax.validation.constraints.Min;

/**
 * Defines ocr4all api properties.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Api {
	/**
	 * The JWT properties.
	 */
	private JWT jwt = new JWT();

	/**
	 * The documentation properties.
	 */
	private Documentation documentation = new Documentation();

	/**
	 * Returns the JWT properties.
	 *
	 * @return The JWT properties.
	 * @since 1.8
	 */
	public JWT getJwt() {
		return jwt;
	}

	/**
	 * Set the JWT properties.
	 *
	 * @param jwt The JWT properties to set.
	 * @since 1.8
	 */
	public void setJwt(JWT jwt) {
		this.jwt = jwt;
	}

	/**
	 * Returns the documentation.
	 *
	 * @return The documentation.
	 * @since 1.8
	 */
	public Documentation getDocumentation() {
		return documentation;
	}

	/**
	 * Set the documentation.
	 *
	 * @param documentation The documentation to set.
	 * @since 1.8
	 */
	public void setDocumentation(Documentation documentation) {
		this.documentation = documentation;
	}

	/**
	 * Defines JWT properties.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class JWT {
		/**
		 * The default access token secret.
		 */
		private static final String defaultSecret = "--access token secret--";

		/**
		 * The default access token issuer.
		 */
		private static final String defaultIssuer = "ocr4all.org";

		/**
		 * The access token secret. The default secret is --access token secret--.
		 */
		private String secret = defaultSecret;

		/**
		 * The access token issuer. The default issuer is ocr4all.org.
		 */
		private String issuer = defaultIssuer;

		/**
		 * The access token validity time in milliseconds. The default value is one week
		 * (7 * 24 * 60 * 60 * 1000 = 604800000).
		 */
		@Min(value = 1000, message = "The access token validity should not be less than 1000 ms")
		private long validity = 604800000;

		/**
		 * Returns the access token secret.
		 *
		 * @return The access token secret.
		 * @since 1.8
		 */
		public String getSecret() {
			return OCR4all.getNotEmpty(secret, defaultSecret);
		}

		/**
		 * Set the access token secret.
		 *
		 * @param secret The secret to set.
		 * @since 1.8
		 */
		public void setSecret(String secret) {
			this.secret = secret;
		}

		/**
		 * Returns the access token issuer.
		 *
		 * @return The access token issuer.
		 * @since 1.8
		 */
		public String getIssuer() {
			return OCR4all.getNotEmpty(issuer, defaultIssuer);
		}

		/**
		 * Set the access token issuer.
		 *
		 * @param issuer The issuer to set.
		 * @since 1.8
		 */
		public void setIssuer(String issuer) {
			this.issuer = issuer;
		}

		/**
		 * Returns the access token validity time in milliseconds.
		 *
		 * @return The access token validity time in milliseconds.
		 * @since 1.8
		 */
		public long getValidity() {
			return validity;
		}

		/**
		 * Set the access token validity time in milliseconds.
		 *
		 * @param validity The validity in milliseconds to set.
		 * @since 1.8
		 */
		public void setValidity(long validity) {
			this.validity = validity;
		}

	}

	/**
	 * Defines documentation properties.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Documentation {
		/**
		 * The default source.
		 */
		private static final String defaultSource = "de.uniwuerzburg.zpd.ocr4all.application.api";

		/**
		 * The default title.
		 */
		private static final String defaultTile = "OCR4all RESTful web API";

		/**
		 * The default description.
		 */
		private static final String defaultDescription = "This is the official RESTful web API for OCR4all, a software for digitization of primarily very early printed documents";

		/**
		 * The default version.
		 */
		private static final String defaultVersion = "1.0";

		/**
		 * The default license.
		 */
		private static final String defaultLicense = "MIT License";

		/**
		 * The source, this means, the base package name. The default source is
		 * 'de.uniwuerzburg.zpd.ocr4all.application.api'.
		 */
		private String source = defaultSource;

		/**
		 * The title. The default title is 'OCR4all RESTful web API'.
		 */
		private String title = defaultTile;

		/**
		 * The description. The default description is 'This is the official RESTful web
		 * API for OCR4all, a software for digitization of primarily very early printed
		 * documents'.
		 */
		private String description = defaultDescription;

		/**
		 * The version. The default version is '1.0'.
		 */
		private String version = defaultVersion;

		/**
		 * The contact.
		 */
		private Contact contact = new Contact();

		/**
		 * The license. The default license is 'MIT License'.
		 */
		private String license = defaultLicense;

		/**
		 * The url.
		 */
		private URL url = new URL();

		/**
		 * Returns the source, this means, the base package name.
		 *
		 * @return The source.
		 * @since 1.8
		 */
		public String getSource() {
			return OCR4all.getNotEmpty(source, defaultSource);
		}

		/**
		 * Set the source, this means, the base package name.
		 *
		 * @param source The source to set.
		 * @since 1.8
		 */
		public void setSource(String source) {
			this.source = source;
		}

		/**
		 * Returns the title.
		 *
		 * @return The title.
		 * @since 1.8
		 */
		public String getTitle() {
			return OCR4all.getNotEmpty(title, defaultTile);
		}

		/**
		 * Set the title.
		 *
		 * @param title The title to set.
		 * @since 1.8
		 */
		public void setTitle(String title) {
			this.title = title;
		}

		/**
		 * Returns the description.
		 *
		 * @return The description.
		 * @since 1.8
		 */
		public String getDescription() {
			return OCR4all.getNotEmpty(description, defaultDescription);
		}

		/**
		 * Set the description.
		 *
		 * @param description The description to set.
		 * @since 1.8
		 */
		public void setDescription(String description) {
			this.description = description;
		}

		/**
		 * Returns the version.
		 *
		 * @return The version.
		 * @since 1.8
		 */
		public String getVersion() {
			return OCR4all.getNotEmpty(version, defaultVersion);
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
		 * Returns the contact.
		 *
		 * @return The contact.
		 * @since 1.8
		 */
		public Contact getContact() {
			return contact;
		}

		/**
		 * Set the contact.
		 *
		 * @param contact The contact to set.
		 * @since 1.8
		 */
		public void setContact(Contact contact) {
			this.contact = contact;
		}

		/**
		 * Returns the license.
		 *
		 * @return The license.
		 * @since 1.8
		 */
		public String getLicense() {
			return OCR4all.getNotEmpty(license, defaultLicense);
		}

		/**
		 * Set the license.
		 *
		 * @param license The license to set.
		 * @since 1.8
		 */
		public void setLicense(String license) {
			this.license = license;
		}

		/**
		 * Returns the url.
		 *
		 * @return The url.
		 * @since 1.8
		 */
		public URL getUrl() {
			return url;
		}

		/**
		 * Set the url.
		 *
		 * @param url The url to set.
		 * @since 1.8
		 */
		public void setUrl(URL url) {
			this.url = url;
		}

		/**
		 * Defines contact properties.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class Contact {
			/**
			 * The default name.
			 */
			private static final String defaultName = "ocr4all";

			/**
			 * The default email.
			 */
			private static final String defaultEmail = "ocr4all@uni-wuerzburg.de";

			/**
			 * The name. The default name is 'ocr4all'.
			 */
			private String name = defaultName;

			/**
			 * The email. The default email is
			 * <a href="mailto:ocr4all@uni-wuerzburg.de">ocr4all@uni-wuerzburg.de</a>.
			 */
			private String email = defaultEmail;

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
			 * Returns the email.
			 *
			 * @return The email.
			 * @since 1.8
			 */
			public String getEmail() {
				return OCR4all.getNotEmpty(email, defaultEmail);
			}

			/**
			 * Set the email.
			 *
			 * @param email The email to set.
			 * @since 1.8
			 */
			public void setEmail(String email) {
				this.email = email;
			}

		}

		/**
		 * Defines url properties.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class URL {
			/**
			 * The default terms of service url.
			 */
			private static final String defaultTOS = "https://www.uni-wuerzburg.de/en/sonstiges/imprint-privacy-policy";

			/**
			 * The default contact url.
			 */
			private static final String defaultContact = "http://www.ocr4all.org";

			/**
			 * The default license url.
			 */
			private static final String defaultLicense = "https://opensource.org/licenses/MIT";

			/**
			 * The terms of service url. The default license url is
			 * {@link https://www.uni-wuerzburg.de/en/sonstiges/imprint-privacy-policy}.
			 */
			private String tos = defaultTOS;

			/**
			 * The contact url. The default license url is {@link http://www.ocr4all.org}.
			 */
			private String contact = defaultContact;

			/**
			 * The license url. The default license url is
			 * {@link https://opensource.org/licenses/MIT}.
			 */
			private String license = defaultLicense;

			/**
			 * Returns the terms of service url.
			 *
			 * @return The terms of service url.
			 * @since 1.8
			 */
			public String getTos() {
				return OCR4all.getNotEmpty(tos, defaultTOS);
			}

			/**
			 * Set the terms of service url.
			 *
			 * @param url The url to set.
			 * @since 1.8
			 */
			public void setTos(String url) {
				tos = url;
			}

			/**
			 * Returns the contact url.
			 *
			 * @return The contact url.
			 * @since 1.8
			 */
			public String getContact() {
				return OCR4all.getNotEmpty(contact, defaultContact);
			}

			/**
			 * Set the contact url.
			 *
			 * @param url The url to set.
			 * @since 1.8
			 */
			public void setContact(String url) {
				contact = url;
			}

			/**
			 * Returns the license url.
			 *
			 * @return The license url.
			 * @since 1.8
			 */
			public String getLicense() {
				return OCR4all.getNotEmpty(license, defaultLicense);
			}

			/**
			 * Set the license url.
			 *
			 * @param url The url to set.
			 * @since 1.8
			 */
			public void setLicense(String url) {
				license = url;
			}

		}
	}

}
