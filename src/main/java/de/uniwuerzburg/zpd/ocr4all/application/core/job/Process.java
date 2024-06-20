/**
 * File:     Process.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.job
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     25.01.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.job;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.util.FileSystemUtils;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Sandbox;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Snapshot;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.History;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.job.ProcessHistory;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.ServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessorCore;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Framework;

/**
 * Defines processes.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public abstract class Process extends Job {
	/**
	 * The logger.
	 */
	protected static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Process.class);

	/**
	 * The project.
	 */
	private final Project project;

	/**
	 * The sandbox.
	 */
	private final Sandbox sandbox;

	/**
	 * Creates a process.
	 * 
	 * @param configurationService The configuration service.
	 * @param locale               The application locale.
	 * @param processing           The processing mode.
	 * @param steps                The number of steps. This is a positive number.
	 * @param project              The project.
	 * @throws IllegalArgumentException Throws if the processing or project argument
	 *                                  is missed or steps is not a positive number.
	 * @since 1.8
	 */
	Process(ConfigurationService configurationService, Locale locale, Processing processing, int steps, Project project)
			throws IllegalArgumentException {
		this(configurationService, locale, processing, steps, project, null);
	}

	/**
	 * Creates a process.
	 * 
	 * @param configurationService The configuration service.
	 * @param locale               The application locale.
	 * @param processing           The processing mode.
	 * @param steps                The number of steps. This is a positive number.
	 * @param sandbox              The sandbox.
	 * @throws IllegalArgumentException Throws if the processing or project argument
	 *                                  is missed or steps is not a positive number.
	 * @since 1.8
	 */
	Process(ConfigurationService configurationService, Locale locale, Processing processing, int steps, Sandbox sandbox)
			throws IllegalArgumentException {
		this(configurationService, locale, processing, steps, sandbox.getProject(), sandbox);
	}

	/**
	 * Creates a process.
	 * 
	 * @param configurationService The configuration service.
	 * @param locale               The application locale.
	 * @param processing           The processing mode.
	 * @param steps                The number of steps. This is a positive number.
	 * @param project              The project.
	 * @param sandbox              The sandbox.
	 * @throws IllegalArgumentException Throws if the processing or project argument
	 *                                  is missed or steps is not a positive number.
	 * @since 1.8
	 */
	Process(ConfigurationService configurationService, Locale locale, Processing processing, int steps, Project project,
			Sandbox sandbox) throws IllegalArgumentException {
		super(configurationService, locale, processing, steps);

		if (project == null)
			throw new IllegalArgumentException("Process: the project argument is mandatory.");

		this.project = project;
		this.sandbox = sandbox;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#depend(java.util.
	 * Collection)
	 */
	@Override
	public Set<Job> depend(Collection<Job> jobs) {
		Set<Job> dependencies = new HashSet<Job>();

		if (jobs != null)
			for (Job job : jobs)
				if (job != null && (job instanceof Process process) && (this == job
						|| (isProjectType() && process.isProjectType() && project.isSame(process.getProject()))
						|| (isSandboxType() && process.isSandboxType() && sandbox.isSame(process.getSandbox()))))
					dependencies.add(job);

		return dependencies;
	}

	/**
	 * Returns true if it a project type.
	 *
	 * @return True if it a project type.
	 * @since 1.8
	 */
	public boolean isProjectType() {
		return sandbox == null;
	}

	/**
	 * Returns true if it a sandbox type.
	 *
	 * @return True if it a sandbox type.
	 * @since 1.8
	 */
	public boolean isSandboxType() {
		return !isProjectType();
	}

	/**
	 * Returns the project.
	 *
	 * @return The project.
	 * @since 1.8
	 */
	public Project getProject() {
		return project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#isExecute()
	 */
	@Override
	public boolean isExecute() {
		return getProject().isExecute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#isSpecial()
	 */
	@Override
	public boolean isSpecial() {
		return getProject().isSpecial();
	}

	/**
	 * Returns the sandbox.
	 *
	 * @return The sandbox.
	 * @since 1.8
	 */
	public Sandbox getSandbox() {
		return sandbox;
	}

	/**
	 * Defines process instances.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public class Instance extends InstanceCore<ProcessServiceProvider> {
		/**
		 * The processor for the service provider.
		 */
		private final ProcessServiceProvider.Processor processor;

		/**
		 * The snapshot.
		 */
		private final Snapshot snapshot;

		/**
		 * The snapshot lock.
		 */
		private SnapshotLock snapshotLock = null;

		/**
		 * True if the snapshot is lockable.
		 */
		private final boolean isSnapshotLockable;

		/**
		 * Creates a process instance with initialized state.
		 * 
		 * @param serviceProvider         The service provider.
		 * @param serviceProviderArgument The service provider arguments.
		 * @param journal                 The journal step.
		 * @throws IllegalArgumentException Throws if the service provider, the model or
		 *                                  the journal argument is missed.
		 * @since 1.8
		 */
		public Instance(ProcessServiceProvider serviceProvider, ServiceProvider serviceProviderArgument,
				Journal.Step journal) throws IllegalArgumentException {
			this(serviceProvider, serviceProviderArgument, null, false, journal);
		}

		/**
		 * Creates a process instance with initialized state.
		 * 
		 * @param serviceProvider The service provider.
		 * @param snapshot        The snapshot. The snapshot is lockable.
		 * @param journal         The journal step.
		 * @throws IllegalArgumentException Throws if the service provider, the model or
		 *                                  the journal argument is missed.
		 * @since 1.8
		 */
		public Instance(ProcessServiceProvider serviceProvider, Snapshot snapshot, Journal.Step journal)
				throws IllegalArgumentException {
			this(serviceProvider, snapshot, true, journal);
		}

		/**
		 * Creates a process instance with initialized state.
		 * 
		 * @param serviceProvider    The service provider.
		 * @param snapshot           The snapshot.
		 * @param isSnapshotLockable True if the snapshot is lockable.
		 * @param journal            The journal step.
		 * @throws IllegalArgumentException Throws if the service provider, the model or
		 *                                  the journal argument is missed.
		 * @since 1.8
		 */
		public Instance(ProcessServiceProvider serviceProvider, Snapshot snapshot, boolean isSnapshotLockable,
				Journal.Step journal) throws IllegalArgumentException {
			this(serviceProvider,
					snapshot.getConfiguration().isConsistent()
							? snapshot.getConfiguration().getConfiguration().getMainConfiguration().getServiceProvider()
							: null,
					snapshot, isSnapshotLockable, journal);
		}

		/**
		 * Creates a process instance with initialized state.
		 * 
		 * @param serviceProvider         The service provider.
		 * @param serviceProviderArgument The service provider arguments.
		 * @param snapshot                The snapshot.
		 * @param isSnapshotLockable      True if the snapshot is lockable.
		 * @param journal                 The journal step.
		 * @throws IllegalArgumentException Throws if the service provider, the model or
		 *                                  the journal argument is missed.
		 * @since 1.8
		 */
		private Instance(ProcessServiceProvider serviceProvider, ServiceProvider serviceProviderArgument,
				Snapshot snapshot, boolean isSnapshotLockable, Journal.Step journal) throws IllegalArgumentException {
			super(serviceProvider, serviceProviderArgument, journal);

			if (snapshot != null) {
				if (!snapshot.getConfiguration().isConsistent())
					throw new IllegalArgumentException("Instance: the snapshot process is not consistent.");
				else if (snapshot.getConfiguration().getConfiguration().getProcessConfiguration().getState() != null)
					throw new IllegalArgumentException("Instance: the snapshot is already in use.");
			}

			this.snapshot = snapshot;
			this.isSnapshotLockable = snapshot != null && isSnapshotLockable;

			processor = serviceProvider.newProcessor();
			if (processor == null)
				logger.warn("no processor available for the service provider "
						+ this.serviceProvider.getClass().getName() + ", " + getShortDescription() + ".");

			// Initializes snapshot process state
			if (this.snapshot != null)
				this.snapshot.getConfiguration().getConfiguration().updateProcess(getState());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.uniwuerzburg.zpd.ocr4all.application.core.job.InstanceCore#setState(de.
		 * uniwuerzburg.zpd.ocr4all.application.core.job.Job.State)
		 */
		@Override
		protected void setState(State state) {
			super.setState(state);

			if (snapshot != null)
				snapshot.getConfiguration().getConfiguration().updateProcess(state);
		}

		/**
		 * Returns the snapshot track.
		 * 
		 * @return The snapshot track. Null if not available
		 * @since 1.8
		 */
		public List<Integer> getSnapshotTrack() {
			return snapshot == null ? null : snapshot.getConfiguration().getTrack();
		}

		/**
		 * Returns the framework for the service provider.
		 * 
		 * @return The framework for the service provider.
		 * @since 1.8
		 */
		private Framework getFramework() {
			Path temporaryDirectory = null;
			try {
				temporaryDirectory = configurationService.getTemporary().getTemporaryDirectory();
			} catch (IOException e) {
				logger.warn("cannot create temporary directory for service provider "
						+ serviceProvider.getClass().getName() + ", " + serviceProvider.getName(locale) + " (v"
						+ serviceProvider.getVersion() + ") - " + e.getMessage() + ".");
			}

			return new Framework(ConfigurationService.getOperatingSystem().getFramework(),
					ConfigurationService.getUID(), ConfigurationService.getGID(),
					new Framework.Application(configurationService.getApplication().getLabel(),
							configurationService.getApplication().getName(),
							configurationService.getApplication().getDateFormat()),
					project.getUser(),
					project.getTarget(sandbox,
							snapshot == null || snapshot.getConfiguration().isRoot() ? null
									: snapshot.getConfiguration().getParent()),
					snapshot == null ? null : snapshot.getConfiguration().getSandbox().getFolder(), getSnapshotTrack(),
					configurationService.getWorkspace().getProjects().getFolder(), temporaryDirectory);
		}

		/**
		 * Appends the instance message to the project history without note.
		 * 
		 * @since 1.8
		 */
		private void appendHistory() {
			appendHistory(null);
		}

		/**
		 * Appends the instance message to the project history.
		 * 
		 * @param note The note. Null if not required.
		 * @since 1.8
		 */
		private void appendHistory(String note) {
			ProcessHistory.Action action;
			History.Level level;
			switch (getState()) {
			case running:
				action = ProcessHistory.Action.startet;
				level = History.Level.info;

				break;
			case completed:
				action = ProcessHistory.Action.completed;
				level = History.Level.info;

				break;
			case canceled:
				action = ProcessHistory.Action.canceled;
				level = History.Level.warn;

				break;
			default:
				action = ProcessHistory.Action.interrupted;
				level = History.Level.error;

				break;
			}

			ProcessHistory processHistory = new ProcessHistory(level, configurationService.getInstance(), action,
					getId(), getJournal().getSize(), journal.getIndex() + 1, journal.getProgress(),
					journal.getStandardOutput(), journal.getStandardError(), serviceProviderArgument,
					serviceProvider.getName(locale), serviceProvider.getVersion(),
					serviceProvider.getDescription(locale).orElse(null), note);

			if (isProjectType())
				project.add(processHistory);
			else
				sandbox.add(processHistory);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.uniwuerzburg.zpd.ocr4all.application.core.job.InstanceCore#executeCallback
		 * ()
		 */
		@Override
		protected void executeCallback() {
			appendHistory();

			ProcessServiceProvider.Processor.State executionState = null;
			if (processor == null)
				journal.setNote("no processor available for the service provider");
			else {
				journal.setFurtherInformation(new StepFurtherInformation());

				Framework framework = getFramework();
				try {
					executionState = processor.execute(new ProcessServiceProvider.Processor.Callback() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see de.uniwuerzburg.zpd.ocr4all.application.core.spi.provider.
						 * ProcessServiceProvider.Processor.Callback#updatedProgress(float)
						 */
						@Override
						public void updatedProgress(float progress) {
							journal.setProgress(progress);
						}

						/*
						 * (non-Javadoc)
						 * 
						 * @see de.uniwuerzburg.zpd.ocr4all.application.core.spi.provider.
						 * ProcessServiceProvider.Processor.Callback#updatedStandardOutput(java.lang.
						 * String)
						 */
						@Override
						public void updatedStandardOutput(String message) {
							journal.setStandardOutput(message);
						}

						/*
						 * (non-Javadoc)
						 * 
						 * @see de.uniwuerzburg.zpd.ocr4all.application.core.spi.provider.
						 * ProcessServiceProvider.Processor.Callback#updatedStandardError(java.lang.
						 * String)
						 */
						@Override
						public void updatedStandardError(String message) {
							journal.setStandardError(message);
						}

						/*
						 * (non-Javadoc)
						 * 
						 * @see de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessServiceProvider.
						 * Processor.Callback#lockSnapshot(java.lang.String)
						 */
						@Override
						public void lockSnapshot(String comment) {
							if (isSnapshotLockable)
								snapshotLock = new SnapshotLock(comment);
							else
								journal.addNote("the snapshot is not lockable, the request with the message '" + comment
										+ "' will be ignored.");
						}
					}, framework, getModelArgument());
				} catch (Exception e) {
					journal.addNote(OCR4allUtils.getStackTrace(e));
				} finally {
					if (framework.getTemporary() != null)
						try {
							FileSystemUtils.deleteRecursively(framework.getTemporary());
						} catch (IOException e) {
							logger.warn("cannot delete temporary directory " + framework.getTemporary()
									+ " from service provider " + serviceProvider.getClass().getName() + ", "
									+ serviceProvider.getName(locale) + " (v" + serviceProvider.getVersion() + ") - "
									+ e.getMessage() + ".");
						}
				}
			}

			if (!State.canceled.equals(getState())) {
				setState(ProcessServiceProvider.Processor.State.completed.equals(executionState) ? State.completed
						: State.interrupted);
				setEnd();

				appendHistory(journal.getNote());
			}

			// Persist the snapshot core data
			if (snapshot != null) {
				snapshot.getConfiguration().getConfiguration().updateProcess(getState(), journal.getProgress(),
						journal.getStandardOutput(), journal.getStandardError(), journal.getNote());

				if (snapshotLock != null)
					snapshot.getConfiguration().getConfiguration().lockSnapshot(serviceProviderArgument.getId(),
							snapshotLock.getComment());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.uniwuerzburg.zpd.ocr4all.application.core.job.InstanceCore#cancelCallback(
		 * )
		 */
		@Override
		protected ProcessorCore cancelCallback() {
			appendHistory();

			return processor;
		}

		/**
		 * Defines further information for journal steps.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public class StepFurtherInformation implements JournalStepFurtherInformation {
			/**
			 * Returns the snapshot track.
			 * 
			 * @return The snapshot track. Null if not set.
			 * @since 1.8
			 */
			public List<Integer> getSnapshotTrack() {
				return snapshot == null ? null : snapshot.getConfiguration().getTrack();
			}

			/**
			 * Returns the service provider id.
			 * 
			 * @return The service provider id.
			 * @since 1.8
			 */
			public String getServiceProviderId() {
				return serviceProvider.getClass().getName();
			}
		}

		/**
		 * SnapshotLock is an immutable class that defines snapshot locks.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		private class SnapshotLock {
			/**
			 * The comment.
			 */
			private final String comment;

			/**
			 * Creates a snapshot lock.
			 * 
			 * @param comment The comment.
			 * @since 1.8
			 */
			public SnapshotLock(String comment) {
				super();

				this.comment = comment;
			}

			/**
			 * Returns the comment.
			 *
			 * @return The comment.
			 * @since 1.8
			 */
			public String getComment() {
				return comment;
			}

		}
	}
}
