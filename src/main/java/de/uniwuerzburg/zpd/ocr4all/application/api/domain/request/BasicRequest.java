/**
 * File:     BasicRequest.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.request
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     08.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.request;

import java.util.Set;

/**
 * Defines basic requests for the api. It includes identification, name, state,
 * description and keywords.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @since 1.8
 */
public class BasicRequest extends NameStateRequest {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The description.
	 */
	private String description = null;

	/**
	 * The keywords.
	 */
	private Set<String> keywords = null;

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
	 * Set the description.
	 *
	 * @param description The description to set.
	 * @since 1.8
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the keywords.
	 *
	 * @return The keywords.
	 * @since 1.8
	 */
	public Set<String> getKeywords() {
		return keywords;
	}

	/**
	 * Set the keywords.
	 *
	 * @param keywords The keywords to set.
	 * @since 1.8
	 */
	public void setKeywords(Set<String> keywords) {
		this.keywords = keywords;
	}

}
