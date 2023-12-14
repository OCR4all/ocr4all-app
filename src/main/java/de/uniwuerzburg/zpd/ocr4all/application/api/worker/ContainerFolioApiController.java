/**
 * File:     ContainerFolioApiController.java
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * 
 * Author:   Herbert Baier
 * Date:     13.12.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.FolioResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.repository.ContainerService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.folio.Folio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Defines container folio repository controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Tag(name = "container folio", description = "the repository container folio API")
@RestController
@RequestMapping(path = ContainerFolioApiController.contextPath, produces = CoreApiController.applicationJson)
public class ContainerFolioApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = ContainerApiController.contextPath + folioRequestMapping;

	/**
	 * The container service.
	 */
	private final ContainerService service;

	/**
	 * Creates a container folio repository controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param service              The container service.
	 * @since 1.8
	 */
	public ContainerFolioApiController(ConfigurationService configurationService, SecurityService securityService,
			ContainerService service) {
		super(ContainerFolioApiController.class, configurationService, securityService);

		this.service = service;
	}

	/**
	 * Authorizes the session user for read security operations.
	 * 
	 * @param id The container id.
	 * @return The authorized container.
	 * @throws ResponseStatusException Throw with http status:
	 *                                 <ul>
	 *                                 <li>400 (Bad Request): if the container is
	 *                                 not available.</li>
	 *                                 <li>401 (Unauthorized): if the read security
	 *                                 permission is not achievable by the session
	 *                                 user.</li>
	 *                                 </ul>
	 * @since 1.8
	 */
	private ContainerService.Container authorizeRead(String id) throws ResponseStatusException {
		ContainerService.Container container = service.getContainer(id);

		if (container == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		else if (!container.getRight().isReadFulfilled())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		else
			return container;
	}

	/**
	 * Authorizes the session user for write security operations.
	 * 
	 * @param id The container id.
	 * @return The authorized container.
	 * @throws ResponseStatusException Throw with http status:
	 *                                 <ul>
	 *                                 <li>400 (Bad Request): if the container is
	 *                                 not available.</li>
	 *                                 <li>401 (Unauthorized): if the write security
	 *                                 permission is not achievable by the session
	 *                                 user.</li>
	 *                                 </ul>
	 * @since 1.8
	 */
	private ContainerService.Container authorizeWrite(String id) throws ResponseStatusException {
		ContainerService.Container container = service.getContainer(id);

		if (container == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		else if (!container.getRight().isWriteFulfilled())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		else
			return container;
	}

	/**
	 * Updates folios order.
	 * 
	 * @param projectId The project id. This is the folder name.
	 * @param file      The uploaded file received in a multipart request with the
	 *                  new order.
	 * @param response  The HTTP-specific functionality in sending a response to the
	 *                  client.
	 * @since 1.8
	 */
	@Operation(summary = "upload folios")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request Succeeded Normally"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(uploadRequestMapping)
	public ResponseEntity<List<FolioResponse>> folioUpload(
			@Parameter(description = "the container id - this is the folder name") @RequestParam String id,
			@RequestParam MultipartFile[] files, HttpServletResponse response) {
		authorizeWrite(id);

		try {
			final List<Folio> uploaded = service.store(id, files);

			if (uploaded == null)
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			else {
				final List<FolioResponse> folios = new ArrayList<>();
				for (Folio folio : uploaded)
					folios.add(new FolioResponse(folio));

				return ResponseEntity.ok().body(folios);
			}
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

}
