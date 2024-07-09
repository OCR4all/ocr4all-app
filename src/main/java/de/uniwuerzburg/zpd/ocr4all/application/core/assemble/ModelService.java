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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.assemble.ModelConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.assemble.ModelConfiguration.Configuration;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.assemble.Engine;
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
	 * Returns the available models sorted by name, this means, the models for which
	 * the user has read rights.
	 * 
	 * @param filter         The filter. If null, no filter is applied.
	 * @param filenameSuffix The suffix for the model file names to return. If
	 *                       empty, do not return them.
	 * @return The models with the desired file names.
	 * @since 1.8
	 */
	public List<ModelFile> getAvailableModels(ModelFilter filter, String filenameSuffix) {
		List<ModelFile> modelFiles = new ArrayList<>();

		// The suffix for the model file names
		final String sufix = filenameSuffix == null || filenameSuffix.isBlank() ? null : filenameSuffix.trim();

		// The version filter
		ComparableVersion minimumVersion = null, maximumVersion = null;
		if (filter != null) {
			if (!filter.isTypeSet() && !filter.isStatesSet() && !filter.isMinimumVersionSet()
					&& !filter.isMaximumVersionSet())
				filter = null;
			else {
				if (filter.isMinimumVersionSet())
					minimumVersion = new ComparableVersion(filter.getMinimumVersion());

				if (filter.isMaximumVersionSet())
					maximumVersion = new ComparableVersion(filter.getMaximumVersion());

			}

		}

		// Select the models
		for (Model model : getModels())
			if (model.getRight().isReadFulfilled()) {
				if (filter != null) {
					if (!model.getConfiguration().getConfiguration().isEngineConfigurationAvailable())
						break;

					Engine engine = model.getConfiguration().getConfiguration().getEngineConfiguration();

					if (filter.isTypeSet() && !filter.getType().equals(engine.getType()))
						break;

					if (filter.isStatesSet() && !filter.getStates().contains(engine.getState()))
						break;

					if (filter.isMinimumVersionSet() || filter.isMaximumVersionSet()) {
						if (engine.getVersion() == null || engine.getVersion().isBlank())
							break;

						ComparableVersion version = new ComparableVersion(engine.getVersion().trim());

						if (minimumVersion != null && minimumVersion.compareTo(version) > 0)
							break;

						if (maximumVersion != null && maximumVersion.compareTo(version) < 0)
							break;
					}
				}

				List<String> filenames = new ArrayList<>();
				if (sufix != null)
					try {
						Files.list(model.getConfiguration().getFolder()).filter(Files::isRegularFile).forEach(path -> {
							String filename = path.getFileName().toString();

							// Ignore file names beginning with a dot and not ending with desired filename
							// suffix
							if (!filename.startsWith(".") && filename.endsWith(sufix))
								filenames.add(filename);
						});
					} catch (IOException e) {
						logger.warn("Cannot not read model files - " + e.getMessage());
					}

				modelFiles.add(new ModelFile(model, filenames));
			}

		return modelFiles;
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
				logger.warn("Cannot remove model '" + uuid + "', since the engine is not done.");
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
	 * Store the models.
	 * 
	 * @param model The model.
	 * @param files The models.
	 * @return False if model is unknown or the write right is not fulfilled or not
	 *         all files could be stored.
	 * @throws IOException Throws on storage troubles.
	 * @since 1.8
	 */
	public boolean store(Model model, MultipartFile[] files) throws IOException {
		// TODO: if engine uploading + update state to done.
		if (model == null || files == null || !model.getRight().isWriteFulfilled())
			return false;
		else {
			// create tmp folder
			Path folder = model.getConfiguration().getFolder();

			// store the files
			boolean isAllStored = true;
			for (MultipartFile file : files)
				if (file != null && !file.isEmpty()) {
					try (InputStream inputStream = file.getInputStream()) {
						Files.copy(inputStream, folder.resolve(Paths.get(file.getOriginalFilename())),
								StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						logger.warn("Failed to store file '" + file.getOriginalFilename() + "' - " + e.getMessage());

						isAllStored = false;
						continue;
					}
				}

			return isAllStored;
		}
	}

	/**
	 * Model is an immutable class that defines models.
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
		 * Creates a model.
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

	/**
	 * ModelFile is an immutable class that defines models with file names.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ModelFile extends Model {
		/**
		 * The file names.
		 */
		private final List<String> filenames;

		/**
		 * Creates a model with file names.
		 * 
		 * @param model     The model.
		 * @param filenames The file names.
		 * @since 17
		 */
		public ModelFile(Model model, List<String> filenames) {
			super(model.getRight(), model.getConfiguration());

			this.filenames = filenames;
		}

		/**
		 * Returns the file names.
		 *
		 * @return The file names.
		 * @since 17
		 */
		public List<String> getFilenames() {
			return filenames;
		}

	}

	/**
	 * Defines filters for models.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class ModelFilter {
		/**
		 * The engine type.
		 */
		private final Engine.Type type;

		/**
		 * The engine states.
		 */
		private final Set<Engine.State> states;

		/**
		 * The minimum version.
		 */
		private final String minimumVersion;

		/**
		 * The maximum version.
		 */
		private final String maximumVersion;

		/**
		 * Creates a filter for models.
		 * 
		 * @param type           The engine type.
		 * @param states         The states.
		 * @param minimumVersion The minimum version.
		 * @param maximumVersion The maximum version.
		 * @since 17
		 */
		public ModelFilter(Engine.Type type, Set<Engine.State> states, String minimumVersion, String maximumVersion) {
			super();

			this.type = type;

			if (states == null)
				this.states = null;
			else {
				Set<Engine.State> set = new HashSet<>();
				for (Engine.State state : states)
					if (state != null)
						set.add(state);

				this.states = set.isEmpty() ? null : set;
			}

			this.minimumVersion = minimumVersion == null || minimumVersion.isBlank() ? null : minimumVersion.trim();
			this.maximumVersion = maximumVersion == null || maximumVersion.isBlank() ? null : maximumVersion.trim();
		}

		/**
		 * Returns true if the engine type is set.
		 *
		 * @return True if the engine type is set.
		 * @since 17
		 */
		public boolean isTypeSet() {
			return type != null;
		}

		/**
		 * Returns the engine type.
		 *
		 * @return The engine type.
		 * @since 17
		 */
		public Engine.Type getType() {
			return type;
		}

		/**
		 * Returns true if the engine states are set.
		 *
		 * @return True if the engine states are set.
		 * @since 17
		 */
		public boolean isStatesSet() {
			return states != null;
		}

		/**
		 * Returns the engine states.
		 *
		 * @return The engine states.
		 * @since 17
		 */
		public Set<Engine.State> getStates() {
			return states;
		}

		/**
		 * Returns true if the minimum version is set.
		 *
		 * @return True if the minimum version is set.
		 * @since 17
		 */
		public boolean isMinimumVersionSet() {
			return minimumVersion != null;
		}

		/**
		 * Returns the minimum version.
		 *
		 * @return The minimum version.
		 * @since 17
		 */
		public String getMinimumVersion() {
			return minimumVersion;
		}

		/**
		 * Returns true if the maximum version is set.
		 *
		 * @return True if the maximum version is set.
		 * @since 17
		 */
		public boolean isMaximumVersionSet() {
			return maximumVersion != null;
		}

		/**
		 * Returns the maximum version.
		 *
		 * @return The maximum version.
		 * @since 17
		 */
		public String getMaximumVersion() {
			return maximumVersion;
		}

	}

}
