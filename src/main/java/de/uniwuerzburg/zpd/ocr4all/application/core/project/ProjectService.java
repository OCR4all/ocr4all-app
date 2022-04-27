/**
 * File:     ProjectService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.project
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     09.11.2020
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;

/**
 * Defines project services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public abstract class ProjectService extends CoreService {
	/**
	 * The security service.
	 */
	protected final SecurityService securityService;

	/**
	 * Creates a project service.
	 * 
	 * @param logger               The logger class.
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @since 1.8
	 */
	protected ProjectService(Class<? extends ProjectService> logger, ConfigurationService configurationService,
			SecurityService securityService) {
		super(logger, configurationService);

		this.securityService = securityService;
	}

	/**
	 * Loads the project and returns it.
	 * 
	 * @param path The project path.
	 * @return The project.
	 * @since 1.8
	 */
	protected abstract Project loadProject(Path path);

	/**
	 * Returns the projects root folder.
	 * 
	 * @return The projects root folder.
	 * @since 1.8
	 */
	public Path getRootFolder() {
		return configurationService.getWorkspace().getProjects().getFolder();
	}

	/**
	 * Returns the project path. Creates it if required.
	 * 
	 * @return The project path. Empty, if the path is not a directory.
	 * @since 1.8
	 */
	private Optional<Path> getProjectsPath() {
		return ConfigurationService.initializeFolder(false, getRootFolder(), "projects");
	}

	/**
	 * Returns the project.
	 * 
	 * @param path The project path.
	 * @return The project. Empty, if the project is not a available.
	 * @since 1.8
	 */
	private Optional<Project> getProject(Path path) {
		if (Files.isDirectory(path)) {
			Project project = loadProject(path);

			logger.info("Loaded project: " + project);

			return Optional.of(project);
		} else {
			logger.warn("The project folder '" + path + "' is not a directory.");

			return Optional.empty();
		}
	}

	/**
	 * Returns the projects sorted by name.
	 * 
	 * @param isRightExist True if consider the given rights.
	 * @param rights       The required rights.
	 * @return The projects.
	 * @since 1.8
	 */
	private List<Project> getProjects(boolean isRightExist, Project.Right... rights) {
		List<Project> projects = new ArrayList<>();

		try {
			Optional<Path> projectsPath = getProjectsPath();
			if (projectsPath.isPresent()) {
				Files.list(projectsPath.get()).filter(Files::isDirectory).forEach(path -> {
					Optional<Project> project = getProject(path);
					if (project.isPresent() && (isRightExist || project.get().isRights(rights))
							&& (!isRightExist || project.get().isRightExist()))
						projects.add(project.get());
				});

				Collections.sort(projects, (p1, p2) -> p1.getConfiguration().getConfiguration().getName()
						.compareToIgnoreCase(p2.getConfiguration().getConfiguration().getName()));
			}

		} catch (IOException e) {
			logger.warn("Cannot not load projects - " + e.getMessage());
		}

		return projects;
	}

	/**
	 * Returns the projects sorted by name with required rights.
	 * 
	 * @param rights The required rights.
	 * @return The projects.
	 * @since 1.8
	 */
	public List<Project> getProjects(Project.Right... rights) {
		return getProjects(false, rights);
	}

	/**
	 * Returns the projects sorted by name with some rights.
	 * 
	 * @return The projects.
	 * @since 1.8
	 */
	public List<Project> getProjectsRightExist() {
		return getProjects(true);
	}

	/**
	 * Returns true if the project is available.
	 * 
	 * @param folderName The project folder name.
	 * @return True if the project is available.
	 * @since 1.8
	 */
	public boolean isAvailable(String folderName) {
		return configurationService.getWorkspace().getProjects().isAvailable(folderName);
	}

	/**
	 * Returns the project with given folder if some rights is available.
	 * 
	 * @param folder The project folder.
	 * @return The project. Null if not available.
	 * @since 1.8
	 */
	public Project getProject(String folder) {
		Path path = configurationService.getWorkspace().getProjects().getProjectPath(folder);
		if (path == null)
			return null;

		Optional<Project> project = getProject(path);
		return project.isPresent() && project.get().isRightExist() ? project.get() : null;
	}

	/**
	 * Authorizes the project.
	 * 
	 * @param id The project id. This is the folder name.
	 * @return The project if authorized. Otherwise returns null.
	 * @since 1.8
	 */
	public Project authorize(String id) {
		return authorize(getProject(id));
	}

	/**
	 * Authorizes the project.
	 * 
	 * @param project The project.
	 * @return The project if authorized. Otherwise returns null.
	 * @since 1.8
	 */
	public Project authorize(Project project) {
		return project != null
				&& (project.isCoordinator() || !project.getConfiguration().getConfiguration().isStateBlocked())
						? project
						: null;
	}

}
