/**
 * File:     ServiceProviderCoreApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     23.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker.spi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.SnapshotRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi.ServiceProviderResponse;
import de.uniwuerzburg.zpd.ocr4all.application.api.worker.CoreApiController;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.Job;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.SchedulerService;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.Task;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow.Snapshot;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow.Workflow;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow.WorkflowService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Target;

/**
 * Defines core service provider controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class ServiceProviderCoreApiController<S extends CoreServiceProvider<? extends ServiceProvider>>
		extends CoreApiController {

	/**
	 * The spi prefix path.
	 */
	public static final String spiContextPath = "/spi";

	/**
	 * The spi version 1.0 prefix path.
	 */
	public static final String spiContextPathVersion_1_0 = apiContextPathVersion_1_0 + spiContextPath;

	/**
	 * The providers request mapping.
	 */
	public static final String providersRequestMapping = "/providers";

	/**
	 * The schedule request mapping.
	 */
	public static final String scheduleRequestMapping = "/schedule";

	/**
	 * Define types.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public enum Type {
		imp, launcher, preprocessing, olr, ocr, postcorrection;

		/**
		 * Returns the respective persistence snapshot type.
		 * 
		 * @return The respective persistence snapshot type. Null if not available.
		 * @since 1.8
		 */
		public de.uniwuerzburg.zpd.ocr4all.application.persistence.project.workflow.Snapshot.Type getSnapshotType() {
			switch (this) {
			case launcher:
				return de.uniwuerzburg.zpd.ocr4all.application.persistence.project.workflow.Snapshot.Type.launcher;
			case preprocessing:
				return de.uniwuerzburg.zpd.ocr4all.application.persistence.project.workflow.Snapshot.Type.preprocessing;
			case olr:
				return de.uniwuerzburg.zpd.ocr4all.application.persistence.project.workflow.Snapshot.Type.olr;
			case ocr:
				return de.uniwuerzburg.zpd.ocr4all.application.persistence.project.workflow.Snapshot.Type.ocr;
			case postcorrection:
				return de.uniwuerzburg.zpd.ocr4all.application.persistence.project.workflow.Snapshot.Type.postcorrection;
			case imp:
			default:
				return null;
			}
		}
	}

	/**
	 * The scheduler service.
	 */
	protected final SchedulerService schedulerService;

	/**
	 * The service.
	 */
	protected final S service;

	/**
	 * The type.
	 */
	protected final Type type;

	/**
	 * The required project rights.
	 */
	protected final Project.Right[] requiredProjectRights;

	/**
	 * Creates a core service provider controller for the api.
	 * 
	 * @param logger                The logger class.
	 * @param configurationService  The configuration service.
	 * @param securityService       The security service.
	 * @param projectService        The project service.
	 * @param workflowService       The workflow service.
	 * @param schedulerService      The scheduler service.
	 * @param type                  The type.
	 * @param service               The service.
	 * @param requiredProjectRights The required project rights.
	 * @since 1.8
	 */
	protected ServiceProviderCoreApiController(Class<?> logger, ConfigurationService configurationService,
			SecurityService securityService, ProjectService projectService, WorkflowService workflowService,
			SchedulerService schedulerService, Type type, S service, Project.Right... requiredProjectRights) {
		super(logger, configurationService, securityService, projectService, workflowService);

		this.schedulerService = schedulerService;
		this.type = type;
		this.service = service;
		this.requiredProjectRights = requiredProjectRights;
	}

	/**
	 * Returns true if the service is available.
	 * 
	 * @param project  The project.
	 * @param workflow The workflow. Null if the service core data does not require
	 *                 the workflow.
	 * @return True if the service is available. However, if the service core data
	 *         requires the workflow and it is not defined, then returns false.
	 * @since 1.8
	 */
	protected boolean isAvailable(Project project, Workflow workflow) {
		// A selected project with required rights is always required
		if (!service.isActiveProviderAvailable() || project == null
				|| !project.getConfiguration().getConfiguration().isStateActive()
				|| !project.isRights(requiredProjectRights))
			return false;

		switch (service.getCoreData()) {
		case project:

			return true;
		case workflow:
		default:
			if (workflow == null)
				return false;

			de.uniwuerzburg.zpd.ocr4all.application.persistence.project.workflow.Workflow.State state = workflow
					.getConfiguration().getConfiguration().getState();

			return de.uniwuerzburg.zpd.ocr4all.application.persistence.project.workflow.Workflow.State.active
					.equals(state)
					|| (de.uniwuerzburg.zpd.ocr4all.application.persistence.project.workflow.Workflow.State.secured
							.equals(state) && project.isRights(Project.Right.special));
		}
	}

	/**
	 * Returns the service providers in the response body.
	 * 
	 * @param authorization The authorization.
	 * @param snapshotTrack The snapshot track. Null if not required.
	 * @param lang          The language. if null, then use the application
	 *                      preferred locale.
	 * @return The service providers in the response body.
	 * @throws ResponseStatusException Throws if service providers are not available
	 *                                 for the project/workflow.
	 * @since 1.8
	 */
	protected ResponseEntity<List<ServiceProviderResponse>> serviceProviders(Authorization authorization,
			List<Integer> snapshotTrack, String lang) throws ResponseStatusException {
		if (!isAvailable(authorization.project, authorization.workflow))
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);

		try {
			final Locale locale = getLocale(lang);
			final Target target = authorization.project.getTarget(authorization.workflow,
					authorization.workflow == null || snapshotTrack == null ? null
							: authorization.workflow.getSnapshot(snapshotTrack).getConfiguration());

			final List<ServiceProviderResponse> providers = new ArrayList<>();
			for (CoreServiceProvider<? extends ServiceProvider>.Provider provider : service.getActiveProviders())
				providers.add(new ServiceProviderResponse(locale, type, provider.getId(), provider.getServiceProvider(),
						target));

			Collections.sort(providers, new Comparator<ServiceProviderResponse>() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
				 */
				@Override
				public int compare(ServiceProviderResponse o1, ServiceProviderResponse o2) {
					if (o1.getIndex() != o2.getIndex())
						return o1.getIndex() - o2.getIndex();
					else {
						int compare = o1.getName().compareToIgnoreCase(o2.getName());
						if (compare != 0)
							return compare;
						else
							return o1.getVersion() <= o2.getVersion() ? -1 : 1;
					}
				}
			});

			return ResponseEntity.ok().body(providers);
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Schedules a process to execute the service provider.
	 * 
	 * @param authorization The authorization.
	 * @param request       The service provider request. Services of core data type
	 *                      workflow requires a service provider snapshot request.
	 * @param lang          The language. if null, then use the application
	 *                      preferred locale.
	 * @param response      The HTTP-specific functionality in sending a response to
	 *                      the client.
	 * @throws ResponseStatusException Throws if service provider is not an instance
	 *                                 of process or it is not available for the
	 *                                 project/workflow. Furthermore, an internal
	 *                                 server error is thrown if the service's core
	 *                                 data type is workflow and the request is not
	 *                                 of type service provider snapshot request.
	 * @since 1.8
	 */
	public void schedule(Authorization authorization, ServiceProviderRequest request, String lang,
			HttpServletResponse response) throws ResponseStatusException {
		if (!isAvailable(authorization.project, authorization.workflow))
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);

		try {
			final ServiceProvider provider = service.getActiveProvider(request.getId());

			if (provider == null || !(provider instanceof ProcessServiceProvider))
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

			final Locale locale = getLocale(lang);
			Task task;
			switch (service.getCoreData()) {
			case project:
				task = new Task(configurationService, locale, Job.Processing.parallel, authorization.project,
						(ProcessServiceProvider) provider, request);

				break;
			case workflow:
			default:
				if (authorization.workflow == null || request instanceof ServiceProviderSnapshotCoreRequest)
					try {
						final ServiceProviderSnapshotCoreRequest snapshotRequest = (ServiceProviderSnapshotCoreRequest) request;
						final List<Integer> track = (request instanceof ServiceProviderSnapshotRequest)
								? ((ServiceProviderSnapshotRequest) request).getParentSnapshot().getTrack()
								: null;

						Snapshot snapshot = authorization.workflow.createSnapshot(type.getSnapshotType(), track,
								snapshotRequest.getLabel(), snapshotRequest.getDescription(), request,
								configurationService.getInstance());

						task = new Task(configurationService, locale, Job.Processing.parallel, snapshot,
								(ProcessServiceProvider) provider);
					} catch (Exception ex) {
						ex.printStackTrace();
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
					}
				else
					throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);

				break;
			}

			Job.State jobState = schedulerService.schedule(task);

			ObjectMapper objectMapper = new ObjectMapper();

			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType(applicationJson);
			response.getWriter().write(objectMapper
					.writeValueAsString(new JobJsonResponse(task.getId(), jobState, task.getSnapshotTrack())));
			response.getWriter().flush();
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines JSON objects for scheduled jobs to send responses to clients.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public class JobJsonResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The job id.
		 */
		@JsonProperty("job-id")
		private int jobId;

		/**
		 * The job state.
		 */
		@JsonProperty("job-state")
		private Job.State jobState;

		/**
		 * The snapshot track.
		 */
		@JsonInclude(JsonInclude.Include.NON_NULL)
		@JsonProperty("snapshot-track")
		private List<Integer> snapshotTrack;

		/**
		 * Creates a JSON object for a scheduled job to send responses to clients.
		 * 
		 * @param jobId         The job id.
		 * @param jobState      The job state.
		 * @param snapshotTrack The snapshot track.
		 * @since 1.8
		 */
		public JobJsonResponse(int jobId, Job.State jobState, List<Integer> snapshotTrack) {
			super();

			this.jobId = jobId;
			this.jobState = jobState;

			this.snapshotTrack = snapshotTrack;
		}

		/**
		 * Returns the job id.
		 *
		 * @return The job id.
		 * @since 1.8
		 */
		public int getJobId() {
			return jobId;
		}

		/**
		 * Set the job id.
		 *
		 * @param jobId The job id to set.
		 * @since 1.8
		 */
		public void setJobId(int jobId) {
			this.jobId = jobId;
		}

		/**
		 * Returns the job state.
		 *
		 * @return The job state.
		 * @since 1.8
		 */
		public Job.State getJobState() {
			return jobState;
		}

		/**
		 * Set the job state.
		 *
		 * @param jobState The job state to set.
		 * @since 1.8
		 */
		public void setJobState(Job.State jobState) {
			this.jobState = jobState;
		}

		/**
		 * Returns the snapshot track.
		 *
		 * @return The snapshot track.
		 * @since 1.8
		 */
		public List<Integer> getSnapshotTrack() {
			return snapshotTrack;
		}

		/**
		 * Set the snapshot track.
		 *
		 * @param snapshotTrack The snapshot track to set.
		 * @since 1.8
		 */
		public void setSnapshotTrack(List<Integer> snapshotTrack) {
			this.snapshotTrack = snapshotTrack;
		}

	}

	/**
	 * Defines service provider requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ServiceProviderRequest
			extends de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.ServiceProvider {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Defines service provider snapshot core requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ServiceProviderSnapshotCoreRequest extends ServiceProviderRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The label.
		 */
		@NotNull
		private String label;

		/**
		 * The description.
		 */
		private String description;

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
	 * Defines service provider snapshot requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ServiceProviderSnapshotRequest extends ServiceProviderSnapshotCoreRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The parent snapshot.
		 */
		@NotNull
		@JsonProperty("parent-snapshot")
		private SnapshotRequest parentSnapshot;

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
