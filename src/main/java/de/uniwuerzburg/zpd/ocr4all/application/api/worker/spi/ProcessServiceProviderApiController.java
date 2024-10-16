/**
 * File:     ProcessServiceProviderApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     23.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.SnapshotRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.JobJsonResponse;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi.ServiceProviderResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.ModelService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.Job;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.SchedulerService;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.Task;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Sandbox;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.SandboxService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessorCore;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessorServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.ProcessFramework;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Target;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;

/**
 * Defines process service provider controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @param <P> The service provider.
 * @param <S> The core service provider type.
 * @since 17
 */
public class ProcessServiceProviderApiController<P extends ProcessorServiceProvider<ProcessorCore.LockSnapshotCallback, ProcessFramework>, S extends de.uniwuerzburg.zpd.ocr4all.application.core.spi.ProcessServiceProvider<P>>
		extends CoreServiceProviderApiController<S> {

	/**
	 * The required project rights.
	 */
	protected final Project.Right[] requiredProjectRights;

	/**
	 * Creates a process service provider controller for the api.
	 * 
	 * @param logger                The logger class.
	 * @param configurationService  The configuration service.
	 * @param securityService       The security service.
	 * @param collectionService     The collection service.
	 * @param modelService          The model service.
	 * @param projectService        The project service.
	 * @param sandboxService        The sandbox service.
	 * @param schedulerService      The scheduler service.
	 * @param type                  The type.
	 * @param service               The service.
	 * @param requiredProjectRights The required project rights.
	 * @since 17
	 */
	protected ProcessServiceProviderApiController(Class<?> logger, ConfigurationService configurationService,
			SecurityService securityService, CollectionService collectionService, ModelService modelService,
			ProjectService projectService, SandboxService sandboxService, SchedulerService schedulerService, Type type,
			S service, Project.Right... requiredProjectRights) {
		super(logger, configurationService, securityService, collectionService, modelService, projectService,
				sandboxService, schedulerService, type, service);

		this.requiredProjectRights = requiredProjectRights;
	}

	/**
	 * Returns true if the service is available.
	 * 
	 * @param project The project.
	 * @param sandbox The sandbox. Null if the service core data does not require
	 *                the sandbox.
	 * @return True if the service is available. However, if the service core data
	 *         requires the sandbox and it is not defined, then returns false.
	 * @since 17
	 */
	private boolean isAvailable(Project project, Sandbox sandbox) {
		// A selected project with required rights is always required
		if (!service.isActiveProviderAvailable() || project == null
				|| !project.getConfiguration().getConfiguration().isStateActive()
				|| !project.isRights(requiredProjectRights))
			return false;

		switch (service.getCoreData()) {
		case project:

			return true;
		case sandbox:
		default:
			if (sandbox == null)
				return false;

			de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Sandbox.State state = sandbox
					.getConfiguration().getConfiguration().getState();

			return de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Sandbox.State.active
					.equals(state)
					|| (de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Sandbox.State.secured
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
	 *                                 for the project/sandbox.
	 * @since 17
	 */
	protected ResponseEntity<List<ServiceProviderResponse>> serviceProviders(Authorization authorization,
			List<Integer> snapshotTrack, String lang) throws ResponseStatusException {
		if (!isAvailable(authorization.project, authorization.sandbox))
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);

		try {
			final Locale locale = getLocale(lang);
			final Target target = authorization.project.getTarget(authorization.sandbox,
					authorization.sandbox == null || snapshotTrack == null ? null
							: authorization.sandbox.getSnapshot(snapshotTrack).getConfiguration());

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
	 *                      sandbox requires a service provider snapshot request.
	 * @param lang          The language. if null, then use the application
	 *                      preferred locale.
	 * @param response      The HTTP-specific functionality in sending a response to
	 *                      the client.
	 * @throws ResponseStatusException Throws if service provider is not an instance
	 *                                 of process or it is not available for the
	 *                                 project/sandbox. Furthermore, an internal
	 *                                 server error is thrown if the service's core
	 *                                 data type is sandbox and the request is not
	 *                                 of type service provider snapshot request.
	 * @since 17
	 */
	public void schedule(Authorization authorization, ServiceProviderRequest request, String lang,
			HttpServletResponse response) throws ResponseStatusException {
		if (!isAvailable(authorization.project, authorization.sandbox))
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);

		authorizeRead(request.getWeights());

		try {
			final P provider = service.getActiveProvider(request.getId());

			if (provider == null)
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

			final Locale locale = getLocale(lang);
			Task task;
			switch (service.getCoreData()) {
			case project:
				task = new Task(configurationService, locale, request.getJobShortDescription(), Job.Processing.parallel,
						authorization.project, provider, request);

				break;
			case sandbox:
			default:
				if (authorization.sandbox == null || request instanceof ServiceProviderSnapshotCoreRequest)
					try {
						final ServiceProviderSnapshotCoreRequest snapshotRequest = (ServiceProviderSnapshotCoreRequest) request;
						final List<Integer> snapshotTrackParent = (request instanceof ServiceProviderSnapshotRequest spsr)
								? spsr.getParentSnapshot().getTrack()
								: null;

						task = new Task(configurationService, locale, request.getJobShortDescription(),
								Job.Processing.parallel, authorization.sandbox, type.getSnapshotType(),
								snapshotTrackParent, snapshotRequest.getLabel(), snapshotRequest.getDescription(),
								provider, request);
					} catch (Exception ex) {
						log(ex);
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
			response.getWriter().write(objectMapper.writeValueAsString(new JobJsonResponse(task.getId(), jobState)));
			response.getWriter().flush();
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines service provider snapshot core requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
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
		 * @since 17
		 */
		public String getLabel() {
			return label;
		}

		/**
		 * Set the label.
		 *
		 * @param label The label to set.
		 * @since 17
		 */
		public void setLabel(String label) {
			this.label = label;
		}

		/**
		 * Returns the description.
		 *
		 * @return The description.
		 * @since 17
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * Set the description.
		 *
		 * @param description The description to set.
		 * @since 17
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
	 * @since 17
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
		 * @since 17
		 */
		public SnapshotRequest getParentSnapshot() {
			return parentSnapshot;
		}

		/**
		 * Set the parent snapshot.
		 *
		 * @param parentSnapshot The parent snapshot to set.
		 * @since 17
		 */
		public void setParentSnapshot(SnapshotRequest parentSnapshot) {
			this.parentSnapshot = parentSnapshot;
		}

	}
}
