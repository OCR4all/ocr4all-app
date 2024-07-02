/**
 * File:     RecognitionModelResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     24.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.spi.model.RecognitionModelField;

/**
 * Defines recognition model responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class RecognitionModelResponse extends FieldResponse<String, RecognitionModelField> {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The model type.
	 */
	@JsonProperty("model-type")
	private RecognitionModelField.Type modelType;

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
	 * True if multiple models can be selected.
	 */
	@JsonProperty("multiple-models")
	private boolean isMultipleModels;

	/**
	 * Creates a recognition model response for the api.
	 * 
	 * @param locale                The locale.
	 * @param recognitionModelField The recognition model field.
	 * @since 1.8
	 */
	public RecognitionModelResponse(Locale locale, RecognitionModelField recognitionModelField) {
		super(locale, Type.recognition, recognitionModelField);

		modelType = recognitionModelField.getType();
		minimumVersion = recognitionModelField.getMinimumVersion().orElse(null);
		maximumVersion = recognitionModelField.getMaximumVersion().orElse(null);
		isMultipleModels = recognitionModelField.isMultipleModels();
	}

	/**
	 * Returns the model type.
	 *
	 * @return The model type.
	 * @since 17
	 */
	public RecognitionModelField.Type getModelType() {
		return modelType;
	}

	/**
	 * Set the model type.
	 *
	 * @param modelType The model type flag to set.
	 * @since 17
	 */
	public void setModelType(RecognitionModelField.Type modelType) {
		this.modelType = modelType;
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
	 * Returns true if multiple models can be selected.
	 *
	 * @return True if multiple models can be selected.
	 * @since 1.8
	 */
	@JsonGetter("multiple-models")
	public boolean isMultipleModels() {
		return isMultipleModels;
	}

	/**
	 * Set to true if multiple models can be selected.
	 *
	 * @param isMultipleModels The multiple models flag to set.
	 * @since 1.8
	 */
	public void setMultipleModels(boolean isMultipleModels) {
		this.isMultipleModels = isMultipleModels;
	}

}
