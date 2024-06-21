/**
 * File:     ServiceProviderResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     23.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.uniwuerzburg.zpd.ocr4all.application.api.worker.spi.CoreServiceProviderApiController;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.ServiceProviderException;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Premise;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Target;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.BooleanField;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.DecimalField;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.Entry;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.Group;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.ImageField;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.IntegerField;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.RecognitionModelField;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.SelectField;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.StringField;

/**
 * Defines service provider responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class ServiceProviderResponse implements Serializable {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The type.
	 */
	private CoreServiceProviderApiController.Type type;

	/**
	 * The id.
	 */
	private String id;

	/**
	 * The provider.
	 */
	private String provider;

	/**
	 * The language.
	 */
	private String language;

	/**
	 * The name.
	 */
	private String name;

	/**
	 * The version.
	 */
	private float version;

	/**
	 * The description.
	 */
	private String description;

	/**
	 * The categories.
	 */
	private List<String> categories;

	/**
	 * The steps.
	 */
	private List<String> steps;

	/**
	 * The icon.
	 */
	private String icon;

	/**
	 * The index.
	 */
	private int index;

	/**
	 * The premise.
	 */
	private PremiseResponse premise;

	/**
	 * The entries.
	 */
	private List<EntryResponse> entries;

	/**
	 * Creates a service provider response for the api.
	 * 
	 * @param locale          The locale.
	 * @param type            The type.
	 * @param id              The id.
	 * @param serviceProvider The service provider.
	 * @param target          The target for the service provider.
	 * @throws ServiceProviderException Throws on service provider exceptions.
	 * @since 1.8
	 */
	public ServiceProviderResponse(Locale locale, CoreServiceProviderApiController.Type type, String id,
			ServiceProvider serviceProvider, Target target) throws ServiceProviderException {
		super();

		try {
			language = locale.getLanguage();

			this.type = type;
			this.id = id;
			provider = serviceProvider.getProvider();

			String name = serviceProvider.getName(locale);
			this.name = name == null || name.isBlank() ? serviceProvider.getName(null) : name;

			version = serviceProvider.getVersion();

			description = serviceProvider.getDescription(locale).orElse(null);
			categories = serviceProvider.getCategories();
			steps = serviceProvider.getSteps();
			icon = serviceProvider.getIcon().orElse(null);
			index = serviceProvider.getIndex();

			premise = new PremiseResponse(serviceProvider.getPremise(target), locale);

			entries = getEntryResponses(locale, serviceProvider.getModel(target).getEntries());
		} catch (Exception e) {
			throw new ServiceProviderException(e);
		}

	}

	/**
	 * Returns the entry responses for the api for given entries.
	 * 
	 * @param locale  The locale.
	 * @param entries The entries.
	 * @return The entry responses for the api for given entries.
	 * @since 1.8
	 */
	public static List<EntryResponse> getEntryResponses(Locale locale, List<Entry> entries) {
		List<EntryResponse> list = new ArrayList<>();

		for (Entry entry : entries) {
			if (entry instanceof Group group)
				list.add(new GroupResponse(locale, group));
			else if (entry instanceof BooleanField field)
				list.add(new BooleanResponse(locale, field));
			else if (entry instanceof DecimalField field)
				list.add(new DecimalResponse(locale, field));
			else if (entry instanceof IntegerField field)
				list.add(new IntegerResponse(locale, field));
			else if (entry instanceof RecognitionModelField field)
				list.add(new RecognitionModelResponse(locale, field));
			else if (entry instanceof SelectField field)
				list.add(new SelectResponse(locale, field));
			else if (entry instanceof StringField field)
				list.add(new StringResponse(locale, field));
			else if (entry instanceof ImageField field)
				list.add(new ImageResponse(locale, field));
			else
				org.slf4j.LoggerFactory.getLogger(EntryResponse.class).error(
						"No responce is implemented for model entry " + entry.getClass().getCanonicalName() + ".");
		}

		return list;
	}

	/**
	 * Returns the type.
	 *
	 * @return The type.
	 * @since 1.8
	 */
	public CoreServiceProviderApiController.Type getType() {
		return type;
	}

	/**
	 * Set the type.
	 *
	 * @param type The type to set.
	 * @since 1.8
	 */
	public void setType(CoreServiceProviderApiController.Type type) {
		this.type = type;
	}

	/**
	 * Returns the id.
	 *
	 * @return The id.
	 * @since 1.8
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the id.
	 *
	 * @param id The id to set.
	 * @since 1.8
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the provider.
	 *
	 * @return The provider.
	 * @since 1.8
	 */
	public String getProvider() {
		return provider;
	}

	/**
	 * Set the provider.
	 *
	 * @param provider The provider to set.
	 * @since 1.8
	 */
	public void setProvider(String provider) {
		this.provider = provider;
	}

	/**
	 * Returns the language.
	 *
	 * @return The language.
	 * @since 1.8
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Set the language.
	 *
	 * @param language The language to set.
	 * @since 1.8
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * Returns the name.
	 *
	 * @return The name.
	 * @since 1.8
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name.
	 *
	 * @param name The name to set.
	 * @since 1.8
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the version.
	 *
	 * @return The version.
	 * @since 1.8
	 */
	public float getVersion() {
		return version;
	}

	/**
	 * Set the version.
	 *
	 * @param version The version to set.
	 * @since 1.8
	 */
	public void setVersion(float version) {
		this.version = version;
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

	/**
	 * Returns the categories.
	 *
	 * @return The categories.
	 * @since 1.8
	 */
	public List<String> getCategories() {
		return categories;
	}

	/**
	 * Set the categories.
	 *
	 * @param categories The categories to set.
	 * @since 1.8
	 */
	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	/**
	 * Returns the steps.
	 *
	 * @return The steps.
	 * @since 1.8
	 */
	public List<String> getSteps() {
		return steps;
	}

	/**
	 * Set the steps.
	 *
	 * @param steps The steps to set.
	 * @since 1.8
	 */
	public void setSteps(List<String> steps) {
		this.steps = steps;
	}

	/**
	 * Returns the icon.
	 *
	 * @return The icon.
	 * @since 1.8
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * Set the icon.
	 *
	 * @param icon The icon to set.
	 * @since 1.8
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}

	/**
	 * Returns the index.
	 *
	 * @return The index.
	 * @since 1.8
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Set the index.
	 *
	 * @param index The index to set.
	 * @since 1.8
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Returns the premise.
	 *
	 * @return The premise.
	 * @since 1.8
	 */
	public PremiseResponse getPremise() {
		return premise;
	}

	/**
	 * Set the premise.
	 *
	 * @param premise The premise to set.
	 * @since 1.8
	 */
	public void setPremise(PremiseResponse premise) {
		this.premise = premise;
	}

	/**
	 * Returns the entries.
	 *
	 * @return The entries.
	 * @since 1.8
	 */
	public List<EntryResponse> getEntries() {
		return entries;
	}

	/**
	 * Set the entries.
	 *
	 * @param entries The entries to set.
	 * @since 1.8
	 */
	public void setEntries(List<EntryResponse> entries) {
		this.entries = entries;
	}

	/**
	 * Defines premise responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class PremiseResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The state.
		 */
		private Premise.State state;

		/**
		 * The message.
		 */
		private String message;

		/**
		 * Creates a premise response for the api.
		 * 
		 * @param premise The premise.
		 * @param locale  The locale.
		 * @since 1.8
		 */
		public PremiseResponse(Premise premise, Locale locale) {
			super();

			if (premise == null)
				premise = new Premise();

			state = premise.getState();
			message = premise.getMessage(locale);
		}

		/**
		 * Returns the state.
		 *
		 * @return The state.
		 * @since 1.8
		 */
		public Premise.State getState() {
			return state;
		}

		/**
		 * Set the state.
		 *
		 * @param state The state to set.
		 * @since 1.8
		 */
		public void setState(Premise.State state) {
			this.state = state;
		}

		/**
		 * Returns the message.
		 *
		 * @return The message.
		 * @since 1.8
		 */
		public String getMessage() {
			return message;
		}

		/**
		 * Set the message.
		 *
		 * @param message The message to set.
		 * @since 1.8
		 */
		public void setMessage(String message) {
			this.message = message;
		}

	}
}
