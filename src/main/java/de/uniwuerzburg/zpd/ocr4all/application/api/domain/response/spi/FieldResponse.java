/**
 * File:     FieldResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     23.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi;

import java.util.Locale;

import de.uniwuerzburg.zpd.ocr4all.application.spi.model.Field;

/**
 * Defines field responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @param <T> The value type.
 * @param <F> The field type.
 * @since 1.8
 */
public class FieldResponse<T, F extends Field<T>> extends EntryResponse {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The argument.
	 */
	private String argument;

	/**
	 * The default value. Null if not required.
	 */
	private T value;

	/**
	 * The description. Null if no description is required.
	 */
	private String description;

	/**
	 * The placeholder. Null if no placeholder is required.
	 */
	private String placeholder;

	/**
	 * Creates a field response for the api.
	 * 
	 * @param locale The locale.
	 * @param type   The field type.
	 * @param field  The field.
	 * @since 1.8
	 */
	public FieldResponse(Locale locale, Type type, F field) {
		super(locale, type, field);

		argument = field.getArgument();
		value = field.getValue().orElse(null);

		description = field.getDescription(locale).orElse(null);
		placeholder = field.getPlaceholder(locale).orElse(null);
	}

	/**
	 * Returns the argument.
	 *
	 * @return The argument.
	 * @since 1.8
	 */
	public String getArgument() {
		return argument;
	}

	/**
	 * Set the argument.
	 *
	 * @param argument The argument to set.
	 * @since 1.8
	 */
	public void setArgument(String argument) {
		this.argument = argument;
	}

	/**
	 * Returns the default value.
	 *
	 * @return The default value. Null if not required.
	 * @since 1.8
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Set the default value. Null if not required.
	 *
	 * @param value The value to set.
	 * @since 1.8
	 */
	public void setValue(T value) {
		this.value = value;
	}

	/**
	 * Returns the description.
	 *
	 * @return The description. Null if no description is required.
	 * @since 1.8
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description. Null if no description is required.
	 *
	 * @param description The description to set.
	 * @since 1.8
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the placeholder.
	 *
	 * @return The placeholder. Null if no placeholder is required.
	 * @since 1.8
	 */
	public String getPlaceholder() {
		return placeholder;
	}

	/**
	 * Set the placeholder. Null if no placeholder is required.
	 *
	 * @param placeholder The placeholder to set.
	 * @since 1.8
	 */
	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

}
