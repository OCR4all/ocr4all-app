/**
 * File:     JobResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     21.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.uniwuerzburg.zpd.ocr4all.application.core.job.Job;

/**
 * Defines job responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class JobResponse implements Serializable {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The id.
	 */
	private int id;

	/**
	 * True if execute or special right is available.
	 */
	private boolean isExecute;

	/**
	 * True if special right is available.
	 */
	private boolean isSpecial;

	/**
	 * The created time.
	 */
	private Date created;

	/**
	 * The start time.
	 */
	private Date start;

	/**
	 * The end time.
	 */
	private Date end;

	/**
	 * The state.
	 */
	private Job.State state;

	/**
	 * The journal.
	 */
	private JournalResponse journal;

	/**
	 * The target.
	 */
	private String target;

	/**
	 * The description.
	 */
	private String description;

	/**
	 * Creates a job response for the api.
	 * 
	 * @param job The job.
	 * @since 1.8
	 */
	public JobResponse(Job job) {
		super();

		id = job.getId();
		isExecute = job.isExecute();
		isSpecial = job.isSpecial();
		created = job.getCreated();
		start = job.getStart();
		end = job.getEnd();
		state = job.getState();
		journal = new JournalResponse(job.getJournal());
		target = job.getTargetName();
		description = job.getShortDescription();
	}

	/**
	 * Returns the id.
	 *
	 * @return The id.
	 * @since 1.8
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the id.
	 *
	 * @param id The id to set.
	 * @since 1.8
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Returns true if execute or special right is available.
	 *
	 * @return True if execute or special right is available.
	 * @since 1.8
	 */
	public boolean isExecute() {
		return isExecute;
	}

	/**
	 * Set to true if execute or special right is available.
	 *
	 * @param isExecute The execute flag to set.
	 * @since 1.8
	 */
	public void setExecute(boolean isExecute) {
		this.isExecute = isExecute;
	}

	/**
	 * Returns true if special right is available.
	 *
	 * @return True if special right is available.
	 * @since 1.8
	 */
	public boolean isSpecial() {
		return isSpecial;
	}

	/**
	 * Set to true if special right is available.
	 *
	 * @param isSpecial The special flag to set.
	 * @since 1.8
	 */
	public void setSpecial(boolean isSpecial) {
		this.isSpecial = isSpecial;
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
	 * Set the created time.
	 *
	 * @param created The created time to set.
	 * @since 1.8
	 */
	public void setCreated(Date created) {
		this.created = created;
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
	 * Returns the end time.
	 *
	 * @return The end time.
	 * @since 1.8
	 */
	public Date getEnd() {
		return end;
	}

	/**
	 * Set the end time.
	 *
	 * @param end The end time to set.
	 * @since 1.8
	 */
	public void setEnd(Date end) {
		this.end = end;
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
	 * Set the state.
	 *
	 * @param state The state to set.
	 * @since 1.8
	 */
	public void setState(Job.State state) {
		this.state = state;
	}

	/**
	 * Returns the journal.
	 *
	 * @return The journal.
	 * @since 1.8
	 */
	public JournalResponse getJournal() {
		return journal;
	}

	/**
	 * Set the journal.
	 *
	 * @param journal The journal to set.
	 * @since 1.8
	 */
	public void setJournal(JournalResponse journal) {
		this.journal = journal;
	}

	/**
	 * Returns the target.
	 *
	 * @return The target.
	 * @since 1.8
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * Set the target.
	 *
	 * @param target The target to set.
	 * @since 1.8
	 */
	public void setTarget(String target) {
		this.target = target;
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

	/**
	 * Defines journal responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class JournalResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The index of the running step. The first index is 0. Null if not set. In a
		 * journal with exactly one step, this value is 0.
		 */
		private Integer index;

		/**
		 * The steps.
		 */
		private List<StepResponse> steps;

		/**
		 * The size, this means, the number of steps.
		 */
		private int size;

		/**
		 * The expected progress taking into account all journal steps.
		 */
		private float progress;

		/**
		 * Creates a journal response for the api.
		 * 
		 * @param journal The journal.
		 * @since 1.8
		 */
		public JournalResponse(Job.Journal journal) {
			super();

			index = journal.getIndex() < 0 ? null : journal.getIndex();

			steps = new ArrayList<>();
			for (Job.Journal.Step step : journal.getSteps())
				steps.add(new StepResponse(step));
			size = steps.size();

			progress = journal.getProgress();
		}

		/**
		 * Returns the index of the running step. The first index is 0. Null if not set.
		 * In a journal with exactly one step, this value is 0.
		 *
		 * @return The index.
		 * @since 1.8
		 */
		public Integer getIndex() {
			return index;
		}

		/**
		 * Set the index of the running step. The first index is 0. Null if not set. In
		 * a journal with exactly one step, this value is 0.
		 *
		 * @param index The index to set.
		 * @since 1.8
		 */
		public void setIndex(Integer index) {
			this.index = index;
		}

		/**
		 * Returns the steps.
		 *
		 * @return The steps.
		 * @since 1.8
		 */
		public List<StepResponse> getSteps() {
			return steps;
		}

		/**
		 * Set the steps.
		 *
		 * @param steps The steps to set.
		 * @since 1.8
		 */
		public void setSteps(List<StepResponse> steps) {
			this.steps = steps;
		}

		/**
		 * Returns the size, this means, the number of steps.
		 *
		 * @return The size.
		 * @since 1.8
		 */
		public int getSize() {
			return size;
		}

		/**
		 * Set the size, this means, the number of steps.
		 *
		 * @param size The size to set.
		 * @since 1.8
		 */
		public void setSize(int size) {
			this.size = size;
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

		/**
		 * Defines journal step responses for the api.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class StepResponse implements Serializable {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The index. The first index is 0.
			 */
			private int index;

			/**
			 * The progress. This is a value between 0 and 1 inclusive. The initial value is
			 * 0.
			 */
			private float progress;

			/**
			 * True if the progress is completed, this means, its progress is 1.
			 */
			private boolean isCompleted;

			/**
			 * The standard output message. Null if not set.
			 */
			private String standardOutput;

			/**
			 * The standard error message. Null if not set.
			 */
			private String standardError;

			/**
			 * The note. Null if not set.
			 */
			private String note;

			/**
			 * Creates a journal step response for the api.
			 * 
			 * @param step The journal step.
			 * @since 1.8
			 */
			public StepResponse(Job.Journal.Step step) {
				super();

				index = step.getIndex();

				progress = step.getProgress();

				isCompleted = step.isCompleted();

				standardOutput = step.getStandardOutput();
				standardError = step.getStandardError();

				note = step.getNote();
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
			 * Set the index. The first index is 0.
			 *
			 * @param index The index to set.
			 * @since 1.8
			 */
			public void setIndex(int index) {
				this.index = index;
			}

			/**
			 * Returns the progress. This is a value between 0 and 1 inclusive. The initial
			 * value is 0.
			 *
			 * @return The progress.
			 * @since 1.8
			 */
			public float getProgress() {
				return progress;
			}

			/**
			 * Set the progress. This is a value between 0 and 1 inclusive. The initial
			 * value is 0.
			 *
			 * @param progress The progress to set.
			 * @since 1.8
			 */
			public void setProgress(float progress) {
				this.progress = progress;
			}

			/**
			 * Returns true if the progress is completed, this means, its progress is 1.
			 *
			 * @return The isCompleted.
			 * @since 1.8
			 */
			public boolean isCompleted() {
				return isCompleted;
			}

			/**
			 * Set to true if the progress is completed, this means, its progress is 1.
			 *
			 * @param isCompleted The completed flag to set.
			 * @since 1.8
			 */
			public void setCompleted(boolean isCompleted) {
				this.isCompleted = isCompleted;
			}

			/**
			 * Returns the standard output message. Null if not set.
			 *
			 * @return The standard output message. Null if not set.
			 * @since 1.8
			 */
			public String getStandardOutput() {
				return standardOutput;
			}

			/**
			 * Set the standard output message. Null if not set.
			 *
			 * @param standardOutput The standard output message to set.
			 * @since 1.8
			 */
			public void setStandardOutput(String standardOutput) {
				this.standardOutput = standardOutput;
			}

			/**
			 * Returns the standard error message. Null if not set.
			 *
			 * @return The standard error message. Null if not set.
			 * @since 1.8
			 */
			public String getStandardError() {
				return standardError;
			}

			/**
			 * Set the standard error message. Null if not set.
			 *
			 * @param standardError The standard error message to set.
			 * @since 1.8
			 */
			public void setStandardError(String standardError) {
				this.standardError = standardError;
			}

			/**
			 * Returns the note. Null if not set.
			 *
			 * @return The note. Null if not set.
			 * @since 1.8
			 */
			public String getNote() {
				return note;
			}

			/**
			 * Set the note. Null if not set.
			 *
			 * @param note The note to set.
			 * @since 1.8
			 */
			public void setNote(String note) {
				this.note = note;
			}
		}
	}
}
