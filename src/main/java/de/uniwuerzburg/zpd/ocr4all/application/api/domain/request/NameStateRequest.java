/**
 * File:     NameStateRequest.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.request
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     08.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.request;

/**
 * Defines requests with identification, name and state for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class NameStateRequest extends IdentifierRequest {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The name.
	 */
	private String name = null;

	/**
	 * The state.
	 */
	private String state = null;

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
	 * Set the name.
	 *
	 * @param name The name to set.
	 * @since 1.8
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the state.
	 *
	 * @return The state.
	 * @since 1.8
	 */
	public String getState() {
		return state;
	}

	/**
	 * Set the state.
	 *
	 * @param state The state to set.
	 * @since 1.8
	 */
	public void setState(String state) {
		this.state = state;
	}

}
