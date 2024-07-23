/**
 * File:     ProcessorAction.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.job
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     23.07.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.job;

import java.util.Locale;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.ServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessorCore;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessorServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Framework;

/**
 * Defines processor actions.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public abstract class ProcessorAction extends Action {

	/**
	 * Creates a processor action.
	 * 
	 * @param logger               The logger class.
	 * @param configurationService The configuration service.
	 * @param locale               The application locale.
	 * @param processing           The processing mode.
	 * @param steps                The number of steps. This is a positive number.
	 * @since 17
	 */
	public ProcessorAction(Class<? extends ProcessorAction> logger, ConfigurationService configurationService,
			Locale locale, Processing processing, int steps) {
		super(logger, configurationService, locale, processing, steps);
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
