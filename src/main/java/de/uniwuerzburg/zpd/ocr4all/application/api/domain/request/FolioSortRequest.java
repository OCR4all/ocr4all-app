/**
 * File:     FolioSortRequest.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.request
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     15.12.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.request;

import jakarta.validation.constraints.NotNull;

/**
 * Defines folio sort requests for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class FolioSortRequest extends IdentifiersRequest {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * True if the folios that do not belong to the order are to be inserted after
	 * the folios that belong to the order. Otherwise, they are placed at the
	 * beginning.
	 */
	@NotNull
	private Boolean isAfter;

	/**
	 * Returns true if the folios that do not belong to the order are to be inserted
	 * after the folios that belong to the order. Otherwise, they are placed at the
	 * beginning.
	 *
	 * @return True if the folios that do not belong to the order are to be inserted
	 *         after the folios that belong to the order. Otherwise, they are placed
	 *         at the beginning.
	 * @since 1.8
	 */
	public Boolean isAfter() {
		return isAfter;
	}

	/**
	 * Set to true if the folios that do not belong to the order are to be inserted
	 * after the folios that belong to the order. Otherwise, they are placed at the
	 * beginning.
	 *
	 * @param isAfter The insert flag to set.
	 * @since 1.8
	 */
	public void setAfter(Boolean isAfter) {
		this.isAfter = isAfter;
	}

}
