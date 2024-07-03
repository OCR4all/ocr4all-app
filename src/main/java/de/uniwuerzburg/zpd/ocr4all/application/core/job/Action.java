/**
 * File:     Action.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.job
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     20.06.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.job;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.ServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessorCore;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessorServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Framework;

/**
 * Defines actions.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public abstract class Action extends Job {
	/**
	 * The logger.
	 */
	protected final org.slf4j.Logger logger;

	/**
	 * Creates an action.
	 * 
	 * @param logger               The logger class.
	 * @param configurationService The configuration service.
	 * @param locale               The application locale.
	 * @param processing           The processing mode.
	 * @param steps                The number of steps. This is a positive number.
	 * @since 1.8
	 */
	Action(Class<? extends Action> logger, ConfigurationService configurationService, Locale locale,
			Processing processing, int steps) {
		super(configurationService, locale, processing, steps);

		this.logger = org.slf4j.LoggerFactory.getLogger(logger);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#depend(java.util.
	 * Collection)
	 */
	@Override
	public Set<Job> depend(Collection<Job> jobs) {
		return new HashSet<Job>();
	}

	/**
	 * Defines action instances.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public abstract class InstanceAction<C extends ProcessorCore.Callback, F extends Framework>
			extends InstanceCore<ProcessorServiceProvider<C, F>> {
		/**
		 * The processor for the service provider.
		 */
		private final ProcessorServiceProvider.Processor<C, F> processor;

		/**
		 * Creates a process instance with initialized state.
		 * 
		 * @param serviceProvider         The service provider.
		 * @param serviceProviderArgument The service provider arguments.
		 * @param journal                 The journal step.
		 * @throws IllegalArgumentException Throws if the service provider, the model or
		 *                                  the journal argument is missed.
		 * @since 1.8
		 */
		protected InstanceAction(ProcessorServiceProvider<C, F> serviceProvider,
				ServiceProvider serviceProviderArgument, Journal.Step journal) throws IllegalArgumentException {
			super(serviceProvider, serviceProviderArgument, journal);

			processor = this.serviceProvider.newProcessor();
			if (processor == null)
				logger.warn("no processor available for the service provider "
						+ this.serviceProvider.getClass().getName() + ", " + getShortDescription() + ".");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.uniwuerzburg.zpd.ocr4all.application.core.job.InstanceCore#getLocale()
		 */
		@Override
		protected Locale getLocale() {
			return locale;
		}

	}
}
