/**
 * File:     WeightResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     24.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.spi.model.WeightField;

/**
 * Defines weight responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class WeightResponse extends FieldResponse<String, WeightField> {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The weight type.
	 */
	@JsonProperty("weight-type")
	private WeightField.Type weightType;

	/**
	 * /** The minimum version. Null if not set.
	 */
	@JsonProperty("minimum-version")
	private String minimumVersion;

	/**
	 * The maximum version. Null if not set.
	 */
	@JsonProperty("maximum-version")
	private String maximumVersion;

	/**
	 * True if multiple weights can be selected.
	 */
	@JsonProperty("multiple-select")
	private boolean isMultipleSelect;

	/**
	 * The suffix for the model file names.
	 */
	private String suffix;

	/**
	 * Creates a weight response for the api.
	 * 
	 * @param locale      The locale.
	 * @param weightField The weight field.
	 * @since 1.8
	 */
	public WeightResponse(Locale locale, WeightField weightField) {
		super(locale, Type.weight, weightField);

		weightType = weightField.getType();
		minimumVersion = weightField.getMinimumVersion().orElse(null);
		maximumVersion = weightField.getMaximumVersion().orElse(null);
		isMultipleSelect = weightField.isMultipleSelect();
		suffix = weightField.getSuffix();
	}

	/**
	 * Returns the weight type.
	 *
	 * @return The weight type.
	 * @since 17
	 */
	public WeightField.Type getWeightType() {
		return weightType;
	}

	/**
	 * Set the weight type.
	 *
	 * @param weightType The weight type to set.
	 * @since 17
	 */
	public void setWeightType(WeightField.Type weightType) {
		this.weightType = weightType;
	}

	/**
	 * Returns the minimum version. Null if not set.
	 *
	 * @return The minimum version.
	 * @since 17
	 */
	public String getMinimumVersion() {
		return minimumVersion;
	}

	/**
	 * Set the minimum version. Null if not set.
	 *
	 * @param minimumVersion The minimum version flag to set.
	 * @since 17
	 */
	public void setMinimumVersion(String minimumVersion) {
		this.minimumVersion = minimumVersion;
	}

	/**
	 * Returns the maximum version. Null if not set.
	 *
	 * @return The maximum version.
	 * @since 17
	 */
	public String getMaximumVersion() {
		return maximumVersion;
	}

	/**
	 * Set the maximum version. Null if not set.
	 *
	 * @param maximumVersion The maximum version flag to set.
	 * @since 17
	 */
	public void setMaximumVersion(String maximumVersion) {
		this.maximumVersion = maximumVersion;
	}

	/**
	 * Returns true if multiple weights can be selected.
	 *
	 * @return True if multiple weights can be selected.
	 * @since 17
	 */
	@JsonGetter("multiple-select")
	public boolean isMultipleSelect() {
		return isMultipleSelect;
	}

	/**
	 * Set to true if multiple weights can be selected.
	 *
	 * @param isMultipleSelect The select flag to set.
	 * @since 17
	 */
	public void setMultipleSelect(boolean isMultipleSelect) {
		this.isMultipleSelect = isMultipleSelect;
	}

	/**
	 * Returns the suffix for the model file names.
	 *
	 * @return The suffix for the model file names.
	 * @since 17
	 */
	public String getSuffix() {
		return suffix;
	}

	/**
	 * Set the suffix for the model file names.
	 *
	 * @param suffix The suffix to set.
	 * @since 17
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

}
