/**
 * File:    ActionServiceProviderApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     19.09.2024
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi.ServiceProviderResponse;
import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi.ServiceProviderTaskResponse;
import de.uniwuerzburg.zpd.ocr4all.application.api.worker.CoreApiController;
import de.uniwuerzburg.zpd.ocr4all.application.communication.action.EvaluationMeasure;
import de.uniwuerzburg.zpd.ocr4all.application.core.assemble.ModelService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.evaluation.EvaluationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.action.ActionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.SPIUtils;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.ServiceProviderException;
import de.uniwuerzburg.zpd.ocr4all.application.spi.ActionServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Defines action service provider controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@Profile("api")
@Tag(name = "SPI action", description = "the action service provider API")
@RestController
@RequestMapping(path = ActionServiceProviderApiController.contextPath, produces = CoreApiController.applicationJson)
public class ActionServiceProviderApiController extends CoreServiceProviderApiController<ActionService> {
	/**
	 * The context path.
	 */
	public static final String contextPath = spiContextPathVersion_1_0 + "/action";

	/**
	 * The calamari request mapping.
	 */
	public static final String calamariRequestMapping = "/calamari";

	/**
	 * Defines filters for action service providers.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	private enum Filter {
		all, calamariEvaluation
	}

	/**
	 * The evaluation service.
	 */
	protected final EvaluationService evaluationService;

	/**
	 * Creates a action service provider controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param collectionService    The collection service.
	 * @param modelService         The model service.
	 * @param evaluationService    The evaluation service.
	 * @param service              The service.
	 * @since 17
	 */
	public ActionServiceProviderApiController(ConfigurationService configurationService,
			SecurityService securityService, CollectionService collectionService, ModelService modelService,
			EvaluationService evaluationService, ActionService service) {
		super(ActionServiceProviderApiController.class, configurationService, securityService, collectionService,
				modelService, null, null, null, Type.action, service);

		this.evaluationService = evaluationService;
	}

	/**
	 * Returns the service providers.
	 * 
	 * @param lang   The language. if null, then use the application preferred
	 *               locale.
	 * @param filter The filter for action service providers.
	 * @return The service providers.
	 * @throws ServiceProviderException Throws on service provider exceptions.
	 * @since 17
	 */
	public List<ServiceProviderResponse> getServiceProviders(String lang, Filter filter)
			throws ServiceProviderException {
		final Locale locale = getLocale(lang);

		final List<ServiceProviderResponse> providers = new ArrayList<>();
		for (CoreServiceProvider<? extends ServiceProvider>.Provider provider : service.getActiveProviders()) {
			boolean isProvider;
			switch (filter) {
			case calamariEvaluation:
				isProvider = EvaluationService
						.isCalamariEvaluationProvider((ActionServiceProvider) (provider.getServiceProvider()));
				break;
			case all:
			default:
				isProvider = true;
				break;
			}

			if (isProvider)
				providers.add(new ServiceProviderResponse(locale, type, provider.getId(), provider.getServiceProvider(),
						null));
		}

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

		return providers;
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
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(providersRequestMapping)
	public ResponseEntity<List<ServiceProviderResponse>> serviceProviders(
			@Parameter(description = "the language") @RequestParam(required = false) String lang) {
		try {
			return ResponseEntity.ok().body(getServiceProviders(lang, Filter.all));
		} catch (Exception ex) {
			log(ex);

			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
		}

	}

	/**
	 * Returns the calamari evaluation service providers in the response body.
	 * 
	 * @param lang The language. if null, then use the application preferred locale.
	 * @return The calamari evaluation service providers in the response body.
	 * @since 17
	 */
	@Operation(summary = "returns the calamari evaluation service providers in the response body")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Calamari Evaluation Service Providers", content = {
					@Content(mediaType = CoreApiController.applicationJson, array = @ArraySchema(schema = @Schema(implementation = ServiceProviderResponse.class))) }),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(providersRequestMapping + evaluateRequestMapping + calamariRequestMapping)
	public ResponseEntity<List<ServiceProviderResponse>> serviceProvidersCalamariEvaluation(
			@Parameter(description = "the language") @RequestParam(required = false) String lang) {
		try {
			return ResponseEntity.ok().body(getServiceProviders(lang, Filter.calamariEvaluation));
		} catch (Exception ex) {
			log(ex);

			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
		}

	}

	/**
	 * Evaluates the dataset using calamari.
	 * 
	 * @param request The evaluation request.
	 * @param lang    The language. if null, then use the application preferred
	 *                locale.
	 * @return The evaluation measure in the response body.
	 * @since 17
	 */
	@Operation(summary = "evaluates the dataset using calamari evaluation provider")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Dataset Evaluation", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = EvaluationResponse.class)) }),
			@ApiResponse(responseCode = "204", description = "No Content", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "405", description = "Method Not Allowed", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@PostMapping(evaluateRequestMapping + calamariRequestMapping)
	public ResponseEntity<EvaluationResponse> evaluateCalamari(@RequestBody @Valid EvaluationRequest request,
			@Parameter(description = "the language") @RequestParam(required = false) String lang) {
		if (request.getDataset().getCollections() == null)
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		else
			for (EvaluationService.Dataset.Collection collection : request.getDataset().getCollections())
				if (collection == null)
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
				else
					authorizeCollectionRead(collection.getId());

		try {
			EvaluationMeasure evaluation = evaluationService.evaluateCalamari(
					service.getActiveProvider(request.getId()), SPIUtils.getModelArgument(request),
					request.getDataset());

			if (evaluation == null)
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			else if (evaluation.getState() == null)
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new EvaluationResponse(evaluation));
			else
				switch (evaluation.getState()) {
				case completed:
					return ResponseEntity.ok().body(new EvaluationResponse(evaluation));
				case interrupted:
					return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
							.body(new EvaluationResponse(evaluation));
				case inconsistent:
				default:
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new EvaluationResponse(evaluation));
				}
		} catch (Exception ex) {
			log(ex);

			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.body(new EvaluationResponse(new EvaluationMeasure(EvaluationMeasure.State.interrupted, ex)));
		}
	}

	/**
	 * Defines evaluation requests for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class EvaluationRequest
			extends de.uniwuerzburg.zpd.ocr4all.application.persistence.spi.ServiceProvider {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The dataset.
		 */
		@NotNull
		private EvaluationService.Dataset dataset;

		/**
		 * Returns the dataset.
		 *
		 * @return The dataset.
		 * @since 17
		 */
		public EvaluationService.Dataset getDataset() {
			return dataset;
		}

		/**
		 * Set the dataset.
		 *
		 * @param dataset The dataset to set.
		 * @since 17
		 */
		public void setDataset(EvaluationService.Dataset dataset) {
			this.dataset = dataset;
		}

	}

	/**
	 * Defines evaluation responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class EvaluationResponse extends ServiceProviderTaskResponse {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The summary.
		 */
		private final EvaluationMeasure.Summary summary;

		/**
		 * The details.
		 */
		private final List<EvaluationMeasure.Detail> details;

		/**
		 * Creates an evaluation response for the api.
		 * 
		 * @param evaluation The evaluation measure.
		 * @since 17
		 */
		public EvaluationResponse(EvaluationMeasure evaluation) {
			super(evaluation);

			summary = evaluation.getSummary();
			details = evaluation.getDetails();
		}

		/**
		 * Returns the summary.
		 *
		 * @return The summary.
		 * @since 17
		 */
		public EvaluationMeasure.Summary getSummary() {
			return summary;
		}

		/**
		 * Returns the details.
		 *
		 * @return The details.
		 * @since 17
		 */
		public List<EvaluationMeasure.Detail> getDetails() {
			return details;
		}

	}
}
