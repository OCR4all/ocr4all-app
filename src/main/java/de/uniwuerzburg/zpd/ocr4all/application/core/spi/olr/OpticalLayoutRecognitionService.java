/**
 * File:     OpticalLayoutRecognitionService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi.preprocessing
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.09.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.spi.olr;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.communication.CommunicationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.ProcessServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.OpticalLayoutRecognitionServiceProvider;

/**
 * Defines optical layout recognition (OLR) services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Service
public class OpticalLayoutRecognitionService extends ProcessServiceProvider<OpticalLayoutRecognitionServiceProvider> {
	/**
	 * Creates an optical layout recognition (OLR) service.
	 * 
	 * @param configurationService The configuration service.
	 * @param communicationService The communication service.
	 * @param taskExecutor         The task executor.
	 * @since 1.8
	 */
	public OpticalLayoutRecognitionService(ConfigurationService configurationService,
			CommunicationService communicationService, ThreadPoolTaskExecutor taskExecutor) {
		super(OpticalLayoutRecognitionService.class, configurationService, communicationService,
				OpticalLayoutRecognitionServiceProvider.class, taskExecutor);
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
