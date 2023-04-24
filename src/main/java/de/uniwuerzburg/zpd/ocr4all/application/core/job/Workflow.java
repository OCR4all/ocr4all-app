/**
 * File:     Workflow.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.job
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     24.04.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.job;

import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Sandbox;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Snapshot;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Snapshot.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.Processor;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessServiceProvider;

/**
 * Defines workflows.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Workflow extends Process {
	/**
	 * The root snapshot.
	 */
	private final de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Snapshot rootSnapshot;

	/**
	 * The UUID.
	 */
	private final String uuid;

	/**
	 * The providers. The key is the path ID.
	 */
	private final Hashtable<String, Provider> providers;

	/**
	 * The provider paths.
	 */
	private final List<de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.Path> paths;

	/**
	 * The running process instance. Null if not started.
	 */
	private Instance instance = null;

	/**
	 * Creates a workflow.
	 * 
	 * @param configurationService The configuration service.
	 * @param locale               The application locale.
	 * @param processing           The processing mode.
	 * @param steps                The number of steps. This is a positive number.
	 * @param project              The project.
	 * @param sandbox              The sandbox.
	 * @param rootSnapshot         The root snapshot.
	 * @param uuid                 The UUID.
	 * @param providers            The providers. The key is the path ID.
	 * @param paths                The provider paths.
	 * @throws IllegalArgumentException Throws if the processing, project, sandbox,
	 *                                  root snapshot or uuid argument is missed or
	 *                                  steps is not a positive number or providers
	 *                                  or its paths in null.
	 * @since 1.8
	 */
	public Workflow(ConfigurationService configurationService, Locale locale, Processing processing, int steps,
			Project project, Sandbox sandbox,
			de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Snapshot rootSnapshot, String uuid,
			Hashtable<String, Provider> providers,
			List<de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.Path> paths)
			throws IllegalArgumentException {
		super(configurationService, locale, processing, steps, project, sandbox);

		if (sandbox == null)
			throw new IllegalArgumentException("Workflow: the sandbox argument is mandatory.");

		if (rootSnapshot == null)
			throw new IllegalArgumentException("Workflow: the root snapshot argument is mandatory.");

		if (providers == null)
			throw new IllegalArgumentException("Workflow: no provider available.");

		if (paths == null)
			throw new IllegalArgumentException("Workflow: no provider path available.");

		this.uuid = uuid == null || uuid.isBlank() ? null : uuid.trim();
		if (uuid == null)
			throw new IllegalArgumentException("Workflow: no workflow uuid available.");

		this.rootSnapshot = rootSnapshot;
		this.providers = providers;
		this.paths = paths;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#getTargetName()
	 */
	@Override
	public String getTargetName() {
		return getProject().getName() + " (" + getSandbox().getConfiguration().getConfiguration().getName() + ")";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#getShortDescription()
	 */
	@Override
	public String getShortDescription() {
		return "workflow  ID " + uuid + " / root snaptshot track " + rootSnapshot.getConfiguration().getTrack()
				+ (instance == null ? "" : instance.getShortDescription());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#execute()
	 */
	@Override
	protected State execute() {
		return execute(rootSnapshot, paths, 0);
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

	/**
	 * Executes the workflow defined in the paths using depth-first search.
	 * 
	 * @param parentSnapshot The parent snapshot.
	 * @param paths          The paths.
	 * @param step           The current step.
	 * @return The end state of the execution, this means, canceled, completed or
	 *         interrupted.
	 * @since 1.8
	 */
	private State execute(de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Snapshot parentSnapshot,
			List<de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.Path> paths, int step) {
		if (paths != null)
			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.Path path : paths)
				if (path != null) {
					getJournal().setIndex(step);

					Provider provider = providers.get(path.getId());
					if (provider == null) {
						getJournal().getStep().setNote("unknown service provider with path ID '" + path.getId() + "'.");

						return State.interrupted;
					}

					de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Snapshot snapshot;
					try {
						snapshot = getSandbox().createSnapshot(provider.getSnapshotType(),
								parentSnapshot.getConfiguration().getTrack(), provider.getProcessor().getLabel(),
								provider.getProcessor().getDescription(), provider.getProcessor(),
								configurationService.getInstance());

						instance = new Instance(provider.getServiceProvider(), snapshot, getJournal().getStep());
					} catch (IllegalArgumentException e) {
						getJournal().getStep().setNote(OCR4allUtils.getStackTrace(e));

						return State.interrupted;
					}

					instance.schedule();

					State state = instance.execute();
					if (!State.completed.equals(state))
						return state;

					state = execute(snapshot, path.getChildren(), step++);
					if (!State.completed.equals(state))
						return state;
				}

		return State.completed;
	}

	/**
	 * Provider is an immutable class that defines workflow providers.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Provider {
		/**
		 * The service provider.
		 */
		private final ProcessServiceProvider serviceProvider;

		/**
		 * The snapshot type.
		 */
		private final Snapshot.Type snapshotType;

		/**
		 * The processor.
		 */
		private final Processor processor;

		/**
		 * Creates a workflow provider.
		 * 
		 * @param provider     The service provider.
		 * @param snapshotType The snapshot type.
		 * @since 1.8
		 */
		public Provider(ProcessServiceProvider serviceProvider, Type snapshotType, Processor processor) {
			super();
			this.serviceProvider = serviceProvider;
			this.snapshotType = snapshotType;
			this.processor = processor;
		}

		/**
		 * Returns the service provider.
		 *
		 * @return The service provider.
		 * @since 1.8
		 */
		public ProcessServiceProvider getServiceProvider() {
			return serviceProvider;
		}

		/**
		 * Returns the snapshot type.
		 *
		 * @return The snapshot type.
		 * @since 1.8
		 */
		public Snapshot.Type getSnapshotType() {
			return snapshotType;
		}

		/**
		 * Returns the processor.
		 *
		 * @return The processor.
		 * @since 1.8
		 */
		public Processor getProcessor() {
			return processor;
		}

	}

}
