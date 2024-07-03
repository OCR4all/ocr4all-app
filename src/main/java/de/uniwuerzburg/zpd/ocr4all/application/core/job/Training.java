/**
 * File:     Training.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.job
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     03.07.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.job;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.springframework.util.FileSystemUtils;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.Job.Journal.Step;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.SchedulerService.ThreadPool;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Engine;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.ServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.TrainingServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessorCore;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessorServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Dataset;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Framework;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.TrainingFramework;

/**
 * Defines trainings.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public class Training extends Action {
	/**
	 * The process instance.
	 */
	private Instance instance = null;

	/**
	 * The short description.
	 */
	private final String shortDescription;

	/**
	 * The user. Null if not defined.
	 */
	private final String user;

	/**
	 * The dataset.
	 */
	private final Dataset dataset;

	/**
	 * The model id.
	 */
	private final String modelId;

	/**
	 * The service provider.
	 */
	private final TrainingServiceProvider serviceProvider;

	/**
	 * The service provider arguments.
	 */
	private final ServiceProvider serviceProviderArgument;

	/**
	 * Creates a training.
	 * 
	 * @param configurationService    The configuration service.
	 * @param locale                  The application locale.
	 * @param shortDescription        The short description. If null, use instance
	 *                                short description.
	 * @param user                    The user. Null if not defined.
	 * @param dataset                 The dataset.
	 * @param modelId                 The model id.
	 * @param serviceProvider         The service provider.
	 * @param serviceProviderArgument The service provider arguments.
	 * @since 17
	 */
	public Training(ConfigurationService configurationService, Locale locale, String shortDescription, String user,
			Dataset dataset, String modelId, TrainingServiceProvider serviceProvider,
			ServiceProvider serviceProviderArgument) {
		super(Training.class, configurationService, locale, Job.Processing.parallel, 1);

		this.shortDescription = shortDescription == null || shortDescription.isBlank()
				? serviceProvider.getName(locale) + " (v" + serviceProvider.getVersion() + ")"
				: shortDescription.trim();

		this.user = user;
		this.dataset = dataset;
		this.modelId = modelId;

		this.serviceProvider = serviceProvider;
		this.serviceProviderArgument = serviceProviderArgument;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#getShortDescription()
	 */
	@Override
	public String getShortDescription() {
		return shortDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#getThreadPool()
	 */
	@Override
	public ThreadPool getThreadPool() {
		return SchedulerService.ThreadPool.training;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.core.job.Job#getThreadPoolWorkspace()
	 */
	@Override
	public String getThreadPoolWorkspace() {
		return serviceProvider.getThreadPool();
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
		try {
			instance = new Instance(serviceProvider, serviceProviderArgument, getJournal().getStep());
		} catch (IllegalArgumentException e) {
			getJournal().getStep().setNote(OCR4allUtils.getStackTrace(e));

			return State.interrupted;
		}

		return instance.execute();
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
	 * Defines instances.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public class Instance extends InstanceAction<ProcessorCore.Callback, TrainingFramework> {
		/**
		 * The processor for the service provider.
		 */
		private final ProcessorServiceProvider.Processor<ProcessorCore.Callback, TrainingFramework> processor;

		/**
		 * Creates an instance.
		 * 
		 * @param serviceProvider         The service provider.
		 * @param serviceProviderArgument The service provider arguments.
		 * @param journal                 The journal step.
		 * @throws IllegalArgumentException Throws if the service provider, the model or
		 *                                  the journal argument is missed.
		 * @since 17
		 */
		public Instance(TrainingServiceProvider serviceProvider, ServiceProvider serviceProviderArgument, Step journal)
				throws IllegalArgumentException {
			super(serviceProvider, serviceProviderArgument, journal);

			processor = serviceProvider.newProcessor();
		}

		/**
		 * Returns the framework for the service provider.
		 * 
		 * @return The framework for the service provider.
		 * @since 1.8
		 */
		private TrainingFramework getFramework() {
			Path temporaryDirectory = null;
			try {
				temporaryDirectory = configurationService.getTemporary().getTemporaryDirectory();
			} catch (IOException e) {
				logger.warn("cannot create temporary directory for service provider "
						+ serviceProvider.getClass().getName() + ", " + serviceProvider.getName(locale) + " (v"
						+ serviceProvider.getVersion() + ") - " + e.getMessage() + ".");
			}

			return new TrainingFramework(ConfigurationService.getOperatingSystem().getFramework(),
					ConfigurationService.getUID(), ConfigurationService.getGID(),
					new Framework.Application(configurationService.getApplication().getLabel(),
							configurationService.getApplication().getName(),
							configurationService.getApplication().getDateFormat()),
					user, configurationService.getData().getFolder(), configurationService.getAssemble().getFolder(),
					dataset,
					new TrainingFramework.ModelConfiguration(
							configurationService.getAssemble().getModel().getConfiguration().getFolder(),
							configurationService.getAssemble().getModel().getConfiguration().getFiles().getEngine()),
					modelId, temporaryDirectory);
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
			TrainingFramework framework = null;
			ProcessorServiceProvider.Processor.State executionState = null;
			if (processor == null)
				journal.setNote("no processor available for the service provider");
			else {
				framework = getFramework();
				try {
					executionState = processor.execute(new ProcessorServiceProvider.Processor.Callback() {
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
				setState(ProcessorServiceProvider.Processor.State.completed.equals(executionState) ? State.completed
						: State.interrupted);
				setEnd();
			}

			// Updates the engine configuration
			if (framework != null) {
				try {
					PersistenceManager persistenceManager = new PersistenceManager(
							Paths.get(framework.getAssemble().toString(), modelId,
									framework.getModelConfiguration().getFolder(),
									framework.getModelConfiguration().getEngine()),
							de.uniwuerzburg.zpd.ocr4all.application.persistence.Type.assemble_engine_v1);

					Engine engine = persistenceManager.getEntity(Engine.class);
					if (engine == null)
						journal.addNote("could not update engine state - no data is available");
					else {
						Engine.State stateEngine;
						switch (getState()) {
						case canceled:
							stateEngine = Engine.State.canceled;
							break;
						case completed:
							stateEngine = Engine.State.completed;
							break;
						case interrupted:
						default:
							stateEngine = Engine.State.interrupted;
							break;
						}

						engine.setState(stateEngine);

						persistenceManager.persist(engine);
					}
				} catch (Exception e) {
					journal.addNote(
							"could not update engine state" + System.lineSeparator() + OCR4allUtils.getStackTrace(e));
				}

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
			return processor;
		}

	}

}
