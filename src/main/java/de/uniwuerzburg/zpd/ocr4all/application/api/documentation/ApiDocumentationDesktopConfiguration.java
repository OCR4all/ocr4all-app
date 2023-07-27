/**
 * File:     ApiDocumentationDesktopConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.documentation
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     13.04.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.documentation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * Defines configurations for the ope API documentation of Spring REST Web
 * Services for desktop profiles. The user interface of the documentation is
 * available at the following URL:
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
@Profile("desktop & api & documentation")
@Configuration
public class ApiDocumentationDesktopConfiguration extends ApiDocumentationConfiguration {
	/**
	 * Creates a configuration for the api documentation of Spring REST Web Services
	 * with Swagger 2 for desktop profile.
	 * 
	 * @param configurationService The configuration service.
	 * @since 17
	 */
	public ApiDocumentationDesktopConfiguration(ConfigurationService configurationService) {
		super(configurationService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.api.documentation.
	 * ApiDocumentationConfiguration#api()
	 */
	@Bean
	@Override
	public OpenAPI api() {
		return api(null);
	}
}
