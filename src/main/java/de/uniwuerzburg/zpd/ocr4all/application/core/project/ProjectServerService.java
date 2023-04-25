/**
 * File:     ProjectServerService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.project
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     09.11.2020
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.project;

import java.nio.file.Path;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project.ProjectConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project.Right;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityServerService;

/**
 * Defines project services for server profiles.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("server")
@Service
public class ProjectServerService extends ProjectService {
	/**
	 * Creates a project service for a server profile.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @since 1.8
	 */
	public ProjectServerService(ConfigurationService configurationService, SecurityServerService securityService) {
		super(ProjectServerService.class, configurationService, securityService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService#
	 * loadProject(java.nio.file.Path)
	 */
	@Override
	protected Project loadProject(Path path) {
		final String user = securityService.getUser();

		Project project = new Project(configurationService.getWorkspace().getProjects().getProject(path, user));
		project.setSecurityLevel(securityService.getSecurityLevel());

		/*
		 * Set the project security rights
		 */
		if (user == null)
			project.removeAllRights();
		else {
			if (project.isCoordinator())
				project.addAllRights();
			else {
				// The user rights
				addRights(project, project.getConfiguration().getConfiguration().getRights(user));

				// The groups rights
				addRights(project,
						project.getConfiguration().getConfiguration().getRights(securityService.getActiveGroups()));

				// The other rights
				addRights(project, project.getConfiguration().getConfiguration().getRights());
			}
		}

		return project;
	}

	/**
	 * Adds the rights to the project.
	 * 
	 * @param project The project to add the rights.
	 * @param right   The rights to add.
	 * @since 1.8
	 */
	private void addRights(Project project, ProjectConfiguration.Right right) {
		if (right != null) {
			if (right.isRead())
				project.addRight(Right.read);
			if (right.isWrite())
				project.addRight(Right.write);
			if (right.isExecute())
				project.addRight(Right.execute);
			if (right.isSpecial())
				project.addRight(Right.special);
		}
	}
}
