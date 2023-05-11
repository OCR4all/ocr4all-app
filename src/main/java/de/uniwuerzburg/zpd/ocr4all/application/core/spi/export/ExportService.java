/**
 * File:     ExportService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi.postcorrection
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     11.05.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.spi.export;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.ExportServiceProvider;

/**
 * Defines export services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Service
public class ExportService extends CoreServiceProvider<ExportServiceProvider> {
	/**
	 * Creates an export service.
	 * 
	 * @param configurationService The configuration service.
	 * @param taskExecutor         The task executor.
	 * @since 1.8
	 */
	public ExportService(ConfigurationService configurationService, ThreadPoolTaskExecutor taskExecutor) {
		super(ExportService.class, configurationService, ExportServiceProvider.class, taskExecutor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProvider#
	 * getCoreData()
	 */
	@Override
	public CoreData getCoreData() {
		return CoreData.sandbox;
	}

}
