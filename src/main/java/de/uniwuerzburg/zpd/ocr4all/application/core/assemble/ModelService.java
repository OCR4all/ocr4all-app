/**
 * File:     ModelService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.assemble
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     12.06.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.assemble;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.assemble.ModelConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.assemble.ModelConfiguration.Configuration;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.security.SecurityGrant;

/**
 * Defines model services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@Service
public class ModelService extends CoreService {
	/**
	 * The security service.
	 */
	private final SecurityService securityService;

	/**
	 * The data service.
	 */
	private final AssembleService dataService;

	/**
	 * The folder.
	 */
	protected final Path folder;

	/**
	 * Creates a model service.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param dataService          The data service.
	 * @since 1.8
	 */
	public ModelService(ConfigurationService configurationService, SecurityService securityService,
			AssembleService dataService) {
		super(ModelService.class, configurationService);

		this.securityService = securityService;
		this.dataService = dataService;

		folder = configurationService.getAssemble().getFolder().normalize();
	}

	/**
	 * Returns the model.
	 * 
	 * @param configuration The model configuration.
	 * @return The model.
	 * @since 1.8
	 */
	private Model getModel(ModelConfiguration configuration) {
		return new Model(dataService.isAdministrator() ? SecurityGrant.Right.maximal
				: configuration.getConfiguration().getRight(securityService.getUser(),
						securityService.getActiveGroups()),
				configuration);
	}

	/**
	 * Returns the model folder.
	 * 
	 * @param uuid The model uuid.
	 * @return The model folder. If the uuid is invalid, null is returned.
	 * @since 1.8
	 */
	private Path getPath(String uuid) {
		if (uuid == null || uuid.isBlank())
			return null;
		else {
			Path folder = Paths.get(this.folder.toString(), uuid.trim()).normalize();

			// Ignore directories beginning with a dot
			return Files.isDirectory(folder) && folder.getParent().equals(this.folder)
					&& !folder.getFileName().toString().startsWith(".") ? folder : null;
		}
	}

	/**
	 * Returns the model.
	 * 
	 * @param path The model path.
	 * @return The model.
	 * @since 1.8
	 */
	private Model getModel(Path path) {
		return getModel(new ModelConfiguration(configurationService.getAssemble().getModel(), path));
	}

	/**
	 * Returns true if a model can be created.
	 * 
	 * @return True if a model can be created.
	 * @since 1.8
	 */
	public boolean isCreate() {
		return dataService.isCreateModel();
	}

	/**
	 * Creates a model.
	 * 
	 * @param name        The name.
	 * @param description The description.
	 * @param keywords    The keywords.
	 * @return The model configuration. Null if the model can not be created.
	 * @since 1.8
	 */
	public Model create(String name, String description, Set<String> keywords) {
		if (isCreate()) {
			final String user = securityService.getUser();
			final Path folder = Paths.get(ModelService.this.folder.toString(), OCR4allUtils.getUUID()).normalize();

			try {
				Files.createDirectory(folder);

				logger.info("Created model folder '" + folder.toString() + "'" + (user == null ? "" : ", user=" + user)
						+ ".");
			} catch (Exception e) {
				logger.warn("Cannot create model '" + folder.toString() + "'" + (user == null ? "" : ", user=" + user)
						+ " - " + e.getMessage() + ".");

				return null;
			}

			return getModel(new ModelConfiguration(configurationService.getAssemble().getModel(), folder,
					new Configuration.CoreData(user, name, description, keywords)));
		} else
			return null;
	}

	/**
	 * Returns the model.
	 * 
	 * @param uuid The model uuid.
	 * @return The model. Null if unknown.
	 * @since 1.8
	 */
	public Model getModel(String uuid) {
		Path path = getPath(uuid);

		return path == null ? null : getModel(path);
	}

	/**
	 * Returns the models sorted by name.
	 * 
	 * @return The models.
	 * @since 1.8
	 */
	public List<Model> getModels() {
		List<Model> models = new ArrayList<>();

		try {
			Files.list(ModelService.this.folder).filter(Files::isDirectory).forEach(path -> {
				// Ignore directories beginning with a dot
				if (!path.getFileName().toString().startsWith("."))
					models.add(getModel(path));
			});
		} catch (IOException e) {
			logger.warn("Cannot not load models - " + e.getMessage());
		}

		Collections.sort(models, (p1, p2) -> p1.getConfiguration().getConfiguration().getName()
				.compareToIgnoreCase(p2.getConfiguration().getConfiguration().getName()));

		return models;
	}

	/**
	 * Returns the readable models sorted by name.
	 * 
	 * @return The models.
	 * @since 1.8
	 */
	public List<Model> getReadableModels() {
		List<Model> models = new ArrayList<>();
		
		// TODO: continue

		try {
			Files.list(ModelService.this.folder).filter(Files::isDirectory).forEach(path -> {
				// Ignore directories beginning with a dot
				if (!path.getFileName().toString().startsWith("."))
					models.add(getModel(path));
			});
		} catch (IOException e) {
			logger.warn("Cannot not load models - " + e.getMessage());
		}

		Collections.sort(models, (p1, p2) -> p1.getConfiguration().getConfiguration().getName()
				.compareToIgnoreCase(p2.getConfiguration().getConfiguration().getName()));

		return models;
	}

	/**
	 * Removes the model if the special right is fulfilled.
	 * 
	 * @param uuid The model uuid.
	 * @return True if the model could be removed.
	 * @since 1.8
	 */
	public boolean remove(String uuid) {
		Path path = getPath(uuid);

		if (path != null && getModel(path).getRight().isSpecialFulfilled()) {
			Model model = getModel(path);

			if (model.getConfiguration().getConfiguration().isEngineConfigurationAvailable()
					&& !model.getConfiguration().getConfiguration().getEngineConfiguration().getState().isDone())
				logger.warn("Cannot remove model '" + uuid + "', since the training engine is running.");
			else
				try {
					Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);

					if (!Files.exists(path)) {
						logger.info("Removed model '" + path.toString() + "'.");

						return true;
					} else
						logger.warn("Troubles removing the model '" + path.toString() + "'.");
				} catch (Exception e) {
					logger.warn("Cannot remove model '" + path.toString() + "' - " + e.getMessage() + ".");
				}
		} else {
			logger.warn("Cannot remove model '" + uuid + "'.");
		}

		return false;
	}

	/**
	 * Updates a model.
	 * 
	 * @param uuid        The model uuid.
	 * @param name        The name.
	 * @param description The description.
	 * @param keywords    The keywords.
	 * @return The model. Null if the model can not be updated.
	 * @since 1.8
	 */
	public Model update(String uuid, String name, String description, Set<String> keywords) {
		Path path = getPath(uuid);

		if (path != null) {
			Model model = getModel(path);

			if (model.getRight().isSpecialFulfilled()
					&& model.getConfiguration().getConfiguration().update(securityService.getUser(),
							new ModelConfiguration.Configuration.Information(name, description, keywords)))
				return model;
		}

		return null;
	}

	/**
	 * Updates the security.
	 *
	 * @param uuid     The model uuid.
	 * @param security The model security.
	 * @return The updated model. Null if it can not be updated.
	 * @since 1.8
	 */
	public Model update(String uuid, SecurityGrant security) {
		Path path = getPath(uuid);

		if (path != null) {
			Model model = getModel(path);

			if (model.getRight().isSpecialFulfilled()
					&& model.getConfiguration().getConfiguration().update(securityService.getUser(), security))
				return model;
		}

		return null;
	}

	/**
	 * Model is an immutable class that defines collections.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Model {
		/**
		 * The right.
		 */
		private final SecurityGrant.Right right;

		/**
		 * The configuration.
		 */
		private final ModelConfiguration configuration;

		/**
		 * Creates a collection.
		 * 
		 * @param right         The right.
		 * @param configuration The configuration.
		 * @since 1.8
		 */
		public Model(SecurityGrant.Right right, ModelConfiguration configuration) {
			super();

			this.right = right;
			this.configuration = configuration;
		}

		/**
		 * Returns the right.
		 *
		 * @return The right.
		 * @since 1.8
		 */
		public SecurityGrant.Right getRight() {
			return right;
		}

		/**
		 * Returns the configuration.
		 *
		 * @return The configuration.
		 * @since 1.8
		 */
		public ModelConfiguration getConfiguration() {
			return configuration;
		}

	}
}
