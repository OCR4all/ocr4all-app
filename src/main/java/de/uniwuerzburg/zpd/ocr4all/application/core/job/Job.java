/**
 * File:     Job.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.job
 *
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.01.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;

/**
 * Defines jobs for scheduler.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public abstract class Job {
	/**
	 * Defines job states.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public enum State {
		/**
		 * The initialized state.
		 */
		initialized,
		/**
		 * The scheduled state.
		 */
		scheduled,
		/**
		 * The running state.
		 */
		running,
		/**
		 * The completed state.
		 */
		completed,
		/**
		 * The canceled state.
		 */
		canceled,
		/**
		 * The interrupted state.
		 */
		interrupted;

		/**
		 * Returns the respective persistence state.
		 *
		 * @return The persistence state.
		 * @since 1.8
		 */
		public de.uniwuerzburg.zpd.ocr4all.application.persistence.job.Process.State getPersistence() {
			switch (this) {
			case scheduled:
				return de.uniwuerzburg.zpd.ocr4all.application.persistence.job.Process.State.scheduled;
			case running:
				return de.uniwuerzburg.zpd.ocr4all.application.persistence.job.Process.State.running;
			case completed:
				return de.uniwuerzburg.zpd.ocr4all.application.persistence.job.Process.State.completed;
			case canceled:
				return de.uniwuerzburg.zpd.ocr4all.application.persistence.job.Process.State.canceled;
			case interrupted:
				return de.uniwuerzburg.zpd.ocr4all.application.persistence.job.Process.State.interrupted;
			case initialized:
				return de.uniwuerzburg.zpd.ocr4all.application.persistence.job.Process.State.initialized;
			default:
				return null;
			}
		}

		/**
		 * Returns the respective state for given persistence state.
		 *
		 * @param state The persistence state.
		 * @return The state. Null if the given state is null.
		 * @since 1.8
		 */
		public static State getState(de.uniwuerzburg.zpd.ocr4all.application.persistence.job.Process.State state) {
			if (state == null)
				return null;
			else
				switch (state) {
				case scheduled:
					return State.scheduled;
				case running:
					return State.running;
				case completed:
					return State.completed;
				case canceled:
					return State.canceled;
				case interrupted:
					return State.interrupted;
				case initialized:
					return State.initialized;
				default:
					return null;
				}
		}
	}

	/**
	 * Defines processing modes.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public enum Processing {
		/**
		 * Sequential processing.
		 */
		sequential,
		/**
		 * Parallel processing.
		 */
		parallel
	}

	/**
	 * The logger.
	 */
	protected static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Job.class);

	/**
	 * The configuration service.
	 */
	protected final ConfigurationService configurationService;

	/**
	 * The application locale.
	 */
	protected final Locale locale;

	/**
	 * The id. 0 if not set, this means, it is not under the control of the
	 * scheduler.
	 */
	private int id = 0;

	/**
	 * The processing mode.
	 */
	private final Processing processing;

	/**
	 * The state. The initial state is initialized.
	 */
	private State state = State.initialized;

	/**
	 * The journal.
	 */
	private final Journal journal;

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
	 * Creates a job
	 *
	 * @param configurationService The configuration service.
	 * @param locale               The application locale.
	 * @param processing           The processing mode.
	 * @param steps                The number of steps. This is a positive number.
	 * @throws IllegalArgumentException Throws if the processing argument is missed
	 *                                  or steps is not a positive number.
	 * @since 1.8
	 */
	Job(ConfigurationService configurationService, Locale locale, Processing processing, int steps)
			throws IllegalArgumentException {
		super();

		this.configurationService = configurationService;
		this.locale = locale;

		if (processing == null)
			throw new IllegalArgumentException("Job: the processing argument is mandatory.");
		this.processing = processing;

		journal = new Journal(steps);
	}

	/**
	 * Returns the short description.
	 *
	 * @return The short description.
	 * @since 1.8
	 */
	public abstract String getShortDescription();

	/**
	 * Returns the thread pool.
	 *
	 * @return The thread pool.
	 * @since 1.8
	 */
	public abstract SchedulerService.ThreadPool getThreadPool();

	/**
	 * Returns true if the workspace thread pool is set.
	 *
	 * @return True if the workspace thread pool is set.
	 * @since 1.8
	 */
	public final boolean isThreadPoolWorkspace() {
		return getThreadPoolWorkspace() != null;
	}

	/**
	 * Returns the workspace thread pool. Extending classes can overwrite this
	 * method to set a pool.
	 *
	 * @return The thread pool. Null if not set.
	 * @since 1.8
	 */
	public String getThreadPoolWorkspace() {
		return null;
	}

	/**
	 * Returns true if execute or special right is available.
	 *
	 * @return True if execute or special right is available.
	 * @since 1.8
	 */
	public abstract boolean isExecute();

	/**
	 * Returns true if special right is available.
	 *
	 * @return True if special right is available.
	 * @since 1.8
	 */
	public abstract boolean isSpecial();

	/**
	 * Returns the journal.
	 *
	 * @return The journal.
	 * @since 1.8
	 */
	public Journal getJournal() {
		return journal;
	}

	/**
	 * Returns the jobs in the given collection that the current job depends on,
	 * this means the jobs that the current job must wait for to finish before it
	 * can run.
	 *
	 * @param jobs The jobs to test.
	 * @return The jobs in the given collection that the current job depends on.
	 * @since 1.8
	 */
	public abstract Set<Job> depend(Collection<Job> jobs);

	/**
	 * Executes the job if it is in scheduled state.
	 *
	 * @return The end state of the execution, this means, canceled, completed or
	 *         interrupted.
	 * @since 1.8
	 */
	protected abstract State execute();

	/**
	 * Kills the job if it is not done.
	 *
	 * @since 1.8
	 */
	protected abstract void kill();

	/**
	 * Returns the id. 0 if not set, this means, it is not under the control of the
	 * scheduler.
	 *
	 * @return The id.
	 * @since 1.8
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns true if the processing mode is sequential.
	 *
	 * @return True if the processing mode is sequential.
	 * @since 1.8
	 */
	public boolean isProcessingSequential() {
		return Processing.sequential.equals(processing);
	}

	/**
	 * Returns true if the processing mode is parallel.
	 *
	 * @return True if the processing mode is parallel.
	 * @since 1.8
	 */
	public boolean isProcessingParallel() {
		return Processing.parallel.equals(processing);
	}

	/**
	 * Returns the processing mode.
	 *
	 * @return The processing mode.
	 * @since 1.8
	 */
	public Processing getProcessing() {
		return processing;
	}

	/**
	 * Returns true if the job is under the control of the scheduler.
	 *
	 * @return True if the job is under the control of the scheduler.
	 * @since 1.8
	 */
	public boolean isSchedulerControl() {
		return !State.initialized.equals(state);
	}

	/**
	 * Schedules the job if it is not under the control of the scheduler and the
	 * given id is greater than 0.
	 *
	 * @param id The job id.
	 * @return True if the job was scheduled.
	 * @since 1.8
	 */
	boolean schedule(int id) {
		if (!isSchedulerControl() && id > 0) {
			state = State.scheduled;
			this.id = id;

			return true;
		} else
			return false;
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
	 * Returns true if the job is done.
	 *
	 * @return True if the job is done.
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
	public State getState() {
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
	 * Starts the job in a new thread if it is in scheduled state.
	 *
	 * @param taskExecutor The task executor.
	 * @param callback     The callback method when the job finishes. If null, no
	 *                     callback is performed.
	 * @return The job state.
	 * @since 1.8
	 */
	synchronized State start(ThreadPoolTaskExecutor taskExecutor, Callback callback) {
		if (isStateScheduled()) {
			state = State.running;
			start = new Date();

			taskExecutor.execute(() -> {
				logger.info("Start execution of job ID " + getId() + ".");

				State executionState = execute();

				if (!State.canceled.equals(state)) {
					state = State.completed.equals(executionState) ? State.completed : State.interrupted;

					end = new Date();
				}

				if (callback != null)
					callback.done(Job.this);

				logger.info("End execution of the job ID " + getId() + ".");
			});
		}

		return state;
	}

	/**
	 * Cancels the job in a new thread if it is not done.
	 *
	 * @return The state of the job. It is cancelled if the job was not done.
	 *         Otherwise, the state of the done job is returned.
	 * @since 1.8
	 */
	synchronized State cancel() {
		if (isStateScheduled() || isStateRunning()) {
			final boolean isRunning = State.running.equals(state);

			state = State.canceled;
			end = new Date();

			if (isRunning)
				new Thread(new Runnable() {
					/*
					 * (non-Javadoc)
					 *
					 * @see java.lang.Runnable#run()
					 */
					@Override
					public void run() {
						kill();
					}
				}).start();
		}

		return state;
	}

	/**
	 * Defines callback.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	@FunctionalInterface
	public interface Callback {
		/**
		 * Callback method at the end of the job.
		 *
		 * @param job The job that has been done.
		 * @since 1.8
		 */
		public void done(Job job);
	}

	/**
	 * Defines clusters.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	@FunctionalInterface
	public interface Cluster {
		/**
		 * Returns the jobs that are associated of the entity.
		 *
		 * @param jobs The jobs to be reviewed.
		 * @return The jobs that are associated of the entity.
		 * @since 1.8
		 */
		public Set<Job> associated(Collection<Job> jobs);
	}

	/**
	 * Defines further information for journal steps.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public interface JournalStepFurtherInformation {
	}

	/**
	 * Defines journals.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public class Journal {
		/**
		 * The steps.
		 */
		private final List<Step> steps = new ArrayList<>();

		/**
		 * The index of the running step. The first index is 0. -1 if not set. In a
		 * journal with exactly one step, this value is 0.
		 */
		private int index;

		/**
		 * Creates a job journal.
		 *
		 * @param steps The number of steps. This is a positive number.
		 * @throws IllegalArgumentException Throw if the number of steps is not a
		 *                                  positive number.
		 * @since 1.8
		 */
		private Journal(int steps) throws IllegalArgumentException {
			super();

			if (steps < 1)
				throw new IllegalArgumentException("Journal: the number of steps has to be a positive number.");

			for (int index = 0; index < steps; index++)
				this.steps.add(new Step(index));

			resetIndex();
		}

		/**
		 * Returns true if the journal is completed, this means, all its steps are
		 * completed.
		 *
		 * @return True if the journal is completed.
		 * @since 1.8
		 */
		public boolean isCompleted() {
			for (Step step : steps)
				if (!step.isCompleted())
					return false;

			return true;
		}

		/**
		 * Initializes the journal.
		 *
		 * @since 1.8
		 */
		void initialize() {
			resetIndex();

			for (Step step : steps)
				step.initialize();
		}

		/**
		 * Completes the journal.
		 *
		 * @since 1.8
		 */
		void complete() {
			for (Step step : steps)
				step.complete();
		}

		/**
		 * Returns the size, this means, the number of steps.
		 *
		 * @return The size.
		 * @since 1.8
		 */
		public int getSize() {
			return steps.size();
		}

		/**
		 * Returns the steps.
		 *
		 * @return The steps.
		 * @since 1.8
		 */
		public List<Step> getSteps() {
			return new ArrayList<>(steps);
		}

		/**
		 * Returns the running step. In a journal with exactly one step, the running
		 * step is this one.
		 *
		 * @return The running step. Null if not set.
		 * @since 1.8
		 */
		public Step getStep() {
			return isIndexSet() ? steps.get(index) : null;
		}

		/**
		 * Returns true if the index of the running step is set.
		 *
		 * @return True if the index of the running step is set.
		 * @since 1.8
		 */
		public boolean isIndexSet() {
			return index >= 0;
		}

		/**
		 * Returns the index of the running step. The first index is 0. In a journal
		 * with exactly one step, this value is 0.
		 *
		 * @return The index of the running step. -1 if not set.
		 * @since 1.8
		 */
		public int getIndex() {
			return index;
		}

		/**
		 * Set the index of the running step if there are more than one step. Otherwise,
		 * the index remain 0.
		 *
		 * @param index The index to set. Its range is between 0 (inclusive) and the
		 *              number of steps (exclusive). If the given index is out of range,
		 *              then resets the index of the running step.
		 * @since 1.8
		 */
		void setIndex(int index) {
			if (steps.size() > 1) {
				if (0 <= index && index < steps.size())
					this.index = index;
				else
					resetIndex();
			}
		}

		/**
		 * Sets the running step to the next feasible index. If the index is not set,
		 * then set it to 0. If the index is the last, then do nothing.
		 *
		 * @since 1.8
		 */
		void nextIndex() {
			if (steps.size() > 1) {
				if (index < 0)
					index = 0;
				else if (index + 1 < steps.size())
					index++;
			}
		}

		/**
		 * Reset the index of the running step, this means, if there are one step the
		 * value remains 0. Otherwise, it is set to -1.
		 *
		 * @since 1.8
		 */
		void resetIndex() {
			index = steps.size() == 1 ? 0 : -1;
		}

		/**
		 * Returns the expected journal progress taking into account all steps.
		 *
		 * @return The expected journal progress.
		 * @since 1.8
		 */
		public float getProgress() {
			float expectedProgress = 0;

			for (Step step : steps)
				expectedProgress += step.getProgress();

			return expectedProgress / steps.size();
		}

		/**
		 * Defines steps for journals.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public class Step {
			/**
			 * The index. The first index is 0.
			 */
			private final int index;

			/**
			 * The progress. This is a value between 0 and 1 inclusive. The initial value is
			 * 0.
			 */
			private float progress;

			/**
			 * The standard output message. Null if not set.
			 */
			private String standardOutput;

			/**
			 * The standard error message. Null if not set.
			 */
			private String standardError;

			/**
			 * The note.
			 */
			private String note;

			/**
			 * The further information.
			 */
			private JournalStepFurtherInformation furtherInformation;

			/**
			 * Default constructor for a step in a journal.
			 *
			 * @param index The index. The first index is 0.
			 * @since 1.8
			 */
			private Step(int index) {
				super();

				this.index = index;

				initialize();
			}

			/**
			 * Returns the index. The first index is 0.
			 *
			 * @return The index.
			 * @since 1.8
			 */
			public int getIndex() {
				return index;
			}

			/**
			 * Returns true if the progress is completed, this means, its progress is 1.
			 *
			 * @return True if the progress is completed.
			 * @since 1.8
			 */
			public boolean isCompleted() {
				return progress == 1;
			}

			/**
			 * Returns the progress.
			 *
			 * @return The progress.
			 * @since 1.8
			 */
			public float getProgress() {
				return progress;
			}

			/**
			 * Set the progress. This is a progress between 0 and 1 inclusive.
			 *
			 * @param progress The progress to set.
			 * @since 1.8
			 */
			void setProgress(float progress) {
				if (progress >= 1)
					this.progress = 1;
				else if (progress <= 0)
					this.progress = 0;
				else
					this.progress = progress;
			}

			/**
			 * Resets the progress.
			 *
			 * @since 1.8
			 */
			void resetProgress() {
				progress = 0;
			}

			/**
			 * Returns true if the standard output message is set.
			 *
			 * @return True if the standard output message is set.
			 * @since 1.8
			 */
			public boolean isStandardOutputSet() {
				return standardOutput != null;
			}

			/**
			 * Returns the standard output message. Null if not set.
			 *
			 * @return The standard output message.
			 * @since 1.8
			 */
			public String getStandardOutput() {
				return standardOutput;
			}

			/**
			 * Set the standard output message.
			 *
			 * @param message The message to set.
			 * @since 1.8
			 */
			void setStandardOutput(String message) {
				standardOutput = message;
			}

			/**
			 * Resets the standard output message.
			 *
			 * @since 1.8
			 */
			void resetStandardOutput() {
				standardOutput = null;
			}

			/**
			 * Returns true if the standard error message is set.
			 *
			 * @return True if the standard error message is set.
			 * @since 1.8
			 */
			public boolean isStandardErrorSet() {
				return standardError != null;
			}

			/**
			 * Returns the standard error message. Null if not set.
			 *
			 * @return The standard error.
			 * @since 1.8
			 */
			public String getStandardError() {
				return standardError;
			}

			/**
			 * Set the standard error message.
			 *
			 * @param message The message to set.
			 * @since 1.8
			 */
			void setStandardError(String message) {
				standardError = message;
			}

			/**
			 * Resets the standard error message.
			 *
			 * @since 1.8
			 */
			void resetStandardError() {
				standardError = null;
			}

			/**
			 * Returns true if the note is set.
			 *
			 * @return True if the note is set.
			 * @since 1.8
			 */
			public boolean isNoteSet() {
				return note != null;
			}

			/**
			 * Returns the note.
			 *
			 * @return The note.
			 * @since 1.8
			 */
			public String getNote() {
				return note;
			}

			/**
			 * Set the note.
			 *
			 * @param note The note to set.
			 * @since 1.8
			 */
			public void setNote(String note) {
				this.note = note;
			}

			/**
			 * Adds the note to the end.
			 *
			 * @param note The note to add.
			 * @since 1.8
			 */
			public void addNote(String note) {
				if (isNoteSet()) {
					if (note != null)
						this.note = this.note + "\n" + note;
				} else
					setNote(note);
			}

			/**
			 * Reset the note.
			 *
			 * @since 1.8
			 */
			public void resetNote() {
				note = null;
			}

			/**
			 * Returns true if the further information is set.
			 *
			 * @return True if the further information is set.
			 * @since 1.8
			 */
			public boolean isFurtherInformationSet() {
				return furtherInformation != null;
			}

			/**
			 * Returns the further information.
			 *
			 * @return The further information.
			 * @since 1.8
			 */
			public JournalStepFurtherInformation getFurtherInformation() {
				return furtherInformation;
			}

			/**
			 * Set the further information.
			 *
			 * @param furtherInformation The further information to set.
			 * @since 1.8
			 */
			public void setFurtherInformation(JournalStepFurtherInformation furtherInformation) {
				this.furtherInformation = furtherInformation;
			}

			/**
			 * Reset the further information.
			 *
			 * @since 1.8
			 */
			public void resetFurtherInformation() {
				furtherInformation = null;
			}

			/**
			 * Initializes the step.
			 *
			 * @since 1.8
			 */
			void initialize() {
				resetProgress();

				resetStandardOutput();
				resetStandardError();

				resetNote();

				resetFurtherInformation();
			}

			/**
			 * Completes the step, this means, sets the progress to 1.
			 *
			 * @since 1.8
			 */
			void complete() {
				setProgress(1);
			}
		}
	}

}
