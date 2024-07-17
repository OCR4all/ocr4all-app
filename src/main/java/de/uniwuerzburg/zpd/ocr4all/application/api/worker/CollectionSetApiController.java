/**
 * File:     CollectionSetApiController.java
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * 
 * Author:   Herbert Baier
 * Date:     13.12.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.IdentifierRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.IdentifiersRequest;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.SetResponse;
import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.ModelService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.spi.util.pagexml.PageXMLLevel;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Defines collection set data controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Tag(name = "collection set", description = "the data collection set API")
@RestController
@RequestMapping(path = CollectionSetApiController.contextPath, produces = CoreApiController.applicationJson)
public class CollectionSetApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = CollectionApiController.contextPath + setRequestMapping;

	/**
	 * Creates a collection set data controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param modelService         The model service.
	 * @param service              The collection service.
	 * @since 1.8
	 */
	public CollectionSetApiController(ConfigurationService configurationService, SecurityService securityService,
			ModelService modelService, CollectionService service) {
		super(CollectionSetApiController.class, configurationService, securityService, service, modelService);
	}

	/**
	 * Returns the files for all sets.
	 * 
	 * @param folder The collection path.
	 * @return The files for the sets. The key is the set id.
	 * @throws IOException
	 * @since 17
	 */
	private Hashtable<String, List<Path>> getSetFiles(Path folder) throws IOException {
		return getSetFiles(folder, null);
	}

	/**
	 * Returns the files for the sets.
	 * 
	 * @param folder The collection path.
	 * @param id     The set id. If null, returns the files for all sets.
	 * @return The files for the sets. The key is the set id.
	 * @throws IOException
	 * @since 17
	 */
	private Hashtable<String, List<Path>> getSetFiles(Path folder, String id) throws IOException {
		List<Path> files;

		try (Stream<Path> stream = Files.list(folder)) {
			files = stream.filter(file -> {
				if (Files.isDirectory(file))
					return false;
				else {
					String name = file.getFileName().toString();
					return (id == null || name.startsWith(id + "."));
				}
			}).collect(Collectors.toList());
		}

		Hashtable<String, List<Path>> set = new Hashtable<>();
		for (Path file : files) {
			String[] split = file.getFileName().toString().split("\\.", 2);
			if (split.length == 2 && !split[0].isEmpty()) {
				List<Path> list = set.get(split[0]);
				if (list == null) {
					list = new ArrayList<Path>();
					set.put(split[0], list);
				}

				list.add(file);
			}
		}

		return set;
	}

	/**
	 * Upload the sets.
	 * 
	 * @param collectionId The collection id. This is the folder name.
	 * @param files        The files to upload in a multipart request.
	 * @param response     The HTTP-specific functionality in sending a response to
	 *                     the client.
	 * @return The list of uploaded sets in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "upload sets")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Uploaded Sets"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(uploadRequestMapping + collectionPathVariable)
	public ResponseEntity<List<SetResponse>> upload(
			@Parameter(description = "the collection id - this is the folder name") @PathVariable String collectionId,
			@RequestParam MultipartFile[] files, HttpServletResponse response) {
		CollectionService.Collection collection = authorizeCollectionWrite(collectionId);

		try {
			final List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> uploaded = collectionService
					.store(collection, files);

			if (uploaded == null)
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			else {
				Hashtable<String, List<Path>> setFiles = getSetFiles(collection.getConfiguration().getFolder());
				final List<SetResponse> sets = new ArrayList<>();

				for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : uploaded)
					sets.add(new SetResponse(set, setFiles));

				return ResponseEntity.ok().body(sets);
			}
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the set of given collection in the response body.
	 * 
	 * @param collectionId The collection id. This is the folder name.
	 * @param id           The set id.
	 * @return The set in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the set of given collection in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Set", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = SetResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(entityRequestMapping + collectionPathVariable)
	public ResponseEntity<SetResponse> entity(
			@Parameter(description = "the collection id - this is the folder name") @PathVariable String collectionId,
			@Parameter(description = "the set id") @RequestParam String id) {
		CollectionService.Collection collection = authorizeCollectionRead(collectionId);

		try {
			de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set = collectionService.getSet(collection, id);

			return set == null ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
					: ResponseEntity.ok()
							.body(new SetResponse(set, getSetFiles(collection.getConfiguration().getFolder(), id)));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the list of sets of given collection in the response body.
	 * 
	 * @param collectionId The collection id. This is the folder name.
	 * @return The list of sets of given collection in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the list of sets of given collection in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sets", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = SetResponse.class))) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(listRequestMapping + collectionPathVariable)
	public ResponseEntity<List<SetResponse>> list(
			@Parameter(description = "the collection id - this is the folder name") @PathVariable String collectionId) {
		CollectionService.Collection collection = authorizeCollectionRead(collectionId);

		try {
			Hashtable<String, List<Path>> setFiles = getSetFiles(collection.getConfiguration().getFolder());
			final List<SetResponse> sets = new ArrayList<>();

			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : collectionService
					.getSets(collection))
				sets.add(new SetResponse(set, setFiles));

			return ResponseEntity.ok().body(sets);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Sorts the sets.
	 * 
	 * @param collectionId The collection id. This is the folder name.
	 * @param request      The sets sort request.
	 * @return The sorted sets in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "sort sets")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sorted Sets", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = SetResponse.class))) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(sortRequestMapping + collectionPathVariable)
	public ResponseEntity<List<SetResponse>> sort(
			@Parameter(description = "the collection id - this is the folder name") @PathVariable String collectionId,
			@RequestBody @Valid SetSortRequest request) {
		CollectionService.Collection collection = authorizeCollectionWrite(collectionId);

		try {
			Hashtable<String, List<Path>> setFiles = getSetFiles(collection.getConfiguration().getFolder());
			final List<SetResponse> sets = new ArrayList<>();

			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : collectionService
					.sortSets(collection, request.getIds(), request.isAfter()))
				sets.add(new SetResponse(set, setFiles));

			return ResponseEntity.ok().body(sets);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Updates the sets.
	 * 
	 * @param collectionId The collection id. This is the folder name.
	 * @param request      The sets update request.
	 * @return The sets in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "updates the required sets and returns all sets in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sets", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = SetResponse.class))) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(updateRequestMapping + collectionPathVariable)
	public ResponseEntity<List<SetResponse>> update(
			@Parameter(description = "the collection id - this is the folder name") @PathVariable String collectionId,
			@RequestBody @Valid SetUpdateRequest request) {
		CollectionService.Collection collection = authorizeCollectionWrite(collectionId);

		try {
			List<CollectionService.Metadata> metadata = new ArrayList<>();
			for (SetUpdateRequest.Metadata update : request.getMetadata())
				if (update != null)
					metadata.add(
							new CollectionService.Metadata(update.getId(), update.getName(), update.getKeywords()));

			Hashtable<String, List<Path>> setFiles = getSetFiles(collection.getConfiguration().getFolder());
			final List<SetResponse> sets = new ArrayList<>();

			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : collectionService
					.updateSets(collection, metadata))
				sets.add(new SetResponse(set, setFiles));

			return ResponseEntity.ok().body(sets);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Removes the set.
	 * 
	 * @param collectionId The collection id. This is the folder name.
	 * @param id           The set id to remove.
	 * @return The sets in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "removes the set and returns the sets in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Set", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = SetResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(removeRequestMapping + entityRequestMapping + collectionPathVariable)
	public ResponseEntity<List<SetResponse>> removeEntity(
			@Parameter(description = "the collection id - this is the folder name") @PathVariable String collectionId,
			@Parameter(description = "the set id") @RequestParam String id) {
		CollectionService.Collection collection = authorizeCollectionWrite(collectionId);

		try {
			List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> bundles = collectionService
					.removeSets(collection, Set.of(id));

			if (bundles == null)
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			else {
				Hashtable<String, List<Path>> setFiles = getSetFiles(collection.getConfiguration().getFolder(), id);
				final List<SetResponse> sets = new ArrayList<>();

				for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : bundles)
					sets.add(new SetResponse(set, setFiles));

				return ResponseEntity.ok().body(sets);
			}
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Removes the sets.
	 * 
	 * @param collectionId The collection id. This is the folder name.
	 * @param request      The sets remove request.
	 * @return The list of sets of given collection in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "removes the sets and returns the sets in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sets", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = SetResponse.class))) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(removeRequestMapping + listRequestMapping + collectionPathVariable)
	public ResponseEntity<List<SetResponse>> removeList(
			@Parameter(description = "the collection id - this is the folder name") @PathVariable String collectionId,
			@RequestBody @Valid IdentifiersRequest request) {
		CollectionService.Collection collection = authorizeCollectionWrite(collectionId);

		try {
			List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> bundles = collectionService
					.removeSets(collection, request.getIds());

			if (bundles == null)
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			else {
				Hashtable<String, List<Path>> setFiles = getSetFiles(collection.getConfiguration().getFolder());
				final List<SetResponse> sets = new ArrayList<>();

				for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : bundles)
					sets.add(new SetResponse(set, setFiles));

				return ResponseEntity.ok().body(sets);
			}
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Removes the file of the set.
	 * 
	 * @param collectionId The collection id. This is the folder name.
	 * @param id           The set id.
	 * @param suffix       The suffix.
	 * @return The sets in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "removes the file of the set and returns the sets in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Set", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = SetResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(removeRequestMapping + fileRequestMapping + collectionPathVariable)
	public ResponseEntity<List<SetResponse>> removeFile(
			@Parameter(description = "the collection id - this is the folder name") @PathVariable String collectionId,
			@Parameter(description = "the set id") @RequestParam String id,
			@Parameter(description = "the suffix") @RequestParam String suffix) {
		CollectionService.Collection collection = authorizeCollectionWrite(collectionId);

		try {
			List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> bundles = collectionService
					.removeFile(collection, id, suffix);

			if (bundles == null)
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			else {
				Hashtable<String, List<Path>> setFiles = getSetFiles(collection.getConfiguration().getFolder(), id);
				final List<SetResponse> sets = new ArrayList<>();

				for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : bundles)
					sets.add(new SetResponse(set, setFiles));

				return ResponseEntity.ok().body(sets);
			}
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Removes all sets.
	 * 
	 * @param collectionId The collection id. This is the folder name.
	 * @param response     The HTTP-specific functionality in sending a response to
	 *                     the client.
	 * @since 1.8
	 */
	@Operation(summary = "removes all sets")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Removed Sets"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(removeRequestMapping + allRequestMapping + collectionPathVariable)
	public void removeAll(
			@Parameter(description = "the collection id - this is the folder name") @PathVariable String collectionId,
			HttpServletResponse response) {
		CollectionService.Collection collection = authorizeCollectionWrite(collectionId);

		try {
			collectionService.removeSets(collection, null);

			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns metadata to be compressed in a zipped file containing the file name
	 * mapping of the sets in a tab-separated values format.
	 * 
	 * @param sets The sets.
	 * @return The metadata to be compressed in a zipped file.
	 * @throws IOException Signals that an I/O exception of some sort has occurred.
	 * @since 17
	 */
	private OCR4allUtils.ZipMetadata getZipMetadataFilenameMappingTSV(
			Collection<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> sets) throws IOException {
		StringBuffer buffer = new StringBuffer();

		for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : sets)
			buffer.append(set.getId() + "\t" + set.getName() + System.lineSeparator());

		return new OCR4allUtils.ZipMetadata(filenameMappingTSV,
				new ByteArrayInputStream(buffer.toString().getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * Downloads the files of given collection with desired set id.
	 * 
	 * @param collectionId The collection id. This is the folder name.
	 * @param id           The set id.
	 * @param response     The HTTP-specific functionality in sending a response to
	 *                     the client.
	 * @throws IOException Signals that an I/O exception of some sort has occurred.
	 * @since 1.8
	 */
	@Operation(summary = "downloads the files of a collection with given set id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Downloaded Files"),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(downloadRequestMapping + collectionPathVariable)
	public void download(
			@Parameter(description = "the collection id - this is the folder name") @PathVariable String collectionId,
			@Parameter(description = "the set id") @RequestParam String id, HttpServletResponse response)
			throws IOException {
		CollectionService.Collection collection = authorizeCollectionRead(collectionId);

		try {
			de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set = collectionService.getSet(collection, id);
			if (set == null)
				throw new ResponseStatusException(HttpStatus.NOT_FOUND);

			Path folder = collection.getConfiguration().getFolder();

			response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=\"collection-" + collectionId.trim() + "_set-" + id + ".zip\"");

			OCR4allUtils.zip(folder, true, response.getOutputStream(), new OCR4allUtils.ZipFilter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils.ZipFilter#
				 * accept(java.io.File)
				 */
				@Override
				public boolean accept(File entry) {
					return entry.getName().startsWith(id + ".");
				}
			}, getZipMetadataFilenameMappingTSV(Set.of(set)));
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Zips the images of given collection.
	 * 
	 * @param collectionId The collection id. This is the folder name.
	 * @param response     The HTTP-specific functionality in sending a response to
	 *                     the client.
	 * @throws IOException Signals that an I/O exception of some sort has occurred.
	 * @since 1.8
	 */
	@Operation(summary = "zip the collection image")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Downloaded Images"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(zipRequestMapping + collectionPathVariable)
	public void zip(
			@Parameter(description = "the collection id - this is the folder name") @PathVariable String collectionId,
			HttpServletResponse response) throws IOException {
		CollectionService.Collection collection = authorizeCollectionRead(collectionId);

		try {
			Path folder = collection.getConfiguration().getFolder();

			response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=\"collection-" + collectionId.trim() + ".zip\"");

			OCR4allUtils.zip(folder, true, response.getOutputStream(), new OCR4allUtils.ZipFilter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils.ZipFilter#
				 * accept(java.io.File)
				 */
				@Override
				public boolean accept(File entry) {
					// Ignore configuration folders
					return !entry.getName().startsWith(".");
				}
			}, getZipMetadataFilenameMappingTSV(collectionService.getSets(collection)));
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the codec.
	 * 
	 * @param request The codec request.
	 * @return The sets in the response body.
	 * @since 1.8
	 */
	@Operation(summary = "returns the codec in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Codec", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = CodecResponse.class))) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(codecRequestMapping)
	public ResponseEntity<List<CodecResponse>> codec(@RequestBody @Valid CodecRequest request) {

		try {
			List<CodecResponse> codecs = new ArrayList<>();

			for (CodecRequest.Dataset dataset : request.getDatasets()) {

				try {
					CollectionService.Collection collection = authorizeCollectionRead(dataset.getId());

					List<CodecResponse.Page> pages = new ArrayList<>();

					for (String filename : dataset.getFilenames())
						if (filename != null && !filename.isBlank())
							pages.add(new CodecResponse.Page(filename.trim(),
									collectionService.getPageXMLCodec(collection, filename, request.getLevel(),
											request.getIndex(), request.getNormalizer())));

					codecs.add(new CodecResponse(collection.getConfiguration().getId(), pages));

				} catch (ResponseStatusException e) {
					codecs.add(new CodecResponse(dataset.getId().trim()));
				}
			}

			return ResponseEntity.ok().body(codecs);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines set sort requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class SetSortRequest extends IdentifiersRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * True if the sets that do not belong to the collection are to be inserted
		 * after the sets that belong to the collection. Otherwise, they are placed at
		 * the beginning.
		 */
		@NotNull
		private Boolean isAfter;

		/**
		 * Returns true if the sets that do not belong to the collection are to be
		 * inserted after the sets that belong to the collection. Otherwise, they are
		 * placed at the beginning.
		 *
		 * @return True if the sets that do not belong to the collection are to be
		 *         inserted after the sets that belong to the collection. Otherwise,
		 *         they are placed at the beginning.
		 * @since 1.8
		 */
		public Boolean isAfter() {
			return isAfter;
		}

		/**
		 * Set to true if the sets that do not belong to the collection are to be
		 * inserted after the sets that belong to the collection. Otherwise, they are
		 * placed at the beginning.
		 *
		 * @param isAfter The insert flag to set.
		 * @since 1.8
		 */
		public void setAfter(Boolean isAfter) {
			this.isAfter = isAfter;
		}
	}

	/**
	 * Defines set update requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class SetUpdateRequest implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The metadata.
		 */
		@NotNull
		private List<Metadata> metadata;

		/**
		 * Returns the metadata.
		 *
		 * @return The metadata.
		 * @since 1.8
		 */
		public List<Metadata> getMetadata() {
			return metadata;
		}

		/**
		 * Set the metadata.
		 *
		 * @param metadata The metadata to set.
		 * @since 1.8
		 */
		public void setMetadata(List<Metadata> metadata) {
			this.metadata = metadata;
		}

		/**
		 * Defines metadata for update.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class Metadata extends IdentifierRequest {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The name without extension.
			 */
			@NotBlank
			private String name;

			/**
			 * The keywords.
			 */
			private Set<String> keywords;

			/**
			 * Returns the name.
			 *
			 * @return The name.
			 * @since 1.8
			 */
			public String getName() {
				return name;
			}

			/**
			 * Set the name.
			 *
			 * @param name The name to set.
			 * @since 1.8
			 */
			public void setName(String name) {
				this.name = name;
			}

			/**
			 * Returns the keywords.
			 *
			 * @return The keywords.
			 * @since 1.8
			 */
			public Set<String> getKeywords() {
				return keywords;
			}

			/**
			 * Set the keywords.
			 *
			 * @param keywords The keywords to set.
			 * @since 1.8
			 */
			public void setKeywords(Set<String> keywords) {
				this.keywords = keywords;
			}
		}
	}

	/**
	 * Defines codec requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class CodecRequest implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The datasets.
		 */
		@NotNull
		private List<Dataset> datasets;

		/**
		 * The PageXML level.
		 */
		@NotNull
		private PageXMLLevel level;

		/**
		 * The PageXML index.
		 */
		@NotNull
		private int index;

		/**
		 * The normalizer. If null, do not normalize.
		 */
		private Normalizer.Form normalizer;

		/**
		 * Returns the datasets.
		 *
		 * @return The datasets.
		 * @since 17
		 */
		public List<Dataset> getDatasets() {
			return datasets;
		}

		/**
		 * Set the datasets.
		 *
		 * @param datasets The datasets to set.
		 * @since 17
		 */
		public void setDatasets(List<Dataset> datasets) {
			this.datasets = datasets;
		}

		/**
		 * Returns the PageXML level.
		 *
		 * @return The PageXML level.
		 * @since 17
		 */
		public PageXMLLevel getLevel() {
			return level;
		}

		/**
		 * Set the PageXML level.
		 *
		 * @param level The PageXML level to set.
		 * @since 17
		 */
		public void setLevel(PageXMLLevel level) {
			this.level = level;
		}

		/**
		 * Returns the PageXML index.
		 *
		 * @return The PageXML index.
		 * @since 17
		 */
		public int getIndex() {
			return index;
		}

		/**
		 * Set the PageXML index.
		 *
		 * @param index The PageXML index to set.
		 * @since 17
		 */
		public void setIndex(int index) {
			this.index = index;
		}

		/**
		 * Returns the normalizer. If null, do not normalize.
		 *
		 * @return The normalizer.
		 * @since 17
		 */
		public Normalizer.Form getNormalizer() {
			return normalizer;
		}

		/**
		 * Set the normalizer. If null, do not normalize.
		 *
		 * @param normalizer The normalizer to set.
		 * @since 17
		 */
		public void setNormalizer(Normalizer.Form normalizer) {
			this.normalizer = normalizer;
		}

		/**
		 * Defines datasets requests for the api.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 17
		 */
		public static class Dataset implements Serializable {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The id.
			 */
			@NotBlank
			private String id;

			/**
			 * The filenames.
			 */
			@NotNull
			private List<String> filenames;

			/**
			 * Returns the id.
			 *
			 * @return The id.
			 * @since 17
			 */
			public String getId() {
				return id;
			}

			/**
			 * Set the id.
			 *
			 * @param id The id to set.
			 * @since 17
			 */
			public void setId(String id) {
				this.id = id;
			}

			/**
			 * Returns the filenames.
			 *
			 * @return The filenames.
			 * @since 17
			 */
			public List<String> getFilenames() {
				return filenames;
			}

			/**
			 * Set the filenames.
			 *
			 * @param filenames The filenames to set.
			 * @since 17
			 */
			public void setFilenames(List<String> filenames) {
				this.filenames = filenames;
			}

		}
	}

	/**
	 * Defines codec responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class CodecResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The collection id.
		 */
		@JsonProperty("collection-id")
		@NotBlank
		private String collectionId;

		/**
		 * The pages.
		 */
		private List<Page> pages;

		/**
		 * Default constructor for a codec response for the api.
		 * 
		 * @since 17
		 */
		public CodecResponse() {
			super();
		}

		/**
		 * Creates a codec response for the api.
		 * 
		 * @param collectionId The collection id.
		 * @since 17
		 */
		public CodecResponse(@NotBlank String collectionId) {
			this(collectionId, null);
		}

		/**
		 * Creates a codec response for the api.
		 * 
		 * @param collectionId The collection id.
		 * @param pages        The pages.
		 * @since 17
		 */
		public CodecResponse(@NotBlank String collectionId, List<Page> pages) {
			super();

			this.collectionId = collectionId;
			this.pages = pages;
		}

		/**
		 * Returns the collection id.
		 *
		 * @return The collection id.
		 * @since 17
		 */
		public String getCollectionId() {
			return collectionId;
		}

		/**
		 * Set the collection id.
		 *
		 * @param collectionId The collection id to set.
		 * @since 17
		 */
		public void setCollectionId(String collectionId) {
			this.collectionId = collectionId;
		}

		/**
		 * Returns the pages.
		 *
		 * @return The pages.
		 * @since 17
		 */
		public List<Page> getPages() {
			return pages;
		}

		/**
		 * Set the pages.
		 *
		 * @param pages The pages to set.
		 * @since 17
		 */
		public void setPages(List<Page> pages) {
			this.pages = pages;
		}

		/**
		 * Defines page responses for the api.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class Page implements Serializable {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The id.
			 */
			@NotBlank
			private String id;

			/**
			 * The codec.
			 */
			private Hashtable<String, Integer> codec;

			/**
			 * Default constructor for a page response for the api.
			 * 
			 * @since 17
			 */
			public Page() {
				super();
			}

			/**
			 * Creates a page response for the api.
			 * 
			 * @param id    The id.
			 * @param codec The codec.
			 * @since 17
			 */
			public Page(@NotBlank String id, Hashtable<String, Integer> codec) {
				super();

				this.id = id;
				this.codec = codec;
			}

			/**
			 * Returns the id.
			 *
			 * @return The id.
			 * @since 17
			 */
			public String getId() {
				return id;
			}

			/**
			 * Set the id.
			 *
			 * @param id The id to set.
			 * @since 17
			 */
			public void setId(String id) {
				this.id = id;
			}

			/**
			 * Returns the codec.
			 *
			 * @return The codec.
			 * @since 17
			 */
			public Hashtable<String, Integer> getCodec() {
				return codec;
			}

			/**
			 * Set the codec.
			 *
			 * @param codec The codec to set.
			 * @since 17
			 */
			public void setCodec(Hashtable<String, Integer> codec) {
				this.codec = codec;
			}

		}

	}
}
