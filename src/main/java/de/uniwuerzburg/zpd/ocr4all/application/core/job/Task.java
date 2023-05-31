/**
 * File:     Task.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.job
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     27.01.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.job;

import java.util.List;
import java.util.Locale;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Sandbox;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.ServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessServiceProvider;

/**
 * Defines tasks.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public final class Task extends Process {
	/**
	 * The process instance.
	 */
	private Instance instance = null;

	/**
	 * The short description.
	 */
	private final String shortDescription;

	/**
	 * The snapshot type.
	 */
	private final de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Snapshot.Type snapshotType;

	/**
	 * The track to the parent snapshot. Null if the snapshot being created is the
	 * root.
	 */
	private final List<Integer> snapshotTrackParent;

	/**
	 * The snapshot label.
	 */
	private final String snapshotLabel;

	/**
	 * The snapshot description.
	 */
	private final String snapshotDescription;

	/**
	 * The service provider.
	 */
	private final ProcessServiceProvider serviceProvider;

	/**
	 * The service provider arguments.
	 */
	private final ServiceProvider serviceProviderArgument;

	/**
	 * Creates a task.
	 * 
	 * @param configurationService    The configuration service.
	 * @param locale                  The application locale.
	 * @param shortDescription        The short description. If null, use instance
	 *                                short description.
	 * @param processing              The processing mode.
	 * @param project                 The project.
	 * @param serviceProvider         The service provider.
	 * @param serviceProviderArgument The service provider arguments.
	 * @throws IllegalArgumentException Throws if the processing, project, service
	 *                                  provider or model argument is missed.
	 * @since 1.8
	 */
	public Task(ConfigurationService configurationService, Locale locale, String shortDescription,
			Processing processing, Project project, ProcessServiceProvider serviceProvider,
			ServiceProvider serviceProviderArgument) throws IllegalArgumentException {
		this(configurationService, locale, shortDescription, processing, project, null, null, null, null, null,
				serviceProvider, serviceProviderArgument);
	}

	/**
	 * Creates a task.
	 * 
	 * @param configurationService    The configuration service.
	 * @param locale                  The application locale.
	 * @param shortDescription        The short description. If null, use instance
	 *                                short description.
	 * @param processing              The processing mode.
	 * @param sandbox                 The sandbox.
	 * @param snapshotType            The snapshot type.
	 * @param snapshotTrackParent     The track to the parent snapshot. Null if the
	 *                                snapshot being created is the root.
	 * @param snapshotLabel           The snapshot label.
	 * @param snapshotDescription     The snapshot description.
	 * @param serviceProvider         The service provider.
	 * @param serviceProviderArgument The service provider arguments.
	 * @throws IllegalArgumentException Throws if the processing, project, service
	 *                                  provider or model argument is missed.
	 * @since 1.8
	 */
	public Task(ConfigurationService configurationService, Locale locale, String shortDescription,
			Processing processing, Sandbox sandbox,
			de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Snapshot.Type snapshotType,
			List<Integer> snapshotTrackParent, String snapshotLabel, String snapshotDescription,
			ProcessServiceProvider serviceProvider, ServiceProvider serviceProviderArgument)
			throws IllegalArgumentException {
		this(configurationService, locale, shortDescription, processing, sandbox.getProject(), sandbox, snapshotType,
				snapshotTrackParent, snapshotLabel, snapshotDescription, serviceProvider, serviceProviderArgument);
	}

	/**
	 * Creates a task.
	 * 
	 * @param configurationService    The configuration service.
	 * @param locale                  The application locale.
	 * @param shortDescription        The short description. If null, use instance
	 *                                short description.
	 * @param processing              The processing mode.
	 * @param project                 The project.
	 * @param sandbox                 The sandbox.
	 * @param snapshotType            The snapshot type.
	 * @param snapshotTrackParent     The track to the parent snapshot. Null if the
	 *                                snapshot being created is the root.
	 * @param snapshotLabel           The snapshot label.
	 * @param snapshotDescription     The snapshot description.
	 * @param serviceProvider         The service provider.
	 * @param serviceProviderArgument The service provider arguments.
	 * @throws IllegalArgumentException Throws if the processing, project, service
	 *                                  provider or model argument is missed.
	 * @since 1.8
	 */
	private Task(ConfigurationService configurationService, Locale locale, String shortDescription,
			Processing processing, Project project, Sandbox sandbox,
			de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Snapshot.Type snapshotType,
			List<Integer> snapshotTrackParent, String snapshotLabel, String snapshotDescription,
			ProcessServiceProvider serviceProvider, ServiceProvider serviceProviderArgument)
			throws IllegalArgumentException {
		super(configurationService, locale, processing, 1, project, sandbox);

		this.shortDescription = shortDescription == null || shortDescription.isBlank()
				? serviceProvider.getName(locale) + " (v" + serviceProvider.getVersion() + ")"
				: shortDescription.trim();

		this.snapshotType = snapshotType;
		this.snapshotTrackParent = snapshotTrackParent;
		this.snapshotLabel = snapshotLabel;
		this.snapshotDescription = snapshotDescription;

		this.serviceProvider = serviceProvider;
		this.serviceProviderArgument = serviceProviderArgument;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#getTargetName()
	 */
	@Override
	public String getTargetName() {
		return getProject().getName()
				+ (isSandboxType() ? " (" + getSandbox().getConfiguration().getConfiguration().getName() + ")" : "");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#getShortDescription()
	 */
	@Override
	public String getShortDescription() {
		return shortDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#getThreadPool()
	 */
	@Override
	public SchedulerService.ThreadPool getThreadPool() {
		return SchedulerService.ThreadPool.task;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#getThreadPoolWorkspace()
	 */
	@Override
	public String getThreadPoolWorkspace() {
		return serviceProvider.getThreadPool();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#execute()
	 */
	@Override
	protected State execute() {
		try {
			if (isSandboxType())
				instance = new Instance(serviceProvider,
						getSandbox().createSnapshot(snapshotType, snapshotTrackParent, snapshotLabel,
								snapshotDescription, serviceProviderArgument, configurationService.getInstance()),
						getJournal().getStep());
			else
				instance = new Instance(serviceProvider, serviceProviderArgument, getJournal().getStep());
		} catch (IllegalArgumentException e) {
			getJournal().getStep().setNote(OCR4allUtils.getStackTrace(e));

			return State.interrupted;
		}

		instance.schedule();
		return instance.execute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#kill()
	 */
	@Override
	protected void kill() {
		if (instance != null)
			instance.cancel();
	}
}
