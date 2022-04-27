/**
 * File:     CoreService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     19.11.2020
 */
package de.uniwuerzburg.zpd.ocr4all.application.core;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;

/**
 * Defines core services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class CoreService {
	/**
	 * The logger.
	 */
	protected final org.slf4j.Logger logger;

	/**
	 * The configuration service.
	 */
	protected final ConfigurationService configurationService;

	/**
	 * Creates a core service.
	 * 
	 * @param logger               The logger class.
	 * @param configurationService The configuration service.
	 * @since 1.8
	 */
	protected CoreService(Class<?> logger, ConfigurationService configurationService) {
		super();

		this.logger = org.slf4j.LoggerFactory.getLogger(logger);
		this.configurationService = configurationService;
	}
}
