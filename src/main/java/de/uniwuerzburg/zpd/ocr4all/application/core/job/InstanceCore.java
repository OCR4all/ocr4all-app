/**
 * File:     InstanceCore.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.job
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     20.06.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.job;

import java.util.Date;
import java.util.Locale;

import de.uniwuerzburg.zpd.ocr4all.application.core.job.Job.Journal;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.Job.State;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.SPIUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.ServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessorCore;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.ModelArgument;

/**
 * Defines instance cores.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public abstract class InstanceCore<T extends de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider> {
	/**
	 * The service provider.
	 */
	protected final T serviceProvider;

	/**
	 * The state. The initial state is initialized.
	 */
	private State state = State.initialized;

	/**
	 * The service provider arguments.
	 */
	protected final ServiceProvider serviceProviderArgument;

	/**
	 * The journal step.
	 */
	protected final Journal.Step journal;

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
	 * Creates a instance core with initialized state.
	 * 
	 * @param serviceProvider         The service provider.
	 * @param serviceProviderArgument The service provider arguments.
	 * @param journal                 The journal step.
	 * @throws IllegalArgumentException Throws if the service provider, the model or
	 *                                  the journal argument is missed.
	 * @since 1.8
	 */
	protected InstanceCore(T serviceProvider, ServiceProvider serviceProviderArgument, Journal.Step journal)
			throws IllegalArgumentException {
		super();

		if (serviceProvider == null)
			throw new IllegalArgumentException("Instance: the service provider is mandatory.");

		if (serviceProviderArgument == null)
			throw new IllegalArgumentException("Instance: the service provider arguments is mandatory.");

		if (journal == null)
			throw new IllegalArgumentException("Instance: the journal step is mandatory.");

		this.serviceProvider = serviceProvider;
		this.serviceProviderArgument = serviceProviderArgument;
		this.journal = journal;
	}

	/**
	 * Returns the application locale.
	 * 
	 * @return The application locale.
	 * @since 17
	 */
	protected abstract Locale getLocale();

	/**
	 * Returns the short description.
	 * 
	 * @return The short description.
	 * @since 1.8
	 */
	public String getShortDescription() {
		return serviceProvider.getName(getLocale()) + " (v" + serviceProvider.getVersion() + ")";
	}

	/**
	 * Returns the service provider model with their arguments.
	 * 
	 * @return The service provider model with their arguments.
	 * @since 1.8
	 */
	protected ModelArgument getModelArgument() {
		return SPIUtils.getModelArgument(serviceProviderArgument);
	}

	/**
	 * Returns the process state.
	 *
	 * @return The process state.
	 * @since 17
	 */
	public State getState() {
		return state;
	}

	/**
	 * Set the process state.
	 * 
	 * @param state The state to set.
	 * @since 1.8
	 */
	protected void setState(State state) {
		this.state = state;
	}

	/**
	 * Returns true if the state is initialized.
	 * 
	 * @return True if the state is initialized.
	 * @since 1.8
	 */
	protected boolean isInitialized() {
		return State.initialized.equals(state);
	}

	/**
	 * Returns true if the state is running.
	 * 
	 * @return True if the state is running.
	 * @since 1.8
	 */
	protected boolean isRunning() {
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
	 * Set the start time to now.
	 *
	 * @since 17
	 */
	protected void setStart() {
		this.start = new Date();
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
	 * Set the end time to now.
	 *
	 * @since 17
	 */
	protected void setEnd() {
		this.end = new Date();
	}

	/**
	 * The callback method instance logic to execute when the when it is in
	 * scheduled state.
	 * 
	 * @since 1.8
	 */
	protected abstract void executeCallback();

	/**
	 * Executes the process instance if it is in scheduled state.
	 * 
	 * @return The state of the instance.
	 * @since 1.8
	 */
	public State execute() {
		if (isInitialized()) {
			setState(State.running);
			setStart();

			executeCallback();
		}

		return state;
	}

	/**
	 * The callback method instance logic to execute when the when it is in
	 * scheduled state.
	 * 
	 * @return The processor to cancel.
	 * @since 17
	 */
	protected abstract ProcessorCore cancelCallback();

	/**
	 * Cancels the process instance in a new thread if it is not done.
	 * 
	 * @return The state of the instance. It is cancelled if the instance was not
	 *         done. Otherwise, the state of the done instance is returned.
	 * @since 1.8
	 */
	public State cancel() {
		if (isInitialized() || isRunning()) {
			final boolean isRunning = State.running.equals(state);

			setState(State.canceled);
			setEnd();

			ProcessorCore processor = cancelCallback();

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
