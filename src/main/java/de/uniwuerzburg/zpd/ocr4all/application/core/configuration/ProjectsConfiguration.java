/**
 * File:     ProjectsConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     24.03.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.assemble.AssembleConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.data.DataConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project.ProjectConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project.Projects;

/**
 * Defines configurations for the project containers.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class ProjectsConfiguration extends CoreFolder {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProjectsConfiguration.class);

	/**
	 * The project properties.
	 */
	private final Projects properties;

	/**
	 * The configuration for the exchange.
	 */
	private final ExchangeConfiguration exchangeConfiguration;

	/**
	 * The configuration for the opt.
	 */
	private final OptConfiguration optConfiguration;

	/**
	 * The configuration for the data.
	 */
	private final DataConfiguration dataConfiguration;

	/**
	 * The configuration for the assemble.
	 */
	private final AssembleConfiguration assembleConfiguration;

	/**
	 * Creates a configuration for a project container.
	 * 
	 * @param properties             The project properties.
	 * @param exchangeConfiguration  The configuration for the exchange.
	 * @param optConfiguration       The configuration for the opt.
	 * @param dataConfiguration      The configuration for the data.
	 * @param assembleConfiguration  The configuration for the assemble.
	 * @param workspaceConfiguration The configuration for the workspace.
	 * @since 1.8
	 */
	public ProjectsConfiguration(Projects properties, ExchangeConfiguration exchangeConfiguration,
			OptConfiguration optConfiguration, DataConfiguration dataConfiguration,
			AssembleConfiguration assembleConfiguration, WorkspaceConfiguration workspaceConfiguration) {
		super(Paths.get(workspaceConfiguration.getFolder().toString(), properties.getFolder()));

		this.properties = properties;

		this.exchangeConfiguration = exchangeConfiguration;
		this.optConfiguration = optConfiguration;
		this.dataConfiguration = dataConfiguration;
		this.assembleConfiguration = assembleConfiguration;

		ConfigurationService.initializeFolder(true, folder, "projects");
	}

	/**
	 * Returns true if the given folder is a valid project folder.
	 * 
	 * @param folder The project folder.
	 * @return True if the given folder is a valid project folder.
	 * @since 1.8
	 */
	private boolean isValid(Path folder) {
		return folder != null && folder.normalize().getParent().equals(this.folder);
	}

	/**
	 * Returns the project configuration.
	 * 
	 * @param folder The project folder.
	 * @param user   The user.
	 * @return The project configuration.
	 * @since 1.8
	 */
	public ProjectConfiguration getProject(Path folder, String user) {
		return isValid(folder) && Files.isDirectory(folder) ? new ProjectConfiguration(properties.getProject(),
				exchangeConfiguration, optConfiguration, dataConfiguration, assembleConfiguration, folder, user) : null;
	}

	/**
	 * Return the project path for given folder name.
	 * 
	 * @param folderName The folder name.
	 * @return The project path for given folder name. Null if the given folder name
	 *         build a not valid project folder.
	 * @since 1.8
	 */
	public Path getProjectPath(String folderName) {
		if (folderName == null || folderName.isBlank())
			return null;
		else {
			Path path = Paths.get(folder.toString(), folderName.trim());
			return isValid(path) ? path : null;
		}

	}

	/**
	 * Returns true if the project is available.
	 * 
	 * @param folderName The project folder name.
	 * @return True if the project is available.
	 * @since 1.8
	 */
	public boolean isAvailable(String folderName) {
		Path path = getProjectPath(folderName);

		return path != null && Files.exists(path);
	}

	/**
	 * Creates the project.
	 * 
	 * @param folderName The project folder name to create.
	 * @param user       The user.
	 * @return The project path. Null if it cannot be created.
	 * @since 1.8
	 */
	public Path createProject(String folderName, String user) {
		if (folderName == null || folderName.isBlank()) {
			logger.warn(
					"Cannot create project" + (user == null ? "" : ", user=" + user) + " - the directory is empty.");

			return null;
		}

		Path folder = Paths.get(this.folder.toString(), folderName.trim()).normalize();

		if (isValid(folder)) {
			if (Files.exists(folder)) {
				logger.warn("Cannot create project '" + folder.toString() + "'" + (user == null ? "" : ", user=" + user)
						+ " - the project is already available.");

				return null;
			}

			try {
				Files.createDirectory(folder);

				logger.info("Created project folder '" + folder.toString() + "'"
						+ (user == null ? "" : ", user=" + user) + ".");

				return folder;
			} catch (Exception e) {
				logger.warn("Cannot create project '" + folder.toString() + "'" + (user == null ? "" : ", user=" + user)
						+ " - " + e.getMessage() + ".");
			}
		} else
			logger.warn("Cannot create project '" + folder.toString() + "'" + (user == null ? "" : ", user=" + user)
					+ " - not a valid project folder.");

		return null;
	}

	/**
	 * Removes the project.
	 * 
	 * @param folder The project folder to remove.
	 * @param user   The user.
	 * @return True if the project could be removed.
	 * @since 1.8
	 */
	public boolean removeProject(Path folder, String user) {
		if (isValid(folder)) {
			boolean isRemoved = false;
			try {
				Files.walk(folder).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);

				isRemoved = !Files.exists(folder);
				if (isRemoved)
					logger.info("Removed project '" + folder.toString() + "'" + (user == null ? "" : ", user=" + user)
							+ ".");
				else
					logger.warn("Cannot remove the complete project '" + folder.toString() + "'"
							+ (user == null ? "" : ", user=" + user) + ".");
			} catch (Exception e) {
				logger.warn("Cannot remove project '" + folder.toString() + "'" + (user == null ? "" : ", user=" + user)
						+ " - " + e.getMessage() + ".");
			}

			return isRemoved;
		} else {
			logger.warn("Cannot remove project '" + folder.toString() + "'" + (user == null ? "" : ", user=" + user)
					+ " - not a valid project folder.");

			return false;
		}
	}

}
