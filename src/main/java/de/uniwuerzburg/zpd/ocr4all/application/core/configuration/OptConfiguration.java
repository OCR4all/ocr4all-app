/**
 * File:     OptConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     12.04.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration;

import java.nio.file.Paths;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.OCR4all;

/**
 * Defines configurations for the opt.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class OptConfiguration extends CoreFolder {

	/**
	 * Creates a configuration for the opt.
	 * 
	 * @param properties The ocr4all properties.
	 * @since 1.8
	 */
	public OptConfiguration(OCR4all properties) {
		super(Paths.get(properties.getOpt().getFolder()));

		ConfigurationService.initializeFolder(true, folder, "opt");
	}

}
