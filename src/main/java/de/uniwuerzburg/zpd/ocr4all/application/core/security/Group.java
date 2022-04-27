/**
 * File:     Group.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.security
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     06.11.2020
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.WorkspaceConfiguration;

/**
 * Group is an immutable class that defines groups.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Group extends SecurityEntity<Group> {
	/**
	 * The label.
	 */
	private final String label;

	/**
	 * The state.
	 */
	private final State state;

	/**
	 * The user logins.
	 */
	private final Set<String> users = new HashSet<>();

	/**
	 * The name.
	 */
	private final String name;

	/**
	 * Creates a group.
	 * 
	 * @param version The configuration version.
	 * @param entry   The configuration entry.
	 * @throws IllegalArgumentException Thrown to indicate that the entry is an
	 *                                  inappropriate argument.
	 * @since 1.8
	 */
	public Group(WorkspaceConfiguration.Version version, String entry) throws IllegalArgumentException {
		super();

		final String[] split = (entry + "#").split(configurationSeparator, 4);

		switch (version) {
		case _1_0:
		default:
			if (split.length != 4)
				throw new IllegalArgumentException("expected 4 items, but read " + split.length + ".");

			String label = filter(split[0]);
			if (label == null)
				throw new IllegalArgumentException("the label can not be empty.");

			this.label = label.toLowerCase();
			state = State.getState(split[1]);

			for (String user : split[2].split(elementSeparator)) {
				user = filter(user);
				if (user != null)
					users.add(user.toLowerCase());
			}

			String name = split[3].substring(0, split[3].length() - 1);
			this.name = name.isBlank() ? null : name.trim();

			break;
		}
	}

	/**
	 * Creates a group.
	 * 
	 * @param label The label.
	 * @throws IllegalArgumentException Thrown to indicate that the entry is an
	 *                                  inappropriate argument.
	 * @since 1.8
	 */
	public Group(String label) {
		this(label, null, null, null);
	}

	/**
	 * Creates a group.
	 * 
	 * @param label The label.
	 * @param name  The name.
	 * @param users The users.
	 * @param state The state.
	 * @throws IllegalArgumentException Thrown to indicate that the entry is an
	 *                                  inappropriate argument.
	 * @since 1.8
	 */
	public Group(String label, String name, Collection<String> users, State state) {
		super();

		label = filter(label);
		if (label == null)
			throw new IllegalArgumentException("the label can not be empty.");

		this.label = label.toLowerCase();
		this.state = state == null ? State.defaultState : state;
		this.name = name == null || name.isBlank() ? null : name.trim();

		if (users != null)
			for (String user : users) {
				user = filter(user);
				if (user != null)
					this.users.add(user.toLowerCase());
			}
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
	 * Returns the state.
	 *
	 * @return The state.
	 * @since 1.8
	 */
	public State getState() {
		return state;
	}

	/**
	 * Returns the user logins.
	 *
	 * @return The user logins.
	 * @since 1.8
	 */
	public Set<String> getUsers() {
		return new HashSet<>(users);
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
	public boolean isSame(Group group) {
		return group != null && label.equals(group.getLabel());
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
			StringBuffer buffer = new StringBuffer();
			for (String user : users) {
				if (buffer.length() > 0)
					buffer.append(elementSeparator);
				buffer.append(user);
			}

			return label + configurationSeparator + state.name() + configurationSeparator + buffer.toString()
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
		List<String> logins = new ArrayList<>(users);
		Collections.sort(logins);
		StringBuffer buffer = new StringBuffer();
		for (String login : logins) {
			if (buffer.length() > 0)
				buffer.append(", ");
			buffer.append(login);
		}

		return "label=" + label + ", state=" + state.name() + ", users=[" + buffer.toString() + "], name="
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
			return new String[] { "label:[active|blocked]:[login[" + elementSeparator + " login]*]:[name]" };
		}
	}

}
