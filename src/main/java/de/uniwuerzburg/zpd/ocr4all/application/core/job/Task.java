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
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Snapshot;
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
	private final Instance instance;

	/**
	 * Creates a task.
	 * 
	 * @param configurationService    The configuration service.
	 * @param locale                  The application locale.
	 * @param processing              The processing mode.
	 * @param project                 The project.
	 * @param serviceProvider         The service provider.
	 * @param serviceProviderArgument The service provider arguments.
	 * @throws IllegalArgumentException Throws if the processing, project, service
	 *                                  provider or model argument is missed.
	 * @since 1.8
	 */
	public Task(ConfigurationService configurationService, Locale locale, Processing processing, Project project,
			ProcessServiceProvider serviceProvider, ServiceProvider serviceProviderArgument)
			throws IllegalArgumentException {
		this(configurationService, locale, processing, project, null, serviceProvider, serviceProviderArgument);
	}

	/**
	 * Creates a task.
	 * 
	 * @param configurationService The configuration service.
	 * @param locale               The application locale.
	 * @param processing           The processing mode.
	 * @param snapshot             The snapshot.
	 * @param serviceProvider      The service provider.
	 * @throws IllegalArgumentException Throws if the processing, project, service
	 *                                  provider or model argument is missed.
	 * @since 1.8
	 */
	public Task(ConfigurationService configurationService, Locale locale, Processing processing, Snapshot snapshot,
			ProcessServiceProvider serviceProvider) throws IllegalArgumentException {
		this(configurationService, locale, processing, snapshot.getSandbox().getProject(), snapshot, serviceProvider,
				null);
	}

	/**
	 * Creates a task.
	 * 
	 * @param configurationService    The configuration service.
	 * @param locale                  The application locale.
	 * @param processing              The processing mode.
	 * @param project                 The project.
	 * @param snapshot                The snapshot.
	 * @param serviceProvider         The service provider.
	 * @param serviceProviderArgument The service provider arguments.
	 * @throws IllegalArgumentException Throws if the processing, project, service
	 *                                  provider or model argument is missed.
	 * @since 1.8
	 */
	private Task(ConfigurationService configurationService, Locale locale, Processing processing, Project project,
			Snapshot snapshot, ProcessServiceProvider serviceProvider, ServiceProvider serviceProviderArgument)
			throws IllegalArgumentException {
		super(configurationService, locale, processing, 1, project, snapshot == null ? null : snapshot.getSandbox());

		instance = snapshot == null ? new Instance(serviceProvider, serviceProviderArgument, getJournal().getStep())
				: new Instance(serviceProvider, snapshot, getJournal().getStep());
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
		return instance.getShortDescription();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#schedule(int)
	 */
	@Override
	boolean schedule(int id) {
		if (super.schedule(id)) {
			instance.schedule();

			return true;
		} else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#execute()
	 */
	@Override
	protected State execute() {
		return instance.execute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#kill()
	 */
	@Override
	protected void kill() {
		instance.cancel();
	}

	/**
	 * Returns the snapshot track.
	 * 
	 * @return The snapshot track. Null if not available
	 * @since 1.8
	 */
	public List<Integer> getSnapshotTrack() {
		return instance.getSnapshotTrack();
	}
}
