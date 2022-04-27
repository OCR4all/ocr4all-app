/**
 * File:     RecognitionModelResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     24.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi;

import java.util.Locale;

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
	 * True if the application recognition models are required.
	 */
	private boolean isApplicationModels;

	/**
	 * True if the recognition models of the selected project are required.
	 */
	private boolean isProjectModels;

	/**
	 * True if the recognition models of the remainder projects are required.
	 */
	private boolean isRemainderProjectsModels;

	/**
	 * True if multiple models can be selected.
	 */
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

		isApplicationModels = recognitionModelField.isApplicationModels();
		isProjectModels = recognitionModelField.isProjectModels();
		isRemainderProjectsModels = recognitionModelField.isRemainderProjectsModels();
		isMultipleModels = recognitionModelField.isMultipleModels();
	}

	/**
	 * Returns true if the application recognition models are required.
	 *
	 * @return True if the application recognition models are required.
	 * @since 1.8
	 */
	public boolean isApplicationModels() {
		return isApplicationModels;
	}

	/**
	 * Set to true if the application recognition models are required.
	 *
	 * @param isApplicationModels The recognition models flag to set.
	 * @since 1.8
	 */
	public void setApplicationModels(boolean isApplicationModels) {
		this.isApplicationModels = isApplicationModels;
	}

	/**
	 * Returns true if the recognition models of the selected project are required.
	 *
	 * @return True if the recognition models of the selected project are required.
	 * @since 1.8
	 */
	public boolean isProjectModels() {
		return isProjectModels;
	}

	/**
	 * Set to true if the recognition models of the selected project are required.
	 *
	 * @param isProjectModels The selected project model flag to set.
	 * @since 1.8
	 */
	public void setProjectModels(boolean isProjectModels) {
		this.isProjectModels = isProjectModels;
	}

	/**
	 * Returns true if the recognition models of the remainder projects are
	 * required.
	 *
	 * @return True if the recognition models of the remainder projects are
	 *         required.
	 * @since 1.8
	 */
	public boolean isRemainderProjectsModels() {
		return isRemainderProjectsModels;
	}

	/**
	 * Set to true if the recognition models of the remainder projects are required.
	 *
	 * @param isRemainderProjectsModels The remainder projects flag to set.
	 * @since 1.8
	 */
	public void setRemainderProjectsModels(boolean isRemainderProjectsModels) {
		this.isRemainderProjectsModels = isRemainderProjectsModels;
	}

	/**
	 * Returns true if multiple models can be selected.
	 *
	 * @return True if multiple models can be selected.
	 * @since 1.8
	 */
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
