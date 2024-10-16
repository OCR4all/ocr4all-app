/**
 * File:     WorkflowApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 *
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     17.04.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.Serializable;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.SnapshotRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.JobJsonResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.ModelService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.Job;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.SchedulerService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Sandbox;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.SandboxService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.workflow.WorkflowCoreData;
import de.uniwuerzburg.zpd.ocr4all.application.core.workflow.WorkflowService;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.Metadata;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.Workflow;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Defines workflow controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Tag(name = "workflow", description = "the workflow API")
@RestController
@RequestMapping(path = WorkflowApiController.contextPath, produces = CoreApiController.applicationJson)
public class WorkflowApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = apiContextPathVersion_1_0 + "/workflow";

	/**
	 * The scheduler service.
	 */
	protected final SchedulerService schedulerService;

	/**
	 * The workflow service.
	 */
	private final WorkflowService service;

	/**
	 * Creates a workflow controller for the api.
	 *
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param collectionService    The collection service.
	 * @param modelService         The model service.
	 * @param schedulerService     The scheduler service.
	 * @param service              The workflow service.
	 * @param projectService       The project service.
	 * @param sandboxService       The sandbox service.
	 * @since 1.8
	 */
	// TODO: add security like repository
	public WorkflowApiController(ConfigurationService configurationService, SecurityService securityService,
			CollectionService collectionService, ModelService modelService, SchedulerService schedulerService,
			WorkflowService service, ProjectService projectService, SandboxService sandboxService) {
		super(WorkflowApiController.class, configurationService, securityService, collectionService, modelService,
				projectService, sandboxService);

		this.schedulerService = schedulerService;
		this.service = service;
	}

	/**
	 * Returns the workflow core data in the response body.
	 *
	 * @param workflowId The workflow id.
	 * @return The workflow core data in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the workflow core data in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Workflow Core Data", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = WorkflowCoreData.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(pullRequestMapping + workflowPathVariable)
	public ResponseEntity<WorkflowCoreData> entity(
			@Parameter(description = "the workflow id") @PathVariable String workflowId) {
		try {
			WorkflowCoreData coreData = service.getCoreData(workflowId);
			return coreData == null ? ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
					: ResponseEntity.ok().body(coreData);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the metadata of the workflows in the response body.
	 *
	 * @return The metadata of the workflows in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the metadata of the workflows in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Workflows Metadata", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = Metadata.class))) }),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(listRequestMapping)
	public ResponseEntity<List<Metadata>> list() {
		try {
			List<Metadata> list = service.getMetadata();
			return list == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
					: ResponseEntity.ok().body(list);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Creates the workflow and returns its metadata in the response body.
	 *
	 * @param request The workflow request.
	 * @return The metadata in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "creates the workflow and returns its metadata in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Workflow Metadata", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = Metadata.class)) }),
//			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(pushRequestMapping)
	public ResponseEntity<Metadata> create(@RequestBody WorkflowRequest request) {
//		if (!securityService.isCoordinator())
//			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

		try {
			Metadata metadata = service.create(request.getLabel(), request.getDescription(), request.getViewModel(),
					request.getWorkflow());

			return metadata == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
					: ResponseEntity.ok().body(metadata);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates the workflow metadata and returns it in the response body.
	 *
	 * @param workflowId The workflow id.
	 * @param request    The workflow request.
	 * @return The updated metadata in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the workflow metadata and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Updated Workflow Metadata", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = Metadata.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
//			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(updateRequestMapping + workflowPathVariable)
	public ResponseEntity<Metadata> update(@Parameter(description = "the workflow id") @PathVariable String workflowId,
			@RequestBody WorkflowMatadataRequest request) {
//		if (!securityService.isCoordinator())
//			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

		try {
			Metadata metadata = service.update(workflowId, request.getLabel(), request.getDescription());

			return metadata == null ? ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
					: ResponseEntity.ok().body(metadata);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates the workflow and returns its metadata in the response body.
	 *
	 * @param workflowId The workflow id.
	 * @param request    The workflow request.
	 * @return The updated metadata in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the workflow and returns its metadata in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Updated Workflow Metadata", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = Metadata.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
//			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(pushRequestMapping + workflowPathVariable)
	public ResponseEntity<Metadata> update(@Parameter(description = "the workflow id") @PathVariable String workflowId,
			@RequestBody WorkflowRequest request) {
//		if (!securityService.isCoordinator())
//			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

		try {
			Metadata metadata = service.persist(workflowId, request.getLabel(), request.getDescription(),
					request.getViewModel(), request.getWorkflow());

			return metadata == null ? ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
					: ResponseEntity.ok().body(metadata);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Removes the workflow.
	 *
	 * @param workflowId The workflow id.
	 * @param response   The HTTP-specific functionality in sending a response to
	 *                   the client.
	 * @since 1.8
	 */
	@Operation(summary = "removes the workflow")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Removed Workflow"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
