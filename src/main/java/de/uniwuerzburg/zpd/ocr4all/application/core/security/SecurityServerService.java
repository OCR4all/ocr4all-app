/**
 * File:     SecurityServerService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.application
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     25.05.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.security;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;

/**
 * Defines security services for server profiles.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("server")
@Service
public class SecurityServerService extends SecurityService {
	/**
	 * The account service.
	 */
	private final AccountService accountService;

	/**
	 * Creates a security service for a server profile.
	 * 
	 * @param configurationService The configuration service.
	 * @param accountService       The account service.
	 * @since 1.8
	 */
	@Autowired
	public SecurityServerService(ConfigurationService configurationService, AccountService accountService) {
		super(SecurityServerService.class, configurationService);

		this.accountService = accountService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.core.application.ApplicationService#
	 * getUser()
	 */
	@Override
	public String getUser() {
		return accountService.getAuthenticationName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService#
	 * isSecured()
	 */
	@Override
	public boolean isSecured() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService#
	 * getSecurityLevel()
	 */
	@Override
	public Level getSecurityLevel() {
		String user = getUser();
		
		return user == null ? Level.user
				: accountService.isAdministrator(user) ? Level.administrator
						: (accountService.isCoordinator(user) ? Level.coordinator : Level.user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService#
	 * getActiveGroups()
	 */
	@Override
	public Set<String> getActiveGroups() {
		return accountService.getActiveGroups(getUser());
	}

}
