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
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.communication.CommunicationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.TaskExecutorServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.ConfigurationServiceProvider;

/**
 * Defines core service providers.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @param <P> The service provider type.
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
		/**
		 * The project core data.
		 */
		project,
		/**
		 * The sandbox core data.
		 */
		sandbox
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
	 * @param communicationService The communication service.
	 * @param service              The interface or abstract class representing the
	 *                             service.
	 * @param taskExecutor         The task executor.
	 * @since 1.8
	 */
	protected CoreServiceProvider(Class<? extends CoreServiceProvider<P>> logger,
			ConfigurationService configurationService, CommunicationService communicationService, Class<P> service,
			ThreadPoolTaskExecutor taskExecutor) {
		super(logger, configurationService);

		final ConfigurationServiceProvider configuration = configurationService.getWorkspace().getConfiguration()
				.getConfigurationServiceProvider();

		final Set<String> disabledServiceProviders = configurationService.getWorkspace().getConfiguration()
				.getDisabledServiceProviders();

		final Set<String> lazyInitializedServiceProviders = configurationService.getWorkspace().getConfiguration()
				.getLazyInitializedServiceProviders();

		final Hashtable<String, TaskExecutorServiceProvider> taskExecutorServiceProviders = configurationService
				.getWorkspace().getConfiguration().getTaskExecutorServiceProviders();

		for (P provider : ServiceLoader.load(service)) {
			String id = provider.getClass().getName();

			if (serviceProviders.containsKey(id))
				this.logger.warn("Ignored service provider with duplicated key " + id + ".");
			else {
				TaskExecutorServiceProvider taskExecutorServiceProvider = taskExecutorServiceProviders.get(id);

				provider.configure(!lazyInitializedServiceProviders.contains(id),
						!disabledServiceProviders.contains(id),
						taskExecutorServiceProvider == null ? null : taskExecutorServiceProvider.getThreadName(),
						configuration, communicationService.getMicroserviceArchitecture());

				if (provider.getName(configurationService.getApplication().getLocale()) == null
						|| provider.getName(configurationService.getApplication().getLocale()).trim().isEmpty())
					this.logger.warn("Ignored service provider with key " + id + ", since its name is not defied.");
				else {
					serviceProviders.put(id, provider);
					providers.add(new Provider(id, provider));

					this.logger.debug("Loaded provider for service " + service.getName() + ": " + id + ".");

					/*
					 * Initializes the service provider if it is enabled. If "Lazy Initialization"
					 * is set, the initialization is performed in a new thread.
					 */
					if (provider.isEnabled()) {
						if (provider.isEagerInitialized())
							initialize(provider);
						else
							taskExecutor.execute(() -> {
								initialize(provider);
							});
					}
				}
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
	 * Initializes the service provider.
	 *
	 * @param provider                        The provider.
	 * @param id                              The provider id.
	 * @param lazyInitializedServiceProviders The lazy initialized service
	 *                                        providers.
	 * @param disabledServiceProviders        The disabled service providers.
	 * @param configuration                   The configuration.
	 * @since 1.8
	 */
	private void initialize(P provider) {
		String id = provider.getClass().getName();

		try {
			Date begin = new Date();
			provider.initialize();

			logger.debug(
					"Provider " + id + ": status " + provider.getStatus().name() + ", initialization time "
							+ ((new Date()).getTime() - begin.getTime()) + " ms"
							+ (provider.isEagerInitialized() && provider.isEnabled() ? ""
									: " , lazy / launched on " + configurationService.getApplication().format(begin))
							+ ".");
		} catch (Exception e) {
			logger.warn("Could not initialize provider: " + id + " - " + e.getMessage() + ".");
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
	 * Returns the active provider with given key.
	 *
	 * @param key The provider key.
	 * @return The active provider with given key. Null if unknown or inactive.
	 * @since 1.8
	 */
	public P getActiveProvider(String key) {
		if (key == null)
			return null;
		else {
			P provider = serviceProviders.get(key);

			return provider == null || !ServiceProvider.Status.active.equals(provider.getStatus()) ? null
					: serviceProviders.get(key);
		}
	}

	/**
	 * Returns true if there are registered active providers.
	 *
	 * @return True if there are registered active providers.
	 * @since 1.8
	 */
	public boolean isActiveProviderAvailable() {
		for (Provider provider : providers)
			if (ServiceProvider.Status.active.equals(provider.getServiceProvider().getStatus()))
				return true;

		return false;
	}

	/**
	 * Returns the registered active providers sorted by name.
	 *
	 * @return The registered active providers sorted by name.
	 * @since 1.8
	 */
	public List<Provider> getActiveProviders() {
		List<Provider> active = new ArrayList<>();

		for (Provider provider : providers)
			if (ServiceProvider.Status.active.equals(provider.getServiceProvider().getStatus()))
				active.add(provider);

		return active;
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
