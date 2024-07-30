/**
 * File:     CoreApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 *
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     02.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.ModelService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Sandbox;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.SandboxService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.folio.Folio;
import jakarta.servlet.http.HttpServletResponse;

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
	 * The tree request mapping.
	 */
	public static final String treeRequestMapping = "/tree";

	/**
	 * The available request mapping.
	 */
	public static final String availableRequestMapping = "/available";

	/**
	 * The summary request mapping.
	 */
	public static final String summaryRequestMapping = "/summary";

	/**
	 * The information request mapping.
	 */
	public static final String informationRequestMapping = "/information";

	/**
	 * The environment request mapping.
	 */
	public static final String environmentRequestMapping = "/environment";

	/**
	 * The domain request mapping.
	 */
	public static final String domainRequestMapping = "/domain";

	/**
	 * The configuration request mapping.
	 */
	public static final String configurationRequestMapping = "/configuration";

	/**
	 * The overview request mapping.
	 */
	public static final String overviewRequestMapping = "/overview";

	/**
	 * The communication request mapping.
	 */
	public static final String communicationRequestMapping = "/communication";

	/**
	 * The provider request mapping.
	 */
	public static final String providerRequestMapping = "/provider";

	/**
	 * The create request mapping.
	 */
	public static final String createRequestMapping = "/create";

	/**
	 * The update request mapping.
	 */
	public static final String updateRequestMapping = "/update";

	/**
	 * The schedule request mapping.
	 */
	public static final String scheduleRequestMapping = "/schedule";

	/**
	 * The push request mapping.
	 */
	public static final String pushRequestMapping = "/push";

	/**
	 * The pull request mapping.
	 */
	public static final String pullRequestMapping = "/pull";

	/**
	 * The lock request mapping.
	 */
	public static final String lockRequestMapping = "/lock";

	/**
	 * The unlock request mapping.
	 */
	public static final String unlockRequestMapping = "/unlock";

	/**
	 * The secure request mapping.
	 */
	public static final String secureRequestMapping = "/secure";

	/**
	 * The unsecure request mapping.
	 */
	public static final String unsecureRequestMapping = "/unsecure";

	/**
	 * The download request mapping.
	 */
	public static final String downloadRequestMapping = "/download";

	/**
	 * The zip request mapping.
	 */
	public static final String zipRequestMapping = "/zip";

	/**
	 * The upload request mapping.
	 */
	public static final String uploadRequestMapping = "/upload";

	/**
	 * The export request mapping.
	 */
	public static final String exportRequestMapping = "/export";

	/**
	 * The import request mapping.
	 */
	public static final String importRequestMapping = "/import";

	/**
	 * The sort request mapping.
	 */
	public static final String sortRequestMapping = "/sort";

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
	 * The normalized image request mapping.
	 */
	public static final String normalizedRequestMapping = "/normalized";

	/**
	 * The derivative request mapping.
	 */
	public static final String derivativeRequestMapping = "/derivative";

	/**
	 * The thumbnail image derivative request mapping.
	 */
	public static final String derivativeThumbnailRequestMapping = derivativeRequestMapping + "/thumbnail";

	/**
	 * The detail image derivative request mapping.
	 */
	public static final String derivativeDetailRequestMapping = derivativeRequestMapping + "/detail";

	/**
	 * The best image derivative request mapping.
	 */
	public static final String derivativeBestRequestMapping = derivativeRequestMapping + "/best";

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
	 * The folio request mapping.
	 */
	public static final String folioRequestMapping = "/folio";

	/**
	 * The collection request mapping.
	 */
	public static final String collectionRequestMapping = "/collection";

	/**
	 * The exchange request mapping.
	 */
	public static final String exchangeRequestMapping = "/exchange";

	/**
	 * The partition request mapping.
	 */
	public static final String partitionRequestMapping = "/partition";

	/**
	 * The model request mapping.
	 */
	public static final String modelRequestMapping = "/model";

	/**
	 * The set request mapping.
	 */
	public static final String setRequestMapping = "/set";

	/**
	 * The codec request mapping.
	 */
	public static final String pageXMLRequestMapping = "/pageXML";

	/**
	 * The codec request mapping.
	 */
	public static final String codecRequestMapping = "/codec";

	/**
	 * The all request mapping.
	 */
	public static final String allRequestMapping = "/all";

	/**
	 * The spi id path variable.
	 */
	public static final String spiPathVariable = "/{spiId}";

	/**
	 * The id path variable.
	 */
	public static final String idPathVariable = "/{id}";

	/**
	 * The project id path variable.
	 */
	public static final String projectPathVariable = "/{projectId}";

	/**
	 * The sandbox id path variable.
	 */
	public static final String sandboxPathVariable = "/{sandboxId}";

	/**
	 * The workflow id path variable.
	 */
	public static final String workflowPathVariable = "/{workflowId}";

	/**
	 * The container id path variable.
	 */
	public static final String containerPathVariable = "/{containerId}";

	/**
	 * The collection id path variable.
	 */
	public static final String collectionPathVariable = "/{collectionId}";

	/**
	 * The model id path variable.
	 */
	public static final String modelPathVariable = "/{modelId}";

	/**
	 * The action path variable.
	 */
	public static final String actionPathVariable = "/{action}";

	/**
	 * The type path variable.
	 */
	public static final String typePathVariable = "/{type}";

	/**
	 * The name of the file containing name mappings in a tab-separated values
	 * format.
	 */
	public static final String filenameMappingTSV = "filename-mapping.tsv";

	/**
	 * Defines project rights.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	protected enum ProjectRight {
		/**
		 * The none project right.
		 */
		none,
		/**
		 * The any project right.
		 */
		any,
		/**
		 * The read project right.
		 */
		read,
		/**
		 * The write project right.
		 */
		write,
		/**
		 * The 'execute' project right
		 */
		execute,
		/**
		 * The 'special' project right
		 */
		special
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
	 * The collection service.
	 */
	protected final CollectionService collectionService;

	/**
	 * The model service.
	 */
	protected final ModelService modelService;

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
	 * @param collectionService    The collection service.
	 * @param modelService         The model service.
	 * @since 1.8
	 */
	protected CoreApiController(Class<?> logger, ConfigurationService configurationService,
			SecurityService securityService, CollectionService collectionService, ModelService modelService) {
		this(logger, configurationService, securityService, collectionService, modelService, null);
	}

	/**
	 * Creates a core controller for the api.
	 *
	 * @param logger               The logger class.
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param collectionService    The collection service.
	 * @param modelService         The model service.
	 * @param projectService       The project service.
	 * @since 1.8
	 */
	protected CoreApiController(Class<?> logger, ConfigurationService configurationService,
			SecurityService securityService, CollectionService collectionService, ModelService modelService,
			ProjectService projectService) {
		this(logger, configurationService, securityService, collectionService, modelService, projectService, null);
	}

	/**
	 * Creates a core controller for the api.
	 *
	 * @param logger               The logger class.
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param collectionService    The collection service.
	 * @param modelService         The model service.
	 * @param projectService       The project service.
	 * @param sandboxService       The sandbox service.
	 * @since 1.8
	 */
	protected CoreApiController(Class<?> logger, ConfigurationService configurationService,
			SecurityService securityService, CollectionService collectionService, ModelService modelService,
			ProjectService projectService, SandboxService sandboxService) {
		super();

		this.logger = org.slf4j.LoggerFactory.getLogger(logger);
		this.configurationService = configurationService;
		this.securityService = securityService;
		this.collectionService = collectionService;
		this.modelService = modelService;

		authorizationFactory = projectService == null ? null : new AuthorizationFactory(projectService, sandboxService);
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
	 * Returns the application preferred locale.
	 *
	 * @return The application preferred locale.
	 * @since 1.8
	 */
	protected Locale getLocale() {
		return getLocale(null);
	}

	/**
	 * Returns the locale.
	 *
	 * @param language The desired language for the locale.
	 * @return The locale. If the given language is not defined or not supported,
	 *         then returns the application preferred locale.
	 * @since 1.8
	 */
	protected Locale getLocale(String language) {
		Locale locale = null;
		if (language != null && !language.isBlank()) {
			language = language.trim().toLowerCase();

			for (String view : configurationService.getApplication().getViewLanguages())
				if (view.equals(language)) {
					locale = new Locale(language);

					break;
				}
		}

		return locale != null ? locale
				: (configurationService.getApplication().getViewLanguages().isEmpty()
						? configurationService.getApplication().getLocale()
						: new Locale(configurationService.getApplication().getViewLanguages().get(0)));
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
	 * Authorizes the session user for read security operations on a data
	 * collection.
	 * 
	 * @param id The collection id.
	 * @return The authorized collection.
	 * @throws ResponseStatusException Throw with http status:
	 *                                 <ul>
	 *                                 <li>400 (Bad Request): if the collection is
	 *                                 not available.</li>
	 *                                 <li>401 (Unauthorized): if the read security
	 *                                 permission is not achievable by the session
	 *                                 user.</li>
	 *                                 </ul>
	 * @since 17
	 */
	protected CollectionService.Collection authorizeCollectionRead(String id) throws ResponseStatusException {
		CollectionService.Collection collection = collectionService.getCollection(id);

		if (collection == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		else if (!collection.getRight().isReadFulfilled())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		else
			return collection;
	}

	/**
	 * Authorizes the session user for write security operations on a data
	 * collection.
	 * 
	 * @param id The collection id.
	 * @return The authorized collection.
	 * @throws ResponseStatusException Throw with http status:
	 *                                 <ul>
	 *                                 <li>400 (Bad Request): if the collection is
	 *                                 not available.</li>
	 *                                 <li>401 (Unauthorized): if the write security
	 *                                 permission is not achievable by the session
	 *                                 user.</li>
	 *                                 </ul>
	 * @since 17
	 */
	protected CollectionService.Collection authorizeCollectionWrite(String id) throws ResponseStatusException {
		CollectionService.Collection collection = collectionService.getCollection(id);

		if (collection == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		else if (!collection.getRight().isWriteFulfilled())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		else
			return collection;
	}

	/**
	 * Authorizes the session user for read security operations on an assemble
	 * model.
	 * 
	 * @param id The model id.
	 * @return The authorized model.
	 * @throws ResponseStatusException Throw with http status:
	 *                                 <ul>
	 *                                 <li>400 (Bad Request): if the model is not
	 *                                 available.</li>
	 *                                 <li>401 (Unauthorized): if the read security
	 *                                 permission is not achievable by the session
	 *                                 user.</li>
	 *                                 </ul>
	 * @since 17
	 */
	protected ModelService.Model authorizeModelRead(String id) throws ResponseStatusException {
		ModelService.Model collection = modelService.getModel(id);

		if (collection == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		else if (!collection.getRight().isReadFulfilled())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		else
			return collection;
	}

	/**
	 * Authorizes the session user for write security operations on an assemble
	 * model.
	 * 
	 * @param id The model id.
	 * @return The authorized model.
	 * @throws ResponseStatusException Throw with http status:
	 *                                 <ul>
	 *                                 <li>400 (Bad Request): if the model is not
	 *                                 available.</li>
	 *                                 <li>401 (Unauthorized): if the write security
	 *                                 permission is not achievable by the session
	 *                                 user.</li>
	 *                                 </ul>
	 * @since 17
	 */
	protected ModelService.Model authorizeModelWrite(String id) throws ResponseStatusException {
		ModelService.Model collection = modelService.getModel(id);

		if (collection == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		else if (!collection.getRight().isWriteFulfilled())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		else
			return collection;
	}

	/**
	 * Authorizes the session user for special security operations on an assemble
	 * model.
	 * 
	 * @param id The model id.
	 * @return The authorized model.
	 * @throws ResponseStatusException Throw with http status:
	 *                                 <ul>
	 *                                 <li>400 (Bad Request): if the model is not
	 *                                 available.</li>
	 *                                 <li>401 (Unauthorized): if the write security
	 *                                 permission is not achievable by the session
	 *                                 user.</li>
	 *                                 </ul>
	 * @since 17
	 */
	protected ModelService.Model authorizeModelSpecial(String id) throws ResponseStatusException {
		ModelService.Model collection = modelService.getModel(id);

		if (collection == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		else if (!collection.getRight().isSpecialFulfilled())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		else
			return collection;
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
	protected static void getImage(Path folder, String id, String format, HttpServletResponse response)
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
	 * Returns metadata to be compressed in a zipped file containing the file name
	 * mapping of the project folios in a tab-separated values format.
	 * 
	 * @param project The project.
	 * @return The metadata to be compressed in a zipped file.
	 * @throws IOException Signals that an I/O exception of some sort has occurred.
	 * @since 17
	 */
	protected static OCR4allUtils.ZipMetadata getZipMetadataFilenameMappingTSV(Project project) throws IOException {
		StringBuffer buffer = new StringBuffer();

		for (Folio folio : project.getFolios())
			buffer.append(folio.getId() + "\t" + folio.getName() + System.lineSeparator());

		return new OCR4allUtils.ZipMetadata(filenameMappingTSV,
				new ByteArrayInputStream(buffer.toString().getBytes(StandardCharsets.UTF_8)));
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
		 * The sandbox.
		 */
		public final Sandbox sandbox;

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
		 * Creates an authorization for project and sandbox actions.
		 *
		 * @param projectId      The project id. This is the folder name.
		 * @param sandboxId      The sandbox id. This is the folder name.
		 * @param projectService The project service.
		 * @param sandboxService The sandbox service. Null if only project
		 *                       authorizations is available.
		 * @throws ResponseStatusException Throws on authorization troubles.
		 * @since 1.8
		 */
		private Authorization(String projectId, String sandboxId, ProjectService projectService,
				SandboxService sandboxService) throws ResponseStatusException {
			this(projectId, sandboxId, ProjectRight.any, projectService, sandboxService);
		}

		/**
		 * Creates an authorization for project and sandbox actions.
		 *
		 * @param projectId      The project id. This is the folder name.
		 * @param sandboxId      The sandbox id. This is the folder name.
		 * @param projectRight   The right required on the project. If null, no right is
		 *                       required.
		 * @param projectService The project service.
		 * @param sandboxService The sandbox service. Null if only project
		 *                       authorizations is available.
		 * @throws ResponseStatusException Throws on authorization troubles.
		 * @since 1.8
		 */
		private Authorization(String projectId, String sandboxId, ProjectRight projectRight,
				ProjectService projectService, SandboxService sandboxService) throws ResponseStatusException {
			super();
			if (projectRight == null)
				projectRight = ProjectRight.none;

			Project project = null;
			Sandbox sandbox = null;

			if (projectId == null || projectId.isBlank()
					|| (sandboxService != null && (sandboxId == null || sandboxId.isBlank())))
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
						else if (sandboxService != null) {
							if (!sandboxService.isAvailable(project, sandboxId))
								throw new ResponseStatusException(HttpStatus.NO_CONTENT);
							else {
								sandbox = sandboxService.authorize(project, sandboxId);
								if (sandbox == null)
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
			this.sandbox = sandbox;
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
		 * The sandbox service.
		 */
		private final SandboxService sandboxService;

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
		 * @param projectService The project service.
		 * @param sandboxService The sandbox service. Null if only project
		 *                       authorizations is available.
		 * @throws IllegalArgumentException The project service is mandatory and can not
		 *                                  be null.
		 * @since 1.8
		 */
		public AuthorizationFactory(ProjectService projectService, SandboxService sandboxService)
				throws IllegalArgumentException {
			super();

			if (projectService == null)
				throw new IllegalArgumentException("AuthorizationFactory: the project service is mandatory.");

			this.projectService = projectService;
			this.sandboxService = sandboxService;
		}

		/**
		 * Returns true if the sandbox service is available.
		 *
		 * @return True if the sandbox service is available.
		 * @since 1.8
		 */
		public boolean isSandboxService() {
			return sandboxService != null;
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
		 * Authorizes for project and sandbox actions.
		 *
		 * @param projectId The project id. This is the folder name.
		 * @param sandboxId The sandbox id. This is the folder name.
		 * @return The authorization. Null if no sandbox service is available.
		 * @throws ResponseStatusException Throws on authorization troubles.
		 * @since 1.8
		 */
		public Authorization authorize(String projectId, String sandboxId) throws ResponseStatusException {
			return isSandboxService() ? new Authorization(projectId, sandboxId, projectService, sandboxService) : null;
		}

		/**
		 * Authorizes for project and sandbox actions.
		 *
		 * @param projectId    The project id. This is the folder name.
		 * @param sandboxId    The sandbox id. This is the folder name.
		 * @param projectRight The right required on the project. If null, no right is
		 *                     required.
		 * @return The authorization. Null if no sandbox service is available.
		 * @throws ResponseStatusException Throws on authorization troubles.
		 * @since 1.8
		 */
		public Authorization authorize(String projectId, String sandboxId, ProjectRight projectRight)
				throws ResponseStatusException {
			return isSandboxService()
					? new Authorization(projectId, sandboxId, projectRight, projectService, sandboxService)
					: null;
		}

		/**
		 * Authorizes for actions on snapshots.
		 *
		 * @param projectId The project id. This is the folder name.
		 * @param sandboxId The sandbox id. This is the folder name.
		 * @return The authorization. Null if no sandbox service is available.
		 * @throws ResponseStatusException Throws on authorization troubles.
		 * @since 1.8
		 */
		public Authorization authorizeSnapshot(String projectId, String sandboxId) throws ResponseStatusException {
			return authorizeSnapshot(projectId, sandboxId, ProjectRight.any);
		}

		/**
		 * Authorizes for actions on snapshots.
		 *
		 * @param projectId    The project id. This is the folder name.
		 * @param sandboxId    The sandbox id. This is the folder name.
		 * @param projectRight The right required on the project. If null, no right is
		 *                     required.
		 * @return The authorization. Null if no sandbox service is available.
		 * @throws ResponseStatusException Throws on authorization troubles.
		 * @since 1.8
		 */
		public Authorization authorizeSnapshot(String projectId, String sandboxId, ProjectRight projectRight)
				throws ResponseStatusException {
			Authorization authorization = authorize(projectId, sandboxId, projectRight);

			if (!authorization.sandbox.isSnapshotAccess())
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

			return authorization;
		}

	}

}
