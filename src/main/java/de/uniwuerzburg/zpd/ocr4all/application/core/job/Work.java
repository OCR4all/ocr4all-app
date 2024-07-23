/**
 * File:     Work.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.job
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     23.07.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.job;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.SchedulerService.ThreadPool;

/**
 * Defines works.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public class Work extends Action {
	/**
	 * The owner.
	 */
	private final String owner;

	/**
	 * The description.
	 */
	private final String description;

	/**
	 * The instance;
	 */
	private final Instance instance;

	/**
	 * Creates a work. No locale is defined.
	 * 
	 * @param owner                The owner.
	 * @param configurationService The configuration service.
	 * @param description          The description.
	 * @since 17
	 */
	public Work(String owner, ConfigurationService configurationService, String description, Instance instance) {
		super(Work.class, configurationService, null, Job.Processing.parallel, 1);

		this.owner = owner == null || owner.isBlank() ? null : owner.trim();
		this.description = description;
		this.instance = instance;
	}

	/**
	 * Returns true the owner requirements are fulfilled.
	 *
	 * @return True the owner requirements are fulfilled.
	 * @since 17
	 */
	public boolean isOwnerRequirements(String user) {
		return owner == null || owner.equals(user == null ? null : user.trim());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#getShortDescription()
	 */
	@Override
	public String getShortDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#getThreadPool()
	 */
	@Override
	public ThreadPool getThreadPool() {
		return SchedulerService.ThreadPool.work;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#isExecute()
	 */
	@Override
	public boolean isExecute() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#isSpecial()
	 */
	@Override
	public boolean isSpecial() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#execute()
	 */
	@Override
	protected State execute() {
		return instance.execute(getJournal().getStep());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#kill()
	 */
	@Override
	protected void kill() {
		instance.cancel(getJournal().getStep());
	}

	/**
	 * Defines work instances.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public interface Instance {
		/**
		 * Executes the instance.
		 *
		 * @param journal The journal.
		 * @return The end state of the execution, this means, canceled, completed or
		 *         interrupted.
		 * @since 17
		 */
		public State execute(Journal.Step journal);

		/**
		 * Cancels the instance if it is not done.
		 * 
		 * @param journal The journal.
		 * @since 17
		 */
		public void cancel(Journal.Step journal);
	}
}
