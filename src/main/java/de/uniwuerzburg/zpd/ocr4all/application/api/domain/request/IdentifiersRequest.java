/**
 * File:     IdentifiersRequest.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.persistence.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     18.12.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.request;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.constraints.NotNull;

/**
 * Defines requests with identifiers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class IdentifiersRequest implements Serializable {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The identifiers.
	 */
	@NotNull
	private List<String> ids = null;

	/**
	 * Returns the identifiers.
	 *
	 * @return The identifiers.
	 * @since 1.8
	 */
	public List<String> getIds() {
		return ids;
	}

	/**
	 * Set the identifiers.
	 *
	 * @param ids The identifiers to set.
	 * @since 1.8
	 */
	public void setIds(List<String> ids) {
		this.ids = ids;
	}

}
