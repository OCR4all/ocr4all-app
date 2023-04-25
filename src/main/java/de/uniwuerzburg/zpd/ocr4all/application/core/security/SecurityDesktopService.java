/**
 * File:     SecurityDesktopService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.application
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     25.05.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.security;

import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;

/**
 * Defines security services for desktop profiles.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("desktop")
@Service
public class SecurityDesktopService extends SecurityService {

	/**
	 * Creates a security service for a desktop profile.
	 * 
	 * @param configurationService The configuration service.
	 * @since 1.8
	 */
	public SecurityDesktopService(ConfigurationService configurationService) {
		super(SecurityDesktopService.class, configurationService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService#getUser
	 * ()
	 */
	@Override
	public String getUser() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService#
	 * isSecured()
	 */
	@Override
	public boolean isSecured() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService#
	 * getSecurityLevel()
	 */
	@Override
	public Level getSecurityLevel() {
		return Level.administrator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService#
	 * getActiveGroups()
	 */
	@Override
	public Set<String> getActiveGroups() {
		return new HashSet<>();
	}

}
