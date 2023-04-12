/**
 * File:     SandboxesConfiguration.java
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
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project.Sandboxes;

/**
 * Defines configurations for the sandbox containers.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class SandboxesConfiguration extends CoreFolder {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SandboxesConfiguration.class);

	/**
	 * The sandbox properties.
	 */
	private final Sandboxes properties;

	/**
	 * Creates a configuration for a sandbox container.
	 * 
	 * @param properties           The sandbox properties.
	 * @param projectConfiguration The configuration for the project.
	 * @since 1.8
	 */
	SandboxesConfiguration(Sandboxes properties, ProjectConfiguration projectConfiguration) {
		super(Paths.get(projectConfiguration.getFolder().toString(), properties.getFolder()));

		this.properties = properties;

		ConfigurationService.initializeFolder(true, folder, "sandboxes");
	}

	/**
	 * Returns true if the given folder is a valid sandbox folder.
	 * 
	 * @param folder The sandbox folder.
	 * @return True if the given folder is a valid sandbox folder.
	 * @since 1.8
	 */
	private boolean isValid(Path folder) {
		return folder != null && folder.normalize().getParent().equals(this.folder);
	}

	/**
	 * Returns the sandbox configuration.
	 * 
	 * @param folder The sandbox folder.
	 * @param user   The user.
	 * @return The sandbox configuration. Null if the given folder is a valid
	 *         sandbox folder.
	 * @since 1.8
	 */
	public SandboxConfiguration getSandbox(Path folder, String user) {
		return isValid(folder) && Files.isDirectory(folder)
				? new SandboxConfiguration(properties.getSandbox(), folder, user)
				: null;
	}

	/**
	 * Returns true if the sandbox is available.
	 * 
	 * @param folderName The sandbox folder name.
	 * @return True if the sandbox is available.
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
	 * Creates the sandbox.
	 * 
	 * @param folderName The sandbox folder name to create.
	 * @param user       The user.
	 * @return The sandbox path. Null if it cannot be created.
	 * @since 1.8
	 */
	public Path create(String folderName, String user) {
		if (folderName == null || folderName.isBlank()) {
			logger.warn(
					"Cannot create sandbox" + (user == null ? "" : ", user=" + user) + " - the directory is empty.");

			return null;
		}

		Path folder = Paths.get(this.folder.toString(), folderName.trim()).normalize();

		if (isValid(folder)) {
			if (Files.exists(folder)) {
				logger.warn("Cannot create sandbox '" + folder.toString() + "'" + (user == null ? "" : ", user=" + user)
						+ " - the sandbox is already available.");

				return null;
			}

			try {
				Files.createDirectory(folder);

				logger.info("Created sandbox folder '" + folder.toString() + "'"
						+ (user == null ? "" : ", user=" + user) + ".");

				return folder;
			} catch (Exception e) {
				logger.warn("Cannot create sandbox '" + folder.toString() + "'" + (user == null ? "" : ", user=" + user)
						+ " - " + e.getMessage() + ".");
			}
		} else
			logger.warn("Cannot create sandbox '" + folder.toString() + "'" + (user == null ? "" : ", user=" + user)
					+ " - not a valid sandbox folder.");

		return null;
	}

	/**
	 * Removes the sandbox.
	 * 
	 * @param folder The sandbox folder to remove.
	 * @param user   The user.
	 * @return True if the sandbox could be removed.
	 * @since 1.8
	 */
	public boolean remove(Path folder, String user) {
		if (isValid(folder)) {
			boolean isRemoved = false;
			try {
				Files.walk(folder).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);

				isRemoved = !Files.exists(folder);
				if (isRemoved)
					logger.info("Removed sandbox '" + folder.toString() + "'" + (user == null ? "" : ", user=" + user)
							+ ".");
				else
					logger.warn("Cannot remove the complete sandbox '" + folder.toString() + "'"
							+ (user == null ? "" : ", user=" + user) + ".");
			} catch (Exception e) {
				logger.warn("Cannot remove sandbox '" + folder.toString() + "'" + (user == null ? "" : ", user=" + user)
						+ " - " + e.getMessage() + ".");
			}

			return isRemoved;
		} else {
			logger.warn("Cannot remove sandbox '" + folder.toString() + "'" + (user == null ? "" : ", user=" + user)
					+ " - not a valid sandbox folder.");

			return false;
		}

	}

	/**
	 * Resets the sandboxes.
	 *
	 * @return True if the sandboxes could be reset.
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
		return properties.getSandbox().getMets().getFile();
	}

	/**
	 * Returns the mets group.
	 *
	 * @return The mets group.
	 * @since 1.8
	 */
	public String getMetsGroup() {
		return properties.getSandbox().getMets().getGroup();
	}

}
