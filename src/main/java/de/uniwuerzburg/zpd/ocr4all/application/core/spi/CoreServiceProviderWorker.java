/**
 * File:     CoreServiceProviderWorker.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     25.11.2020
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uniwuerzburg.zpd.ocr4all.application.spi.core.JournalEntryServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.ConfigurationServiceProvider;

/**
 * Defines core service provider workers.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public abstract class CoreServiceProviderWorker implements ServiceProvider {
	/**
	 * The base name of the service provider resource bundle.
	 */
	private static final String resourceBundleBaseName = "lang/core/service-provider-messages";

	/**
	 * The resource bundle message source.
	 */
	private static final ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
	{
		resourceBundleMessageSource.setBasenames(resourceBundleBaseName);
	}

	/**
	 * The JSON object mapper.
	 */
	protected final ObjectMapper objectMapper = new ObjectMapper();
	{
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	/**
	 * The prefix of the message keys in the resource bundle.
	 */
	protected final String resourceBundleKeyPrefix;

	/**
	 * The configuration.
	 */
	protected ConfigurationServiceProvider configuration;

	/**
	 * The status.
	 */
	protected ServiceProvider.Status status = ServiceProvider.Status.loaded;

	/**
	 * True if the service provider is enabled.
	 */
	protected boolean isEnabled = false;

	/**
	 * The journal.
	 */
	protected List<JournalEntryServiceProvider> journal = new ArrayList<>();

	/**
	 * Default constructor for a core service provider worker.
	 * 
	 * @since 1.8
	 */
	public CoreServiceProviderWorker() {
		this(null);
	}

	/**
	 * Creates a core service provider worker.
	 * 
	 * @param resourceBundleKeyPrefix The prefix of the keys in the resource bundle.
	 * @since 1.8
	 */
	public CoreServiceProviderWorker(String resourceBundleKeyPrefix) {
		super();

		this.resourceBundleKeyPrefix = resourceBundleKeyPrefix == null ? "" : resourceBundleKeyPrefix.trim();

		journal.add(new JournalEntryServiceProvider(true, JournalEntryServiceProvider.Level.info,
				"loaded service provider", null, status));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider#initialize(
	 * boolean,
	 * de.uniwuerzburg.zpd.ocr4all.application.spi.env.ConfigurationServiceProvider)
	 */
	@Override
	public void initialize(boolean isEnabled, ConfigurationServiceProvider configuration) {
		this.isEnabled = isEnabled;
		this.configuration = configuration;

		ServiceProvider.Status sourceState = status;
		status = ServiceProvider.Status.initialized;

		journal.add(new JournalEntryServiceProvider(true, JournalEntryServiceProvider.Level.info,
				"initialized service provider", sourceState, status));

		sourceState = status;
		status = isEnabled ? ServiceProvider.Status.active : ServiceProvider.Status.inactive;

		journal.add(new JournalEntryServiceProvider(true, JournalEntryServiceProvider.Level.info,
				isEnabled ? "started service provider" : "stopped service provider", sourceState, status));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider#setEnabled(
	 * java.lang.String, java.lang.Boolean)
	 */
	@Override
	public void setEnabled(String user, Boolean isEnabled) {
		if (this.isEnabled != isEnabled) {
			this.isEnabled = isEnabled;

			journal.add(new JournalEntryServiceProvider(user, true, JournalEntryServiceProvider.Level.info,
					(isEnabled ? "enabled" : "disabled") + " service provider", status, status));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider#getStatus()
	 */
	@Override
	public Status getStatus() {
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider#start(java.
	 * lang.String)
	 */
	@Override
	public JournalEntryServiceProvider start(String user) {
		JournalEntryServiceProvider journalEntry;
		if (ServiceProvider.Status.inactive.equals(status)) {
			status = ServiceProvider.Status.active;

			journalEntry = new JournalEntryServiceProvider(user, true, JournalEntryServiceProvider.Level.info,
					"started service provider", ServiceProvider.Status.inactive, status);

			journal.add(journalEntry);
		} else
			journalEntry = new JournalEntryServiceProvider(user, false, JournalEntryServiceProvider.Level.warn,
					"the service provider can oly be started in 'inactive' status ", status, status);

		return journalEntry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider#restart(java
	 * .lang.String)
	 */
	@Override
	public JournalEntryServiceProvider restart(String user) {
		JournalEntryServiceProvider journalEntry;
		if (ServiceProvider.Status.active.equals(status)) {
			journalEntry = new JournalEntryServiceProvider(user, true, JournalEntryServiceProvider.Level.info,
					"restarted service provider", status, status);

			journal.add(journalEntry);
		} else
			journalEntry = new JournalEntryServiceProvider(user, false, JournalEntryServiceProvider.Level.warn,
					"the service provider can oly be restarted in 'active' status ", status, status);

		return journalEntry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider#stop(java.
	 * lang.String)
	 */
	@Override
	public JournalEntryServiceProvider stop(String user) {
		JournalEntryServiceProvider journalEntry;
		if (ServiceProvider.Status.active.equals(status)) {
			status = ServiceProvider.Status.inactive;

			journalEntry = new JournalEntryServiceProvider(user, true, JournalEntryServiceProvider.Level.info,
					"stopped service provider", ServiceProvider.Status.active, status);

			journal.add(journalEntry);
		} else
			journalEntry = new JournalEntryServiceProvider(user, false, JournalEntryServiceProvider.Level.warn,
					"the service provider can oly be stopped in 'active' status ", status, status);

		return journalEntry;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider#getJournal()
	 */
	@Override
	public List<JournalEntryServiceProvider> getJournal() {
		return new ArrayList<>(journal);
	}

	/**
	 * Returns the string for the given key from the resource bundle. Returns the
	 * key with "?" at the beginning and at the end, if the resource is missed.
	 * 
	 * @param locale The locale for which a resource bundle is desired.
	 * @param key    The message key for the desired string.
	 * @return the string for the given key.
	 * @since 1.8
	 */
	protected String getString(Locale locale, String key) {
		return getString(locale, key, null, "?" + resourceBundleKeyPrefix + key + "?");
	}

	/**
	 * Returns the string for the given key from the resource bundle. Returns the
	 * key with "?" at the beginning and at the end, if the resource is missed.
	 * 
	 * @param locale The locale for which a resource bundle is desired.
	 * @param key    The message key for the desired string.
	 * @param args   An array of arguments that will be filled in for params within
	 *               the message (params look like "{0}", "{1,date}", "{2,time}"
	 *               within a message), or null if none.
	 * @return the string for the given key.
	 * @since 1.8
	 */
	protected String getString(Locale locale, String key, Object[] args) {
		return getString(locale, key, args, "?" + resourceBundleKeyPrefix + key + "?");
	}

	/**
	 * Returns the string for the given key from the resource bundle.
	 * 
	 * @param locale        The locale for which a resource bundle is desired.
	 * @param key           The message key for the desired string.
	 * @param defaultString The default string if the resource is missed.
	 * @return the string for the given key.
	 * @since 1.8
	 */
	protected String getString(Locale locale, String key, String defaultString) {
		return getString(locale, key, null, defaultString);
	}

	/**
	 * Returns the string for the given key from the resource bundle.
	 * 
	 * @param locale        The locale for which a resource bundle is desired.
	 * @param key           The message key for the desired string.
	 * @param args          An array of arguments that will be filled in for params
	 *                      within the message (params look like "{0}", "{1,date}",
	 *                      "{2,time}" within a message), or null if none.
	 * @param defaultString The default string if the resource is missed.
	 * @return the string for the given key.
	 * @since 1.8
	 */
	protected String getString(Locale locale, String key, Object[] args, String defaultString) {
		try {
			return resourceBundleMessageSource.getMessage(resourceBundleKeyPrefix + (key == null ? "" : key.trim()),
					args, locale);
		} catch (NoSuchMessageException e) {
			return defaultString;
		}
	}

	/**
	 * Returns the message for the given key from the resource bundle. No prefix is
	 * added to the message keys.
	 * 
	 * @param locale The locale for which a resource bundle is desired.
	 * @param key    The message key for the desired string.
	 * @return the string for the given key.
	 * @since 1.8
	 */
	protected String getMessage(Locale locale, String key) {
		return getMessage(locale, key, null);
	}

	/**
	 * Returns the message for the given key from the resource bundle. No prefix is
	 * added to the message keys.
	 * 
	 * @param locale The locale for which a resource bundle is desired.
	 * @param key    The message key for the desired string.
	 * @param args   An array of arguments that will be filled in for params within
	 *               the message (params look like "{0}", "{1,date}", "{2,time}"
	 *               within a message), or null if none.
	 * @return the string for the given key.
	 * @since 1.8
	 */
	protected String getMessage(Locale locale, String key, Object[] args) {
		return getMessage(locale, key, args, "?" + key + "?");
	}

	/**
	 * Returns the message for the given key from the resource bundle. No prefix is
	 * added to the message keys.
	 * 
	 * @param locale        The locale for which a resource bundle is desired.
	 * @param key           The message key for the desired string.
	 * @param args          An array of arguments that will be filled in for params
	 *                      within the message (params look like "{0}", "{1,date}",
	 *                      "{2,time}" within a message), or null if none.
	 * @param defaultString The default string if the resource is missed.
	 * @return the string for the given key.
	 * @since 1.8
	 */
	protected String getMessage(Locale locale, String key, Object[] args, String defaultString) {
		try {
			return resourceBundleMessageSource.getMessage((key == null ? "" : key.trim()), args, locale);
		} catch (NoSuchMessageException e) {
			return defaultString;
		}
	}

}
