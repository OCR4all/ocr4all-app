/**
 * File:     ImportServiceProviderApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     4.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker.spi;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi.ServiceProviderResponse;
import de.uniwuerzburg.zpd.ocr4all.application.api.worker.CoreApiController;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.SchedulerService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.imp.ImportService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * Defines import service provider controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = ImportServiceProviderApiController.contextPath, produces = CoreApiController.applicationJson)
public class ImportServiceProviderApiController extends ServiceProviderCoreApiController<ImportService> {
	/**
	 * The context path.
	 */
	public static final String contextPath = spiContextPathVersion_1_0 + "/import";

	/**
	 * Creates an import service provider controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param projectService       The project service.
	 * @param schedulerService     The scheduler service.
	 * @param service              The service.
	 * @since 1.8
	 */
	public ImportServiceProviderApiController(ConfigurationService configurationService,
			SecurityService securityService, ProjectService projectService, SchedulerService schedulerService,
			ImportService service) {
		super(ImportServiceProviderApiController.class, configurationService, securityService, projectService, null,
				schedulerService, Type.imp, service, Project.Right.special);
	}

	/**
	 * Returns the service providers in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param lang      The language. if null, then use the application preferred
	 *                  locale.
	 * @return The service providers in the response body.
	 * @since 1.8
	 */
	@GetMapping(providersRequestMapping + projectPathVariable)
	public ResponseEntity<List<ServiceProviderResponse>> serviceProviders(@PathVariable String projectId,
			@RequestParam(required = false) String lang) {
		return serviceProviders(authorizationFactory.authorize(projectId, ProjectRight.special), null, lang);
	}

	/**
	 * Schedules a process to execute the service provider.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param request   The service provider request.
	 * @param lang      The language. if null, then use the application preferred
	 *                  locale.
	 * @param response  The HTTP-specific functionality in sending a response to the
	 *                  client.
	 * @since 1.8
	 */
	@PostMapping(scheduleRequestMapping + projectPathVariable)
	public void schedule(@PathVariable String projectId, @RequestBody @Valid ServiceProviderRequest request,
			@RequestParam(required = false) String lang, HttpServletResponse response) {
		schedule(authorizationFactory.authorize(projectId, ProjectRight.special), request, lang, response);
	}
}
