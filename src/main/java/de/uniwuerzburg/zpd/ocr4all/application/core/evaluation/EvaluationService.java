/**
 * File:     EvaluationService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.evaluation
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     19.09.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.evaluation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import de.uniwuerzburg.zpd.ocr4all.application.communication.action.EvaluationMeasure;
import de.uniwuerzburg.zpd.ocr4all.application.communication.spi.ServiceProviderTask;
import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.spi.ActionServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.WorkerServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Database;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.ModelArgument;

/**
 * Defines evaluation services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@Service
public class EvaluationService extends CoreService {
	/**
	 * The calamari evaluation provider.
	 */
	public static final String calamariEvaluationProvider = "calamari/evaluation";

	/**
	 * The collection service.
	 */
	protected final CollectionService collectionService;

	/**
	 * Creates an evaluation service.
	 * 
	 * @param configurationService The configuration service.
	 * @param collectionService    The collection service.
	 * @since 17
	 */
	public EvaluationService(ConfigurationService configurationService, CollectionService collectionService) {
		super(EvaluationService.class, configurationService);

		this.collectionService = collectionService;
	}

	/**
	 * Delete the supplied Path — for directories, recursively delete any nested
	 * directories or files as well.
	 * 
	 * @param path The root Path to delete. If null, do nothing.
	 * @since 1.8
	 */
	private void deleteRecursively(Path path) {
		if (path != null)
			try {
				FileSystemUtils.deleteRecursively(path);
			} catch (IOException e) {
				logger.warn("cannot delete directory " + path + " - " + e.getMessage() + ".");
			}

	}

	/**
	 * Returns true if the provider is of Calamari evaluation type.
	 * 
	 * @param provider The provider.
	 * @return True if the provider is of Calamari evaluation type.
	 * @since 17
	 */
	public static boolean isCalamariEvaluationProvider(ActionServiceProvider provider) {
		return provider != null && calamariEvaluationProvider.equals(provider.getProvider());
	}

	/**
	 * Returns the evaluation measure of the dataset using a Calamari evaluation
	 * provider.
	 * 
	 * @param provider      The provider.
	 * @param modelArgument The provider arguments.
	 * @param dataset       The dataset.
	 * @return The evaluation measure of the dataset. On troubles returns null.
	 * @since 17
	 */
	public EvaluationMeasure evaluateCalamari(ActionServiceProvider provider, ModelArgument modelArgument,
			Dataset dataset) {
		if (!isCalamariEvaluationProvider(provider) || modelArgument == null || dataset == null
				|| dataset.getCollections() == null || dataset.getCollections().isEmpty())
			return null;

		Path temporaryDirectory = null;
		try {
			temporaryDirectory = configurationService.getTemporary().getTemporaryDirectory();

			// copy data to temporary directory
			boolean isEmptyDataset = true;
			for (Dataset.Collection collection : dataset.getCollections())
				if (collection == null || collection.getId() == null || collection.getId().isBlank())
					return null;
				else if (collection.getSets() != null && !collection.getSets().isEmpty()) {
					CollectionService.Collection collectionEntity = collectionService.getCollection(collection.getId());
					if (collectionEntity == null)
						return null;

					Path path = collectionEntity.getConfiguration().getFolder();

					for (String set : collection.getSets())
						if (set != null && !set.isBlank()) {
							Path source = path.resolve(set.trim() + ".xml");
							if (Files.isRegularFile(source)) {
								Files.copy(source, temporaryDirectory.resolve(source.getFileName()),
										StandardCopyOption.REPLACE_EXISTING);

								isEmptyDataset = false;
							}
						}
				}

			if (isEmptyDataset)
				return new EvaluationMeasure(EvaluationMeasure.State.inconsistent, "evaluation: empty dataset");

			WorkerServiceProvider.Worker<Database> worker = provider.newAgent();
			if (worker == null)
				return new EvaluationMeasure(EvaluationMeasure.State.interrupted, "evaluation: no worker available");

			ServiceProviderTask task = worker.execute(
					new Database(temporaryDirectory.getParent(), temporaryDirectory.getFileName().toString()),
					modelArgument);

			if (task == null)
				return new EvaluationMeasure(EvaluationMeasure.State.interrupted,
						"evaluation: no evaluation available");
			if (task instanceof EvaluationMeasure evaluation)
				return evaluation;
			else
				return new EvaluationMeasure(EvaluationMeasure.State.interrupted, "evaluation: wrong worker type");

		} catch (Exception e) {
			logger.warn("cannot evaluate - " + e.getMessage() + ".");

			return new EvaluationMeasure(EvaluationMeasure.State.interrupted, e);
		} finally {
			deleteRecursively(temporaryDirectory);
		}
	}

	/**
	 * Defines data sets.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class Dataset {
		/**
		 * The collections.
		 */
		private List<Collection> collections;

		/**
		 * Default constructor for a data set.
		 * 
		 * @since 17
		 */
		public Dataset() {
			super();
		}

		/**
		 * Returns the collections.
		 *
		 * @return The collections.
		 * @since 17
		 */
		public List<Collection> getCollections() {
			return collections;
		}

		/**
		 * Set the collections.
		 *
		 * @param collections The collections to set.
		 * @since 17
		 */
		public void setCollections(List<Collection> collections) {
			this.collections = collections;
		}

		/**
		 * Defines collections for data sets.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class Collection {
			/**
			 * The id.
			 */
			private String id;

			/**
			 * The sets.
			 */
			private List<String> sets;

			/**
			 * Default constructor for a collection.
			 * 
			 * @since 17
			 */
			public Collection() {
				super();
			}

			/**
			 * Returns the id.
			 *
			 * @return The id.
			 * @since 17
			 */
			public String getId() {
				return id;
			}

			/**
			 * Set the id.
			 *
			 * @param id The id to set.
			 * @since 17
			 */
			public void setId(String id) {
				this.id = id;
			}

			/**
			 * Returns the sets.
			 *
			 * @return The sets.
			 * @since 17
			 */
			public List<String> getSets() {
				return sets;
			}

			/**
			 * Set the sets.
			 *
			 * @param sets The sets to set.
			 * @since 17
			 */
			public void setSets(List<String> sets) {
				this.sets = sets;
			}

		}
	}

}
