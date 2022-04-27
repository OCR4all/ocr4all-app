/**
 * File:     OCR4allApplication.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application
 * 
 * Author:   Herbert Baier
 * Date:     03.11.2020
 */
package de.uniwuerzburg.zpd.ocr4all.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import de.uniwuerzburg.zpd.ocr4all.application.core.administration.AdministrationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.OCR4all;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.SchedulerService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow.WorkflowService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.AccountService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityConfig;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.imp.ImportService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.preprocessing.PreprocessingService;

/**
 * Defines Spring boot-based tests. Launches the main configuration class
 * {@link OCR4allApplication}, this means, the class with annotation
 * {@code @SpringBootApplication}.
 *
 * @author Herbert Baier
 * @version 1.0
 * @since 1.8
 */
@SpringBootTest
class OCR4allApplicationTests {
	/**
	 * The environment in which the current application is running.
	 */
	@Autowired
	private Environment environment;

	/**
	 * The ocr4all properties in core package.
	 */
	@Autowired
	private OCR4all ocr4allProperties;

	/**
	 * The configuration service in core package.
	 */
	@Autowired
	private ConfigurationService configurationService;

	/**
	 * The scheduler service in core package.
	 */
	@Autowired
	private SchedulerService schedulerService;

	/**
	 * The administration service in core package.
	 */
	@Autowired
	private AdministrationService administrationService;

	/**
	 * The project service in core package.
	 */
	@Autowired
	private ProjectService projectService;

	/**
	 * The workflow service in core package.
	 */
	@Autowired
	private WorkflowService workflowService;

	/**
	 * The account service in core package. Available in server profile.
	 */
	@Autowired
	private AccountService accountService;

	/**
	 * The security configuration in core package.
	 */
	@Autowired
	private SecurityConfig securityConfig;

	/**
	 * The security service in core package.
	 */
	@Autowired
	private SecurityService securityService;

	/**
	 * The spi image service in core package.
	 */
	@Autowired
	private ImportService imageService;

	/**
	 * The spi preprocessing service in core package.
	 */
	@Autowired
	private PreprocessingService preprocessingService;

	/**
	 * Test if the application is able to load the Spring context successfully and
	 * if the required beans for active profiles are available.
	 * 
	 * @since 1.8
	 */
	@Test
	void contextLoads() {
		/*
		 * The core package
		 */

		// The configuration package
		assertThat(ocr4allProperties).isNotNull();
		assertThat(configurationService).isNotNull();

		// The job package
		assertThat(schedulerService).isNotNull();

		// The administration package
		assertThat(administrationService).isNotNull();

		// The project package
		assertThat(projectService).isNotNull();
		assertThat(workflowService).isNotNull();

		// The security package
		assertThat(securityConfig).isNotNull();
		assertThat(securityService).isNotNull();

		// Available in server profile
		if (environment.acceptsProfiles(Profiles.of("server")))
			// The security package
			assertThat(accountService).isNotNull();

		// The spi package
		assertThat(imageService).isNotNull();
		assertThat(preprocessingService).isNotNull();

	}

}
