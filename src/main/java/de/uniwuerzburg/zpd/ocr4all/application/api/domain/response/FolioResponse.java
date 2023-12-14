/**
 * File:     FolioResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     14.12.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response;

import de.uniwuerzburg.zpd.ocr4all.application.persistence.folio.Folio;

/**
 * Defines folio responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class FolioResponse extends Folio {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a folio response for the api.
	 * 
	 * @param folio The folio.
	 * @since 1.8
	 */
	public FolioResponse(Folio folio) {
		super(folio.getDate(), folio.getUser(), folio.getKeywords(), folio.getId(), folio.getName(), folio.getFormat(),
				folio.getSize(), folio.getDerivatives());
	}

}
