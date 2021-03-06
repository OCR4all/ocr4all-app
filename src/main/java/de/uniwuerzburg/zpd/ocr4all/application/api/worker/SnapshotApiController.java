/**
 * File:     SnapshotApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     18.03.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.SnapshotRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.SnapshotResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.ProjectService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow.Snapshot;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow.WorkflowService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;

/**
 * Defines snapshot controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Controller
@RequestMapping(path = SnapshotApiController.contextPath, produces = CoreApiController.applicationJson)
public class SnapshotApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = apiContextPathVersion_1_0 + "/snapshot";

	/**
	 * The path request mapping.
	 */
	public static final String derivedRequestMapping = "/derived";

	/**
	 * The path request mapping.
	 */
	public static final String pathRequestMapping = "/path";

	/**
	 * The sandbox request mapping.
	 */
	public static final String sandboxRequestMapping = "/sandbox";

	/**
	 * Creates a snapshot controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param projectService       The project service.
	 * @param workflowService      The workflow service.
	 * @since 1.8
	 */
	@Autowired
	public SnapshotApiController(ConfigurationService configurationService, SecurityService securityService,
			ProjectService projectService, WorkflowService workflowService) {
		super(ProjectApiController.class, configurationService, securityService, projectService, workflowService);
	}

	/**
	 * Returns the leaf snapshot in the track of the request in the response body.
	 * If the track is empty, then returns the root snapshot.
	 * 
	 * @param projectId  The project id. This is the folder name.
	 * @param workflowId The workflow id. This is the folder name.
	 * @param request    The snapshot request.
	 * @return The snapshot in the response body.
	 * @since 1.8
	 */
	@PostMapping(entityRequestMapping + projectPathVariable + workflowPathVariable)
	public ResponseEntity<SnapshotResponse> entity(@PathVariable String projectId, @PathVariable String workflowId,
			@RequestBody @Valid SnapshotRequest request) {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, workflowId);
		try {
			return ResponseEntity.ok()
					.body(new SnapshotResponse(authorization.workflow.getSnapshot(request.getTrack())));
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the derived snapshots of the leaf snapshot in the track of the
	 * request in the response body.
	 * 
	 * @param projectId  The project id. This is the folder name.
	 * @param workflowId The workflow id. This is the folder name.
	 * @param request    The snapshot request.
	 * @return The snapshots in the response body.
	 * @since 1.8
	 */
	@PostMapping(derivedRequestMapping + projectPathVariable + workflowPathVariable)
	public ResponseEntity<List<SnapshotResponse>> derived(@PathVariable String projectId,
			@PathVariable String workflowId, @RequestBody @Valid SnapshotRequest request) {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, workflowId);
		try {
			List<Snapshot> snapshots = authorization.workflow.getDerived(request.getTrack());

			List<SnapshotResponse> response = new ArrayList<>();
			for (Snapshot snapshot : snapshots)
				response.add(new SnapshotResponse(snapshot));

			return ResponseEntity.ok().body(response);
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the snapshots in the track of the request in the response body.
	 * 
	 * @param projectId  The project id. This is the folder name.
	 * @param workflowId The workflow id. This is the folder name.
	 * @param request    The snapshot request.
	 * @return The snapshots in the response body.
	 * @since 1.8
	 */
	@PostMapping(pathRequestMapping + projectPathVariable + workflowPathVariable)
	public ResponseEntity<List<SnapshotResponse>> path(@PathVariable String projectId, @PathVariable String workflowId,
			@RequestBody  @Valid SnapshotRequest request) {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, workflowId);
		try {
			List<Snapshot> snapshots = authorization.workflow.getSnapshots(request.getTrack());

			List<SnapshotResponse> response = new ArrayList<>();
			for (Snapshot snapshot : snapshots)
				response.add(new SnapshotResponse(snapshot));

			return ResponseEntity.ok().body(response);

		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates the configuration of the leaf snapshot in the track of the request
	 * and returns it in the response body.
	 * 
	 * @param projectId  The project id. This is the folder name.
	 * @param workflowId The workflow id. This is the folder name.
	 * @param request    The snapshot configuration request.
	 * @return The updated snapshot in the response body.
	 * @since 1.8
	 */
	@PostMapping(updateRequestMapping + projectPathVariable + workflowPathVariable)
	public ResponseEntity<SnapshotResponse> update(@PathVariable String projectId, @PathVariable String workflowId,
			@RequestBody @Valid SnapshotConfigurationRequest request) {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, workflowId,
				ProjectRight.execute);
		try {
			Snapshot snapshot = authorization.workflow.getSnapshot(request.getTrack());

			return snapshot.getConfiguration().getConfiguration().updateMainConfiguration(request.getLabel(),
					request.getDescription()) ? ResponseEntity.ok().body(new SnapshotResponse(snapshot))
							: ResponseEntity.status(HttpStatus.CONFLICT).build();
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Removes the leaf snapshot in the track of the request and returns it in the
	 * response body.
	 * 
	 * @param projectId  The project id. This is the folder name.
	 * @param workflowId The workflow id. This is the folder name.
	 * @param request    The snapshot request.
	 * @return The removed snapshot in the response body.
	 * @since 1.8
	 */
	@PostMapping(removeRequestMapping + projectPathVariable + workflowPathVariable)
	public ResponseEntity<SnapshotResponse> remove(@PathVariable String projectId, @PathVariable String workflowId,
			@RequestBody  @Valid SnapshotRequest request) {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, workflowId,
				ProjectRight.execute);
		try {
			Snapshot snapshot = authorization.workflow.getSnapshot(request.getTrack());

			return (snapshot.getConfiguration().isRoot()
					? authorization.workflow.getConfiguration().getSnapshots().reset()
					: snapshot.getConfiguration().getParent()
							.removeDerived(request.getTrack().get(request.getTrack().size() - 1)))
									? ResponseEntity.ok().body(new SnapshotResponse(snapshot))
									: ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the files in the sandbox of the leaf snapshot in the track of the
	 * request in the response body. If the track is null or empty, then the sandbox
	 * belongs to the root snapshot.
	 * 
	 * @param projectId  The project id. This is the folder name.
	 * @param workflowId The workflow id. This is the folder name.
	 * @param request    The snapshot request.
	 * @return The files in the sandbox of the snapshot.
	 * @since 1.8
	 */
	@PostMapping(sandboxRequestMapping + fileRequestMapping + projectPathVariable + workflowPathVariable)
	public ResponseEntity<SandboxResponse> sandboxFile(@PathVariable String projectId, @PathVariable String workflowId,
			@RequestBody  @Valid SnapshotRequest request) {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, workflowId);
		try {
			return ResponseEntity.ok().body(new SandboxResponse(authorization.workflow.getSnapshot(request.getTrack())
					.getConfiguration().getSandbox().listAllFiles()));
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Downloads the file in the sandbox of the leaf snapshot in the track of the
	 * request.
	 * 
	 * @param projectId  The project id. This is the folder name.
	 * @param workflowId The workflow id. This is the folder name.
	 * @param request    The sandbox request.
	 * @param response   The HTTP-specific functionality in sending a response to
	 *                   the client.
	 * @throws IOException Signals that an I/O exception of some sort has occurred.
	 * @since 1.8
	 */
	@PostMapping(sandboxRequestMapping + downloadRequestMapping + projectPathVariable + workflowPathVariable)
	public void sandboxDownload(@PathVariable String projectId, @PathVariable String workflowId,
			@RequestBody @Valid SandboxRequest request, HttpServletResponse response) throws IOException {
		Authorization authorization = authorizationFactory.authorizeSnapshot(projectId, workflowId);
		try {
			Snapshot snapshot = authorization.workflow.getSnapshot(request.getTrack());

			Path path = Paths.get(snapshot.getConfiguration().getSandbox().getFolder().toString(), request.getFile())
					.normalize();
			if (!path.startsWith(snapshot.getConfiguration().getSandbox().getFolder()) || !Files.exists(path)
					|| Files.isDirectory(path))
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

			byte[] content = Files.readAllBytes(path);

			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName() + "\"");
			response.getOutputStream().write(content, 0, content.length);
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines snapshot configuration requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class SnapshotConfigurationRequest extends SnapshotRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The label.
		 */
		@NotBlank
		private String label;

		/**
		 * The description.
		 */
		private String description;

		/**
		 * Returns the label.
		 *
		 * @return The label.
		 * @since 1.8
		 */
		public String getLabel() {
			return label;
		}

		/**
		 * Set the label.
		 *
		 * @param label The label to set.
		 * @since 1.8
		 */
		public void setLabel(String label) {
			this.label = label;
		}

		/**
		 * Returns the description.
		 *
		 * @return The description.
		 * @since 1.8
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * Set the description.
		 *
		 * @param description The description to set.
		 * @since 1.8
		 */
		public void setDescription(String description) {
			this.description = description;
		}

	}

	/**
	 * Defines sandbox responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class SandboxResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The files.
		 */
		private List<String> files;

		/**
		 * Creates a sandbox responses for the api.
		 * 
		 * @param files The files.
		 * @since 1.8
		 */
		public SandboxResponse(List<String> files) {
			super();

			this.files = files;
		}

		/**
		 * Returns the files.
		 *
		 * @return The files.
		 * @since 1.8
		 */
		public List<String> getFiles() {
			return files;
		}

		/**
		 * Set the files.
		 *
		 * @param files The files to set.
		 * @since 1.8
		 */
		public void setFiles(List<String> files) {
			this.files = files;
		}

	}

	/**
	 * Defines sandbox requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class SandboxRequest extends SnapshotRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The file.
		 */
		@NotBlank
		private String file;

		/**
		 * Returns the file.
		 *
		 * @return The file.
		 * @since 1.8
		 */
		public String getFile() {
			return file;
		}

		/**
		 * Set the file.
		 *
		 * @param file The file to set.
		 * @since 1.8
		 */
		public void setFile(String file) {
			this.file = file;
		}

	}

}
