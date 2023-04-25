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

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.JobResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.Job;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.SchedulerService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;

/**
 * Defines job controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Controller
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
	 * The scheduler snapshot request mapping.
	 */
	public static final String schedulerSnapshotRequestMapping = schedulerRequestMapping + "/snapshot";

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
		run, pause, expunge
	}

	/**
	 * Defines rescheduler actions.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public enum ReschedulerAction {
		begin, end, swap
	}

	/**
	 * Defines scheduler snapshot types.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public enum SchedulerSnapshotType {
		project, domain, administration
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
	 * @param projectService       The project service.
	 * @since 1.8
	 */
	public JobApiController(ConfigurationService configurationService, SecurityService securityService,
			SchedulerService service, ProjectService projectService) {
		super(ProjectApiController.class, configurationService, securityService, projectService);

		this.service = service;
		this.projectService = projectService;
	}

	/**
	 * Returns the scheduler information in the response body.
	 * 
	 * @return The scheduler information in the response body.
	 * @since 1.8
	 */
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
	@GetMapping(schedulerActionRequestMapping + actionPathVariable)
	public ResponseEntity<SchedulerInformationResponse> schedulerAction(@PathVariable String action) {
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
	 * Returns the scheduler snapshot of given type in the response body.
	 * 
	 * @param type The snapshot type. Available types: project, domain,
	 *             administration.
	 * @param id   The id of the project for the associated jobs. This parameter is
	 *             only required for snapshot type project.
	 * @return The scheduler snapshot in the response body.
	 * @since 1.8
	 */
	@GetMapping(schedulerSnapshotRequestMapping + typePathVariable)
	public ResponseEntity<SchedulerSnapshotResponse> schedulerSnapshot(@PathVariable String type,
			@RequestParam(required = false) String id) {

		SchedulerSnapshotType schedulerSnapshotType;
		try {
			schedulerSnapshotType = SchedulerSnapshotType.valueOf(type);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		try {
			switch (schedulerSnapshotType) {
			case administration:
				return isCoordinator() ? ResponseEntity.ok().body(new SchedulerSnapshotResponse(service, null))
						: ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			case project:
				Authorization authorization = authorizationFactory.authorize(id, ProjectRight.execute);

				return ResponseEntity.ok()
						.body(new SchedulerSnapshotResponse(service, Arrays.asList(authorization.project)));
			case domain:
				if (!isSecured())
					return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();

				Set<Job.Cluster> clusters = new HashSet<>();
				for (Project project : projectService.getProjectsRightExist())
					if (project.isCoordinator()
							|| (!project.getConfiguration().getConfiguration().isStateBlocked() && project.isExecute()))
						clusters.add(project);

				return ResponseEntity.ok().body(new SchedulerSnapshotResponse(service, clusters));
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
	@GetMapping(cancelRequestMapping + jobPathVariable)
	public void cancel(@PathVariable int jobId, @RequestParam(required = false) String type,
			@RequestParam(required = false) String id, HttpServletResponse response) {

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
	@GetMapping(rescheduleRequestMapping + actionPathVariable + jobPathVariable)
	public void reschedule(@PathVariable String action, @PathVariable int jobId,
			@RequestParam(required = false) String type, @RequestParam(required = false) String id,
			@RequestParam(required = false) Integer swap, HttpServletResponse response) {
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
			running, paused
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
	 * Defines scheduler snapshot responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class SchedulerSnapshotResponse implements Serializable {
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
		 * Creates a scheduler snapshot response for the api.
		 * 
		 * @param service  The scheduler service.
		 * @param clusters The clusters to select the jobs under control of the
		 *                 scheduler. If null, all jobs are selected.
		 * @since 1.8
		 */
		public SchedulerSnapshotResponse(SchedulerService service, Collection<Job.Cluster> clusters) {
			super();

			SchedulerService.Snapshot snapshot = service.getSnapshot(clusters);

			scheduled = getJobResponses(snapshot.getScheduled());
			running = getJobResponses(snapshot.getRunning());
			done = getJobResponses(snapshot.getDone());
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
				response.add(new JobResponse(job));

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
