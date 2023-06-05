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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

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
	@JsonProperty("execute")
	private boolean isExecute;

	/**
	 * True if special right is available.
	 */
	@JsonProperty("special")
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
	 * The description.
	 */
	private String description;

	/**
	 * The process specific data.
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private ProcessResponse process;

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
		description = job.getShortDescription();

		process = job instanceof de.uniwuerzburg.zpd.ocr4all.application.core.job.Process
				? new ProcessResponse((de.uniwuerzburg.zpd.ocr4all.application.core.job.Process) job)
				: null;
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
	@JsonGetter("execute")
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
	@JsonGetter("special")
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
	 * Returns the process.
	 *
	 * @return The process.
	 * @since 1.8
	 */
	public ProcessResponse getProcess() {
		return process;
	}

	/**
	 * Set the process.
	 *
	 * @param process The process to set.
	 * @since 1.8
	 */
	public void setProcess(ProcessResponse process) {
		this.process = process;
	}

	/**
	 * Defines process responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ProcessResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The project id.
		 */
		@JsonProperty("project-id")
		private String idPproject;

		/**
		 * The sandbox id.
		 */
		@JsonInclude(JsonInclude.Include.NON_NULL)
		@JsonProperty("sandbox-id")
		private String idSandbox;

		/**
		 * Creates a process response for the api.
		 * 
		 * @param process The process.
		 * @since 1.8
		 */
		public ProcessResponse(de.uniwuerzburg.zpd.ocr4all.application.core.job.Process process) {
			super();

			idPproject = process.getProject().getId();
			idSandbox = process.isSandboxType() ? process.getSandbox().getId() : null;
		}

		/**
		 * Returns the project id.
		 *
		 * @return The project id.
		 * @since 1.8
		 */
		public String getIdPproject() {
			return idPproject;
		}

		/**
		 * Set the project id.
		 *
		 * @param id The id to set.
		 * @since 1.8
		 */
		public void setIdPproject(String id) {
			idPproject = id;
		}

		/**
		 * Returns the sandbox id.
		 *
		 * @return The sandbox id.
		 * @since 1.8
		 */
		public String getIdSandbox() {
			return idSandbox;
		}

		/**
		 * Set the sandbox id.
		 *
		 * @param id The id to set.
		 * @since 1.8
		 */
		public void setIdSandbox(String id) {
			idSandbox = id;
		}

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
			@JsonProperty("completed")
			private boolean isCompleted;

			/**
			 * The standard output message. Null if not set.
			 */
			@JsonProperty("standard-output")
			private String standardOutput;

			/**
			 * The standard error message. Null if not set.
			 */
			@JsonProperty("standard-error")
			private String standardError;

			/**
			 * The note. Null if not set.
			 */
			private String note;

			/**
			 * The snapshot track.
			 */
			@JsonInclude(JsonInclude.Include.NON_NULL)
			@JsonProperty("snapshot-track")
			private List<Integer> snapshotTrack;

			/**
			 * The service provider id.
			 */
			@JsonInclude(JsonInclude.Include.NON_NULL)
			@JsonProperty("service-provider-id")
			private String serviceProviderId;

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

				if (step.isFurtherInformationSet()) {
					if (step.getFurtherInformation() instanceof de.uniwuerzburg.zpd.ocr4all.application.core.job.Process.Instance.StepFurtherInformation) {
						de.uniwuerzburg.zpd.ocr4all.application.core.job.Process.Instance.StepFurtherInformation furtherInformation = (de.uniwuerzburg.zpd.ocr4all.application.core.job.Process.Instance.StepFurtherInformation) step
								.getFurtherInformation();

						snapshotTrack = furtherInformation.getSnapshotTrack();
						serviceProviderId = furtherInformation.getServiceProviderId();
					}
				}
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
			@JsonGetter("completed")
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

			/**
			 * Returns the service provider id.
			 *
			 * @return The service provider id.
			 * @since 1.8
			 */
			public String getServiceProviderId() {
				return serviceProviderId;
			}

			/**
			 * Set the service provider id.
			 *
			 * @param id The id to set.
			 * @since 1.8
			 */
			public void setServiceProviderId(String id) {
				serviceProviderId = id;
			}
		}
	}
}
