/**
 * File:     CoreApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     02.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow.Workflow;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow.WorkflowService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;

/**
 * Defines core controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class CoreApiController {
	/**
	 * The api context path.
	 */
	public static final String apiContextPath = "/api";

	/**
	 * The api version 1.0 prefix path.
	 */
	public static final String apiContextPathVersion_1_0 = apiContextPath + "/v1.0";

	/**
	 * The application type json.
	 */
	public static final String applicationJson = "application/json";

	/**
	 * The application type zip.
	 */
	public static final String applicationZip = "application/zip";

	/**
	 * The application type text.
	 */
	public static final String applicationText = "application/text";

	/**
	 * The entity request mapping.
	 */
	public static final String entityRequestMapping = "/entity";

	/**
	 * The list request mapping.
	 */
	public static final String listRequestMapping = "/list";

	/**
	 * The information request mapping.
	 */
	public static final String informationRequestMapping = "/information";

	/**
	 * The overview request mapping.
	 */
	public static final String overviewRequestMapping = "/overview";

	/**
	 * The create request mapping.
	 */
	public static final String createRequestMapping = "/create";

	/**
	 * The update request mapping.
	 */
	public static final String updateRequestMapping = "/update";

	/**
	 * The download request mapping.
	 */
	public static final String downloadRequestMapping = "/download";

	/**
	 * The upload request mapping.
	 */
	public static final String uploadRequestMapping = "/upload";

	/**
	 * The history request mapping.
	 */
	public static final String historyRequestMapping = "/history";

	/**
	 * The history information request mapping.
	 */
	public static final String historyInformationRequestMapping = historyRequestMapping + informationRequestMapping;

	/**
	 * The history download request mapping.
	 */
	public static final String historyDownloadRequestMapping = historyRequestMapping + downloadRequestMapping;

	/**
	 * The image request mapping.
	 */
	public static final String imageRequestMapping = "/image";

	/**
	 * The file request mapping.
	 */
	public static final String fileRequestMapping = "/file";

	/**
	 * The initialize request mapping.
	 */
	public static final String initializeRequestMapping = "/initialize";

	/**
	 * The reset request mapping.
	 */
	public static final String resetRequestMapping = "/reset";

	/**
	 * The remove request mapping.
	 */
	public static final String removeRequestMapping = "/remove";

	/**
	 * The cancel request mapping.
	 */
	public static final String cancelRequestMapping = "/cancel";

	/**
	 * The action request mapping.
	 */
	public static final String actionRequestMapping = "/action";

	/**
	 * The security request mapping.
	 */
	public static final String securityRequestMapping = "/security";

	/**
	 * The project id path variable.
	 */
	public static final String projectPathVariable = "/{projectId}";

	/**
	 * The workflow id path variable.
	 */
	public static final String workflowPathVariable = "/{workflowId}";

	/**
	 * The action path variable.
	 */
	public static final String actionPathVariable = "/{action}";

	/**
	 * The type path variable.
	 */
	public static final String typePathVariable = "/{type}";

	/**
	 * Defines project rights.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	protected enum ProjectRight {
		none, any, read, write, execute, special
	}

	/**
	 * The logger.
	 */
	protected final org.slf4j.Logger logger;

	/**
	 * The configuration service.
	 */
	protected final ConfigurationService configurationService;

	/**
	 * The security service.
	 */
	protected final SecurityService securityService;

	/**
	 * The authorization factory.
	 */
	protected final AuthorizationFactory authorizationFactory;

	/**
	 * Creates a core controller for the api.
	 * 
	 * @param logger               The logger class.
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @since 1.8
	 */
	protected CoreApiController(Class<?> logger, ConfigurationService configurationService,
			SecurityService securityService) {
		this(logger, configurationService, securityService, null);
	}

	/**
	 * Creates a core controller for the api.
	 * 
	 * @param logger               The logger class.
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param projectService       The project service.
	 * @since 1.8
	 */
	protected CoreApiController(Class<?> logger, ConfigurationService configurationService,
			SecurityService securityService, ProjectService projectService) {
		this(logger, configurationService, securityService, projectService, null);
	}

	/**
	 * Creates a core controller for the api.
	 * 
	 * @param logger               The logger class.
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param projectService       The project service.
	 * @param workflowService      The workflow service.
	 * @since 1.8
	 */
	protected CoreApiController(Class<?> logger, ConfigurationService configurationService,
			SecurityService securityService, ProjectService projectService, WorkflowService workflowService) {
		super();

		this.logger = org.slf4j.LoggerFactory.getLogger(logger);
		this.configurationService = configurationService;
		this.securityService = securityService;

		authorizationFactory = projectService == null ? null
				: new AuthorizationFactory(projectService, workflowService);
	}

	/**
	 * Logs the exception.
	 * 
	 * @param exception The exception to log.
	 * @since 1.8
	 */
	protected void log(Exception exception) {
		logger.error("throws exception " + exception.getClass().getName(), exception);
	}

	/**
	 * Returns the session user.
	 *
	 * @return The user. Null if not set, this means, it is either running in the
	 *         desktop profile or no user is logged in.
	 * @since 1.8
	 */
	protected String getUser() {
		return securityService.getUser();
	}

	/**
	 * Returns true if the application is secured.
	 * 
	 * @return True if the application is secured.
	 * @since 1.8
	 */
	protected boolean isSecured() {
		return securityService.isSecured();
	}

	/**
	 * Returns true if the administrator security permission is achievable by the
	 * session user.
	 *
	 * @return True if the administrator security permission is achievable.
	 * @since 1.8
	 */
	protected boolean isAdministrator() {
		return securityService.isAdministrator();
	}

	/**
	 * Returns true if the coordinator security permission is achievable by the
	 * session user.
	 *
	 * @return True if the coordinator security permission is achievable.
	 * @since 1.8
	 */
	protected boolean isCoordinator() {
		return securityService.isCoordinator();
	}

	/**
	 * Returns true if the user security permission is achievable by the session
	 * user.
	 *
	 * @return True if the user security permission is achievable.
	 * @since 1.8
	 */
	protected boolean isUser() {
		return securityService.isUser();
	}

	/**
	 * Returns the image media type.
	 * 
	 * @param format The image format.
	 * @return The image media type.
	 * @since 1.8
	 */
	protected static MediaType getImageMediaType(String format) {
		format = format.trim().toLowerCase();

		switch (format) {
		case "gif":
			return MediaType.IMAGE_GIF;

		case "jpg":
			return MediaType.IMAGE_JPEG;

		case "png":
		default:
			return MediaType.IMAGE_PNG;
		}
	}

	/**
	 * Returns the image media type value.
	 * 
	 * @param format The image format.
	 * @return The image media type value.
	 * @since 1.8
	 */
	protected static String getImageMediaTypeValue(String format) {
		format = format.trim().toLowerCase();

		switch (format) {
		case "gif":
			return MediaType.IMAGE_GIF_VALUE;

		case "jpg":
			return MediaType.IMAGE_JPEG_VALUE;

		case "png":
		default:
			return MediaType.IMAGE_PNG_VALUE;
		}
	}

	/**
	 * Returns the image with given id.
	 * 
	 * @param folder   The image derivative folder.
	 * @param id       The id.
	 * @param format   The format.
	 * @param response The HTTP response.
	 * @throws ResponseStatusException Throws if the image does not exists with http
	 *                                 status not found (404).
	 * @since 1.8
	 */
	protected static void getImage(Path folder, int id, String format, HttpServletResponse response)
			throws ResponseStatusException {
		try {
			byte[] image = Files.readAllBytes(Paths.get(folder.toString(), id + "." + format));

			response.setContentType(getImageMediaTypeValue(format));
			response.getOutputStream().write(image, 0, image.length);

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Authorization is an immutable class that authorizes actions.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	protected class Authorization {
		/**
		 * The project.
		 */
		public final Project project;

		/**
		 * The workflow.
		 */
		public final Workflow workflow;

		/**
		 * Creates an authorization for project actions.
		 * 
		 * @param projectId      The project id. This is the folder name.
		 * @param projectService The project service.
		 * @throws ResponseStatusException Throws on authorization troubles.
		 * @since 1.8
		 */
		private Authorization(String projectId, ProjectService projectService) throws ResponseStatusException {
			this(projectId, ProjectRight.any, projectService);
		}

		/**
		 * Creates an authorization for project actions.
		 * 
		 * @param projectId      The project id. This is the folder name.
		 * @param projectRight   The right required on the project. If null, no right is
		 *                       required.
		 * @param projectService The project service.
		 * @throws ResponseStatusException Throws on authorization troubles.
		 * @since 1.8
		 */
		private Authorization(String projectId, ProjectRight projectRight, ProjectService projectService)
				throws ResponseStatusException {
			this(projectId, null, projectRight, projectService, null);
		}

		/**
		 * Creates an authorization for project and workflow actions.
		 * 
		 * @param projectId       The project id. This is the folder name.
		 * @param workflowId      The workflow id. This is the folder name.
		 * @param projectService  The project service.
		 * @param workflowService The workflow service. Null if only project
		 *                        authorizations is available.
		 * @throws ResponseStatusException Throws on authorization troubles.
		 * @since 1.8
		 */
		private Authorization(String projectId, String workflowId, ProjectService projectService,
				WorkflowService workflowService) throws ResponseStatusException {
			this(projectId, workflowId, ProjectRight.any, projectService, workflowService);
		}

		/**
		 * Creates an authorization for project and workflow actions.
		 * 
		 * @param projectId       The project id. This is the folder name.
		 * @param workflowId      The workflow id. This is the folder name.
		 * @param projectRight    The right required on the project. If null, no right
		 *                        is required.
		 * @param projectService  The project service.
		 * @param workflowService The workflow service. Null if only project
		 *                        authorizations is available.
		 * @throws ResponseStatusException Throws on authorization troubles.
		 * @since 1.8
		 */
		private Authorization(String projectId, String workflowId, ProjectRight projectRight,
				ProjectService projectService, WorkflowService workflowService) throws ResponseStatusException {
			super();
			if (projectRight == null)
				projectRight = ProjectRight.none;

			Project project = null;
			Workflow workflow = null;

			if (projectId == null || projectId.isBlank()
					|| (workflowService != null && (workflowId == null || workflowId.isBlank())))
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			else
				try {
					if (!projectService.isAvailable(projectId))
						throw new ResponseStatusException(HttpStatus.NO_CONTENT);
					else {
						project = ProjectRight.none.equals(projectRight) ? projectService.getProject(projectId)
								: projectService.authorize(projectId);
						if (project == null) {
							if (ProjectRight.none.equals(projectRight))
								throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
							else
								throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
						} else if ((ProjectRight.special.equals(projectRight) && !project.isSpecial())
								|| (ProjectRight.execute.equals(projectRight) && !project.isExecute())
								|| (ProjectRight.write.equals(projectRight) && !project.isWrite())
								|| (ProjectRight.read.equals(projectRight) && !project.isRead()))
							throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
						else if (workflowService != null) {
							if (!workflowService.isAvailable(project, workflowId))
								throw new ResponseStatusException(HttpStatus.NO_CONTENT);
							else {
								workflow = workflowService.authorize(project, workflowId);
								if (workflow == null)
									throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
							}
						}
					}
				} catch (ResponseStatusException ex) {
					throw ex;
				} catch (Exception ex) {
					log(ex);

					throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
				}

			this.project = project;
			this.workflow = workflow;
		}

	}

	/**
	 * Defines factories for authorizations.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	protected class AuthorizationFactory {
		/**
		 * The project service.
		 */
		private final ProjectService projectService;

		/**
		 * The workflow service.
		 */
		private final WorkflowService workflowService;

		/**
		 * Creates a factory for authorizations. Only project authorizations is
		 * available.
		 * 
		 * @param projectService The project service.
		 * @throws IllegalArgumentException The project service is mandatory and can not
		 *                                  be null.
		 * @since 1.8
		 */
		public AuthorizationFactory(ProjectService projectService) throws IllegalArgumentException {
			this(projectService, null);
		}

		/**
		 * Creates a factory for authorizations.
		 * 
		 * @param projectService  The project service.
		 * @param workflowService The workflow service. Null if only project
		 *                        authorizations is available.
		 * @throws IllegalArgumentException The project service is mandatory and can not
		 *                                  be null.
		 * @since 1.8
		 */
		public AuthorizationFactory(ProjectService projectService, WorkflowService workflowService)
				throws IllegalArgumentException {
			super();

			if (projectService == null)
				throw new IllegalArgumentException("AuthorizationFactory: the project service is mandatory.");

			this.projectService = projectService;
			this.workflowService = workflowService;
		}

		/**
		 * Returns true if the workflow service is available.
		 * 
		 * @return True if the workflow service is available.
		 * @since 1.8
		 */
		public boolean isWorkflowService() {
			return workflowService != null;
		}

		/**
		 * Authorizes for project actions.
		 * 
		 * @param projectId The project id. This is the folder name.
		 * @return The authorization.
		 * @throws ResponseStatusException Throws on authorization troubles.
		 * @since 1.8
		 */
		public Authorization authorize(String projectId) throws ResponseStatusException {
			return new Authorization(projectId, projectService);
		}

		/**
		 * Authorizes for project actions.
		 * 
		 * @param projectId    The project id. This is the folder name.
		 * @param projectRight The right required on the project. If null, no right is
		 *                     required.
		 * @return The authorization.
		 * @throws ResponseStatusException Throws on authorization troubles.
		 * @since 1.8
		 */
		public Authorization authorize(String projectId, ProjectRight projectRight) throws ResponseStatusException {
			return new Authorization(projectId, projectRight, projectService);

		}

		/**
		 * Authorizes for project and workflow actions.
		 * 
		 * @param projectId  The project id. This is the folder name.
		 * @param workflowId The workflow id. This is the folder name.
		 * @return The authorization. Null if no workflow service is available.
		 * @throws ResponseStatusException Throws on authorization troubles.
		 * @since 1.8
		 */
		public Authorization authorize(String projectId, String workflowId) throws ResponseStatusException {
			return isWorkflowService() ? new Authorization(projectId, workflowId, projectService, workflowService)
					: null;
		}

		/**
		 * Authorizes for project and workflow actions.
		 * 
		 * @param projectId    The project id. This is the folder name.
		 * @param workflowId   The workflow id. This is the folder name.
		 * @param projectRight The right required on the project. If null, no right is
		 *                     required.
		 * @return The authorization. Null if no workflow service is available.
		 * @throws ResponseStatusException Throws on authorization troubles.
		 * @since 1.8
		 */
		public Authorization authorize(String projectId, String workflowId, ProjectRight projectRight)
				throws ResponseStatusException {
			return isWorkflowService()
					? new Authorization(projectId, workflowId, projectRight, projectService, workflowService)
					: null;
		}

		/**
		 * Authorizes for actions on snapshots.
		 * 
		 * @param projectId  The project id. This is the folder name.
		 * @param workflowId The workflow id. This is the folder name.
		 * @return The authorization. Null if no workflow service is available.
		 * @throws ResponseStatusException Throws on authorization troubles.
		 * @since 1.8
		 */
		public Authorization authorizeSnapshot(String projectId, String workflowId) throws ResponseStatusException {
			return authorizeSnapshot(projectId, workflowId, ProjectRight.any);
		}

		/**
		 * Authorizes for actions on snapshots.
		 * 
		 * @param projectId    The project id. This is the folder name.
		 * @param workflowId   The workflow id. This is the folder name.
		 * @param projectRight The right required on the project. If null, no right is
		 *                     required.
		 * @return The authorization. Null if no workflow service is available.
		 * @throws ResponseStatusException Throws on authorization troubles.
		 * @since 1.8
		 */
		public Authorization authorizeSnapshot(String projectId, String workflowId, ProjectRight projectRight)
				throws ResponseStatusException {
			Authorization authorization = authorize(projectId, workflowId, projectRight);

			if (!authorization.workflow.isSnapshotAccess())
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

			return authorization;
		}

	}

}
