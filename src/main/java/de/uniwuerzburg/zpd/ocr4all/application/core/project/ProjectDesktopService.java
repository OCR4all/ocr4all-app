/**
 * File:     ProjectDesktopService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.project
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     09.11.2020
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.project;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityDesktopService;

/**
 * Defines project services for desktop profiles.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("desktop")
@Service
public class ProjectDesktopService extends ProjectService {

	/**
	 * Creates a project service for a desktop profile.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @since 1.8
	 */
	@Autowired
	public ProjectDesktopService(ConfigurationService configurationService, SecurityDesktopService securityService) {
		super(ProjectDesktopService.class, configurationService, securityService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService#
	 * loadProject(java.nio.file.Path)
	 */
	@Override
	protected Project loadProject(Path path) {
		Project project = new Project(configurationService.getWorkspace().getProjects().getProject(path, null));

		project.setSecurityLevel(securityService.getSecurityLevel());
		project.addAllRights();

		return project;
	}

}
