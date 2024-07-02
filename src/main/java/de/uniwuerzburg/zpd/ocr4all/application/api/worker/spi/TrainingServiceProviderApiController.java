/**
 * File:    TrainingServiceProviderApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     02.07.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.JobJsonResponse;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi.ServiceProviderResponse;
import de.uniwuerzburg.zpd.ocr4all.application.api.worker.CoreApiController;
import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.AssembleService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.SchedulerService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.training.TrainingService;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider;
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
 * Defines training service provider controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@Profile("api")
@Tag(name = "SPI tool", description = "the tool service provider API")
@RestController
@RequestMapping(path = TrainingServiceProviderApiController.contextPath, produces = CoreApiController.applicationJson)
public class TrainingServiceProviderApiController extends CoreServiceProviderApiController<TrainingService> {
	/**
	 * The context path.
	 */
	public static final String contextPath = spiContextPathVersion_1_0 + "/training";

	/**
	 * The AssembleService.
	 */
	private final AssembleService assembleService;

	/**
	 * Creates a training service provider controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param assembleService      The AssembleService.
	 * @param schedulerService     The scheduler service.
	 * @param service              The service.
	 * @since 17
	 */
	public TrainingServiceProviderApiController(ConfigurationService configurationService,
			SecurityService securityService, AssembleService assembleService, SchedulerService schedulerService,
			TrainingService service) {
		super(TrainingServiceProviderApiController.class, configurationService, securityService, null, null,
				schedulerService, Type.training, service);

		this.assembleService = assembleService;
	}

	/**
	 * Returns the service providers in the response body.
	 * 
	 * @param lang The language. if null, then use the application preferred locale.
	 * @return The service providers in the response body.
	 * @since 17
	 */
	@Operation(summary = "returns the service providers in the response body")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Service Providers", content = {
			@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = ServiceProviderResponse.class))) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "405", description = "Method Not Allowed", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(providersRequestMapping)
	public ResponseEntity<List<ServiceProviderResponse>> serviceProviders(
			@Parameter(description = "the language") @RequestParam(required = false) String lang) {
		if (!assembleService.isCreateModel())
			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
		else
			try {
				final Locale locale = getLocale(lang);

				final List<ServiceProviderResponse> providers = new ArrayList<>();
				for (CoreServiceProvider<? extends ServiceProvider>.Provider provider : service.getActiveProviders())
					providers.add(new ServiceProviderResponse(locale, type, provider.getId(),
							provider.getServiceProvider(), null));

				Collections.sort(providers, new Comparator<ServiceProviderResponse>() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
					 */
					@Override
					public int compare(ServiceProviderResponse o1, ServiceProviderResponse o2) {
						if (o1.getIndex() != o2.getIndex())
							return o1.getIndex() - o2.getIndex();
						else {
							int compare = o1.getName().compareToIgnoreCase(o2.getName());
							if (compare != 0)
								return compare;
							else
								return o1.getVersion() <= o2.getVersion() ? -1 : 1;
						}
					}
				});

				return ResponseEntity.ok().body(providers);
			} catch (Exception ex) {
				log(ex);

				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
			}

	}

	/**
	 * Schedules a process to execute the service provider.
	 * 
	 * @param request The service provider training request.
	 * @param lang    The language. if null, then use the application preferred
	 *                locale.
	 * @return The job in the response body.
	 * @since 17
	 */
	@Operation(summary = "schedules a process to execute the service provider")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Schedules Process", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = JobJsonResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "405", description = "Method Not Allowed", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(scheduleRequestMapping)
	public ResponseEntity<JobJsonResponse> schedule(
			@Parameter(description = "the project id - this is the folder name") @PathVariable String projectId,
			@Parameter(description = "the sandbox id - this is the folder name") @PathVariable String sandboxId,
			@RequestBody @Valid ServiceProviderTrainingRequest request,
			@Parameter(description = "the language") @RequestParam(required = false) String lang,
			HttpServletResponse response) {
		if (!assembleService.isCreateModel())
			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
		else {
			// TODO: continue
			return ResponseEntity.ok().body(null);
		}
	}

	/**
	 * Defines service provider training requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class ServiceProviderTrainingRequest extends ServiceProviderRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		// TODO: Assemble Model description + Dataset
	}

}
