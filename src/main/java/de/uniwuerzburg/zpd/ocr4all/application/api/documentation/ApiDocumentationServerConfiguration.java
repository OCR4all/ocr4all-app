/**
 * File:     ApiDocumentationServerConfiguration.java
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
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Defines configurations for the ope API documentation of Spring REST Web
 * Services for server profiles. The user interface of the documentation is
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
@Profile("server & api & documentation")
@Configuration
public class ApiDocumentationServerConfiguration extends ApiDocumentationConfiguration {
	/**
	 * The authorization scheme.
	 */
	private static final String authorizationScheme = "Bearer";

	/**
	 * The authorization scheme name.
	 */
	private static final String authorizationSchemeName = "Bearer scheme";

	/**
	 * Creates a configuration for the api documentation of Spring REST Web Services
	 * with Swagger 2 for server profile.
	 * 
	 * @param configurationService The configuration service.
	 * @since 17
	 */
	public ApiDocumentationServerConfiguration(ConfigurationService configurationService) {
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
		return api(api -> addSecurity(api));
	}

	/**
	 * Adds the security components and schema to the open API.
	 * 
	 * @param api The open API to add the security components and schema.
	 * @return The updated open API.
	 * @since 17
	 */
	private OpenAPI addSecurity(OpenAPI api) {
		var components = getAuthorizationComponents();
		var securityItem = new SecurityRequirement().addList(authorizationSchemeName);

		return api.components(components).addSecurityItem(securityItem);
	}

	/**
	 * Returns the authorization components.
	 * 
	 * @return The authorization components.
	 * @since 17
	 */
	private Components getAuthorizationComponents() {
		return new Components().addSecuritySchemes(authorizationSchemeName, getSecurityScheme());
	}

	/**
	 * Returns the authorization scheme.
	 * 
	 * @return The authorization scheme.
	 * @since 17
	 */
	private SecurityScheme getSecurityScheme() {
		return new SecurityScheme().name(authorizationSchemeName).type(SecurityScheme.Type.HTTP)
				.scheme(authorizationScheme);
	}
}
