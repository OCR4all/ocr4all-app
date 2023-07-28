/**
 * File:     OverviewServiceProviderApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     05.04.2023
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi.ServiceProviderResponse;
import de.uniwuerzburg.zpd.ocr4all.application.api.worker.CoreApiController;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.export.ExportService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.imp.ImportService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.launcher.LauncherService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.ocr.OpticalCharacterRecognitionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.olr.OpticalLayoutRecognitionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.postcorrection.PostcorrectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.preprocessing.PreprocessingService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.tool.ToolService;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.ServiceProviderException;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Target;

/**
 * Defines overview service provider controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = OverviewServiceProviderApiController.contextPath, produces = CoreApiController.applicationJson)
public class OverviewServiceProviderApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = ServiceProviderCoreApiController.spiContextPathVersion_1_0;

	/**
	 * The import service.
	 */
	private final ImportService importService;

	/**
	 * The launcher service.
	 */
	private final LauncherService launcherService;

	/**
	 * The preprocessing service.
	 */
	private final PreprocessingService preprocessingService;

	/**
	 * The optical layout recognition (OLR) service.
	 */
	private final OpticalLayoutRecognitionService olrService;

	/**
	 * The optical character recognition (OCR) service.
	 */
	private final OpticalCharacterRecognitionService ocrService;

	/**
	 * The post-correction service.
	 */
	private final PostcorrectionService postcorrectionService;

	/**
	 * The tool service.
	 */
	private final ToolService toolService;

	/**
	 * The export service.
	 */
	private final ExportService exportService;

	/**
	 * Creates an overview service provider controller for the api.
	 * 
	 * @param configurationService  The configuration service.
	 * @param securityService       The security service.
	 * @param importService         The import service.
	 * @param launcherService       The launcher service.
	 * @param preprocessingService  The preprocessing service.
	 * @param olrService            The optical layout recognition (OLR) service.
	 * @param ocrService            The optical character recognition (OCR) service.
	 * @param postcorrectionService The post-correction service.
	 * @param toolService           The tool service.
	 * @param exportService         The export service.
	 * @since 1.8
	 */
	public OverviewServiceProviderApiController(ConfigurationService configurationService,
			SecurityService securityService, ImportService importService, LauncherService launcherService,
			PreprocessingService preprocessingService, OpticalLayoutRecognitionService olrService,
			OpticalCharacterRecognitionService ocrService, PostcorrectionService postcorrectionService,
			ToolService toolService, ExportService exportService) {
		super(OverviewServiceProviderApiController.class, configurationService, securityService);

		this.importService = importService;
		this.launcherService = launcherService;
		this.preprocessingService = preprocessingService;
		this.olrService = olrService;
		this.ocrService = ocrService;
		this.postcorrectionService = postcorrectionService;
		this.toolService = toolService;
		this.exportService = exportService;
	}

	/**
	 * Set the service providers.
	 * 
	 * @param type    The type of the service providers.
	 * @param service The service for the providers.
	 * @param lang    The language. if null, then use the application preferred
	 *                locale.
	 * @return The service providers.
	 * @throws ServiceProviderException Throws on service provider exceptions.
	 * @since 1.8
	 */
	private List<ServiceProviderResponse> serviceProviders(ServiceProviderCoreApiController.Type type,
			CoreServiceProvider<? extends ServiceProvider> service, String lang) throws ServiceProviderException {
		final Locale locale = getLocale(lang);
		final Target target = new Target(configurationService.getExchange().getFolder().normalize(),
				configurationService.getOpt().getFolder().normalize(), null, null);

		final List<ServiceProviderResponse> providers = new ArrayList<>();
		for (CoreServiceProvider<? extends ServiceProvider>.Provider provider : service.getActiveProviders())
			providers.add(
					new ServiceProviderResponse(locale, type, provider.getId(), provider.getServiceProvider(), target));

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
	 * @since 1.8
	 */
	@GetMapping(listRequestMapping)
	public ResponseEntity<List<ServiceProviderResponse>> serviceProviders(@RequestParam(required = false) String lang) {
		try {
			final List<ServiceProviderResponse> providers = serviceProviders(ServiceProviderCoreApiController.Type.imp,
					importService, lang);

			providers.addAll(serviceProviders(ServiceProviderCoreApiController.Type.launcher, launcherService, lang));
			providers.addAll(
					serviceProviders(ServiceProviderCoreApiController.Type.preprocessing, preprocessingService, lang));
			providers.addAll(serviceProviders(ServiceProviderCoreApiController.Type.olr, olrService, lang));
			providers.addAll(serviceProviders(ServiceProviderCoreApiController.Type.ocr, ocrService, lang));
			providers.addAll(serviceProviders(ServiceProviderCoreApiController.Type.postcorrection,
					postcorrectionService, lang));
			providers.addAll(serviceProviders(ServiceProviderCoreApiController.Type.tool, toolService, lang));
			providers.addAll(serviceProviders(ServiceProviderCoreApiController.Type.export, exportService, lang));

			return ResponseEntity.ok().body(providers);
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the service provider with required id in the response body.
	 * 
	 * @param spiId The spi id.
	 * @param lang  The language. if null, then use the application preferred
	 *              locale.
	 * @return The service provider in the response body.
	 * @since 1.8
	 */
	@GetMapping(entityRequestMapping + spiPathVariable)
	public ResponseEntity<ServiceProviderResponse> serviceProviders(@PathVariable String spiId,
			@RequestParam(required = false) String lang) {
		if (spiId == null || spiId.isBlank())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

		spiId = spiId.trim();

		try {
			for (ServiceProviderResponse provider : serviceProviders(ServiceProviderCoreApiController.Type.imp,
					importService, lang))
				if (spiId.equals(provider.getId()))
					return ResponseEntity.ok().body(provider);

			for (ServiceProviderResponse provider : serviceProviders(ServiceProviderCoreApiController.Type.launcher,
					launcherService, lang))
				if (spiId.equals(provider.getId()))
					return ResponseEntity.ok().body(provider);

			for (ServiceProviderResponse provider : serviceProviders(
					ServiceProviderCoreApiController.Type.preprocessing, preprocessingService, lang))
				if (spiId.equals(provider.getId()))
					return ResponseEntity.ok().body(provider);

			for (ServiceProviderResponse provider : serviceProviders(ServiceProviderCoreApiController.Type.olr,
					olrService, lang))
				if (spiId.equals(provider.getId()))
					return ResponseEntity.ok().body(provider);

			for (ServiceProviderResponse provider : serviceProviders(ServiceProviderCoreApiController.Type.ocr,
					ocrService, lang))
				if (spiId.equals(provider.getId()))
					return ResponseEntity.ok().body(provider);

			for (ServiceProviderResponse provider : serviceProviders(
					ServiceProviderCoreApiController.Type.postcorrection, postcorrectionService, lang))
				if (spiId.equals(provider.getId()))
					return ResponseEntity.ok().body(provider);

			for (ServiceProviderResponse provider : serviceProviders(ServiceProviderCoreApiController.Type.tool,
					toolService, lang))
				if (spiId.equals(provider.getId()))
					return ResponseEntity.ok().body(provider);

			for (ServiceProviderResponse provider : serviceProviders(ServiceProviderCoreApiController.Type.export,
					exportService, lang))
				if (spiId.equals(provider.getId()))
					return ResponseEntity.ok().body(provider);

		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}

		throw new ResponseStatusException(HttpStatus.NO_CONTENT);
	}
}
