/**
 * File:     ToolService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi.postcorrection
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     11.05.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.spi.tool;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.communication.CommunicationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.ToolServiceProvider;

/**
 * Defines tools services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Service
public class ToolService extends CoreServiceProvider<ToolServiceProvider> {
	/**
	 * Creates a tool service.
	 * 
	 * @param configurationService The configuration service.
	 * @param communicationService The communication service.
	 * @param taskExecutor         The task executor.
	 * @since 1.8
	 */
	public ToolService(ConfigurationService configurationService, CommunicationService communicationService,
			ThreadPoolTaskExecutor taskExecutor) {
		super(ToolService.class, configurationService, communicationService, ToolServiceProvider.class, taskExecutor);
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
