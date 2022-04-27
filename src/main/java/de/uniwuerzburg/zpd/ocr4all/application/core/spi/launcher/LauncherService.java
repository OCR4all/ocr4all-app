/**
 * File:     LauncherService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi.launcher
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     30.03.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.spi.launcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.LauncherServiceProvider;

/**
 * Defines launcher services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Service
public class LauncherService extends CoreServiceProvider<LauncherServiceProvider> {
	/**
	 * Creates a launcher service.
	 * 
	 * @param configurationService The configuration service.
	 * @since 1.8
	 */
	@Autowired
	public LauncherService(ConfigurationService configurationService) {
		super(LauncherService.class, configurationService, LauncherServiceProvider.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProvider#
	 * getCoreData()
	 */
	@Override
	public CoreData getCoreData() {
		return CoreData.workflow;
	}

}
