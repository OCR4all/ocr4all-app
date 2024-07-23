/**
 * File:     SchedulerService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.job
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     05.02.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;

/**
 * Defines scheduler services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Service
@ApplicationScope
public class SchedulerService extends CoreService {
	/**
	 * The prefix to use for the names of newly created threads by task executor.
	 */
	private static final String taskExecutorThreadNamePrefix = "job";

	/**
	 * Defines thread pools.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public enum ThreadPool {
		/**
		 * The work thread pool.
		 */
		work("wk"),
		/**
		 * The task thread pool.
		 */
		task("tk"),
		/**
		 * The workflow thread pool.
		 */
		workflow("wf"),
		/**
		 * The training thread pool.
		 */
		training("tr"),
		/**
		 * The workspace thread pool.
		 */
		workspace("ws");

		/**
		 * The label.
		 */
		private final String label;

		/**
		 * Creates a thread pool.
		 * 
		 * @param label The label.
		 * @since 1.8
		 */
		private ThreadPool(String label) {
			this.label = label;
		}

		/**
		 * Returns the label.
		 *
		 * @return The label.
		 * @since 1.8
		 */
		public String getLabel() {
			return label;
		}

	}

	/**
	 * Defines queue positions.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public enum Position {
		/**
		 * The first queue position.
		 */
		first,
		/**
		 * The last queue position.
		 */
		last,
		/**
		 * The index queue position.
		 */
		index
	}

	/**
	 * The job id.
	 */
	private int id = 0;

	/**
	 * The jobs. The key is the job id.
	 */
	private final Hashtable<Integer, Job> jobs = new Hashtable<>();

	/**
	 * The running jobs. The key is the job id.
	 */
	private final Hashtable<Integer, Job> running = new Hashtable<>();

	/**
	 * The scheduled jobs.
	 */
	private final List<Job> scheduled = new ArrayList<>();

	/**
	 * The start time.
	 */
	private final Date start = new Date();

	/**
	 * The state update time.
	 */
	private Date stateUpdated = start;

	/**
	 * True if the scheduler is running. Otherwise it is paused.
	 */
	private boolean isRunning = true;

	/**
	 * The thread pool for work.
	 */
	private final ThreadPoolTaskExecutor threadPoolWork;

	/**
	 * The thread pool for task.
	 */
	private final ThreadPoolTaskExecutor threadPoolTask;

	/**
	 * The thread pool for workflow.
	 */
	private final ThreadPoolTaskExecutor threadPoolWorkflow;

	/**
	 * The thread pool for training.
	 */
	private final ThreadPoolTaskExecutor threadPoolTraining;

	/**
	 * The thread pool for workspace.
	 */
	private final Hashtable<String, ThreadPoolTaskExecutor> threadPoolWorkspace = new Hashtable<>();

	/**
	 * Creates a scheduler service.
	 * 
	 * @param configurationService The configuration service.
	 * @since 1.8
	 */
	public SchedulerService(ConfigurationService configurationService) {
		super(SchedulerService.class, configurationService);

		/*
		 * The application thread pools
		 */
		threadPoolWork = createThreadPool(taskExecutorThreadNamePrefix, ThreadPool.work.getLabel(),
				configurationService.getApplication().getThreadPoolSizeProperties().getWork());
		threadPoolTask = createThreadPool(taskExecutorThreadNamePrefix, ThreadPool.task.getLabel(),
				configurationService.getApplication().getThreadPoolSizeProperties().getTask());
		threadPoolWorkflow = createThreadPool(taskExecutorThreadNamePrefix, ThreadPool.workflow.getLabel(),
				configurationService.getApplication().getThreadPoolSizeProperties().getWorkflow());
		threadPoolTraining = createThreadPool(taskExecutorThreadNamePrefix, ThreadPool.training.getLabel(),
				configurationService.getApplication().getThreadPoolSizeProperties().getTraining());

		/*
		 * The workspace thread pools
		 */
		final String taskExecutorThreadNamePrefixWorkspace = taskExecutorThreadNamePrefix + "-"
				+ ThreadPool.workspace.getLabel();

		Hashtable<String, Integer> poolSizes = configurationService.getWorkspace().getConfiguration()
				.getTaskExecutorPoolSizes();
		for (String threadName : poolSizes.keySet())
			threadPoolWorkspace.put(threadName,
					createThreadPool(taskExecutorThreadNamePrefixWorkspace, threadName, poolSizes.get(threadName)));

		// The callback for thread pool for workspace updates
		configurationService.getWorkspace().getConfiguration().register((threadName, corePoolSize) -> {
			if (corePoolSize == 0) {
				ThreadPoolTaskExecutor threadPool = threadPoolWorkspace.remove(threadName);

				if (threadPool != null) {
					threadPool.shutdown();

					logger.info(
							"removed thread pool '" + taskExecutorThreadNamePrefixWorkspace + "-" + threadName + "'.");
				}
			} else {
				ThreadPoolTaskExecutor threadPool = threadPoolWorkspace.get(threadName);
				if (threadPool == null)
					threadPoolWorkspace.put(threadName,
							createThreadPool(taskExecutorThreadNamePrefixWorkspace, threadName, corePoolSize));
				else {
					threadPool.setCorePoolSize(corePoolSize);
					threadPool.afterPropertiesSet();

					logger.info("updated size of workspace thread pool '" + threadName + "' to " + corePoolSize + ".");
				}
			}
		});
	}

	/**
	 * Creates a thread pool.
	 * 
	 * @param prefix       The prefix for the .
	 * @param threadName   The thread name.
	 * @param corePoolSize The core pool size.
	 * @return The thread pool.
	 * @since 1.8
	 */
	private ThreadPoolTaskExecutor createThreadPool(String prefix, String threadName, int corePoolSize) {
		String name = prefix + "-" + threadName;

		ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();

		threadPool.setThreadNamePrefix(name + "-");
		threadPool.setCorePoolSize(corePoolSize);
		threadPool.setWaitForTasksToCompleteOnShutdown(false);

		threadPool.afterPropertiesSet();

		logger.info("created thread pool '" + name + "' with size " + corePoolSize + ".");

		return threadPool;
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
	 * Returns the state update time.
	 *
	 * @return The state update time.
	 * @since 1.8
	 */
	public Date getStateUpdated() {
		return stateUpdated;
	}

	/**
	 * Returns true if the scheduler is running. Otherwise it is paused.
	 *
	 * @return True if the scheduler is running. Otherwise it is paused.
	 * @since 1.8
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * Updates the scheduler state.
	 * 
	 * @param isRun True if the scheduler should run. Otherwise it should pause.
	 * @since 1.8
	 */
	private void update(boolean isRun) {
		if (isRunning != isRun) {
			isRunning = isRun;

			stateUpdated = new Date();
		}

		schedule();
	}

	/**
	 * Runs the scheduler.
	 * 
	 * @since 1.8
	 */
	public void run() {
		update(true);
	}

	/**
	 * Pauses the scheduler.
	 * 
	 * @since 1.8
	 */
	public void pause() {
		update(false);
	}

	/**
	 * Returns the job.
	 * 
	 * @param id The job id.
	 * @return The job.
	 * @throws IllegalArgumentException Throws if the job is unknown.
	 * @since 1.8
	 */
	public Job getJob(int id) throws IllegalArgumentException {
		Job job = jobs.get(id);

		if (job == null)
			throw new IllegalArgumentException("SchedulerService: unknown job id " + id + ".");

		return job;
	}

	/**
	 * Starts the job.
	 * 
	 * @param job The job to start.
	 * @since 1.8
	 */
	private void start(Job job) {
		ThreadPoolTaskExecutor threadPool = null;

		if (job.getThreadPoolWorkspace() != null) {
			threadPool = threadPoolWorkspace.get(job.getThreadPoolWorkspace());

			if (threadPool == null)
				logger.error("unknown thread pool '" + job.getThreadPoolWorkspace() + "' for job " + job.getId());
		}

		if (threadPool == null)
			switch (job.getThreadPool()) {
			case training:
				threadPool = threadPoolTraining;
				break;
			case workflow:
				threadPool = threadPoolWorkflow;
				break;
			case task:
				threadPool = threadPoolTask;
				break;
			case work:
			default:
				threadPool = threadPoolWork;
				break;
			}

		job.start(threadPool, instance -> schedule());

		if (job.isStateRunning())
			running.put(job.getId(), job);
	}

	/**
	 * Schedule the jobs.
	 * 
	 * @since 1.8
	 */
	private synchronized void schedule() {
		// expunge done jobs from running table and search for a sequential job
		boolean isSequentialRunning = false;
		for (Job job : new ArrayList<>(running.values()))
			if (job.isDone())
				running.remove(job.getId());
			else if (job.isProcessingSequential())
				isSequentialRunning = true;

		synchronized (scheduled) {
			for (Job job : new ArrayList<>(scheduled))
				if (!job.isStateScheduled())
					scheduled.remove(job);

			// if a sequential process is running, do not schedule additional processes
			if (isRunning && !isSequentialRunning)
				for (Job job : new ArrayList<>(scheduled))
					if (job.isProcessingSequential()) {
						if (running.isEmpty()) {
							scheduled.remove(job);

							start(job);
						}

						break;
					} else if (job.depend(running.values()).isEmpty()) {
						scheduled.remove(job);

						start(job);
					}
		}
	}

	/**
	 * Schedules the job if it is not under scheduler control.
	 * 
	 * @param job The job to schedule.
	 * @return The job state.
	 * @since 1.8
	 */
	public synchronized Job.State schedule(Job job) {
		if (job != null && !job.isSchedulerControl() && job.schedule(++id)) {
			jobs.put(job.getId(), job);

			scheduled.add(job);

			schedule();
		}

		return job == null ? null : job.getState();
	}

	/**
	 * Reschedules the job to the desired position.
	 * 
	 * @param id       The job id.
	 * @param position The position.
	 * @param index    The index. It is needed if the index position type is used.
	 *                 If the index is smaller than 0, it is rescheduled at the
	 *                 beginning of the queue. If it is larger than the queue size,
	 *                 it is rescheduled to the end of the queue.
	 * @throws IllegalArgumentException Throws if the job is unknown.
	 * @since 1.8
	 */
	private synchronized void reschedule(int id, Position position, int index) throws IllegalArgumentException {
		Job job = getJob(id);

		synchronized (scheduled) {
			if (scheduled.remove(job))
				switch (position) {
				case first:
					scheduled.add(0, job);
					break;

				case last:
					scheduled.add(job);
					break;

				case index:
					if (index < 0)
						scheduled.add(0, job);
					else if (index >= scheduled.size())
						scheduled.add(job);
					else
						scheduled.add(index, job);
					break;

				default:
					break;
				}
		}

		schedule();
	}

	/**
	 * Swaps the scheduled jobs.
	 * 
	 * @param id1 The id of the job to swap.
	 * @param id2 The id of the job to swap.
	 * @since 1.8
	 */
	public void swapScheduled(int id1, int id2) {
		if (id1 > 0 && id2 > 0 && id1 != id2) {
			int index1 = -1, index2 = -1;

			synchronized (scheduled) {
				int index = 0;
				for (Job job : scheduled) {
					if (job.getId() == id1) {
						index1 = index;

						if (index2 >= 0)
							break;
					} else if (job.getId() == id2) {
						index2 = index;

						if (index1 >= 0)
							break;
					}
					index++;
				}

				if (index1 >= 0 && index2 >= 0) {
					if (index1 > index2) {
						int aux = index1;

						index1 = index2;
						index2 = aux;
					}

					Job job1 = scheduled.remove(index1);
					Job job2 = scheduled.remove(index2 - 1);

					scheduled.add(index1, job2);
					scheduled.add(index2, job1);
				}
			}

			schedule();
		}
	}

	/**
	 * Reschedules the job to the desired index. The first index is 1.
	 * 
	 * @param id    The job id.
	 * @param index The index. If the index is smaller than 0, it is rescheduled at
	 *              the beginning of the queue. If it is larger than the queue size,
	 *              it is rescheduled to the end of the queue.
	 * @throws IllegalArgumentException Throws if the job is unknown.
	 * @since 1.8
	 */
	public void reschedule(int id, int index) throws IllegalArgumentException {
		reschedule(id, Position.index, index - 1);
	}

	/**
	 * Reschedules the job to the top of the queue.
	 * 
	 * @param id The job id.
	 * @throws IllegalArgumentException Throws if the job is unknown.
	 * @since 1.8
	 */
	public void rescheduleBegin(int id) throws IllegalArgumentException {
		reschedule(id, Position.first, 0);
	}

	/**
	 * Reschedules the job to the end of the queue.
	 * 
	 * @param id The job id.
	 * @throws IllegalArgumentException Throws if the job is unknown.
	 * @since 1.8
	 */
	public void rescheduleEnd(int id) throws IllegalArgumentException {
		reschedule(id, Position.last, 0);
	}

	/**
	 * Returns the jobs of given collection that are associated to the given
	 * cluster.
	 * 
	 * @param cluster    The cluster.
	 * @param collection The collection.
	 * @return The jobs of given collection that are associated to the given
	 *         cluster.
	 * @since 1.8
	 */
	private Set<Job> associated(Job.Cluster cluster, Collection<Job> collection) {
		if (cluster == null)
			return null;
		else {
			schedule();

			return cluster.associated(collection);
		}

	}

	/**
	 * Returns the jobs that are associated to the given cluster.
	 * 
	 * @param cluster The cluster.
	 * @return The jobs that are associated to the given cluster.
	 * @since 1.8
	 */
	public Set<Job> associated(Job.Cluster cluster) {
		return associated(cluster, jobs.values());
	}

	/**
	 * Returns the running jobs that are associated to the given cluster.
	 * 
	 * @param cluster The cluster.
	 * @return The running jobs that are associated to the given cluster.
	 * @since 1.8
	 */
	public Set<Job> associatedRunning(Job.Cluster cluster) {
		return associated(cluster, running.values());
	}

	/**
	 * Returns the scheduled jobs that are associated to the given cluster.
	 * 
	 * @param cluster The cluster.
	 * @return The scheduled jobs that are associated to the given cluster.
	 * @since 1.8
	 */
	public Set<Job> associatedScheduled(Job.Cluster cluster) {
		return associated(cluster, scheduled);
	}

	/**
	 * Returns true if the project is a target of the job.
	 * 
	 * @param id      The job id.
	 * @param project The project.
	 * @return True if the project is a target of the job.
	 * @throws IllegalArgumentException Throws if the job is unknown.
	 * @since 1.8
	 */
	public boolean isTarget(int id, Project project) throws IllegalArgumentException {
		return project != null && isTarget(id, Arrays.asList(project));
	}

	/**
	 * Cancels the job.
	 * 
	 * @param id The job id.
	 * @throws IllegalArgumentException Throws if the job is unknown.
	 * @since 1.8
	 */
	public void cancelJob(int id) throws IllegalArgumentException {
		getJob(id).cancel();

		schedule();
	}

	/**
	 * Expunges the done jobs.
	 * 
	 * @since 1.8
	 */
	public synchronized void expungeDone() {
		List<Integer> expunges = new ArrayList<>();
		for (int id : jobs.keySet())
			if (jobs.get(id).isDone())
				expunges.add(id);

		for (int id : expunges)
			jobs.remove(id);
	}

	/**
	 * Removes the done job.
	 * 
	 * @param id The job id.
	 * @return True if the job could be removed.
	 * @since 1.8
	 */
	public synchronized boolean removeDone(int id) {
		Job job = jobs.get(id);

		if (job != null && job.isDone()) {
			jobs.remove(id);

			return true;
		} else
			return false;
	}

	/**
	 * Returns true if a project is a target of the job.
	 * 
	 * @param id       The job id.
	 * @param projects The projects.
	 * @return True if a project is a target of the job.
	 * @throws IllegalArgumentException Throws if the job is unknown.
	 * @since 1.8
	 */
	public boolean isTarget(int id, Collection<Project> projects) throws IllegalArgumentException {
		Job job = getJob(id);

		if (job instanceof Process process && projects != null) {
			Project target = process.getProject();

			for (Project project : projects)
				if (target.isSame(project))
					return true;
		}

		return false;
	}

	/**
	 * Returns all jobs.
	 * 
	 * @return The all jobs.
	 * @since 1.8
	 */
	public Container getJobs() {
		return getJobs(null, null, null);
	}

	/**
	 * Returns the jobs that are associated to the given clusters.
	 * 
	 * @param clusters         The clusters. If null, all jobs are returned in the
	 *                         snapshot.
	 * @param trainingModelIds The training jobs to add, that are running on the
	 *                         given assemble model ids.
	 * @param owner            The owner for the work jobs.
	 * @return The jobs that are associated to the given clusters.
	 * @since 1.8
	 */
	public Container getJobs(Collection<Job.Cluster> clusters, Set<String> trainingModelIds, String owner) {
		// Filter target jobs
		Set<Job> jobs;
		if (clusters == null)
			jobs = new HashSet<>(this.jobs.values());
		else {
			jobs = new HashSet<>();
			for (Job.Cluster cluster : clusters)
				if (cluster != null)
					jobs.addAll(associated(cluster));

			for (Job job : this.jobs.values())
				if (job instanceof Training training) {
					if (trainingModelIds != null && trainingModelIds.contains(training.getModelId()))
						jobs.add(job);
				} else if (job instanceof Work work)
					if (work.isOwnerRequirements(owner))
						jobs.add(job);

		}

		// Add scheduled target jobs to the container in the same order
		Container container = new Container();
		for (Job job : scheduled)
			if (jobs.remove(job))
				container.getScheduled().add(job);

		// Adds running and completed target jobs to the snapshot and sorts them.
		for (Job job : jobs)
			if (job.isStateRunning())
				container.getRunning().add(job);
			else
				container.getDone().add(job);

		container.sort();

		return container;
	}

	/**
	 * Defines scheduler containers.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static final class Container {
		/**
		 * The scheduled jobs in the same order as in the scheduler.
		 */
		private final List<Job> scheduled = new ArrayList<>();

		/**
		 * The running jobs sorted by start time in descending order.
		 */
		private final List<Job> running = new ArrayList<>();

		/**
		 * The done jobs sorted by end time in descending order.
		 */
		private final List<Job> done = new ArrayList<>();

		/**
		 * Default constructor for a scheduler snapshot.
		 * 
		 * @since 1.8
		 */
		public Container() {
			super();
		}

		/**
		 * Sorts the running and done jobs.
		 * 
		 * @since 1.8
		 */
		private void sort() {
			Collections.sort(running, (o1, o2) -> o2.getStart().compareTo(o1.getStart()));
			Collections.sort(done, (o1, o2) -> o2.getEnd().compareTo(o1.getEnd()));
		}

		/**
		 * Returns the scheduled jobs in the same order as in the scheduler.
		 *
		 * @return The scheduled jobs in the same order as in the scheduler.
		 * @since 1.8
		 */
		public List<Job> getScheduled() {
			return scheduled;
		}

		/**
		 * Returns the running jobs sorted by start time in descending order.
		 *
		 * @return The running jobs sorted by start time in descending order.
		 * @since 1.8
		 */
		public List<Job> getRunning() {
			return running;
		}

		/**
		 * Returns the done jobs sorted by end time in descending order.
		 *
		 * @return The done jobs sorted by end time in descending order.
		 * @since 1.8
		 */
		public List<Job> getDone() {
			return done;
		}

	}
}
