/**
 * File:     DecimalResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     24.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi;

import java.util.Locale;

import de.uniwuerzburg.zpd.ocr4all.application.spi.model.DecimalField;

/**
 * Defines decimal responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class DecimalResponse extends NumberResponse<Float, DecimalField> {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a decimal response for the api.
	 * 
	 * @param locale       The locale.
	 * @param decimalField The decimal field.
	 * @since 1.8
	 */
	public DecimalResponse(Locale locale, DecimalField decimalField) {
		super(locale, Type.decimal, decimalField);
	}
}
