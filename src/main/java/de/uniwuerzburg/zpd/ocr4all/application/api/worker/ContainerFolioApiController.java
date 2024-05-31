/**
 * File:     ContainerFolioApiController.java
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * 
 * Author:   Herbert Baier
 * Date:     13.12.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.FolioSortRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.FolioUpdateRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.IdentifiersRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.FolioResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.repository.ContainerService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.ImageUtils;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.folio.Folio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

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
	 * Upload the folios.
	 * 
	 * @param containerId The container id. This is the folder name.
	 * @param files       The files to upload in a multipart request.
	 * @param response    The HTTP-specific functionality in sending a response to
	 *                    the client.
	 * @return The list of uploaded folios in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "upload folios")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Uploaded Folios"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(uploadRequestMapping + containerPathVariable)
	public ResponseEntity<List<FolioResponse>> upload(
			@Parameter(description = "the container id - this is the folder name") @PathVariable String containerId,
			@RequestParam MultipartFile[] files, HttpServletResponse response) {
		ContainerService.Container container = authorizeWrite(containerId);

		try {
			final List<Folio> uploaded = service.store(container, files);

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

	/**
	 * Returns the folio of given container in the response body.
	 * 
	 * @param containerId The container id. This is the folder name.
	 * @param id          The folio id.
	 * @return The folio in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the folio of given container in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Folio", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = FolioResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(entityRequestMapping + containerPathVariable)
	public ResponseEntity<FolioResponse> entity(
			@Parameter(description = "the container id - this is the folder name") @PathVariable String containerId,
			@Parameter(description = "the folio id") @RequestParam String id) {
		ContainerService.Container container = authorizeRead(containerId);

		try {
			Folio folio = service.getFolio(container, id);

			return folio == null ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
					: ResponseEntity.ok().body(new FolioResponse(folio));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the list of folios of given container in the response body.
	 * 
	 * @param containerId The container id. This is the folder name.
	 * @return The list of folios of given container in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the list of folios of given container in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Folios", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = FolioResponse.class))) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(listRequestMapping + containerPathVariable)
	public ResponseEntity<List<FolioResponse>> list(
			@Parameter(description = "the container id - this is the folder name") @PathVariable String containerId) {
		ContainerService.Container container = authorizeRead(containerId);

		try {
			final List<FolioResponse> folios = new ArrayList<>();
			for (Folio folio : service.getFolios(container))
				folios.add(new FolioResponse(folio));

			return ResponseEntity.ok().body(folios);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Sorts the folios.
	 * 
	 * @param containerId The container id. This is the folder name.
	 * @param request     The folios sort request.
	 * @return The sorted folios in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "sort folios")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sorted Folios"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(sortRequestMapping + containerPathVariable)
	public ResponseEntity<List<FolioResponse>> sort(
			@Parameter(description = "the container id - this is the folder name") @PathVariable String containerId,
			@RequestBody @Valid FolioSortRequest request) {
		ContainerService.Container container = authorizeWrite(containerId);

		try {
			final List<FolioResponse> folios = new ArrayList<>();
			for (Folio folio : service.sortFolios(container, request.getIds(), request.isAfter()))
				folios.add(new FolioResponse(folio));

			return ResponseEntity.ok().body(folios);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates the folios.
	 * 
	 * @param containerId The container id. This is the folder name.
	 * @param request     The folios update request.
	 * @return The folios in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the required folios and returns all folios in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Folios", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = FolioResponse.class))) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(updateRequestMapping + containerPathVariable)
	public ResponseEntity<List<FolioResponse>> update(
			@Parameter(description = "the container id - this is the folder name") @PathVariable String containerId,
			@RequestBody @Valid FolioUpdateRequest request) {
		ContainerService.Container container = authorizeWrite(containerId);

		try {
			List<ImageUtils.Metadata> metadata = new ArrayList<>();
			for (FolioUpdateRequest.Metadata update : request.getMetadata())
				if (update != null)
					metadata.add(new ImageUtils.Metadata(update.getId(), update.getName(), update.getKeywords(),
							update.getPageXMLType()));

			final List<FolioResponse> folios = new ArrayList<>();
			for (Folio folio : service.updateFolios(container, metadata))
				folios.add(new FolioResponse(folio));

			return ResponseEntity.ok().body(folios);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Removes the folio.
	 * 
	 * @param containerId The container id. This is the folder name.
	 * @param id          The folio id to remove.
	 * @return The folios in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "removes the folio and returns the folios in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Folio", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = FolioResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(removeRequestMapping + entityRequestMapping + containerPathVariable)
	public ResponseEntity<List<FolioResponse>> removeEntity(
			@Parameter(description = "the container id - this is the folder name") @PathVariable String containerId,
			@Parameter(description = "the folio id") @RequestParam String id) {
		ContainerService.Container container = authorizeWrite(containerId);

		try {
			final List<FolioResponse> folios = new ArrayList<>();
			for (Folio folio : service.removeFolios(container, Set.of(id)))
				folios.add(new FolioResponse(folio));

			return ResponseEntity.ok().body(folios);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Removes the folios.
	 * 
	 * @param containerId The container id. This is the folder name.
	 * @param request     The folios remove request.
	 * @return The list of folios of given container in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "removes the folios and returns the folios in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Folios", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = FolioResponse.class))) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(removeRequestMapping + listRequestMapping + containerPathVariable)
	public ResponseEntity<List<FolioResponse>> removeList(
			@Parameter(description = "the container id - this is the folder name") @PathVariable String containerId,
			@RequestBody @Valid IdentifiersRequest request) {
		ContainerService.Container container = authorizeWrite(containerId);

		try {
			final List<FolioResponse> folios = new ArrayList<>();
			for (Folio folio : service.removeFolios(container, request.getIds()))
				folios.add(new FolioResponse(folio));

			return ResponseEntity.ok().body(folios);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Removes all folios.
	 * 
	 * @param containerId The container id. This is the folder name.
	 * @param response    The HTTP-specific functionality in sending a response to
	 *                    the client.
	 * @since 1.8
	 */
	@Operation(summary = "removes all folios")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Removed Folios"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(removeRequestMapping + allRequestMapping + containerPathVariable)
	public void removeAll(
			@Parameter(description = "the container id - this is the folder name") @PathVariable String containerId,
			HttpServletResponse response) {
		ContainerService.Container container = authorizeWrite(containerId);

		try {
			service.removeFolios(container, null);

			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Downloads the image of given container with desired id.
	 * 
	 * @param containerId The container id. This is the folder name.
	 * @param id          The image id.
	 * @param response    The HTTP-specific functionality in sending a response to
	 *                    the client.
	 * @throws IOException Signals that an I/O exception of some sort has occurred.
	 * @since 1.8
	 */
	@Operation(summary = "downloads the image of a container with given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Downloaded Image"),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(downloadRequestMapping + containerPathVariable)
	public void download(
			@Parameter(description = "the container id - this is the folder name") @PathVariable String containerId,
			@Parameter(description = "the image id") @RequestParam String id, HttpServletResponse response)
			throws IOException {
		ContainerService.Container container = authorizeRead(containerId);

		try {
			Folio folio = service.getFolio(container, id);
			if (folio == null)
				throw new ResponseStatusException(HttpStatus.NOT_FOUND);

			Path path = Paths.get(container.getConfiguration().getImages().getFolios().toString(),
					id + "." + folio.getFormat().name());
			if (!Files.exists(path))
				throw new ResponseStatusException(HttpStatus.NO_CONTENT);

			byte[] content = Files.readAllBytes(path);

			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName() + "\"");
			response.getOutputStream().write(content, 0, content.length);
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns metadata to be compressed in a zipped file containing the file name
	 * mapping of the container images in a tab-separated values format.
	 * 
	 * @param container The container.
	 * @return The metadata to be compressed in a zipped file.
	 * @throws IOException Signals that an I/O exception of some sort has occurred.
	 * @since 17
	 */
	private OCR4allUtils.ZipMetadata getZipMetadataFilenameMappingTSV(ContainerService.Container container)
			throws IOException {
		StringBuffer buffer = new StringBuffer();

		for (Folio folio : service.getFolios(container))
			buffer.append(folio.getId() + "\t" + folio.getName() + System.lineSeparator());

		return new OCR4allUtils.ZipMetadata(filenameMappingTSV,
				new ByteArrayInputStream(buffer.toString().getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * Zips the images of given container.
	 * 
	 * @param containerId The container id. This is the folder name.
	 * @param response    The HTTP-specific functionality in sending a response to
	 *                    the client.
	 * @throws IOException Signals that an I/O exception of some sort has occurred.
	 * @since 1.8
	 */
	@Operation(summary = "zip the images of ")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Downloaded Image"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(zipRequestMapping + containerPathVariable)
	public void zip(
			@Parameter(description = "the container id - this is the folder name") @PathVariable String containerId,
			HttpServletResponse response) throws IOException {
		ContainerService.Container container = authorizeRead(containerId);

		try {
			Path folder = container.getConfiguration().getImages().getFolios();

			response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=\"container-" + containerId.trim() + ".zip\"");

			OCR4allUtils.zip(folder, true, response.getOutputStream(), null,
					getZipMetadataFilenameMappingTSV(container));
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the derivative with given id.
	 * 
	 * @param containerId The container id. This is the folder name.
	 * @param folder      The image derivative folder.
	 * @param imageId     The image id.
	 * @param response    The HTTP response.
	 * @throws ResponseStatusException Throws if the image does not exists with http
	 *                                 status not found (404).
	 * @since 1.8
	 */
	private void getDerivative(ContainerService.Container container, Path folder, String imageId,
			HttpServletResponse response) throws ResponseStatusException {
		getImage(folder, imageId, container.getConfiguration().getImages().getDerivatives().getFormat().name(),
				response);
	}

	/**
	 * Returns the thumbnail derivative with given id.
	 * 
	 * @param containerId The container id. This is the folder name.
	 * @param id          The image id.
	 * @param response    The HTTP-specific functionality in sending a response to
	 *                    the client.
	 * @since 1.8
	 */
	@Operation(summary = "returns the thumbnail derivative with given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Downloaded Thumbnail Derivative"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(derivativeThumbnailRequestMapping + containerPathVariable)
	public void getDerivativeThumbnail(
			@Parameter(description = "the container id - this is the folder name") @PathVariable String containerId,
			@Parameter(description = "the image id") @RequestParam String id, HttpServletResponse response) {
		ContainerService.Container container = authorizeRead(containerId);

		getDerivative(container, container.getConfiguration().getImages().getDerivatives().getThumbnail(), id,
				response);
	}

	/**
	 * Returns the detail derivative with given id.
	 * 
	 * @param containerId The container id. This is the folder name.
	 * @param id          The image id.
	 * @param response    The HTTP-specific functionality in sending a response to
	 *                    the client.
	 * @since 1.8
	 */
	@Operation(summary = "returns the detail derivative with given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Downloaded Detail Derivative"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(derivativeDetailRequestMapping + containerPathVariable)
	public void getDerivativeDetail(
			@Parameter(description = "the container id - this is the folder name") @PathVariable String containerId,
			@Parameter(description = "the image id") @RequestParam String id, HttpServletResponse response) {
		ContainerService.Container container = authorizeRead(containerId);

		getDerivative(container, container.getConfiguration().getImages().getDerivatives().getDetail(), id, response);
	}

	/**
	 * Returns the best derivative with given id.
	 * 
	 * @param containerId The container id. This is the folder name.
	 * @param id          The image id.
	 * @param response    The HTTP-specific functionality in sending a response to
	 *                    the client.
	 * @since 1.8
	 */
	@Operation(summary = "returns the best derivative with given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Downloaded Best Derivative"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(derivativeBestRequestMapping + containerPathVariable)
	public void getDerivativeBest(
			@Parameter(description = "the container id - this is the folder name") @PathVariable String containerId,
			@Parameter(description = "the image id") @RequestParam String id, HttpServletResponse response) {
		ContainerService.Container container = authorizeRead(containerId);

		getDerivative(container, container.getConfiguration().getImages().getDerivatives().getBest(), id, response);
	}

}
