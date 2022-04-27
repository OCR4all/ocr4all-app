/**
 * File:     ApiDocumentationServerConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.documentation
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     13.04.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.documentation;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Defines configurations for the api documentation of Spring REST Web Services
 * with Swagger 2 for server profiles. The user interface of the documentation
 * is available at the following URL:
 * <ul>
 * <li>swagger-ui:
 * <code>http://[HOST]:[PORT][/CONTEXT PATH]/api/doc/swagger-ui/</code></li>
 * <li>swagger v2:
 * <code>http://[HOST]:[PORT][/CONTEXT PATH]/api/doc/v2/json</code></li>
 * <li>open api v3:
 * <code>http://[HOST]:[PORT][/CONTEXT PATH]/api/doc/v3/json</code></li>
 * </ul>
 * The <code>/api/doc</code> prefix path is defined in the configuration file
 * <code>application.yml</code>. Bearer must be inserted before the JWT token in
 * the authorization.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("server & api & documentation")
@Configuration
@EnableSwagger2
public class ApiDocumentationServerConfiguration extends ApiDocumentationConfiguration {
	/**
	 * Creates a configuration for the api documentation of Spring REST Web Services
	 * with Swagger 2 for server profile.
	 * 
	 * @param configurationService The configuration service.
	 * @since 1.8
	 */
	@Autowired
	public ApiDocumentationServerConfiguration(ConfigurationService configurationService) {
		super(configurationService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.api.ApiDocumentationConfiguration#api
	 * ()
	 */
	@Bean
	@Override
	public Docket api() {
		return api(docket -> docket.securityContexts(Arrays.asList(securityContext()))
				.securitySchemes(Arrays.asList(apiKey())));
	}

	/**
	 * Returns the api key.
	 * 
	 * @return The api key.
	 * @since 1.8
	 */
	private ApiKey apiKey() {
		return new ApiKey("JWT", "Authorization", "header");
	}

	/**
	 * Returns the security context.
	 * 
	 * @return The security context.
	 * @since 1.8
	 */
	private SecurityContext securityContext() {
		return SecurityContext.builder().securityReferences(defaultAuthorization()).build();
	}

	/**
	 * Returns the default authorization.
	 * 
	 * @return The default authorization.
	 * @since 1.8
	 */
	private List<SecurityReference> defaultAuthorization() {
		AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Arrays.asList(new SecurityReference("JWT", authorizationScopes));
	}
}
