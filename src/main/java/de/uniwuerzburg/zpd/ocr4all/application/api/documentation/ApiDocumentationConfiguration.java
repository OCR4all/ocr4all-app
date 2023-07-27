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
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * Defines configurations for the ope API documentation of Spring REST Web
 * Services. The user interface of the documentation is available at the
 * following URL:
 * <ul>
 * <li>swagger-ui:
 * <code>http://[HOST]:[PORT][/CONTEXT PATH]/api/doc/swagger-ui.html</code></li>
 * <li>open api: <code>http://[HOST]:[PORT][/CONTEXT PATH]/api/doc</code></li>
 * </ul>
 * The <code>/api/doc</code> path is defined in the configuration file
 * <code>application.yml</code>.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public abstract class ApiDocumentationConfiguration {
	/**
	 * The context path. This path is defined in the configuration file
	 * application.yml, property springdoc.api-docs.path.
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
	 * @since 17
	 */
	public ApiDocumentationConfiguration(ConfigurationService configurationService) {
		super();

		configuration = configurationService.getApi().getDocumentation();

	}

	/**
	 * Returns the open API bean.
	 * 
	 * @return The open API bean.
	 * @since 17
	 */
	public abstract OpenAPI api();

	/**
	 * Returns the sensible defaults and convenience methods for configuration of
	 * the open API.
	 * 
	 * @param configuration The functional interfaces for adding custom
	 *                      configuration to the open API. If null, no custom
	 *                      configuration is added.
	 * @return The builder which is intended to be the primary interface into the
	 *         open API.
	 * @since 17
	 */
	protected OpenAPI api(SpringfoxFrameworkConfiguration configuration) {
		OpenAPI api = new OpenAPI();

		addApiInformation(api);

		if (configuration != null)
			api = configuration.add(api);

		return api;
	}

	/**
	 * Adds the open API meta information.
	 * 
	 * @param api The open API.
	 * @since 17
	 */
	private void addApiInformation(OpenAPI api) {
		var contact = new Contact().name(configuration.getContact().getName())
				.email(configuration.getContact().getEmail()).url(configuration.getUrl().getContact());

		api.info(new Info().contact(contact).title(configuration.getTitle()).description(configuration.getDescription())
				.version(configuration.getVersion())
				.license(new License().name(configuration.getLicense()).url(configuration.getUrl().getLicense())));
	}

	/**
	 * Defines functional interfaces for adding custom configuration to the open
	 * API.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	@FunctionalInterface
	public interface SpringfoxFrameworkConfiguration {
		/**
		 * Adds the configuration to the open API.
		 * 
		 * @param api The builder which is intended to be the primary interface into the
		 *            open API.
		 * @return The open API with custom configuration.
		 * @since 17
		 */
		public OpenAPI add(OpenAPI api);
	}

}
