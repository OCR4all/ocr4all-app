/**
 * File:     CollectionFolioApiController.java
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * 
 * Author:   Herbert Baier
 * Date:     13.12.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Profile;
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
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
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
	public static final String contextPath = CollectionApiController.contextPath + "/set";

	/**
	 * The collection service.
	 */
	private final CollectionService service;

	/**
	 * Creates a collection set data controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param service              The collection service.
	 * @since 1.8
	 */
	public CollectionSetApiController(ConfigurationService configurationService, SecurityService securityService,
			CollectionService service) {
		super(CollectionSetApiController.class, configurationService, securityService);

		this.service = service;
	}

	/**
	 * Authorizes the session user for read security operations.
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
	 * @since 1.8
	 */
	private CollectionService.Collection authorizeRead(String id) throws ResponseStatusException {
		CollectionService.Collection collection = service.getCollection(id);

		if (collection == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		else if (!collection.getRight().isReadFulfilled())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		else
			return collection;
	}

	/**
	 * Authorizes the session user for write security operations.
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
	 * @since 1.8
	 */
	private CollectionService.Collection authorizeWrite(String id) throws ResponseStatusException {
		CollectionService.Collection collection = service.getCollection(id);

		if (collection == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		else if (!collection.getRight().isWriteFulfilled())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		else
			return collection;
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
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Uploaded Folios"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(uploadRequestMapping + collectionPathVariable)
	public ResponseEntity<List<SetResponse>> upload(
			@Parameter(description = "the collection id - this is the folder name") @PathVariable String collectionId,
			@RequestParam MultipartFile[] files, HttpServletResponse response) {
		CollectionService.Collection collection = authorizeWrite(collectionId);

		try {
			final List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> uploaded = service
					.store(collection, files);

			if (uploaded == null)
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			else {
				final List<SetResponse> sets = new ArrayList<>();
				for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : uploaded)
					sets.add(new SetResponse(set));

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
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Folio", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = SetResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(entityRequestMapping + collectionPathVariable)
	public ResponseEntity<SetResponse> entity(
			@Parameter(description = "the collection id - this is the folder name") @PathVariable String collectionId,
			@Parameter(description = "the set id") @RequestParam String id) {
		CollectionService.Collection collection = authorizeRead(collectionId);

		try {
			List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> sets = service.getSets(collection,
					Set.of(id));

			return sets.isEmpty() ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
					: ResponseEntity.ok().body(new SetResponse(sets.get(0)));
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
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Folios", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = SetResponse.class))) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(listRequestMapping + collectionPathVariable)
	public ResponseEntity<List<SetResponse>> list(
			@Parameter(description = "the collection id - this is the folder name") @PathVariable String collectionId) {
		CollectionService.Collection collection = authorizeRead(collectionId);

		try {
			final List<SetResponse> sets = new ArrayList<>();
			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : service.getSets(collection))
				sets.add(new SetResponse(set));

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
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Sorted Folios"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(sortRequestMapping + collectionPathVariable)
	public ResponseEntity<List<SetResponse>> sort(
			@Parameter(description = "the collection id - this is the folder name") @PathVariable String collectionId,
			@RequestBody @Valid FolioSortRequest request) {
		CollectionService.Collection collection = authorizeWrite(collectionId);

		try {
			final List<SetResponse> sets = new ArrayList<>();
			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : service.sortSets(collection,
					request.getIds(), request.isAfter()))
				sets.add(new SetResponse(set));

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
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Folios", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = SetResponse.class))) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(updateRequestMapping + collectionPathVariable)
	public ResponseEntity<List<SetResponse>> update(
			@Parameter(description = "the collection id - this is the folder name") @PathVariable String collectionId,
			@RequestBody @Valid FolioUpdateRequest request) {
		CollectionService.Collection collection = authorizeWrite(collectionId);

		try {
			List<CollectionService.Metadata> metadata = new ArrayList<>();
			for (FolioUpdateRequest.Metadata update : request.getMetadata())
				if (update != null)
					metadata.add(
							new CollectionService.Metadata(update.getId(), update.getName(), update.getKeywords()));

			final List<SetResponse> sets = new ArrayList<>();
			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : service.updateSets(collection,
					metadata))
				sets.add(new SetResponse(set));

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
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Folio", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = SetResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(removeRequestMapping + entityRequestMapping + collectionPathVariable)
	public ResponseEntity<List<SetResponse>> removeEntity(
			@Parameter(description = "the collection id - this is the folder name") @PathVariable String collectionId,
			@Parameter(description = "the set id") @RequestParam String id) {
		CollectionService.Collection collection = authorizeWrite(collectionId);

		try {
			final List<SetResponse> sets = new ArrayList<>();
			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : service.removeSets(collection,
					Set.of(id)))
				sets.add(new SetResponse(set));

			return ResponseEntity.ok().body(sets);
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
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Folios", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = SetResponse.class))) }),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(removeRequestMapping + listRequestMapping + collectionPathVariable)
	public ResponseEntity<List<SetResponse>> removeList(
			@Parameter(description = "the collection id - this is the folder name") @PathVariable String collectionId,
			@RequestBody @Valid IdentifiersRequest request) {
		CollectionService.Collection collection = authorizeWrite(collectionId);

		try {
			final List<SetResponse> sets = new ArrayList<>();
			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : service.removeSets(collection,
					request.getIds()))
				sets.add(new SetResponse(set));

			return ResponseEntity.ok().body(sets);
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
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Removed Folios"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(removeRequestMapping + allRequestMapping + collectionPathVariable)
	public void removeAll(
			@Parameter(description = "the collection id - this is the folder name") @PathVariable String collectionId,
			HttpServletResponse response) {
		CollectionService.Collection collection = authorizeWrite(collectionId);

		try {
			service.removeSets(collection, null);

			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines set responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class SetResponse extends de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a set response for the api.
		 * 
		 * @param set The set.
		 * @since 1.8
		 */
		public SetResponse(de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set) {
			super(set.getDate(), set.getUser(), set.getKeywords(), set.getId(), set.getName());
		}

	}

}
