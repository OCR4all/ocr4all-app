/**
 * File:     ActionServiceProvider.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi
 *
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     21.06.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.spi;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import de.uniwuerzburg.zpd.ocr4all.application.core.communication.CommunicationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider;

/**
 * Defines action service providers.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @param <P> The service provider type.
 * @since 17
 */
public class ActionServiceProvider<P extends ServiceProvider> extends CoreServiceProvider<P> {
	
	/**
	 * Creates an action service provider.
	 *
	 * @param logger               The logger class.
	 * @param configurationService The configuration service.
	 * @param communicationService The communication service.
	 * @param service              The interface or abstract class representing the
	 *                             service.
	 * @param taskExecutor         The task executor.
	 * @since 17
	 */
	protected ActionServiceProvider(Class<? extends ActionServiceProvider<P>> logger,
			ConfigurationService configurationService, CommunicationService communicationService, Class<P> service,
			ThreadPoolTaskExecutor taskExecutor) {
		super(logger, configurationService, communicationService, service, taskExecutor);
	}
	
}
