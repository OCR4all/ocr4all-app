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
	 * The warning. Null if no warning is required.
	 */
	private String warning;

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

		warning = field.getWarning(locale).orElse(null);
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
	 * Returns the warning.
	 *
	 * @return The warning. Null if no warning is required.
	 * @since 1.8
	 */
	public String getWarning() {
		return warning;
	}

	/**
	 * Set the warning. Null if no warning is required.
	 *
	 * @param warning The warning to set.
	 * @since 1.8
	 */
	public void setWarning(String warning) {
		this.warning = warning;
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
