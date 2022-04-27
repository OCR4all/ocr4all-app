/**
 * File:     BooleanResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     24.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi;

import java.util.Locale;

import de.uniwuerzburg.zpd.ocr4all.application.spi.model.BooleanField;

/**
 * Defines boolean responses for the api.
 *
 * @author Herbert Baier
 * @version 1.0
 * @since 1.8
 */
public class BooleanResponse extends FieldResponse<Boolean, BooleanField> {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a boolean response for the api.
	 * 
	 * @param locale       The locale.
	 * @param booleanField The boolean field.
	 * @since 1.8
	 */
	public BooleanResponse(Locale locale, BooleanField booleanField) {
		super(locale, Type.bool, booleanField);
	}
}
