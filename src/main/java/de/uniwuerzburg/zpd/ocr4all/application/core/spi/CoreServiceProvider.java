/**
 * File:     CoreServiceProvider.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     19.11.2020
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.ServiceLoader;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider;

/**
 * Defines core service providers.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public abstract class CoreServiceProvider<P extends ServiceProvider> extends CoreService {
	/**
	 * Define core data.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public enum CoreData {
		project, workflow
	}

	/**
	 * The service providers. The key is the id.
	 */
	private final Hashtable<String, P> serviceProviders = new Hashtable<>();

	/**
	 * The registered providers sorted by name.
	 */
	protected final List<Provider> providers = new ArrayList<>();

	/**
	 * Creates a core service provider.
	 * 
	 * @param logger               The logger class.
	 * @param configurationService The configuration service.
	 * @param service              The interface or abstract class representing the
	 *                             service.
	 * @since 1.8
	 */
	protected CoreServiceProvider(Class<? extends CoreServiceProvider<P>> logger,
			ConfigurationService configurationService, Class<P> service) {
		super(logger, configurationService);

		for (P provider : ServiceLoader.load(service))
			if (provider.getName(configurationService.getApplication().getLocale()) != null
					&& !provider.getName(configurationService.getApplication().getLocale()).trim().isEmpty()) {
				String key = provider.getClass().getName();

				if (serviceProviders.containsKey(key))
					this.logger.warn("Ignored provider for service " + service.getName() + " with duplicated key "
							+ service.getName() + ".");
				else {
					serviceProviders.put(key, provider);
					providers.add(new Provider(key, provider));

					this.logger.debug("Loaded provider for service " + service.getName() + ": " + key + ".");
				}
			}

		if (providers.isEmpty())
			this.logger.warn("No providers registered for " + service.getName() + ".");
		else {
			Collections.sort(providers,
					(sp1, sp2) -> sp1.getServiceProvider().getName(configurationService.getApplication().getLocale())
							.trim().compareToIgnoreCase(sp2.getServiceProvider()
									.getName(configurationService.getApplication().getLocale()).trim()));

			StringBuffer buffer = new StringBuffer();
			for (Provider provider : providers) {
				if (buffer.length() > 0)
					buffer.append(", ");

				buffer.append(
						provider.getServiceProvider().getName(configurationService.getApplication().getLocale()).trim()
								+ " (v" + provider.getServiceProvider().getVersion() + ")");
			}

			this.logger.info("Loaded " + providers.size() + " providers for " + service.getName() + ": "
					+ buffer.toString() + ".");
		}
	}

	/**
	 * Returns the core data.
	 * 
	 * @return The core data.
	 * @since 1.8
	 */
	public abstract CoreData getCoreData();

	/**
	 * Returns the service provider with given key.
	 *
	 * @param key The service provider key.
	 * @return The service provider with given key.
	 * @since 1.8
	 */
	public P getServiceProviders(String key) {
		return key == null ? null : serviceProviders.get(key);
	}

	/**
	 * Returns true if there are registered providers.
	 * 
	 * @return True if there are registered providers.
	 * @since 1.8
	 */
	public boolean isProviderAvailable() {
		return !providers.isEmpty();
	}

	/**
	 * Returns the number of registered providers.
	 * 
	 * @return The number of registered providers.
	 * @since 1.8
	 */
	public int getProviderNumber() {
		return providers.size();
	}

	/**
	 * Returns the registered providers sorted by name.
	 * 
	 * @return The registered providers sorted by name.
	 * @since 1.8
	 */
	public List<Provider> getProviders() {
		return new ArrayList<>(providers);
	}

	/**
	 * Provider is an immutable class that defines process service providers with
	 * their ids.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public class Provider {
		/**
		 * The id.
		 */
		private final String id;

		/**
		 * The service provider.
		 */
		private final P serviceProvider;

		/**
		 * Creates a process service provider.
		 * 
		 * @param id              The id.
		 * @param serviceProvider The service provider.
		 * @since 1.8
		 */
		private Provider(String id, P serviceProvider) {
			super();

			this.id = id;
			this.serviceProvider = serviceProvider;
		}

		/**
		 * Returns true if the given id matches the provider id.
		 *
		 * @param id The id to verify.
		 * @return True if the given id matches the provider id.
		 * @since 1.8
		 */
		public boolean isMatch(String id) {
			return this.id.equals(id);
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
		 * Returns the service provider.
		 *
		 * @return The service provider.
		 * @since 1.8
		 */
		public P getServiceProvider() {
			return serviceProvider;
		}
	}
}
