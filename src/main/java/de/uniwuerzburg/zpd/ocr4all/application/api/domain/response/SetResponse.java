/**
 * File:     SetResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     10.06.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response;

/**
 * Defines set responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public class SetResponse extends de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a set response for the api.
	 * 
	 * @param set The set.
	 * @since 1.8
	 */
	public SetResponse(de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set) {
		super(set.getDate(), set.getUser(), set.getKeywords(), set.getId(), set.getName());
	}

}
