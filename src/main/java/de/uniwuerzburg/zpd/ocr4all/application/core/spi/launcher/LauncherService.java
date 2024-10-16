/**
 * File:     LauncherService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi.launcher
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     30.03.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.spi.launcher;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.communication.CommunicationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.ProcessServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.LauncherServiceProvider;

/**
 * Defines launcher services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Service
public class LauncherService extends ProcessServiceProvider<LauncherServiceProvider> {
	/**
	 * Creates a launcher service.
	 * 
	 * @param configurationService The configuration service.
	 * @param communicationService The communication service.
	 * @param taskExecutor         The task executor.
	 * @since 1.8
	 */
	public LauncherService(ConfigurationService configurationService, CommunicationService communicationService,
			ThreadPoolTaskExecutor taskExecutor) {
		super(LauncherService.class, configurationService, communicationService, LauncherServiceProvider.class,
				taskExecutor);
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
