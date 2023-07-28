/**
 * File:     ApiSecurityDesktopConfig.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.security
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     15.02.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityConfig;

/**
 * Disables Spring Security for desktop profiles and defines CORS filters.
 * Authentication and any security protections like XSS are disabled.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@Profile("api & desktop")
@Configuration
public class ApiSecurityDesktopConfig extends SecurityConfig {
	/**
	 * Customizes {@link WebSecurity}. Beans of this type will automatically be used
	 * by {@link WebSecurityConfiguration} to customize {@link WebSecurity}.
	 * 
	 * @return The customizations to perform on {@link WebSecurity}.
	 * @since 17
	 */
	@Bean
	WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> {
			web.ignoring().requestMatchers(patternMatchZeroMoreDirectories);
		};
	}

}
