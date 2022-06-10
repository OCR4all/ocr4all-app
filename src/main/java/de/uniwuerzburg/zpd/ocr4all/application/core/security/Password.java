/**
 * File:     Password.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.security
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     05.11.2020
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.security;

import org.springframework.security.crypto.password.PasswordEncoder;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.WorkspaceConfiguration;

/**
 * Defines passwords.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Password extends SecurityEntity<Password> {
	/**
	 * The login.
	 */
	private final String login;

	/**
	 * The hash.
	 */
	private final String hash;

	/**
	 * Creates a password.
	 * 
	 * @param version The configuration version.
	 * @param entry   The configuration entry.
	 * @throws IllegalArgumentException Thrown to indicate that the entry is an
	 *                                  inappropriate argument.
	 * @since 1.8
	 */
	public Password(WorkspaceConfiguration.Version version, String entry) throws IllegalArgumentException {
		super();

		final String[] split = entry.split(configurationSeparator, 2);

		switch (version) {
		case _1_0:
		default:
			if (split.length != 2)
				throw new IllegalArgumentException("expected 2 items, but read " + split.length + ".");

			String login = filter(split[0]);
			if (login == null)
				throw new IllegalArgumentException("the login can not be empty.");

			this.login = login.toLowerCase();

			hash = split[1];
			break;
		}
	}

	/**
	 * Creates an empty password.
	 * 
	 * @param login The login.
	 * @throws IllegalArgumentException Thrown if the login is null or empty.
	 * @since 1.8
	 */
	public Password(String login) throws IllegalArgumentException {
		this(login, null);
	}

	/**
	 * Creates a password.
	 * 
	 * @param login The login.
	 * @param hash  The hash.
	 * @throws IllegalArgumentException Thrown if the login is null or empty..
	 * @since 1.8
	 */
	public Password(String login, String hash) throws IllegalArgumentException {
		super();

		login = filter(login);

		if (login == null)
			throw new IllegalArgumentException("the login can not be empty.");

		this.login = login.toLowerCase();
		this.hash = hash == null ? "" : hash;
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
	 * Returns the password hash.
	 *
	 * @return The password hash.
	 * @since 1.8
	 */
	public String getHash() {
		return hash;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityEntity#isSame(
	 * java.lang.Object)
	 */
	@Override
	public boolean isSame(Password password) {
		return password != null && login.equals(password.getLogin());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityEntity#
	 * getConfigurationEntry(de.uniwuerzburg.zpd.ocr4all.application.core.
	 * configuration.WorkspaceConfiguration.Version)
	 */
	@Override
	public String getConfigurationEntry(WorkspaceConfiguration.Version version) {
		return getConfigurationEntry(version, null);
	}

	/**
	 * Returns the configuration entry.
	 * 
	 * @param version The configuration entry.
	 * @param encoder If non null, then encodes the hash using this password
	 *                encoder.
	 * @return The configuration entry.
	 * @since 1.8
	 */
	public String getConfigurationEntry(WorkspaceConfiguration.Version version, PasswordEncoder encoder) {
		switch (version) {
		case _1_0:
		default:
			return login + configurationSeparator + (encoder == null ? hash : encoder.encode(hash));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "login=" + login + ", hash=" + hash;
	}

	/**
	 * Returns the parse syntax for given configuration version.
	 * 
	 * @param version The configuration version.
	 * @return The parse syntax.
	 * @since 1.8
	 */
	public static String[] getParseSyntax(WorkspaceConfiguration.Version version) {
		switch (version) {
		case _1_0:
		default:
			return new String[] { "login" + configurationSeparator + "{algorithm}hash" };
		}
	}

}
