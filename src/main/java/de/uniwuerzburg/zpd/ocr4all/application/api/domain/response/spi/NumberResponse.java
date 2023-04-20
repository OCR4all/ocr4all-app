/**
 * File:     NumberResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     23.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi;

import java.util.Locale;

import de.uniwuerzburg.zpd.ocr4all.application.spi.model.NumberField;

/**
 * Defines number responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @param <T> The value type.
 * @param <N> The number field type.
 * @since 1.8
 */
public class NumberResponse<T extends Number, N extends NumberField<T>> extends FieldResponse<T, N> {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The legal number intervals. Null if not defined.
	 */
	private T step;

	/**
	 * The minimum value. Null if not defined.
	 */
	private T minimum;

	/**
	 * The maximum value. Null if not defined.
	 */
	private T maximum;

	/**
	 * The unit. Null if no unit is available.
	 */
	private String unit;

	/**
	 * Creates a number response for the api.
	 * 
	 * @param locale The locale.
	 * @param type   The field type.
	 * @param number The field.
	 * @since 1.8
	 */
	public NumberResponse(Locale locale, Type type, N number) {
		super(locale, type, number);

		step = number.getStep().orElse(null);

		minimum = number.getMinimum().orElse(null);
		maximum = number.getMaximum().orElse(null);

		String unit = number.getUnit(locale).orElse(null);
		this.unit = unit == null || unit.isBlank() ? number.getUnit(null).orElse(null) : unit;
	}

	/**
	 * Returns the legal number intervals.
	 *
	 * @return The legal number intervals. Null if not defined.
	 * @since 1.8
	 */
	public T getStep() {
		return step;
	}

	/**
	 * Set the legal number intervals. Null if not defined.
	 *
	 * @param step The legal number intervals to set.
	 * @since 1.8
	 */
	public void setStep(T step) {
		this.step = step;
	}

	/**
	 * Returns the minimum value.
	 *
	 * @return The minimum value. Null if not defined.
	 * @since 1.8
	 */
	public T getMinimum() {
		return minimum;
	}

	/**
	 * Set the minimum value. Null if not defined.
	 *
	 * @param minimum The minimum value to set.
	 * @since 1.8
	 */
	public void setMinimum(T minimum) {
		this.minimum = minimum;
	}

	/**
	 * Returns the maximum value.
	 *
	 * @return The maximum value. Null if not defined.
	 * @since 1.8
	 */
	public T getMaximum() {
		return maximum;
	}

	/**
	 * Set the value. Null if not defined.
	 *
	 * @param maximum The maximum value to set.
	 * @since 1.8
	 */
	public void setMaximum(T maximum) {
		this.maximum = maximum;
	}

	/**
	 * Returns the unit.
	 *
	 * @return The unit. Null if no unit is available.
	 * @since 1.8
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * Set the unit. Null if no unit is available.
	 *
	 * @param unit The unit to set.
	 * @since 1.8
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}

}
