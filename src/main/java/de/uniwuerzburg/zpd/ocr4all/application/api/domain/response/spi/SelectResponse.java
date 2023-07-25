/**
 * File:     SelectResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     24.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.spi.model.SelectField;

/**
 * Defines select responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class SelectResponse extends FieldResponse<Object, SelectField> {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * True if multiple options can be selected at once.
	 */
	@JsonProperty("multiple-options")
	private boolean isMultipleOptions;

	/**
	 * The items.
	 */
	private List<ItemResponse> items;

	/**
	 * Creates a select response for the api.
	 * 
	 * @param locale      The locale.
	 * @param selectField The select field.
	 * @since 1.8
	 */
	public SelectResponse(Locale locale, SelectField selectField) {
		super(locale, Type.select, selectField);

		isMultipleOptions = selectField.isMultipleOptions();

		items = new ArrayList<>();
		for (SelectField.Item item : selectField.getItems())
			if (item instanceof SelectField.Option option)
				items.add(new OptionResponse(locale, option));
			else if (item instanceof SelectField.Association association)
				items.add(new AssociationResponse(locale, association));
			else
				org.slf4j.LoggerFactory.getLogger(SelectResponse.class).error(
						"No responce is implemented for select item " + item.getClass().getCanonicalName() + ".");
	}

	/**
	 * Returns true if multiple options can be selected at once.
	 *
	 * @return True if multiple options can be selected at once.
	 * @since 1.8
	 */
	@JsonGetter("multiple-options")
	public boolean isMultipleOptions() {
		return isMultipleOptions;
	}

	/**
	 * Set to true if multiple options can be selected at once.
	 *
	 * @param isMultipleOptions The multiple options flag to set.
	 * @since 1.8
	 */
	public void setMultipleOptions(boolean isMultipleOptions) {
		this.isMultipleOptions = isMultipleOptions;
	}

	/**
	 * Returns the items.
	 *
	 * @return The items.
	 * @since 1.8
	 */
	public List<ItemResponse> getItems() {
		return items;
	}

	/**
	 * Set the items.
	 *
	 * @param items The items to set.
	 * @since 1.8
	 */
	public void setItems(List<ItemResponse> items) {
		this.items = items;
	}

	/**
	 * Defines item responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ItemResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Defines the item types.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		protected enum Type {
			association, option
		}

		/**
		 * The type.
		 */
		private Type type;

		/**
		 * The description. Null if not defined.
		 */
		private String description;

		/**
		 * Creates an item response for the api.
		 * 
		 * @param locale The locale.
		 * @param type   The type.
		 * @param item   The item.
		 * @since 1.8
		 */
		public ItemResponse(Locale locale, Type type, SelectField.Item item) {
			super();

			this.type = type;
			description = item.getDescription(locale).orElse(null);
		}

		/**
		 * Returns the type.
		 *
		 * @return The type.
		 * @since 1.8
		 */
		public Type getType() {
			return type;
		}

		/**
		 * Set the type.
		 *
		 * @param type The type to set.
		 * @since 1.8
		 */
		public void setType(Type type) {
			this.type = type;
		}

		/**
		 * Returns the description.
		 *
		 * @return The description.
		 * @since 1.8
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * Set the description.
		 *
		 * @param description The description to set.
		 * @since 1.8
		 */
		public void setDescription(String description) {
			this.description = description;
		}

	}

	/**
	 * Defines association responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class AssociationResponse extends ItemResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The options.
		 */
		private List<OptionResponse> options;

		/**
		 * Creates an association response for the api.
		 * 
		 * @param locale      The locale.
		 * @param association The association.
		 * @since 1.8
		 */
		public AssociationResponse(Locale locale, SelectField.Association association) {
			super(locale, Type.association, association);

			options = new ArrayList<>();
			for (SelectField.Option option : association.getOptions())
				options.add(new OptionResponse(locale, option));
		}

		/**
		 * Returns the options.
		 *
		 * @return The options.
		 * @since 1.8
		 */
		public List<OptionResponse> getOptions() {
			return options;
		}

		/**
		 * Set the options.
		 *
		 * @param options The options to set.
		 * @since 1.8
		 */
		public void setOptions(List<OptionResponse> options) {
			this.options = options;
		}

	}

	/**
	 * Defines option responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class OptionResponse extends ItemResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * True if selected.
		 */
		private boolean isSelected;

		/**
		 * The value.
		 */
		private String value;

		/**
		 * True if disabled.
		 */
		private boolean isDisabled;

		/**
		 * Creates an option response for the api.
		 * 
		 * @param locale The locale.
		 * @param option The option.
		 * @since 1.8
		 */
		public OptionResponse(Locale locale, SelectField.Option option) {
			super(locale, Type.option, option);

			isSelected = option.isSelected();
			value = option.getValue();
			isDisabled = option.isDisabled();
		}

		/**
		 * Returns true if selected.
		 *
		 * @return True if selected.
		 * @since 1.8
		 */
		public boolean isSelected() {
			return isSelected;
		}

		/**
		 * Set to true if selected.
		 *
		 * @param isSelected The selected flag to set.
		 * @since 1.8
		 */
		public void setSelected(boolean isSelected) {
			this.isSelected = isSelected;
		}

		/**
		 * Returns the value.
		 *
		 * @return The value.
		 * @since 1.8
		 */
		public String getValue() {
			return value;
		}

		/**
		 * Set the value.
		 *
		 * @param value The value to set.
		 * @since 1.8
		 */
		public void setValue(String value) {
			this.value = value;
		}

		/**
		 * Returns true if disabled.
		 *
		 * @return True if disabled.
		 * @since 1.8
		 */
		public boolean isDisabled() {
			return isDisabled;
		}

		/**
		 * Set to true if disabled.
		 *
		 * @param isDisabled The disabled flag to set.
		 * @since 1.8
		 */
		public void setDisabled(boolean isDisabled) {
			this.isDisabled = isDisabled;
		}

	}

}
