/**
 * File:     TrainingService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi.training
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     20.06.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.spi.training;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.communication.CommunicationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.ActionServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.TrainingServiceProvider;

/**
 * Defines training services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Service
public class TrainingService extends ActionServiceProvider<TrainingServiceProvider> {
	/**
	 * Creates a training service.
	 * 
	 * @param configurationService The configuration service.
	 * @param communicationService The communication service.
	 * @param taskExecutor         The task executor.
	 * @since 1.8
	 */
	public TrainingService(ConfigurationService configurationService, CommunicationService communicationService,
			ThreadPoolTaskExecutor taskExecutor) {
		super(TrainingService.class, configurationService, communicationService, TrainingServiceProvider.class,
				taskExecutor);
	}

}
