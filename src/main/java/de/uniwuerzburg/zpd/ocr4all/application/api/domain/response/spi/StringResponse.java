/**
 * File:     StringResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     24.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.spi.model.StringField;

/**
 * Defines string responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class StringResponse extends FieldResponse<String, StringField> {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The content type.
	 */
	@JsonProperty("content-type")
	private String contentType;

	/**
	 * Creates a string response for the api.
	 * 
	 * @param locale      The locale.
	 * @param stringField The string field.
	 * @since 1.8
	 */
	public StringResponse(Locale locale, StringField stringField) {
		super(locale, Type.string, stringField);

		contentType = stringField.getContentType().orElse(null);
	}

	/**
	 * Returns the content type.
	 *
	 * @return The content type.
	 * @since 1.8
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Set the content type.
	 *
	 * @param contentType The content type to set.
	 * @since 1.8
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
