/**
 * File:     ProjectSecurityApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     01.03.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.ProjectSecurity;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project.ProjectConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;

/**
 * Defines project security controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api & server")
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = ProjectSecurityApiController.contextPath, produces = CoreApiController.applicationJson)
public class ProjectSecurityApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = ProjectApiController.contextPath + securityRequestMapping;

	/**
	 * The project service.
	 */
	private final ProjectService service;

	/**
	 * Creates a project security controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param service              The project service.
	 * @since 1.8
	 */
	public ProjectSecurityApiController(ConfigurationService configurationService, SecurityService securityService,
			ProjectService service) {
		super(ProjectSecurityApiController.class, configurationService, securityService, service);

		this.service = service;
	}

	/**
	 * Returns the project security in the response body.
	 * 
	 * @param id The project id. This is the folder name.
	 * @return The project security in the response body.
	 * @since 1.8
	 */
	@GetMapping(informationRequestMapping)
	public ResponseEntity<ProjectSecurity> information(@RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(id, ProjectRight.none);
		try {
			return ResponseEntity.ok().body(new ProjectSecurity(authorization.project));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Given an api project security grants, returns the respective project
	 * configuration grants.
	 * 
	 * @param apiGrants The api project security grants.
	 * @return The project configuration grants.
	 * @since 1.8
	 */
	private Set<ProjectConfiguration.Grant> getGrants(Set<ProjectSecurity.Grant> apiGrants) {
		Set<ProjectConfiguration.Grant> grants = new HashSet<>();
		if (apiGrants != null)
			for (ProjectSecurity.Grant grant : apiGrants)
				if (grant != null && (grant.isRead() || grant.isWrite() || grant.isExecute() || grant.isSpecial())
						&& grant.getTargets() != null && !grant.getTargets().isEmpty())
					grants.add(new ProjectConfiguration.Grant(grant.isRead(), grant.isWrite(), grant.isExecute(),
							grant.isSpecial(), grant.getTargets()));

		return grants;
	}

	/**
	 * Updates a project security and returns it in the response body.
	 * 
	 * @param request The basic request with folder.
	 * @return The updated project security in the response body.
	 * @since 1.8
	 */
	@PostMapping(updateRequestMapping)
	public ResponseEntity<ProjectSecurity> update(@RequestBody ProjectSecurity request) {
		Authorization authorization = authorizationFactory.authorize(request.getId(), ProjectRight.none);
		try {
			if (authorization.project.getConfiguration().getConfiguration().updateSecurity(
					getGrants(request.getUsers()), getGrants(request.getGroups()),
					request.getOther() == null || !(request.getOther().isRead() || request.getOther().isWrite()
							|| request.getOther().isExecute() || request.getOther().isSpecial())
									? null
									: new ProjectConfiguration.Right(request.getOther().isRead(),
											request.getOther().isWrite(), request.getOther().isExecute(),
											request.getOther().isSpecial()))) {
				Project project = service.getProject(request.getId());

				return project == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
						: ResponseEntity.ok().body(new ProjectSecurity(project));
			} else
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
}
