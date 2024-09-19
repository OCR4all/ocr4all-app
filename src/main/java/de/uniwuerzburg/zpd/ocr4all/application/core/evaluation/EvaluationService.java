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

import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uniwuerzburg.zpd.ocr4all.application.communication.action.EvaluationMeasure;
import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.spi.ActionServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.WorkerServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Database;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Dataset;
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
	private static final String calamariEvaluationProvider = "calamari/evaluation";

	/**
	 * The JSON object mapper.
	 */
	protected final ObjectMapper objectMapper = new ObjectMapper();
	{
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

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
		if (provider == null || !calamariEvaluationProvider.equals(provider.getProvider()) || modelArgument == null
				|| dataset == null)
			return null;

		Path temporaryDirectory = null;
		try {
			temporaryDirectory = configurationService.getTemporary().getTemporaryDirectory();

			// copy data to temporary directory
			boolean isEmptyDataset = true;
			for (Dataset.Collection collection : dataset.getCollections()) {
				Path path = collectionService.getCollection(collection.getId()).getConfiguration().getFolder();

				for (Dataset.Collection.Set set : collection.getSets()) {
					Path source = path.resolve(set.getId() + "." + set.getXml());
					if (Files.isRegularFile(source)) {
						Files.copy(source, temporaryDirectory.resolve(source.getFileName()),
								StandardCopyOption.REPLACE_EXISTING);

						isEmptyDataset = false;
					}
				}
			}

			if (isEmptyDataset)
				return null;

			WorkerServiceProvider.Worker<Database> worker = provider.newAgent();
			if (worker == null)
				return null;

			String evaluation = worker.execute(
					new Database(temporaryDirectory.getParent(), temporaryDirectory.getFileName().toString()),
					modelArgument);

			return evaluation == null ? null : objectMapper.readValue(evaluation, EvaluationMeasure.class);
		} catch (Exception e) {
			logger.warn("cannot evaluate - " + e.getMessage() + ".");

			return null;
		} finally {
			deleteRecursively(temporaryDirectory);
		}
	}
}
