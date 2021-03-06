/**
 * File:     IntegerResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     24.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi;

import java.util.Locale;

import de.uniwuerzburg.zpd.ocr4all.application.spi.model.IntegerField;

/**
 * Defines integer responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class IntegerResponse extends NumberResponse<Integer, IntegerField> {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates an integer response for the api.
	 * 
	 * @param locale       The locale.
	 * @param integerField The integer field.
	 * @since 1.8
	 */
	public IntegerResponse(Locale locale, IntegerField integerField) {
		super(locale, Type.integer, integerField);
	}
}
