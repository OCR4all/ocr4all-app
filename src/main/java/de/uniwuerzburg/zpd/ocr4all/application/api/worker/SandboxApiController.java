/**
 * File:     SandboxApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     09.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.BasicRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.HistoryResponse;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.MetsResponse;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.SandboxResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Sandbox;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.SandboxService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;

/**
 * Defines sandbox controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Controller
@RequestMapping(path = SandboxApiController.contextPath, produces = CoreApiController.applicationJson)
public class SandboxApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = apiContextPathVersion_1_0 + "/sandbox";

	/**
	 * The mets request mapping.
	 */
	public static final String metsRequestMapping = "/mets";

	/**
	 * The sandbox service.
	 */
	private final SandboxService service;

	/**
	 * Creates a sandbox controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param service              The sandbox service.
	 * @param projectService       The project service.
	 * @since 1.8
	 */
	public SandboxApiController(ConfigurationService configurationService, SecurityService securityService,
			SandboxService service, ProjectService projectService) {
		super(ProjectApiController.class, configurationService, securityService, projectService, service);

		this.service = service;
	}

	/**
	 * Returns the sandbox in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The sandbox id. This is the folder name.
	 * @return The sandbox in the response body.
	 * @since 1.8
	 */
	@GetMapping(entityRequestMapping + projectPathVariable)
	public ResponseEntity<SandboxResponse> entity(@PathVariable String projectId, @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId, id);
		try {
			return ResponseEntity.ok().body(new SandboxResponse(authorization.sandbox, true));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the list of sandboxes of given project sorted by name in the response
	 * body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @return The list of sandbox sorted by name in the response body.
	 * @since 1.8
	 */
	@GetMapping(listRequestMapping + projectPathVariable)
	public ResponseEntity<List<SandboxResponse>> list(@PathVariable String projectId) {
		Authorization authorization = authorizationFactory.authorize(projectId);
		try {
			List<SandboxResponse> sandboxes = new ArrayList<>();
			for (Sandbox sandbox : service.getSandboxes(authorization.project))
				sandboxes.add(new SandboxResponse(sandbox));

			return ResponseEntity.ok().body(sandboxes);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Creates a sandbox and returns it in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The sandbox id. This is the folder name.
	 * @return The created sandbox in the response body.
	 * @since 1.8
	 */
	@GetMapping(createRequestMapping + projectPathVariable)
	public ResponseEntity<SandboxResponse> create(@PathVariable String projectId, @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId, ProjectRight.special);
		try {
			if (authorization.project.getConfiguration().getSandboxesConfiguration().isAvailable(id)) {
				Sandbox sandbox = service.authorize(authorization.project, id);

				return sandbox == null ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
						: ResponseEntity.status(HttpStatus.CONFLICT).body(new SandboxResponse(sandbox, true));
			}

			Path path = authorization.project.getConfiguration().getSandboxesConfiguration().create(id, getUser());
			if (path == null)
				return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();

			Sandbox sandbox = service.authorize(authorization.project, id);
			return sandbox == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
					: ResponseEntity.ok().body(new SandboxResponse(sandbox));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates a sandbox and returns it in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param request   The sandbox request.
	 * @return The updated sandbox in the response body.
	 * @since 1.8
	 */
	@PostMapping(updateRequestMapping + projectPathVariable)
	public ResponseEntity<SandboxResponse> update(@PathVariable String projectId,
			@RequestBody @Valid SandboxRequest request) {
		if (request.getName() == null || request.getName().isBlank())
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

		de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Sandbox.State state = null;
		try {
			state = de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Sandbox.State
					.valueOf(request.getState());
		} catch (Exception e) {
			// unknown form state, ignore it
		}

		Authorization authorization = authorizationFactory.authorize(projectId, request.getId(), ProjectRight.special);
		try {
			if (authorization.sandbox.getConfiguration().getConfiguration().updateBasicData(request.getName(),
					request.getDescription(), request.getKeywords(), state)) {
				Sandbox updatedSandbox = service.authorize(authorization.project, request.getId());

				return updatedSandbox == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
						: ResponseEntity.ok().body(new SandboxResponse(updatedSandbox, true));
			} else
				return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the sandbox history in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The sandbox id. This is the folder name.
	 * @return The sandbox history in the response body.
	 * @since 1.8
	 */
	@GetMapping(historyInformationRequestMapping + projectPathVariable)
	public ResponseEntity<HistoryResponse> historyInformation(@PathVariable String projectId, @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId, id, ProjectRight.execute);
		try {
			return ResponseEntity.ok().body(new HistoryResponse(authorization.sandbox.getHistory()));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Downloads the sandbox history.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The sandbox id. This is the folder name.
	 * @param response  The HTTP-specific functionality in sending a response to the
	 *                  client.
	 * @throws IOException Signals that an I/O exception of some sort has occurred.
	 * @since 1.8
	 */
	@GetMapping(value = historyDownloadRequestMapping
			+ projectPathVariable, produces = CoreApiController.applicationZip)
	public void historyDownload(@PathVariable String projectId, @RequestParam String id, HttpServletResponse response)
			throws IOException {
		Authorization authorization = authorizationFactory.authorize(projectId, id, ProjectRight.execute);
		try {
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + id + ".zip\"");

			authorization.sandbox.zipHistory(response.getOutputStream());
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
		}
	}

	/**
	 * Resets the sandbox and returns it in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The sandbox id. This is the folder name.
	 * @return The reseted sandbox in the response body.
	 * @since 1.8
	 */
	@GetMapping(resetRequestMapping + projectPathVariable)
	public ResponseEntity<SandboxResponse> reset(@PathVariable String projectId, @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId, id, ProjectRight.special);
		try {
			if (authorization.sandbox.reset()) {
				Sandbox sandbox = service.authorize(authorization.project, id);

				return sandbox == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
						: ResponseEntity.ok().body(new SandboxResponse(sandbox, false));
			} else
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Removes the sandbox and returns it in the response body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The sandbox id. This is the folder name.
	 * @return The removed sandbox in the response body.
	 * @since 1.8
	 */
	@GetMapping(removeRequestMapping + projectPathVariable)
	public ResponseEntity<SandboxResponse> remove(@PathVariable String projectId, @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId, id, ProjectRight.special);
		try {
			return authorization.project.getConfiguration().getSandboxesConfiguration()
					.remove(authorization.sandbox.getConfiguration().getFolder(), getUser())
							? ResponseEntity.ok().body(new SandboxResponse(authorization.sandbox))
							: ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the information contained in the mets (Metadata Encoding and
	 * Transmission Standard) XML file in the specified sandbox in the response
	 * body.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param id        The sandbox id. This is the folder name.
	 * @return The mets (Metadata Encoding and Transmission Standard) information in
	 *         the response body.
	 * @since 1.8
	 */
	@GetMapping(metsRequestMapping + projectPathVariable)
	public ResponseEntity<MetsResponse> mets(@PathVariable String projectId, @RequestParam String id) {
		Authorization authorization = authorizationFactory.authorize(projectId, id);
		try {
			return ResponseEntity.ok().body(new MetsResponse(authorization.sandbox));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
		}
	}

	/**
	 * Defines sandbox requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class SandboxRequest extends BasicRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

	}

}
