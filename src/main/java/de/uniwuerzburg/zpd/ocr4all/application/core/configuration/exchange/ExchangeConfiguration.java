/**
 * File:     ExchangeConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.exchange
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     23.03.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.exchange;

import java.nio.file.Paths;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.CoreFolder;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.OCR4all;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.exchange.Partition;

/**
 * Defines configurations for the exchange.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class ExchangeConfiguration extends CoreFolder {
	/**
	 * The partition properties.
	 */
	private final Partition partition;

	/**
	 * Creates a configuration for the exchange.
	 * 
	 * @param properties The ocr4all properties.
	 * @since 1.8
	 */
	public ExchangeConfiguration(OCR4all properties) {
		super(Paths.get(properties.getExchange().getFolder()));

		ConfigurationService.initializeFolder(true, folder, "exchange");

		partition = properties.getExchange().getPartition();
	}

	/**
	 * Returns the partition.
	 *
	 * @return The partition.
	 * @since 17
	 */
	public Partition getPartition() {
		return partition;
	}

}
