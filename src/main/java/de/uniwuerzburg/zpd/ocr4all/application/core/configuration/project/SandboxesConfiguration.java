/**
 * File:     WorkflowsConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     14.03.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.CoreFolder;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project.Workflows;

/**
 * Defines configurations for the workflow containers.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class WorkflowsConfiguration extends CoreFolder {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WorkflowsConfiguration.class);

	/**
	 * The workflow properties.
	 */
	private final Workflows properties;

	/**
	 * Creates a configuration for a workflow container.
	 * 
	 * @param properties           The workflow properties.
	 * @param projectConfiguration The configuration for the project.
	 * @since 1.8
	 */
	WorkflowsConfiguration(Workflows properties, ProjectConfiguration projectConfiguration) {
		super(Paths.get(projectConfiguration.getFolder().toString(), properties.getFolder()));

		this.properties = properties;

		ConfigurationService.initializeFolder(true, folder, "workflows");
	}

	/**
	 * Returns true if the given folder is a valid workflow folder.
	 * 
	 * @param folder The workflow folder.
	 * @return True if the given folder is a valid workflow folder.
	 * @since 1.8
	 */
	private boolean isValid(Path folder) {
		return folder != null && folder.normalize().getParent().equals(this.folder);
	}

	/**
	 * Returns the workflow configuration.
	 * 
	 * @param folder The workflow folder.
	 * @param user   The user.
	 * @return The workflow configuration. Null if the given folder is a valid
	 *         workflow folder.
	 * @since 1.8
	 */
	public WorkflowConfiguration getWorkflow(Path folder, String user) {
		return isValid(folder) && Files.isDirectory(folder)
				? new WorkflowConfiguration(properties.getWorkflow(), folder, user)
				: null;
	}

	/**
	 * Returns true if the workflow is available.
	 * 
	 * @param folderName The workflow folder name.
	 * @return True if the workflow is available.
	 * @since 1.8
	 */
	public boolean isAvailable(String folderName) {
		if (folderName == null || folderName.isBlank())
			return false;
		else {
			Path path = Paths.get(folder.toString(), folderName.trim());
			return isValid(path) && Files.exists(path);
		}
	}

	/**
	 * Creates the workflow.
	 * 
	 * @param folderName The workflow folder name to create.
	 * @param user       The user.
	 * @return The workflow path. Null if it cannot be created.
	 * @since 1.8
	 */
	public Path create(String folderName, String user) {
		if (folderName == null || folderName.isBlank()) {
			logger.warn(
					"Cannot create workflow" + (user == null ? "" : ", user=" + user) + " - the directory is empty.");

			return null;
		}

		Path folder = Paths.get(this.folder.toString(), folderName.trim()).normalize();

		if (isValid(folder)) {
			if (Files.exists(folder)) {
				logger.warn("Cannot create workflow '" + folder.toString() + "'"
						+ (user == null ? "" : ", user=" + user) + " - the workflow is already available.");

				return null;
			}

			try {
				Files.createDirectory(folder);

				logger.info("Created workflow folder '" + folder.toString() + "'"
						+ (user == null ? "" : ", user=" + user) + ".");

				return folder;
			} catch (Exception e) {
				logger.warn("Cannot create workflow '" + folder.toString() + "'"
						+ (user == null ? "" : ", user=" + user) + " - " + e.getMessage() + ".");
			}
		} else
			logger.warn("Cannot create workflow '" + folder.toString() + "'" + (user == null ? "" : ", user=" + user)
					+ " - not a valid workflow folder.");

		return null;
	}

	/**
	 * Removes the workflow.
	 * 
	 * @param folder The workflow folder to remove.
	 * @param user   The user.
	 * @return True if the workflow could be removed.
	 * @since 1.8
	 */
	public boolean remove(Path folder, String user) {
		if (isValid(folder)) {
			boolean isRemoved = false;
			try {
				Files.walk(folder).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);

				isRemoved = !Files.exists(folder);
				if (isRemoved)
					logger.info("Removed workflow '" + folder.toString() + "'" + (user == null ? "" : ", user=" + user)
							+ ".");
				else
					logger.warn("Cannot remove the complete workflow '" + folder.toString() + "'"
							+ (user == null ? "" : ", user=" + user) + ".");
			} catch (Exception e) {
				logger.warn("Cannot remove workflow '" + folder.toString() + "'"
						+ (user == null ? "" : ", user=" + user) + " - " + e.getMessage() + ".");
			}

			return isRemoved;
		} else {
			logger.warn("Cannot remove workflow '" + folder.toString() + "'" + (user == null ? "" : ", user=" + user)
					+ " - not a valid workflow folder.");

			return false;
		}

	}

	/**
	 * Resets the workflows.
	 *
	 * @return True if the workflows could be reset.
	 * 
	 * @since 1.8
	 */
	public boolean reset() {
		return deleteContents();
	}

	/**
	 * Returns the mets file name.
	 *
	 * @return The mets file name.
	 * @since 1.8
	 */
	public String getMetsFileName() {
		return properties.getWorkflow().getMets().getFile();
	}

	/**
	 * Returns the mets group.
	 *
	 * @return The mets group.
	 * @since 1.8
	 */
	public String getMetsGroup() {
		return properties.getWorkflow().getMets().getGroup();
	}

}
