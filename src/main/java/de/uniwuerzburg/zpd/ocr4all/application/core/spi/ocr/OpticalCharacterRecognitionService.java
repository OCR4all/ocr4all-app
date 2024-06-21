/**
 * File:     OpticalCharacterRecognitionService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi.ocr
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     12.04.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.spi.ocr;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.communication.CommunicationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.ProcessServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.OpticalCharacterRecognitionServiceProvider;

/**
 * Defines optical character recognition (OCR) services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Service
public class OpticalCharacterRecognitionService
		extends ProcessServiceProvider<OpticalCharacterRecognitionServiceProvider> {
	/**
	 * Creates an optical character recognition (OCR) service.
	 * 
	 * @param configurationService The configuration service.
	 * @param communicationService The communication service.
	 * @param taskExecutor         The task executor.
	 * @since 1.8
	 */
	public OpticalCharacterRecognitionService(ConfigurationService configurationService,
			CommunicationService communicationService, ThreadPoolTaskExecutor taskExecutor) {
		super(OpticalCharacterRecognitionService.class, configurationService, communicationService,
				OpticalCharacterRecognitionServiceProvider.class, taskExecutor);
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
