/**
 * File:     CoreServiceProviderApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     20.06.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker.spi;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.api.worker.CoreApiController;
import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.ModelService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.SchedulerService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.SandboxService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.RecognitionModelArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Dataset;

/**
 * Defines process service provider controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @param <S> The core service provider type.
 * @since 17
 */
public class CoreServiceProviderApiController<S extends CoreServiceProvider<? extends ServiceProvider>>
		extends CoreApiController {

	/**
	 * Define types.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public enum Type {
		/**
		 * The imp type.
		 */
		imp,
		/**
		 * The launcher type.
		 */
		launcher,
		/**
		 * The preprocessing type.
		 */
		preprocessing,
		/**
		 * The olr type.
		 */
		olr,
		/**
		 * The ocr type.
		 */
		ocr,
		/**
		 * The postcorrection type.
		 */
		postcorrection,
		/**
		 * The tool type.
		 */
		tool,
		/**
		 * The export type.
		 */
		export,
		/**
		 * The training type.
		 */
		training;

		/**
		 * Returns the respective persistence snapshot type.
		 * 
		 * @return The respective persistence snapshot type. Null if not available.
		 * @since 17
		 */
		public de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Snapshot.Type getSnapshotType() {
			switch (this) {
			case launcher:
				return de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Snapshot.Type.launcher;
			case preprocessing:
				return de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Snapshot.Type.preprocessing;
			case olr:
				return de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Snapshot.Type.olr;
			case ocr:
				return de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Snapshot.Type.ocr;
			case postcorrection:
				return de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Snapshot.Type.postcorrection;
			case tool:
				return de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Snapshot.Type.tool;
			case imp:
			case export:
			case training:
			default:
				return null;
			}
		}
	}

	/**
	 * The spi prefix path.
	 */
	public static final String spiContextPath = "/spi";

	/**
	 * The spi version 1.0 prefix path.
	 */
	public static final String spiContextPathVersion_1_0 = apiContextPathVersion_1_0 + spiContextPath;

	/**
	 * The providers request mapping.
	 */
	public static final String providersRequestMapping = "/providers";

	/**
	 * The scheduler service.
	 */
	protected final SchedulerService schedulerService;

	/**
	 * The type.
	 */
	protected final Type type;

	/**
	 * The service.
	 */
	protected final S service;

	/**
	 * Creates a process service provider controller for the api.
	 * 
	 * @param logger               The logger class.
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param collectionService    The collection service.
	 * @param modelService         The model service.
	 * @param projectService       The project service.
	 * @param sandboxService       The sandbox service.
	 * @param schedulerService     The scheduler service.
	 * @param type                 The type.
	 * @param service              The service.
	 * @since 17
	 */
	protected CoreServiceProviderApiController(Class<?> logger, ConfigurationService configurationService,
			SecurityService securityService, CollectionService collectionService, ModelService modelService,
			ProjectService projectService, SandboxService sandboxService, SchedulerService schedulerService, Type type,
			S service) {
		super(logger, configurationService, securityService, collectionService, modelService, projectService,
				sandboxService);

		this.schedulerService = schedulerService;

		this.type = type;
		this.service = service;
	}

	/**
	 * Authorizes the session user for read security operations on data collections.
	 * 
	 * @param dataset The dataset. If it is null or no collection are available,
	 *                then it is authorized.
	 * @return The authorized dataset. Null if the given dataset is null.
	 * @throws ResponseStatusException Throw with http status:
	 *                                 <ul>
	 *                                 <li>400 (Bad Request): if a collection is not
	 *                                 available.</li>
	 *                                 <li>401 (Unauthorized): if the read security
	 *                                 permission is not achievable by the session
	 *                                 user on a collection.</li>
	 *                                 </ul>
	 * @since 1.8
	 */
	protected Dataset authorizeRead(Dataset dataset) throws ResponseStatusException {
		if (dataset != null)
			for (Dataset.Collection collection : dataset.getCollections())
				authorizeCollectionRead(collection.getId());

		return dataset;
	}

	/**
	 * Authorizes the session user for read security operations on assemble models.
	 * 
	 * @param recognitionModels The recognition models. If it is null or no models
	 *                          are available, then it is authorized.
	 * @return The authorized models. Null if the given models is null.
	 * @throws ResponseStatusException Throw with http status:
	 *                                 <ul>
	 *                                 <li>400 (Bad Request): if a model is not
	 *                                 available.</li>
	 *                                 <li>401 (Unauthorized): if the read security
	 *                                 permission is not achievable by the session
	 *                                 user.</li>
	 *                                 </ul>
	 * @since 17
	 */
	protected List<ModelService.Model> authorizeRead(List<RecognitionModelArgument> recognitionModels)
			throws ResponseStatusException {
		if (recognitionModels == null)
			return null;
		else {
			List<ModelService.Model> models = new ArrayList<>();
			for (RecognitionModelArgument recognitionModel : recognitionModels)
				if (recognitionModel != null && recognitionModel.getAssembles() != null)
					for (RecognitionModelArgument.Assemble assemble : recognitionModel.getAssembles())
						models.add(authorizeModelRead(assemble.getId()));

			return models;
		}
	}

	/**
	 * Defines service provider requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class ServiceProviderRequest
			extends de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.ServiceProvider {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The job short description.
		 */
		@JsonProperty("job-short-description")
		private String jobShortDescription;

		/**
		 * Returns the job short description.
		 *
		 * @return The job short description.
		 * @since 17
		 */
		public String getJobShortDescription() {
			return jobShortDescription;
		}

		/**
		 * Set the job short description.
		 *
		 * @param jobShortDescription The job short description to set.
		 * @since 17
		 */
		public void setJobShortDescription(String jobShortDescription) {
			this.jobShortDescription = jobShortDescription;
		}

	}
}
