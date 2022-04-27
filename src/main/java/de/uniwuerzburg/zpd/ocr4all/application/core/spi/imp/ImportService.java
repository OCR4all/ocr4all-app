/**
 * File:     ImportService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi.imp
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     31.03.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.spi.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.ImportServiceProvider;

/**
 * Defines import services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Service
public class ImportService extends CoreServiceProvider<ImportServiceProvider> {
	/**
	 * Creates an import service.
	 * 
	 * @param configurationService The configuration service.
	 * @since 1.8
	 */
	@Autowired
	public ImportService(ConfigurationService configurationService) {
		super(ImportService.class, configurationService, ImportServiceProvider.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProvider#
	 * getCoreData()
	 */
	@Override
	public CoreData getCoreData() {
		return CoreData.project;
	}

}
