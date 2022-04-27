/**
 * File:     OpticalCharactertRecognitionServiceProviderApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     12.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker.spi;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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
import de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow.WorkflowService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.ocr.OpticalCharacterRecognitionService;

/**
 * Defines optical character recognition (OCR) service provider controllers for
 * the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Controller
@RequestMapping(path = OpticalCharactertRecognitionServiceProviderApiController.contextPath, produces = CoreApiController.applicationJson)
public class OpticalCharactertRecognitionServiceProviderApiController
		extends ServiceProviderCoreApiController<OpticalCharacterRecognitionService> {
	/**
	 * The context path.
	 */
	public static final String contextPath = spiContextPathVersion_1_0 + "/ocr";

	/**
	 * Creates an optical character recognition (OCR) service provider controller
	 * for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param projectService       The project service.
	 * @param workflowService      The workflow service.
	 * @param schedulerService     The scheduler service.
	 * @param service              The service.
	 * @since 1.8
	 */
	@Autowired
	public OpticalCharactertRecognitionServiceProviderApiController(ConfigurationService configurationService,
			SecurityService securityService, ProjectService projectService, WorkflowService workflowService,
			SchedulerService schedulerService, OpticalCharacterRecognitionService service) {
		super(OpticalCharactertRecognitionServiceProviderApiController.class, configurationService, securityService,
				projectService, workflowService, schedulerService, Type.ocr, service, Project.Right.execute);
	}

	/**
	 * Returns the service providers in the response body.
	 * 
	 * @param projectId  The project id. This is the folder name.
	 * @param workflowId The workflow id. This is the folder name.
	 * @param request    The snapshot request. The track can not be null.
	 * @param lang       The language. if null, then use the application preferred
	 *                   locale.
	 * @return The service providers in the response body.
	 * @since 1.8
	 */
	@PostMapping(providersRequestMapping + projectPathVariable + workflowPathVariable)
	public ResponseEntity<List<ServiceProviderResponse>> serviceProviders(@PathVariable String projectId,
			@PathVariable String workflowId, @RequestBody @Valid SnapshotRequest request,
			@RequestParam(required = false) String lang) {
		return serviceProviders(authorizationFactory.authorizeSnapshot(projectId, workflowId, ProjectRight.execute),
				request.getTrack(), lang);
	}

	/**
	 * Schedules a process to execute the service provider.
	 * 
	 * @param projectId  The project id. This is the folder name.
	 * @param workflowId The workflow id. This is the folder name.
	 * @param request    The service provider snapshot request. The track of the
	 *                   parent snapshot can not be null.
	 * @param lang       The language. if null, then use the application preferred
	 *                   locale.
	 * @param response   The HTTP-specific functionality in sending a response to
	 *                   the client.
	 * @since 1.8
	 */
	@PostMapping(scheduleRequestMapping + projectPathVariable + workflowPathVariable)
	public void schedule(@PathVariable String projectId, @PathVariable String workflowId,
			@RequestBody @Valid ServiceProviderSnapshotRequest request, @RequestParam(required = false) String lang,
			HttpServletResponse response) {
		schedule(authorizationFactory.authorizeSnapshot(projectId, workflowId, ProjectRight.execute), request, lang,
				response);
	}
}
