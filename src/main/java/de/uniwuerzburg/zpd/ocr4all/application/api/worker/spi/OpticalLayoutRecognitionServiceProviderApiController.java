/**
 * File:     OpticalLayoutRecognitionServiceProviderApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     06.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker.spi;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.SnapshotRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi.ServiceProviderResponse;
import de.uniwuerzburg.zpd.ocr4all.application.api.worker.CoreApiController;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.SchedulerService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.SandboxService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.olr.OpticalLayoutRecognitionService;

/**
 * Defines optical layout recognition (OLR) service provider controllers for the
 * api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Controller
@RequestMapping(path = OpticalLayoutRecognitionServiceProviderApiController.contextPath, produces = CoreApiController.applicationJson)
public class OpticalLayoutRecognitionServiceProviderApiController
		extends ServiceProviderCoreApiController<OpticalLayoutRecognitionService> {
	/**
	 * The context path.
	 */
	public static final String contextPath = spiContextPathVersion_1_0 + "/olr";

	/**
	 * Creates an optical layout recognition (OLR) service provider controller for
	 * the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param projectService       The project service.
	 * @param sandboxService       The sandbox service.
	 * @param schedulerService     The scheduler service.
	 * @param service              The service.
	 * @since 1.8
	 */
	public OpticalLayoutRecognitionServiceProviderApiController(ConfigurationService configurationService,
			SecurityService securityService, ProjectService projectService, SandboxService sandboxService,
			SchedulerService schedulerService, OpticalLayoutRecognitionService service) {
		super(OpticalLayoutRecognitionServiceProviderApiController.class, configurationService, securityService,
				projectService, sandboxService, schedulerService, Type.olr, service, Project.Right.execute);
	}

	/**
	 * Returns the service providers in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param sandboxId The sandbox id. This is the folder name.
	 * @param request   The snapshot request. The track can not be null.
	 * @param lang      The language. if null, then use the application preferred
	 *                  locale.
	 * @return The service providers in the response body.
	 * @since 1.8
	 */
	@PostMapping(providersRequestMapping + projectPathVariable + sandboxPathVariable)
	public ResponseEntity<List<ServiceProviderResponse>> serviceProviders(@PathVariable String projectId,
			@PathVariable String sandboxId, @RequestBody @Valid SnapshotRequest request,
			@RequestParam(required = false) String lang) {
		return serviceProviders(authorizationFactory.authorizeSnapshot(projectId, sandboxId, ProjectRight.execute),
				request.getTrack(), lang);
	}

	/**
	 * Schedules a process to execute the service provider.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param sandboxId The sandbox id. This is the folder name.
	 * @param request   The service provider snapshot request. The track of the
	 *                  parent snapshot can not be null.
	 * @param lang      The language. if null, then use the application preferred
	 *                  locale.
	 * @param response  The HTTP-specific functionality in sending a response to the
	 *                  client.
	 * @since 1.8
	 */
	@PostMapping(scheduleRequestMapping + projectPathVariable + sandboxPathVariable)
	public void schedule(@PathVariable String projectId, @PathVariable String sandboxId,
			@RequestBody @Valid ServiceProviderSnapshotRequest request, @RequestParam(required = false) String lang,
			HttpServletResponse response) {
		schedule(authorizationFactory.authorizeSnapshot(projectId, sandboxId, ProjectRight.execute), request, lang,
				response);
	}
}
