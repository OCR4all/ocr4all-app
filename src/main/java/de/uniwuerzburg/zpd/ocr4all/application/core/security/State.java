/**
 * File:     State.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.security
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     06.11.2020
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.security;

/**
 * Define states.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public enum State {
	/**
	 * The active state.
	 */
	active,
	/**
	 * The blocked state.
	 */
	blocked;

	/**
	 * The default state.
	 */
	public static final State defaultState = active;

	/**
	 * Returns the respective state of the state name. If the name is null or empty,
	 * then returns the default state, this means, active. Otherwise, if the name
	 * does not matches a state, returns blocked.
	 * 
	 * @param state The state name.
	 * @return The state.
	 * @since 1.8
	 */
	public static State getState(String state) {
		if (state == null || state.trim().isEmpty())
			return defaultState;
		else
			try {
				return State.valueOf(state.trim().toLowerCase());
			} catch (Exception e) {
				return blocked;
			}
	}
}
