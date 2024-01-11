/**
 * File:     EntryResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi
 *
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     23.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi;

import java.io.Serializable;
import java.util.Locale;

import de.uniwuerzburg.zpd.ocr4all.application.spi.model.Entry;

/**
 * Defines entry responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class EntryResponse implements Serializable {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Defines the field types.
	 *
	 * @author Herbert Baier
	 * @version 1.0
	 * @since 1.8
	 */
	protected enum Type {
		/**
		 * The group type.
		 */
		group,
		/**
		 * The bool type.
		 */
		bool("boolean"),
		/**
		 * The integer type.
		 */
		integer,
		/**
		 * The decimal type.
		 */
		decimal,
		/**
		 * The recognition type.
		 */
		recognition,
		/**
		 * The select type.
		 */
		select,
		/**
		 * The string type.
		 */
		string,
		/**
		 * The image type.
		 */
		image;

		/**
		 * The label.
		 */
		private final String label;

		/**
		 * Default constructor for a field type.
		 *
		 * @since 1.8
		 */
		private Type() {
			label = this.name();
		}

		/**
		 * Creates a field type.
		 *
		 * @param label The label.
		 * @since 1.8
		 */
		private Type(String label) {
			this.label = label;
		}

		/**
		 * Returns the label.
		 *
		 * @return The label.
		 * @since 1.8
		 */
		public String getLabel() {
			return label;
		}
	}

	/**
	 * The type.
	 */
	private String type;

	/**
	 * The label.
	 */
	private String label;

	/**
	 * True if the field is disabled.
	 */
	private boolean isDisabled;

	/**
	 * Creates an entry response for the api.
	 *
	 * @param locale The locale.
	 * @param type   The field type.
	 * @param entry  The entry.
	 * @since 1.8
	 */
	public EntryResponse(Locale locale, Type type, Entry entry) {
		super();

		this.type = type.getLabel();

		label = entry.getLabel(locale);
		isDisabled = entry.isDisabled();
	}

	/**
	 * Returns the type.
	 *
	 * @return The type.
	 * @since 1.8
	 */
	public String getType() {
		return type;
	}

	/**
	 * Set the type.
	 *
	 * @param type The type to set.
	 * @since 1.8
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Returns the label.
	 *
	 * @return The label.
	 * @since 1.8
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Set the label.
	 *
	 * @param label The label to set.
	 * @since 1.8
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Returns true if the field is disabled.
	 *
	 * @return True if the field is disabled.
	 * @since 1.8
	 */
	public boolean isDisabled() {
		return isDisabled;
	}

	/**
	 * Set to true if the field is disabled.
	 *
	 * @param isDisabled The disabled flag to set.
	 * @since 1.8
	 */
	public void setDisabled(boolean isDisabled) {
		this.isDisabled = isDisabled;
	}

}
