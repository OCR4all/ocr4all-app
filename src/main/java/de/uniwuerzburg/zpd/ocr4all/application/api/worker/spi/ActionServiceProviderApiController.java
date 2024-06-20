/**
 * File:     ActionServiceProviderApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     20.06.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker.spi;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.SchedulerService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider;

/**
 * Defines action service provider controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @param <S> The core service provider type.
 * @since 17
 */
public class ActionServiceProviderApiController<S extends CoreServiceProvider<? extends ServiceProvider>>
		extends CoreServiceProviderApiController<S> {

	/**
	 * Creates an action service provider controller for the api.
	 * 
	 * @param logger               The logger class.
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param schedulerService     The scheduler service.
	 * @param service              The service.
	 * @since 17
	 */
	protected ActionServiceProviderApiController(Class<?> logger, ConfigurationService configurationService,
			SecurityService securityService, SchedulerService schedulerService, S service) {
		super(logger, configurationService, securityService, null, null, schedulerService, service);
	}

}
