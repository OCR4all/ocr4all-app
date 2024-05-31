/**
 * File:     OCR4allApplication.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     03.11.2020
 */
package de.uniwuerzburg.zpd.ocr4all.application;

import java.nio.file.Files;
import java.util.Locale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;

/**
 * Defines a starter for building the ocr4all web, including RESTful,
 * application using Spring MVC. Uses Tomcat as the default embedded container.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@SpringBootApplication
public class OCR4allApplication {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OCR4allApplication.class);

	/**
	 * The main method to build the ocr4all web application using Spring MVC.
	 * 
	 * @param args The application arguments.
	 * @since 1.8
	 */
	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(OCR4allApplication.class, args);

		ConfigurationService configurationService = context.getBean(ConfigurationService.class);

		if (!Files.isDirectory(configurationService.getWorkspace().getFolder())) {
			logger.error("ocr4all workspace '" + configurationService.getWorkspace().getFolder()
					+ "' is not available. Shutting down the application.");

			context.close();
		} else {
			Locale.setDefault(configurationService.getApplication().getLocale());

			final String instance = configurationService.getWorkspace().getConfiguration().getInstance();

			logger.info("started ocr4all" + (instance == null ? "" : " (" + instance + ")") + ": port "
					+ configurationService.getServerPort() + ", active profiles '"
					+ configurationService.getActiveProfilesCSV() + "', workspace '"
					+ configurationService.getWorkspace().getFolder() + "', exchange '"
					+ configurationService.getExchange().getFolder() + "', repository '"
					+ configurationService.getRepository().getFolder() + "', data '"
					+ configurationService.getData().getFolder() + "', opt '"
					+ configurationService.getOpt().getFolder() + "', temporary '"
					+ configurationService.getTemporary().getFolder() + "', charset "
					+ configurationService.getApplication().getCharset().displayName() + ", locale "
					+ configurationService.getApplication().getLocale() + ", view languages "
					+ configurationService.getApplication().getViewLanguages() + ".");
		}
	}
}
