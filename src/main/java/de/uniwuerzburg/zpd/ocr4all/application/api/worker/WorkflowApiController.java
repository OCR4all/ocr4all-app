/**
 * File:     WorkflowApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     09.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.BasicRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.HistoryResponse;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.MetsResponse;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.WorkflowResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow.Workflow;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow.WorkflowService;
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
	 * The mets request mapping.
	 */
	public static final String metsRequestMapping = "/mets";

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
	 * @since 1.8
	 */
	@Autowired
	public WorkflowApiController(ConfigurationService configurationService, SecurityService securityService,
			WorkflowService service, ProjectService projectService) {
		super(ProjectApiController.class, configurationService, securityService, projectService, service);

		this.service = service;
	}

	/**
	 * Returns the workflow in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The workflow id. This is the folder name.
	 * @return The workflow in the response body.
	 * @since 1.8
	 */
	@GetMapping(entityRequestMapping + projectPathVariable)
	public ResponseEntity<WorkflowResponse> entity(@PathVariable String projectId, @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId, id);
		try {
			return ResponseEntity.ok().body(new WorkflowResponse(authorization.workflow));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the list of workflows of given project sorted by name in the response
	 * body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @return The list of workflow sorted by name in the response body.
	 * @since 1.8
	 */
	@GetMapping(listRequestMapping + projectPathVariable)
	public ResponseEntity<List<WorkflowResponse>> list(@PathVariable String projectId) {
		Authorization authorization = authorizationFactory.authorize(projectId);
		try {
			List<WorkflowResponse> workflows = new ArrayList<>();
			for (Workflow workflow : service.getWorkflows(authorization.project))
				workflows.add(new WorkflowResponse(workflow));

			return ResponseEntity.ok().body(workflows);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Creates a workflow and returns it in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The workflow id. This is the folder name.
	 * @return The created workflow in the response body.
	 * @since 1.8
	 */
	@GetMapping(createRequestMapping + projectPathVariable)
	public ResponseEntity<WorkflowResponse> create(@PathVariable String projectId, @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId, ProjectRight.special);
		try {
			if (authorization.project.getConfiguration().getWorkflowsConfiguration().isAvailable(id)) {
				Workflow workflow = service.authorize(authorization.project, id);

				return workflow == null ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
						: ResponseEntity.status(HttpStatus.CONFLICT).body(new WorkflowResponse(workflow));
			}

			Path path = authorization.project.getConfiguration().getWorkflowsConfiguration().create(id, getUser());
			if (path == null)
				return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();

			Workflow workflow = service.authorize(authorization.project, id);
			return workflow == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
					: ResponseEntity.ok().body(new WorkflowResponse(workflow));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates a workflow and returns it in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param request   The workflow request.
	 * @return The updated workflow in the response body.
	 * @since 1.8
	 */
	@PostMapping(updateRequestMapping + projectPathVariable)
	public ResponseEntity<WorkflowResponse> update(@PathVariable String projectId,
			@RequestBody @Valid WorkflowRequest request) {
		if (request.getName() == null || request.getName().isBlank())
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

		de.uniwuerzburg.zpd.ocr4all.application.persistence.project.workflow.Workflow.State state = null;
		try {
			state = de.uniwuerzburg.zpd.ocr4all.application.persistence.project.workflow.Workflow.State
					.valueOf(request.getState());
		} catch (Exception e) {
			// unknown form state, ignore it
		}

		Authorization authorization = authorizationFactory.authorize(projectId, request.getId(), ProjectRight.special);
		try {
			if (authorization.workflow.getConfiguration().getConfiguration().updateBasicData(request.getName(),
					request.getDescription(), request.getKeywords(), state)) {
				Workflow updatedWorkflow = service.authorize(authorization.project, request.getId());

				return updatedWorkflow == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
						: ResponseEntity.ok().body(new WorkflowResponse(updatedWorkflow));
			} else
				return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the workflow history in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The workflow id. This is the folder name.
	 * @return The workflow history in the response body.
	 * @since 1.8
	 */
	@GetMapping(historyInformationRequestMapping + projectPathVariable)
	public ResponseEntity<HistoryResponse> historyInformation(@PathVariable String projectId, @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId, id, ProjectRight.execute);
		try {
			return ResponseEntity.ok().body(new HistoryResponse(authorization.workflow.getHistory()));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Downloads the workflow history.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The workflow id. This is the folder name.
	 * @param response  The HTTP-specific functionality in sending a response to the
	 *                  client.
	 * @throws IOException Signals that an I/O exception of some sort has occurred.
	 * @since 1.8
	 */
	@GetMapping(value = historyDownloadRequestMapping
			+ projectPathVariable, produces = CoreApiController.applicationZip)
	public void historyDownload(@PathVariable String projectId, @RequestParam String id, HttpServletResponse response)
			throws IOException {
		Authorization authorization = authorizationFactory.authorize(projectId, id, ProjectRight.execute);
		try {
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + id + ".zip\"");

			authorization.workflow.zipHistory(response.getOutputStream());
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
		}
	}

	/**
	 * Resets the workflow and returns it in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The workflow id. This is the folder name.
	 * @return The reseted workflow in the response body.
	 * @since 1.8
	 */
	@GetMapping(resetRequestMapping + projectPathVariable)
	public ResponseEntity<WorkflowResponse> reset(@PathVariable String projectId, @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId, id, ProjectRight.special);
		try {
			if (authorization.workflow.reset()) {
				Workflow workflow = service.authorize(authorization.project, id);

				return workflow == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
						: ResponseEntity.ok().body(new WorkflowResponse(workflow));
			} else
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Removes the workflow and returns it in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The workflow id. This is the folder name.
	 * @return The removed workflow in the response body.
	 * @since 1.8
	 */
	@GetMapping(removeRequestMapping + projectPathVariable)
	public ResponseEntity<WorkflowResponse> remove(@PathVariable String projectId, @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId, id, ProjectRight.special);
		try {
			return authorization.project.getConfiguration().getWorkflowsConfiguration()
					.remove(authorization.workflow.getConfiguration().getFolder(), getUser())
							? ResponseEntity.ok().body(new WorkflowResponse(authorization.workflow))
							: ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the information contained in the mets (Metadata Encoding and
	 * Transmission Standard) XML file in the specified workflow in the response
	 * body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The workflow id. This is the folder name.
	 * @return The mets (Metadata Encoding and Transmission Standard) information in
	 *         the response body.
	 * @since 1.8
	 */
	@GetMapping(metsRequestMapping + projectPathVariable)
	public ResponseEntity<MetsResponse> mets(@PathVariable String projectId, @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId, id);
		try {
			return ResponseEntity.ok().body(new MetsResponse(authorization.workflow));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines workflow requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class WorkflowRequest extends BasicRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

	}

}
