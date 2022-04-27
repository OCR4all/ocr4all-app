/**
 * File:     ExchangeConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     23.03.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration;

import java.nio.file.Paths;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.OCR4all;

/**
 * Defines configurations for the exchange.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class ExchangeConfiguration extends CoreFolder {

	/**
	 * Creates a configuration for the exchange.
	 * 
	 * @param properties The ocr4all properties.
	 * @since 1.8
	 */
	public ExchangeConfiguration(OCR4all properties) {
		super(Paths.get(properties.getExchange().getFolder()));

		ConfigurationService.initializeFolder(true, folder, "exchange");
	}

}
