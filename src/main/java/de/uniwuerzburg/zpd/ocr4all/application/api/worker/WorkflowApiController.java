/**
 * File:     WorkflowApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     17.04.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.WorkflowService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.SandboxService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;

/**
 * Defines workflow controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Controller
@RequestMapping(path = WorkflowApiController.contextPath, produces = CoreApiController.applicationJson)
public class WorkflowApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = apiContextPathVersion_1_0 + "/workflow";

	/**
	 * The workflow service.
	 */
	private final WorkflowService service;

	/**
	 * Creates a workflow controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param service              The workflow service.
	 * @param projectService       The project service.
	 * @param sandboxService       The sandbox service.
	 * @since 1.8
	 */
	public WorkflowApiController(ConfigurationService configurationService, SecurityService securityService,
			WorkflowService service, ProjectService projectService, SandboxService sandboxService) {
		super(WorkflowApiController.class, configurationService, securityService, projectService, sandboxService);

		this.service = service;
	}

	
}
