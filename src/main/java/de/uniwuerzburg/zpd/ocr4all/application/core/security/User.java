/**
 * File:     User.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.security
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     03.11.2020
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.security;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.WorkspaceConfiguration;

/**
 * User is an immutable class that defines user accounts.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class User extends SecurityEntity<User> {
	/**
	 * The login.
	 */
	private final String login;

	/**
	 * The state.
	 */
	private final State state;

	/**
	 * The email.
	 */
	private final String email;

	/**
	 * The name.
	 */
	private final String name;

	/**
	 * Creates a user account.
	 * 
	 * @param version The configuration version.
	 * @param entry   The configuration entry.
	 * @throws IllegalArgumentException Thrown to indicate that the entry is an
	 *                                  inappropriate argument.
	 * @since 1.8
	 */
	public User(WorkspaceConfiguration.Version version, String entry) throws IllegalArgumentException {
		super();

		final String[] split = (entry + "#").split(configurationSeparator, 4);

		switch (version) {
		case _1_0:
		default:
			if (split.length != 4)
				throw new IllegalArgumentException("expected 4 items, but read " + split.length + ".");

			String login = filter(split[0]);
			if (login == null)
				throw new IllegalArgumentException("the login can not be empty.");

			this.login = login.toLowerCase();
			state = State.getState(split[1]);

			email = filter(split[2]);

			String name = split[3].substring(0, split[3].length() - 1);
			this.name = name.isBlank() ? null : name.trim();

			break;
		}
	}

	/**
	 * Creates a user account.
	 * 
	 * @param login The login.
	 * @throws IllegalArgumentException Thrown to indicate that the entry is an
	 *                                  inappropriate argument.
	 * @since 1.8
	 */
	public User(String login) throws IllegalArgumentException {
		this(login, null, null, null);
	}

	/**
	 * Creates a user account.
	 * 
	 * @param login The login.
	 * @param name  The name.
	 * @param email The email.
	 * @param state The state.
	 * @throws IllegalArgumentException Thrown to indicate that the entry is an
	 *                                  inappropriate argument.
	 * @since 1.8
	 */
	public User(String login, String name, String email, State state) throws IllegalArgumentException {
		super();

		login = filter(login);

		if (login == null)
			throw new IllegalArgumentException("the login can not be empty.");

		this.login = login.toLowerCase();
		this.state = state == null ? State.defaultState : state;
		this.email = filter(email);
		this.name = name == null || name.isBlank() ? null : name.trim();
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
	 * Returns the state.
	 *
	 * @return The state.
	 * @since 1.8
	 */
	public State getState() {
		return state;
	}

	/**
	 * Returns true if the email is set.
	 *
	 * @return True if the email is set.
	 * @since 1.8
	 */
	public boolean isEmailSet() {
		return email != null;
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

	/**
	 * Returns true if the name is set.
	 *
	 * @return True if the name is set.
	 * @since 1.8
	 */
	public boolean isNameSet() {
		return name != null;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityEntity#isSame(
	 * java.lang.Object)
	 */
	@Override
	public boolean isSame(User user) {
		return user != null && login.equals(user.getLogin());
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
		switch (version) {
		case _1_0:
		default:
			return login + configurationSeparator + state.name() + configurationSeparator + (isEmailSet() ? email : "")
					+ configurationSeparator + (isNameSet() ? name : "");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "login=" + login + ", state=" + state.name() + ", email=" + (isEmailSet() ? email : "") + ", name="
				+ (isNameSet() ? name : "");
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
			return new String[] { "login" + configurationSeparator + "[active|blocked]" + configurationSeparator
					+ "[email]" + configurationSeparator + "[name]" };
		}
	}

}
