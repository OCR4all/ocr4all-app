/**
 * File:     LauncherServiceProviderApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     30.03.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker.spi;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi.ServiceProviderResponse;
import de.uniwuerzburg.zpd.ocr4all.application.api.worker.CoreApiController;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.SchedulerService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.SandboxService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.launcher.LauncherService;

/**
 * Defines launcher service provider controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Controller
@RequestMapping(path = LauncherServiceProviderApiController.contextPath, produces = CoreApiController.applicationJson)
public class LauncherServiceProviderApiController extends ServiceProviderCoreApiController<LauncherService> {
	/**
	 * The context path.
	 */
	public static final String contextPath = spiContextPathVersion_1_0 + "/launcher";

	/**
	 * Creates a launcher service provider controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param projectService       The project service.
	 * @param sandboxService       The sandbox service.
	 * @param schedulerService     The scheduler service.
	 * @param service              The service.
	 * @since 1.8
	 */
	public LauncherServiceProviderApiController(ConfigurationService configurationService,
			SecurityService securityService, ProjectService projectService, SandboxService sandboxService,
			SchedulerService schedulerService, LauncherService service) {
		super(LauncherServiceProviderApiController.class, configurationService, securityService, projectService,
				sandboxService, schedulerService, Type.launcher, service, Project.Right.special);
	}

	/**
	 * Returns the service providers in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param sandboxId The sandbox id. This is the folder name.
	 * @param lang      The language. if null, then use the application preferred
	 *                  locale.
	 * @return The service providers in the response body.
	 * @since 1.8
	 */
	@GetMapping(providersRequestMapping + projectPathVariable + sandboxPathVariable)
	public ResponseEntity<List<ServiceProviderResponse>> serviceProviders(@PathVariable String projectId,
			@PathVariable String sandboxId, @RequestParam(required = false) String lang) {
		return serviceProviders(authorizationFactory.authorizeSnapshot(projectId, sandboxId, ProjectRight.special),
				null, lang);
	}

	/**
	 * Schedules a process to execute the service provider.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param sandboxId The sandbox id. This is the folder name.
	 * @param request   The service provider root snapshot request.
	 * @param lang      The language. if null, then use the application preferred
	 *                  locale.
	 * @param response  The HTTP-specific functionality in sending a response to the
	 *                  client.
	 * @since 1.8
	 */
	@PostMapping(scheduleRequestMapping + projectPathVariable + sandboxPathVariable)
	public void schedule(@PathVariable String projectId, @PathVariable String sandboxId,
			@RequestBody @Valid ServiceProviderSnapshotCoreRequest request, @RequestParam(required = false) String lang,
			HttpServletResponse response) {
		schedule(authorizationFactory.authorizeSnapshot(projectId, sandboxId, ProjectRight.special), request, lang,
				response);
	}
}
