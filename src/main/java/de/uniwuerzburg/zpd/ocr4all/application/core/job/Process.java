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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.util.FileSystemUtils;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow.Snapshot;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow.Workflow;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.History;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.project.ProcessHistory;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.ServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Framework;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.Argument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.BooleanArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.DecimalArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.ImageArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.IntegerArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.ModelArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.RecognitionModelArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.SelectArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.StringArgument;

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
	 * The workflow.
	 */
	private final Workflow workflow;

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
	 * @param workflow             The workflow.
	 * @throws IllegalArgumentException Throws if the processing or project argument
	 *                                  is missed or steps is not a positive number.
	 * @since 1.8
	 */
	Process(ConfigurationService configurationService, Locale locale, Processing processing, int steps,
			Workflow workflow) throws IllegalArgumentException {
		this(configurationService, locale, processing, steps, workflow.getProject(), workflow);
	}

	/**
	 * Creates a process.
	 * 
	 * @param configurationService The configuration service.
	 * @param locale               The application locale.
	 * @param processing           The processing mode.
	 * @param steps                The number of steps. This is a positive number.
	 * @param project              The project.
	 * @param workflow             The workflow.
	 * @throws IllegalArgumentException Throws if the processing or project argument
	 *                                  is missed or steps is not a positive number.
	 * @since 1.8
	 */
	Process(ConfigurationService configurationService, Locale locale, Processing processing, int steps, Project project,
			Workflow workflow) throws IllegalArgumentException {
		super(configurationService, locale, processing, steps);

		if (project == null)
			throw new IllegalArgumentException("Process: the project argument is mandatory.");

		this.project = project;
		this.workflow = workflow;
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
				if (job != null && (job instanceof Process)
						&& (this == job
								|| (isProjectType() && ((Process) job).isProjectType()
										&& project.isSame(((Process) job).getProject()))
								|| (isWorkflowType() && ((Process) job).isWorkflowType()
										&& workflow.isSame(((Process) job).getWorkflow()))))
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
		return workflow == null;
	}

	/**
	 * Returns true if it a workflow type.
	 *
	 * @return True if it a workflow type.
	 * @since 1.8
	 */
	public boolean isWorkflowType() {
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
	 * Defines process instances.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	protected class Instance {
		/**
		 * The state. The initial state is initialized.
		 */
		private Job.State state = Job.State.initialized;

		/**
		 * The service provider.
		 */
		private final ProcessServiceProvider serviceProvider;

		/**
		 * The processor for the service provider.
		 */
		private final ProcessServiceProvider.Processor processor;

		/**
		 * The service provider arguments.
		 */
		private final ServiceProvider serviceProviderArgument;

		/**
		 * The snapshot.
		 */
		private final Snapshot snapshot;

		/**
		 * The journal step.
		 */
		private final Journal.Step journal;

		/**
		 * The created time.
		 */
		private final Date created = new Date();

		/**
		 * The start time.
		 */
		private Date start = null;

		/**
		 * The end time.
		 */
		private Date end = null;

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
			this(serviceProvider, serviceProviderArgument, null, journal);
		}

		/**
		 * Creates a process instance with initialized state.
		 * 
		 * @param serviceProvider The service provider.
		 * @param snapshot        The snapshot.
		 * @param journal         The journal step.
		 * @throws IllegalArgumentException Throws if the service provider, the model or
		 *                                  the journal argument is missed.
		 * @since 1.8
		 */
		public Instance(ProcessServiceProvider serviceProvider, Snapshot snapshot, Journal.Step journal)
				throws IllegalArgumentException {
			this(serviceProvider,
					snapshot.getConfiguration().isConsistent()
							? snapshot.getConfiguration().getConfiguration().getMainConfiguration().getServiceProvider()
							: null,
					snapshot, journal);
		}

		/**
		 * Creates a process instance with initialized state.
		 * 
		 * @param serviceProvider         The service provider.
		 * @param serviceProviderArgument The service provider arguments.
		 * @param snapshot                The snapshot.
		 * @param journal                 The journal step.
		 * @throws IllegalArgumentException Throws if the service provider, the model or
		 *                                  the journal argument is missed.
		 * @since 1.8
		 */
		private Instance(ProcessServiceProvider serviceProvider, ServiceProvider serviceProviderArgument,
				Snapshot snapshot, Journal.Step journal) throws IllegalArgumentException {
			super();

			if (serviceProvider == null)
				throw new IllegalArgumentException("Instance: the service provider is mandatory.");

			if (snapshot != null) {
				if (!snapshot.getConfiguration().isConsistent())
					throw new IllegalArgumentException("Instance: the snapshot process is not consistent.");
				else if (snapshot.getConfiguration().getConfiguration().getProcessConfiguration().getState() != null)
					throw new IllegalArgumentException("Instance: the snapshot is already in use.");

				snapshot.getConfiguration().getConfiguration().updateProcess(state);
			}

			if (serviceProviderArgument == null)
				throw new IllegalArgumentException("Instance: the service provider arguments is mandatory.");

			if (journal == null)
				throw new IllegalArgumentException("Instance: the journal step is mandatory.");

			this.serviceProvider = serviceProvider;
			this.snapshot = snapshot;

			processor = this.serviceProvider.newProcessor();
			if (processor == null)
				logger.warn("no processor available for the service provider "
						+ this.serviceProvider.getClass().getName() + ", " + getShortDescription() + ".");

			this.serviceProviderArgument = serviceProviderArgument;
			this.journal = journal;

			// Initializes snapshot process state
			if (snapshot != null)
				snapshot.getConfiguration().getConfiguration().updateProcess(state);
		}

		/**
		 * Returns the service provider model with their arguments.
		 * 
		 * @return The service provider model with their arguments.
		 * @since 1.8
		 */
		protected ModelArgument getModelArgument() {
			List<Argument> arguments = new ArrayList<>();

			/*
			 * The boolean arguments.
			 */
			if (serviceProviderArgument.getBooleans() != null)
				for (de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.BooleanArgument bool : serviceProviderArgument
						.getBooleans())
					if (bool != null)
						arguments.add(new BooleanArgument(bool.getArgument(), bool.getValue()));

			/*
			 * The decimal arguments.
			 */
			if (serviceProviderArgument.getDecimals() != null)
				for (de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.DecimalArgument decimal : serviceProviderArgument
						.getDecimals())
					if (decimal != null)
						arguments.add(new DecimalArgument(decimal.getArgument(), decimal.getValue()));

			/*
			 * The integer arguments.
			 */
			if (serviceProviderArgument.getIntegers() != null)
				for (de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.IntegerArgument integer : serviceProviderArgument
						.getIntegers())
					if (integer != null)
						arguments.add(new IntegerArgument(integer.getArgument(), integer.getValue()));

			/*
			 * The string arguments.
			 */
			if (serviceProviderArgument.getStrings() != null)
				for (de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.StringArgument string : serviceProviderArgument
						.getStrings())
					if (string != null)
						arguments.add(new StringArgument(string.getArgument(), string.getValue()));

			/*
			 * The image arguments.
			 */
			if (serviceProviderArgument.getImages() != null)
				for (de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.ImageArgument image : serviceProviderArgument
						.getImages())
					if (image != null)
						arguments.add(new ImageArgument(image.getArgument(), image.getValues()));

			/*
			 * The recognition model arguments.
			 */
			if (serviceProviderArgument.getRecognitionModels() != null)
				for (de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.RecognitionModelArgument recognitionModel : serviceProviderArgument
						.getRecognitionModels())
					if (recognitionModel != null)
						arguments.add(new RecognitionModelArgument(recognitionModel.getArgument(),
								recognitionModel.getValues()));

			/*
			 * The select arguments.
			 */
			if (serviceProviderArgument.getSelects() != null)
				for (de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.SelectArgument select : serviceProviderArgument
						.getSelects())
					if (select != null)
						arguments.add(new SelectArgument(select.getArgument(), select.getValues()));

			/*
			 * The model argument
			 */
			return new ModelArgument(arguments);
		}

		/**
		 * Returns the short description.
		 * 
		 * @return The short description.
		 * @since 1.8
		 */
		public String getShortDescription() {
			return serviceProvider.getName(locale) + " (v" + serviceProvider.getVersion() + ")";
		}

		/**
		 * Set the process state. If a snapshot is available, its process status is also
		 * updated.
		 * 
		 * @param state The state to set.
		 * @since 1.8
		 */
		private void setState(State state) {
			this.state = state;

			if (snapshot != null)
				snapshot.getConfiguration().getConfiguration().updateProcess(state);
		}

		/**
		 * Schedules the job if it is in initialized state.
		 * 
		 * @since 1.8
		 */
		void schedule() {
			if (State.initialized.equals(state))
				setState(State.scheduled);
		}

		/**
		 * Returns true if the state is scheduled.
		 * 
		 * @return True if the state is scheduled.
		 * @since 1.8
		 */
		public boolean isStateScheduled() {
			return State.scheduled.equals(state);
		}

		/**
		 * Returns true if the state is running.
		 * 
		 * @return True if the state is running.
		 * @since 1.8
		 */
		public boolean isStateRunning() {
			return State.running.equals(state);
		}

		/**
		 * Returns true when the instance is done.
		 * 
		 * @return True when the instance is done.
		 * @since 1.8
		 */
		public boolean isDone() {
			switch (state) {
			case canceled:
			case completed:
			case interrupted:
				return true;

			default:
				return false;
			}
		}

		/**
		 * Returns the state.
		 *
		 * @return The state.
		 * @since 1.8
		 */
		public Job.State getState() {
			return state;
		}

		/**
		 * Returns the created time.
		 *
		 * @return The created time.
		 * @since 1.8
		 */
		public Date getCreated() {
			return created;
		}

		/**
		 * Returns the start time. Null if not started.
		 *
		 * @return The start time.
		 * @since 1.8
		 */
		public Date getStart() {
			return start;
		}

		/**
		 * Returns the end time. Null if not ended.
		 *
		 * @return The end time.
		 * @since 1.8
		 */
		public Date getEnd() {
			return end;
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
					configurationService.getWorkspace().getConfiguration().getConfigurationServiceProvider(),
					project.getTarget(workflow,
							snapshot == null || snapshot.getConfiguration().isRoot() ? null
									: snapshot.getConfiguration().getParent()),
					snapshot == null ? null : snapshot.getConfiguration().getSandbox().getFolder(),
					snapshot == null ? null : snapshot.getConfiguration().getTrack(), temporaryDirectory);
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
			switch (state) {
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
				workflow.add(processHistory);
		}

		/**
		 * Executes the process instance if it is in scheduled state.
		 * 
		 * @return The state of the instance.
		 * @since 1.8
		 */
		public synchronized State execute() {
			if (isStateScheduled()) {
				setState(State.running);
				start = new Date();

				appendHistory();

				ProcessServiceProvider.Processor.State executionState = null;
				if (processor == null)
					journal.setNote("no processor available for the service provider");
				else {
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
						}, framework, getModelArgument());
					} catch (Exception e) {
						journal.setNote(OCR4allUtils.getStackTrace(e));
					} finally {
						if (framework.getTemporary() != null)
							try {
								FileSystemUtils.deleteRecursively(framework.getTemporary());
							} catch (IOException e) {
								logger.warn("cannot delete temporary directory " + framework.getTemporary()
										+ " from service provider " + serviceProvider.getClass().getName() + ", "
										+ serviceProvider.getName(locale) + " (v" + serviceProvider.getVersion()
										+ ") - " + e.getMessage() + ".");
							}
					}
				}

				if (!State.canceled.equals(state)) {
					state = ProcessServiceProvider.Processor.State.completed.equals(executionState) ? State.completed
							: State.interrupted;
					end = new Date();

					appendHistory(journal.getNote());
				}

				// Persist the snapshot core data
				if (snapshot != null)
					snapshot.getConfiguration().getConfiguration().updateProcess(state, journal.getProgress(),
							journal.getStandardOutput(), journal.getStandardError(), journal.getNote());
			}

			return state;
		}

		/**
		 * Cancels the process instance in a new thread if it is not done.
		 * 
		 * @return The state of the instance. It is cancelled if the instance was not
		 *         done. Otherwise, the state of the done instance is returned.
		 * @since 1.8
		 */
		public synchronized State cancel() {
			if (isStateScheduled() || isStateRunning()) {
				final boolean isRunning = State.running.equals(state);

				setState(State.canceled);
				end = new Date();

				appendHistory();

				if (isRunning)
					new Thread(new Runnable() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see java.lang.Runnable#run()
						 */
						@Override
						public void run() {
							try {
								processor.cancel();
							} catch (Exception e) {
								// nothing to do
							}
						}
					}).start();

			}

			return state;
		}
	}
}
