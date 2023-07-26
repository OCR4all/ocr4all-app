/**
 * File:     ApiDocumentationConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.documentation
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     11.04.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.documentation;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ApiConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * Defines configurations for the api documentation of Spring REST Web Services
 * with Swagger 2. The user interface of the documentation is available at the
 * following URL:
 * <ul>
 * <li>swagger-ui:
 * <code>http://[HOST]:[PORT][/CONTEXT PATH]/api/doc/swagger-ui/</code></li>
 * <li>swagger v2:
 * <code>http://[HOST]:[PORT][/CONTEXT PATH]/api/doc/v2/json</code></li>
 * <li>open api v3:
 * <code>http://[HOST]:[PORT][/CONTEXT PATH]/api/doc/v3/json</code></li>
 * </ul>
 * The <code>/api/doc</code> prefix path is defined in the configuration file
 * <code>application.yml</code>.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public abstract class ApiDocumentationConfiguration {
	/**
	 * The context path. This path is defined in the configuration file
	 * application.yml.
	 */
	public static final String contextPath = "/api/doc";

	/**
	 * The configuration for the api.
	 */
	private final ApiConfiguration.Documentation configuration;

	/**
	 * Creates a configuration for the api documentation of Spring REST Web Services
	 * with Swagger 2.
	 * 
	 * @param configurationService The configuration service.
	 * @since 1.8
	 */
	public ApiDocumentationConfiguration(ConfigurationService configurationService) {
		super();

		configuration = configurationService.getApi().getDocumentation();

	}

	/**
	 * Returns the docket bean, which mainly includes the configuration of Swagger.
	 * 
	 * @return The docket bean, which mainly includes the configuration of Swagger.
	 * @since 1.8
	 */
	public abstract OpenAPI api();

	/**
	 * Returns the sensible defaults and convenience methods for configuration of
	 * the Springfox framework.
	 * 
	 * @param configuration The functional interfaces for adding custom
	 *                      configuration to the Springfox framework. If null, no
	 *                      custom configuration is added.
	 * @return The builder which is intended to be the primary interface into the
	 *         Springfox framework.
	 * @since 1.8
	 */
	protected OpenAPI api(SpringfoxFrameworkConfiguration configuration) {
		OpenAPI api = new OpenAPI();
		
		//
		addApiInformation(api);

		if (configuration != null)
			api = configuration.add(api);

//		return api.select().apis(RequestHandlerSelectors.basePackage(this.configuration.getBasePackage()))
//				.paths(PathSelectors.any()).build();
		
		return api;
	}

	/**
	 * Returns the api's meta information.
	 * 
	 * @return The api's meta information.
	 * @since 1.8
	 */
	private void addApiInformation(OpenAPI api) {
		api.info(new Info().title(configuration.getTitle())
	              .description(configuration.getDescription())
	              .version(configuration.getVersion())
	              .license(new License().name(configuration.getLicense()).url(configuration.getUrl().getLicense())));
		
//		return new ApiInfo(configuration.getTitle(), configuration.getDescription(), configuration.getVersion(),
//				configuration.getUrl().getTermsOfService(),
//				new Contact(configuration.getContact().getName(), configuration.getUrl().getContact(),
//						configuration.getContact().getEmail()),
//				configuration.getLicense(), configuration.getUrl().getLicense(), Collections.emptyList());
	}

	/**
	 * Defines functional interfaces for adding custom configuration to the
	 * Springfox framework.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	@FunctionalInterface
	public interface SpringfoxFrameworkConfiguration {
		/**
		 * Adds the configuration to the Springfox framework.
		 * 
		 * @param docket The builder which is intended to be the primary interface into
		 *               the Springfox framework.
		 * @return The docket with custom configuration.
		 * @since 1.8
		 */
		public OpenAPI add(OpenAPI api);
	}

}
