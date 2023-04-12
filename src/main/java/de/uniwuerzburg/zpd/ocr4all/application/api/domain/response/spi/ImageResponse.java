/**
 * File:     ImageResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     24.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi;

import java.util.Locale;

import de.uniwuerzburg.zpd.ocr4all.application.spi.model.ImageField;

/**
 * Defines image responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class ImageResponse extends FieldResponse<Object, ImageField> {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * True if it is a sandbox image.
	 */
	private boolean isSandbox;

	/**
	 * True if hide check box.
	 */
	private boolean isHideCheckbox;

	/**
	 * True if zoom the thumbnails.
	 */
	private boolean isZoom;

	/**
	 * True if select by image type.
	 */
	private boolean isSelectType;

	/**
	 * True if select by image keyword.
	 */
	private boolean isSelectKeyword;

	/**
	 * True if select by image altogether.
	 */
	private boolean isSelectAltogether;

	/**
	 * Creates an image response for the api.
	 * 
	 * @param locale     The locale.
	 * @param imageField The image field.
	 * @since 1.8
	 */
	public ImageResponse(Locale locale, ImageField imageField) {
		super(locale, Type.image, imageField);

		isSandbox = imageField.isSandbox();
		isHideCheckbox = imageField.isHideCheckbox();
		isZoom = imageField.isZoom();
		isSelectType = imageField.isSelectType();
		isSelectKeyword = imageField.isSelectKeyword();
		isSelectAltogether = imageField.isSelectAltogether();
	}

	/**
	 * Returns true if it is a sandbox image.
	 *
	 * @return True if it is a sandbox image.
	 * @since 1.8
	 */
	public boolean isSandbox() {
		return isSandbox;
	}

	/**
	 * Set to true if it is a sandbox image.
	 *
	 * @param isSandbox The sandbox image flag to set.
	 * @since 1.8
	 */
	public void setSandbox(boolean isSandbox) {
		this.isSandbox = isSandbox;
	}

	/**
	 * Returns true if hide check box.
	 *
	 * @return True if hide check box.
	 * @since 1.8
	 */
	public boolean isHideCheckbox() {
		return isHideCheckbox;
	}

	/**
	 * Set to true if hide check box.
	 *
	 * @param isHideCheckbox The hide check box flag to set.
	 * @since 1.8
	 */
	public void setHideCheckbox(boolean isHideCheckbox) {
		this.isHideCheckbox = isHideCheckbox;
	}

	/**
	 * Returns true if zoom the thumbnails.
	 *
	 * @return True if zoom the thumbnails.
	 * @since 1.8
	 */
	public boolean isZoom() {
		return isZoom;
	}

	/**
	 * Set to true if zoom the thumbnails.
	 *
	 * @param isZoom The isZoom to set.
	 * @since 1.8
	 */
	public void setZoom(boolean isZoom) {
		this.isZoom = isZoom;
	}

	/**
	 * Returns true if select by image type.
	 *
	 * @return True if select by image type.
	 * @since 1.8
	 */
	public boolean isSelectType() {
		return isSelectType;
	}

	/**
	 * Set to true if select by image type.
	 *
	 * @param isSelectType The select type flag to set.
	 * @since 1.8
	 */
	public void setSelectType(boolean isSelectType) {
		this.isSelectType = isSelectType;
	}

	/**
	 * Returns true if select by image keyword.
	 *
	 * @return True if select by image keyword.
	 * @since 1.8
	 */
	public boolean isSelectKeyword() {
		return isSelectKeyword;
	}

	/**
	 * Set to true if select by image keyword.
	 *
	 * @param isSelectKeyword The select keyword flag to set.
	 * @since 1.8
	 */
	public void setSelectKeyword(boolean isSelectKeyword) {
		this.isSelectKeyword = isSelectKeyword;
	}

	/**
	 * Returns true if select by image altogether.
	 *
	 * @return True if select by image altogether.
	 * @since 1.8
	 */
	public boolean isSelectAltogether() {
		return isSelectAltogether;
	}

	/**
	 * Set to true if select by image altogether.
	 *
	 * @param isSelectAltogether The select altogether to set.
	 * @since 1.8
	 */
	public void setSelectAltogether(boolean isSelectAltogether) {
		this.isSelectAltogether = isSelectAltogether;
	}

}
