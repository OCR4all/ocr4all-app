/**
 * File:     PostcorrectionService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi.postcorrection
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     20.09.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.spi.postcorrection;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.communication.CommunicationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.PostcorrectionServiceProvider;

/**
 * Defines post-correction services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Service
public class PostcorrectionService extends CoreServiceProvider<PostcorrectionServiceProvider> {
	/**
	 * Creates a post-correction service.
	 * 
	 * @param configurationService The configuration service.
	 * @param communicationService The communication service.
	 * @param taskExecutor         The task executor.
	 * @since 1.8
	 */
	public PostcorrectionService(ConfigurationService configurationService, CommunicationService communicationService,
			ThreadPoolTaskExecutor taskExecutor) {
		super(PostcorrectionService.class, configurationService, communicationService,
				PostcorrectionServiceProvider.class, taskExecutor);
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