//			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(removeRequestMapping + workflowPathVariable)
	public void remove(@Parameter(description = "the workflow id") @PathVariable String workflowId,
			HttpServletResponse response) {
//		if (!securityService.isCoordinator())
//			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

		try {
			if (service.remove(workflowId))
				response.setStatus(HttpServletResponse.SC_OK);
			else
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns true if the workflow is available.
	 *
	 * @param project The project.
	 * @param sandbox The sandbox.
	 * @return True if the workflow is available.
	 * @since 1.8
	 */
	protected boolean isAvailable(Project project, Sandbox sandbox) {
		// A selected project with required rights is always required
		if (!project.getConfiguration().getConfiguration().isStateActive() || !project.isRights(Project.Right.execute))
			return false;

		de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Sandbox.State state = sandbox
				.getConfiguration().getConfiguration().getState();

		return de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Sandbox.State.active.equals(state)
				|| (de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Sandbox.State.secured
						.equals(state) && project.isRights(Project.Right.special));
	}

	/**
	 * Schedules a process to execute the workflow.
	 *
	 * @param projectId  The project id. This is the folder name.
	 * @param sandboxId  The sandbox id. This is the folder name.
	 * @param workflowId The workflow id.
	 * @param request    The workflow snapshot request. The track of the parent
	 *                   snapshot can not be null.
	 * @param lang       The language. If null, then use the application preferred
	 *                   locale.
	 * @return The job in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "schedules a process to execute the workflow")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Job", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = JobJsonResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "405", description = "Method Not Allowed", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(scheduleRequestMapping + projectPathVariable + sandboxPathVariable + workflowPathVariable)
	public ResponseEntity<JobJsonResponse> schedule(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @PathVariable String sandboxId,
			@Parameter(description = "the workflow id") @PathVariable String workflowId,
			@RequestBody @Valid WorkflowSnapshotRequest request,
			@Parameter(description = "the language") @RequestParam(required = false) String lang) {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, sandboxId,
				ProjectRight.execute);

		if (!isAvailable(authorization.project, authorization.sandbox))
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);

		try {
			de.uniwuerzburg.zpd.ocr4all.application.core.job.Workflow workflow = service.getJobWorkflow(getLocale(lang),
					request.getJobShortDescription(), authorization.project, authorization.sandbox,
					request.getParentSnapshot().getTrack(), workflowId);

			if (workflow == null)
				throw new ResponseStatusException(HttpStatus.NOT_FOUND);

			Job.State jobState = schedulerService.schedule(workflow);

			return ResponseEntity.ok().body(new JobJsonResponse(workflow.getId(), jobState));
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (IllegalArgumentException ex) {
			log(ex);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		} catch (Exception ex) {
			log(ex);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines workflow metadata requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class WorkflowMatadataRequest implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The label.
		 */
		private String label = null;

		/**
		 * The description.
		 */
		private String description = null;

		/**
		 * Returns the label.
		 *
		 * @return The label.
		 * @since 1.8
		 */
		public String getLabel() {
			return label;
		}

		/**
		 * Set the label.
		 *
		 * @param label The label to set.
		 * @since 1.8
		 */
		public void setLabel(String label) {
			this.label = label;
		}

		/**
		 * Returns the description.
		 *
		 * @return The description.
		 * @since 1.8
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * Set the description.
		 *
		 * @param description The description to set.
		 * @since 1.8
		 */
		public void setDescription(String description) {
			this.description = description;
		}

	}

	/**
	 * Defines workflow requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class WorkflowRequest extends WorkflowMatadataRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The label.
		 */
		private String label = null;

		/**
		 * The description.
		 */
		private String description = null;

		/**
		 * The view model.
		 */
		@JsonProperty("view-model")
		private String viewModel = null;

		/**
		 * The workflow.
		 */
		private Workflow workflow = null;

		/**
		 * Returns the label.
		 *
		 * @return The label.
		 * @since 1.8
		 */
		public String getLabel() {
			return label;
		}

		/**
		 * Set the label.
		 *
		 * @param label The label to set.
		 * @since 1.8
		 */
		public void setLabel(String label) {
			this.label = label;
		}

		/**
		 * Returns the description.
		 *
		 * @return The description.
		 * @since 1.8
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * Set the description.
		 *
		 * @param description The description to set.
		 * @since 1.8
		 */
		public void setDescription(String description) {
			this.description = description;
		}

		/**
		 * Returns the viewModel.
		 *
		 * @return The viewModel.
		 * @since 1.8
		 */
		public String getViewModel() {
			return viewModel;
		}

		/**
		 * Set the viewModel.
		 *
		 * @param viewModel The viewModel to set.
		 * @since 1.8
		 */
		public void setViewModel(String viewModel) {
			this.viewModel = viewModel;
		}

		/**
		 * Returns the workflow.
		 *
		 * @return The workflow.
		 * @since 1.8
		 */
		public Workflow getWorkflow() {
			return workflow;
		}

		/**
		 * Set the workflow.
		 *
		 * @param workflow The workflow to set.
		 * @since 1.8
		 */
		public void setWorkflow(Workflow workflow) {
			this.workflow = workflow;
		}

	}

	/**
	 * Defines workflow snapshot requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class WorkflowSnapshotRequest implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The job short description.
		 */
		@JsonProperty("job-short-description")
		private String jobShortDescription;

		/**
		 * The parent snapshot.
		 */
		@NotNull
		@JsonProperty("parent-snapshot")
		private SnapshotRequest parentSnapshot;

		/**
		 * Returns the job short description.
		 *
		 * @return The job short description.
		 * @since 1.8
		 */
		public String getJobShortDescription() {
			return jobShortDescription;
		}

		/**
		 * Set the job short description.
		 *
		 * @param jobShortDescription The job short description to set.
		 * @since 1.8
		 */
		public void setJobShortDescription(String jobShortDescription) {
			this.jobShortDescription = jobShortDescription;
		}

		/**
		 * Returns the parent snapshot.
		 *
		 * @return The parent snapshot.
		 * @since 1.8
		 */
		public SnapshotRequest getParentSnapshot() {
			return parentSnapshot;
		}

		/**
		 * Set the parent snapshot.
		 *
		 * @param parentSnapshot The parent snapshot to set.
		 * @since 1.8
		 */
		public void setParentSnapshot(SnapshotRequest parentSnapshot) {
			this.parentSnapshot = parentSnapshot;
		}
	}

}
