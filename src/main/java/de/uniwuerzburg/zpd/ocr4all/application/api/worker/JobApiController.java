/**
 * File:     JobApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 *
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     18.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.JobResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.ModelService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.Job;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.SchedulerService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Defines job controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Tag(name = "job", description = "the job API")
@RestController
@RequestMapping(path = JobApiController.contextPath, produces = CoreApiController.applicationJson)
public class JobApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = apiContextPathVersion_1_0 + "/job";

	/**
	 * The scheduler request mapping.
	 */
	public static final String schedulerRequestMapping = "/scheduler";

	/**
	 * The scheduler information request mapping.
	 */
	public static final String schedulerInformationRequestMapping = schedulerRequestMapping + informationRequestMapping;

	/**
	 * The scheduler action request mapping.
	 */
	public static final String schedulerActionRequestMapping = schedulerRequestMapping + actionRequestMapping;

	/**
	 * The reschedule request mapping.
	 */
	public static final String rescheduleRequestMapping = "/reschedule";

	/**
	 * The job id path variable.
	 */
	public static final String jobPathVariable = "/{jobId}";

	/**
	 * Defines scheduler actions.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public enum SchedulerAction {
		/**
		 * The run scheduler action.
		 */
		run,
		/**
		 * The pause scheduler action.
		 */
		pause,
		/**
		 * The expunge scheduler action.
		 */
		expunge
	}

	/**
	 * Defines rescheduler actions.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public enum ReschedulerAction {
		/**
		 * The begin rescheduler action.
		 */
		begin,
		/**
		 * The end rescheduler action.
		 */
		end,
		/**
		 * The swap rescheduler action.
		 */
		swap
	}

	/**
	 * Defines scheduler snapshot types.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public enum SchedulerSnapshotType {
		/**
		 * The project scheduler snapshot type.
		 */
		project,
		/**
		 * The domain scheduler snapshot type.
		 */
		domain,
		/**
		 * The administration scheduler snapshot type.
		 */
		administration
	}

	/**
	 * The scheduler service.
	 */
	private final SchedulerService service;

	/**
	 * The project service.
	 */
	private final ProjectService projectService;

	/**
	 * Creates a folio controller for the api.
	 *
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param service              The scheduler service.
	 * @param collectionService    The collection service.
	 * @param modelService         The model service.
	 * @param projectService       The project service.
	 * @since 1.8
	 */
	public JobApiController(ConfigurationService configurationService, SecurityService securityService,
			CollectionService collectionService, ModelService modelService, SchedulerService service,
			ProjectService projectService) {
		super(ProjectApiController.class, configurationService, securityService, collectionService, modelService,
				projectService);

		this.service = service;
		this.projectService = projectService;
	}

	/**
	 * Returns the scheduler information in the response body.
	 *
	 * @return The scheduler information in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the scheduler information in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Scheduler", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = SchedulerInformationResponse.class)) }),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(schedulerInformationRequestMapping)
	public ResponseEntity<SchedulerInformationResponse> schedulerInformation() {
		try {
			return ResponseEntity.ok().body(new SchedulerInformationResponse(service));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Performs the scheduler action and returns its information in the response
	 * body.
	 *
	 * @param action The action to perform. Available actions: run, pause, expunge.
	 * @return The scheduler information in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "performs the scheduler action and returns its information in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Scheduler", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = SchedulerInformationResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "501", description = "Not Implemented", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(schedulerActionRequestMapping + actionPathVariable)
	public ResponseEntity<SchedulerInformationResponse> schedulerAction(
			@Parameter(description = "the action to perform - available actions: run, pause, expunge") @PathVariable String action) {
		SchedulerAction schedulerAction;
		try {
			schedulerAction = SchedulerAction.valueOf(action);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		try {
			switch (schedulerAction) {
			case run:
				service.run();

				break;
			case pause:
				service.pause();

				break;
			case expunge:
				service.expungeDone();

				break;
			default:
				logger.warn("The scheduler action \"" + schedulerAction.name() + "\" is not implemented.");

				return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
			}

			return ResponseEntity.ok().body(new SchedulerInformationResponse(service));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the job.
	 *
	 * @param id The job id.
	 * @return The job.
	 * @throws ResponseStatusException Throws if the user is not authorized.
	 * @since 1.8
	 */
	private Job getJob(int id) throws ResponseStatusException {
		try {
			Job job = service.getJob(id);

			if (!isCoordinator()) {
				if (job instanceof de.uniwuerzburg.zpd.ocr4all.application.core.job.Process process)
					authorizationFactory.authorize(process.getProject().getId(), ProjectRight.execute);
				else if (job instanceof de.uniwuerzburg.zpd.ocr4all.application.core.job.Training training)
					authorizeModelSpecial(training.getModelId());
				else
					throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
			}

			return job;
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Removes the job and returns it in the response body.
	 *
	 * @param id The job id.
	 * @return The removed job in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "removes the job and returns it in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Job", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = JobResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(removeRequestMapping + idPathVariable)
	public ResponseEntity<JobResponse> remove(@Parameter(description = "the job id") @PathVariable int id) {
		try {
			Job job = getJob(id);

			return service.removeDone(id) ? ResponseEntity.ok().body(new JobResponse(false, job))
					: ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the job in the response body.
	 *
	 * @param id The job id.
	 * @return The job in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the job in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Job", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = JobResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(entityRequestMapping + idPathVariable)
	public ResponseEntity<JobResponse> entity(@Parameter(description = "the job id") @PathVariable int id) {
		try {
			return ResponseEntity.ok().body(new JobResponse(false, getJob(id)));
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the job summary in the response body.
	 *
	 * @param id The job id.
	 * @return The job summary in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the job summary in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Job Summary", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = JobSummaryResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(summaryRequestMapping + idPathVariable)
	public ResponseEntity<JobSummaryResponse> summary(@Parameter(description = "the job id") @PathVariable int id) {
		try {
			return ResponseEntity.ok().body(new JobSummaryResponse(getJob(id)));
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the overview of the jobs of given type in the response body.
	 *
	 * @param type The overview type. Available types: project, domain,
	 *             administration.
	 * @param id   The id of the project for the associated jobs. This parameter is
	 *             only required for snapshot type project.
	 * @return The overview in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the overview of the jobs of given type in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Jobs Overview", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = JobOverviewResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "405", description = "Method Not Allowed", content = @Content),
			@ApiResponse(responseCode = "501", description = "Not Implemented", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(overviewRequestMapping + typePathVariable)
	public ResponseEntity<JobOverviewResponse> overview(
			@Parameter(description = "the overview type - available types: project, domain administration") @PathVariable String type,
			@Parameter(description = "the id of the project for the associated jobs - this parameter is only required for snapshot type project") @RequestParam(required = false) String id) {

		SchedulerSnapshotType schedulerSnapshotType;
		try {
			schedulerSnapshotType = SchedulerSnapshotType.valueOf(type);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		try {
			switch (schedulerSnapshotType) {
			case administration:
				return isCoordinator() ? ResponseEntity.ok().body(new JobOverviewResponse(service, null, null, null))
						: ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			case project:
				Authorization authorization = authorizationFactory.authorize(id, ProjectRight.execute);

				return ResponseEntity.ok()
						.body(new JobOverviewResponse(service, Arrays.asList(authorization.project), null, null));
			case domain:
				if (!isSecured())
					return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();

				// The process jobs
				Set<Job.Cluster> clusters = new HashSet<>();
				for (Project project : projectService.getProjectsRightExist())
					if (project.isCoordinator()
							|| (!project.getConfiguration().getConfiguration().isStateBlocked() && project.isExecute()))
						clusters.add(project);

				// The training model ids
				Set<String> trainingModelIds = new HashSet<>();
				for (ModelService.Model model : modelService.getModels())
					if (model.getRight().isReadFulfilled())
						trainingModelIds.add(model.getConfiguration().getId());

				return ResponseEntity.ok()
						.body(new JobOverviewResponse(service, clusters, trainingModelIds, securityService.getUser()));
			default:
				logger.warn("The scheduler snapshot type \"" + schedulerSnapshotType.name() + "\" is not implemented.");

				return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
			}
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Secures the job task. Users with coordinator security permission are
	 * authorized immediately.
	 *
	 * @param schedulerSnapshotType The scheduler snapshot type. It is required for
	 *                              users without coordinator security permission
	 *                              and is either project or domain.
	 * @param projectId             The id of the project for the associated jobs.
	 *                              This parameter is only required for snapshot
	 *                              type project.
	 * @param isSpecial             True if the special right is required.
	 *                              Otherwise, the execute right is required.
	 * @param ids                   The job ids.
	 * @throws ResponseStatusException Throws on security violation.
	 * @since 1.8
	 */
	private void secure(String schedulerSnapshotType, String projectId, boolean isSpecial, int... ids)
			throws ResponseStatusException {
		if (!isCoordinator()) {
			SchedulerSnapshotType type;
			try {
				type = SchedulerSnapshotType.valueOf(schedulerSnapshotType);
			} catch (Exception e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}

			switch (type) {
			case administration:

				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
			case domain:
				List<Project> projects = new ArrayList<>();
				for (Project project : projectService.getProjectsRightExist())
					if (!project.getConfiguration().getConfiguration().isStateBlocked()
							&& (project.isSpecial() || (!isSpecial && project.isExecute())))
						projects.add(project);

				for (int id : ids)
					if (!service.isTarget(id, projects))
						throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

				break;
			case project:
				Authorization authorization = authorizationFactory.authorize(projectId,
						isSpecial ? ProjectRight.special : ProjectRight.execute);
				if (authorization.project.getConfiguration().getConfiguration().isStateBlocked())
					throw new ResponseStatusException(HttpStatus.FORBIDDEN);

				for (int id : ids)
					if (!service.isTarget(id, authorization.project))
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

				break;
			}
		}
	}

	/**
	 * Cancels the job.
	 *
	 * @param jobId    The job id.
	 * @param type     The snapshot type. It is required for users without
	 *                 coordinator security permission and is either project or
	 *                 domain.
	 * @param id       The id of the project for the associated jobs. This parameter
	 *                 is only required for snapshot type project.
	 * @param response The HTTP-specific functionality in sending a response to the
	 *                 client.
	 * @since 1.8
	 */
	@Operation(summary = "cancels the job")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Canceled Job"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(cancelRequestMapping + jobPathVariable)
	public void cancel(@Parameter(description = "the job id") @PathVariable int jobId,
			@Parameter(description = "the snapshot type - it is required for users without coordinator security permission and is either project or domain") @RequestParam(required = false) String type,
			@Parameter(description = "the id of the project for the associated jobs - this parameter is only required for snapshot type project") @RequestParam(required = false) String id,
			HttpServletResponse response) {

		secure(type, id, false, jobId);

		try {
			service.cancelJob(jobId);

			response.setStatus(HttpServletResponse.SC_OK);
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Reschedules the job.
	 *
	 * @param action   The action to perform. Available actions are: begin, end,
	 *                 swap.
	 * @param jobId    The job id.
	 * @param type     The snapshot type. It is required for users without
	 *                 coordinator security permission and is either project or
	 *                 domain.
	 * @param id       The id of the project for the associated jobs. This parameter
	 *                 is only required for snapshot type project.
	 * @param swap     The job id to be swapped with the job defined in the jobId
	 *                 parameter. It is only required for swap actions.
	 * @param response The HTTP-specific functionality in sending a response to the
	 *                 client.
	 * @since 1.8
	 */
	@Operation(summary = "reschedules the job")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Rescheduled Job"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
			@ApiResponse(responseCode = "501", description = "Not Implemented", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(rescheduleRequestMapping + actionPathVariable + jobPathVariable)
	public void reschedule(
			@Parameter(description = "the action to perform - available actions are: begin, end, swap") @PathVariable String action,
			@Parameter(description = "the job id") @PathVariable int jobId,
			@Parameter(description = "the snapshot type. It is required for users without coordinator security permission and is either project or domain") @RequestParam(required = false) String type,
			@Parameter(description = "the id of the project for the associated jobs - this parameter is only required for snapshot type project") @RequestParam(required = false) String id,
			@Parameter(description = "the job id to be swapped with the job defined in the jobId parameter - it is only required for swap actions") @RequestParam(required = false) Integer swap,
			HttpServletResponse response) {
		ReschedulerAction reschedulerAction;
		try {
			reschedulerAction = ReschedulerAction.valueOf(action);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}

		try {
			switch (reschedulerAction) {
			case begin:
				if (!isCoordinator())
					throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

				service.rescheduleBegin(jobId);

				break;
			case end:
				if (!isCoordinator())
					throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

				service.rescheduleEnd(jobId);

				break;
			case swap:
				if (swap == null || swap <= 0)
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

				secure(type, id, true, jobId, swap);

				service.swapScheduled(jobId, swap);

				break;
			default:
				logger.warn("The rescheduler action \"" + reschedulerAction.name() + "\" is not implemented.");

				throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
			}

			response.setStatus(HttpServletResponse.SC_OK);
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines job summary responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class JobSummaryResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The id.
		 */
		private int id;

		/**
		 * The state.
		 */
		private Job.State state;

		/**
		 * The expected progress taking into account all journal steps.
		 */
		private float progress;

		/**
		 * Creates a job summary response for the api.
		 * 
		 * @param job The job.
		 * @since 1.8
		 */
		public JobSummaryResponse(Job job) {
			super();

			id = job.getId();
			state = job.getState();
			progress = job.getJournal().getProgress();
		}

		/**
		 * Returns the id.
		 *
		 * @return The id.
		 * @since 17
		 */
		public int getId() {
			return id;
		}

		/**
		 * Set the id.
		 *
		 * @param id The id to set.
		 * @since 17
		 */
		public void setId(int id) {
			this.id = id;
		}

		/**
		 * Returns the state.
		 *
		 * @return The state.
		 * @since 17
		 */
		public Job.State getState() {
			return state;
		}

		/**
		 * Set the state.
		 *
		 * @param state The state to set.
		 * @since 17
		 */
		public void setState(Job.State state) {
			this.state = state;
		}

		/**
		 * Returns the expected progress taking into account all journal steps.
		 *
		 * @return The expected progress.
		 * @since 1.8
		 */
		public float getProgress() {
			return progress;
		}

		/**
		 * Set the expected progress taking into account all journal steps.
		 *
		 * @param progress The expected progress to set.
		 * @since 1.8
		 */
		public void setProgress(float progress) {
			this.progress = progress;
		}
	}

	/**
	 * Defines scheduler information responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class SchedulerInformationResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Defines the states.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public enum State {
			/**
			 * The running state.
			 */
			running,
			/**
			 * The running state.
			 */
			paused
		}

		/**
		 * The start time.
		 */
		private Date start;

		/**
		 * The updated time.
		 */
		private Date updated;

		/**
		 * The state.
		 */
		private State state;

		/**
		 * Creates a scheduler information response for the api.
		 *
		 * @param service The scheduler service.
		 * @since 1.8
		 */
		public SchedulerInformationResponse(SchedulerService service) {
			super();

			start = service.getStart();
			updated = service.getStateUpdated();
			state = service.isRunning() ? State.running : State.paused;
		}

		/**
		 * Returns the start time.
		 *
		 * @return The start time.
		 * @since 1.8
		 */
		public Date getStart() {
			return start;
		}

		/**
		 * Set the start time.
		 *
		 * @param start The start time to set.
		 * @since 1.8
		 */
		public void setStart(Date start) {
			this.start = start;
		}

		/**
		 * Returns the updated time.
		 *
		 * @return The updated time.
		 * @since 1.8
		 */
		public Date getUpdated() {
			return updated;
		}

		/**
		 * Set the updated time.
		 *
		 * @param updated The updated time to set.
		 * @since 1.8
		 */
		public void setUpdated(Date updated) {
			this.updated = updated;
		}

		/**
		 * Returns the state.
		 *
		 * @return The state.
		 * @since 1.8
		 */
		public State getState() {
			return state;
		}

		/**
		 * Set the state.
		 *
		 * @param state The state to set.
		 * @since 1.8
		 */
		public void setState(State state) {
			this.state = state;
		}

	}

	/**
	 * Defines job overview responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class JobOverviewResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The scheduled jobs in the same order as in the scheduler.
		 */
		private List<JobResponse> scheduled;

		/**
		 * The running jobs sorted by start time in descending order.
		 */
		private List<JobResponse> running;

		/**
		 * The done jobs sorted by end time in descending order.
		 */
		private List<JobResponse> done;

		/**
		 * Creates a job overview response for the api.
		 *
		 * @param service          The scheduler service.
		 * @param clusters         The clusters to select the jobs under control of the
		 *                         scheduler. If null, all jobs are selected.
		 * @param trainingModelIds The training jobs to add, that are running on the
		 *                         given assemble model ids.
		 * @param owner            The owner for the work jobs.
		 * @since 1.8
		 */
		public JobOverviewResponse(SchedulerService service, Collection<Job.Cluster> clusters,
				Set<String> trainingModelIds, String owner) {
			super();

			SchedulerService.Container jobs = service.getJobs(clusters, trainingModelIds, owner);

			scheduled = getJobResponses(jobs.getScheduled());
			running = getJobResponses(jobs.getRunning());
			done = getJobResponses(jobs.getDone());
		}

		/**
		 * Returns the job responses for given jobs.
		 *
		 * @param jobs The jobs.
		 * @return The job responses.
		 * @since 1.8
		 */
		private static List<JobResponse> getJobResponses(List<Job> jobs) {
			List<JobResponse> response = new ArrayList<>();

			for (Job job : jobs)
				response.add(new JobResponse(true, job));

			return response;
		}

		/**
		 * Returns the scheduled jobs in the same order as in the scheduler.
		 *
		 * @return The scheduled jobs in the same order as in the scheduler.
		 * @since 1.8
		 */
		public List<JobResponse> getScheduled() {
			return scheduled;
		}

		/**
		 * Set the scheduled jobs in the same order as in the scheduler.
		 *
		 * @param scheduled The scheduled jobs to set.
		 * @since 1.8
		 */
		public void setScheduled(List<JobResponse> scheduled) {
			this.scheduled = scheduled;
		}

		/**
		 * Returns the running jobs sorted by start time in descending order.
		 *
		 * @return The running jobs sorted by start time in descending order.
		 * @since 1.8
		 */
		public List<JobResponse> getRunning() {
			return running;
		}

		/**
		 * Set the running jobs sorted by start time in descending order.
		 *
		 * @param running The running jobs to set.
		 * @since 1.8
		 */
		public void setRunning(List<JobResponse> running) {
			this.running = running;
		}

		/**
		 * Returns the done jobs sorted by end time in descending order.
		 *
		 * @return The done jobs sorted by end time in descending order.
		 * @since 1.8
		 */
		public List<JobResponse> getDone() {
			return done;
		}

		/**
		 * Set the done jobs sorted by end time in descending order.
		 *
		 * @param done The done jobs to set.
		 * @since 1.8
		 */
		public void setDone(List<JobResponse> done) {
			this.done = done;
		}

	}

}
